package server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.SocketChannel;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.concurrent.TimeUnit;

public class ServerClientConnection implements Runnable {
	private SocketChannel currentClient;
	private InputStream clientInput; 
	private OutputStream clientOutput;
	private Certificate serverCertificate; 

	/**
	 * Constructor for the client connection. Sets up the socket and brings in the
	 * Certificate from CreateServer
	 * @param client
	 * @param client, serverCertificate
	 * @throws IOException 
	 */
	ServerClientConnection(SocketChannel client, Certificate serverCertificate) throws IOException {
		//set the client and all of the input/output streams
		//set the server certificate for the client
		currentClient = client; 
		try {
			clientInput = currentClient.socket().getInputStream();
			clientOutput = currentClient.socket().getOutputStream();
			this.serverCertificate = serverCertificate;
		} catch (IOException e) {
			System.out.println("Unable to set up client Input or output");
			e.printStackTrace();
			currentClient.close();
		}
	}

	@Override
	public void run() {
		handShake();
	}

	/**
	 * Completes and verifies the handshake from the client. If the
	 * Handshake cannot complete or is wrong, shuts down the client.
	 * 
	 * The protocol is as follows
	 * 
	 * Client							Server
	 * 		----Certificate------------->
	 * 		<--- Certificate, RC+{R1}---- 									// RC+ is the public key from the client R1 is a nonce
	 * 		---- RS+{R2}----------------> 									// RS+ is the public key from the server R2 is a nonce
	 * 		<--- Hash{RC+, RS+, R1, R2, S}, SERVER ---- 						// S is the master secret key. It is R1 XOR R2
	 *		---- Hash{RC+, RS+, R1, R2, S, (Hash From Server)}, CLIENT --->
	 * 		<--- File -------------------									//The server then sends a file to the client
	 * 		---- Connection closed ------
	 *
	 * The File protocol is 
	 * AuthenticationKey{ File Length, File Type }							//The File Type is the file extension. Eg. .txt, .png, .pdf 
	 * Sequence #, EncryptionKey{ data }, hash { Sequence #, data} 
	 */
	private void handShake() {
		//sets server private key
		ServerDecryption decrypt;
		try {
			decrypt = new ServerDecryption();
		} catch (NoSuchAlgorithmException e) {
			closeClient("Incorrect Algorithm");
			return;
		} catch (InvalidKeySpecException e) {
			closeClient("PKCS8 Failure");
			return;
		} catch (IOException e) {
			closeClient("Certificate File Read Failed");
			return;
		}
		//sets up the client public key
		
		ServerEncryption encrypt;
		try {
			encrypt = new ServerEncryption(clientInput);
		} catch (CertificateException e) {
			closeClient("Certificate creation failed. Closing client: ");
			return;
		}
		
		//Sends the Server Certificate as well as a nonce (Secure Random using 20 bytes)
		//If sending the certificate fails for some reason closes the connection to the client
		if(!encrypt.sendCertR1(serverCertificate, clientOutput)) {
			closeClient("Unable to send R1 to client: Closing client: ");
			return;
		}
		
		//decrypt.decodeR2 will decode R2 sent from the Client
		//encrypt.setR2 will set R2 for later in the program
		if(!encrypt.setR2(decrypt.decodeR2(clientInput))) {
			closeClient("Bad R2");
			return;
		}
		
		//sends a hash of client public key, server public key, r1, r2, s. Then sends SERVER at the end
		//if it cannot send the hash, will close the client
		if (!encrypt.sendHashFirst3Messages(serverCertificate.getPublicKey(), clientOutput)) {
			closeClient("Unable to send first hash: closing client: ");
			return;
		}
		//creates a hash of the same things as the first hash along with the first hash and SERVER
		//Then gets the hash from the client and confirms they are the same.
		//If the verification is incorrect, will close the client
		if(!decrypt.verifyHash2(encrypt.createHash2(serverCertificate.getPublicKey()), clientInput)) {
			closeClient("Bad hash2, closing client: ");
			return;
		}
		
		//Makes the session keys. If the session keys fail, closes the client
		if(!encrypt.makeSessionKeys()) {
			closeClient("Unable to create Session Keys, closing client: ");
			return;
		}
		
		//Will send the file (given in encrypt.sendFile()) to the client
		//If this fails closes the client.
		if(!encrypt.sendFile(clientOutput)) {
			closeClient("Error Sending files, closing connection: ");
			return;
		}
		
		//File sent, close the file
		closeClient("Sent File closing connection: ");
		
	}
	
	
	/**
	 * Closes the client connection with the given reason. 
	 * Will print out why it is closing, and the ip address and port of the client
	 * @param reasonClosing
	 */
	private void closeClient(String reasonClosing) {
		try {
			System.out.println(reasonClosing + currentClient.getRemoteAddress());
			currentClient.close();
		} catch (IOException e) {
			System.out.println("Unable to close client");
			e.printStackTrace();
		}
	}

}

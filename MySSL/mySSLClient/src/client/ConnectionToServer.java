package client;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.InvalidKeySpecException;

public class ConnectionToServer {

	private Socket client;
	private OutputStream clientOut;
	private InputStream clientIn;
	private Certificate clientCertificate;
	private PublicKey clientPublicKey;

	/**
	 * This will establish a connection to the Server and start a Handshake Once the
	 * handshake is complete, a file will be saved. If something goes Wrong with the
	 * handshake, will disconnect from server.
	 */
	ConnectionToServer() {
		try {
			// bind to the server
			InetAddress host = InetAddress.getByName("localhost");
			client = new Socket(host, 8080);
			clientOut = client.getOutputStream();
			clientIn = client.getInputStream();

			// Certificate Factory tells what kind of certificate it is
			CertificateFactory cf = CertificateFactory.getInstance("X.509");

			// grab the certificate from the computer
			clientCertificate = cf.generateCertificate(new FileInputStream("sslClientCertSigned.cert"));

			// create the public key from the certificate
			clientPublicKey = clientCertificate.getPublicKey();

			// Start the Handshake
			handshakeServer();

		} catch (IOException e) {
			System.out.println("Unable to get input/output stream or unable to write to server");
			e.printStackTrace();
			try {
				client.close();
			} catch (IOException e1) {
				System.out.println("Unable to close client");
				e1.printStackTrace();
			}
			return;
		} catch (CertificateException e) {
			System.out.println("Unable to create Certificate");
			try {
				client.close();
			} catch (IOException e1) {
				System.out.println("Unable to close client");
				e1.printStackTrace();
			}
			return;
		}
	}

	/**
	 * Creates a handshake with the server
	 * 
	 * The protocol is as follows
	 * 
	 * Client Server ----Certificate-------------> <--- Certificate, RC+{R1}---- //
	 * RC+ is the public key from the client R1 is a nonce ----
	 * RS+{R2}----------------> // RS+ is the public key from the server R2 is a
	 * nonce <--- Hash{RC+, RS+, R1, R2, S}, SERVER ---- // S is the master secret
	 * key. It is R1 XOR R2 ---- Hash{RC+, RS+, R1, R2, S, (Hash From Server)},
	 * CLIENT ---> <--- File ------------------- //The server then sends a file to
	 * the client ---- Connection closed ------
	 *
	 * The File protocol is AuthenticationKey{ File Length, File Type } //The File
	 * Type is the file extension. Eg. .txt, .png, .pdf Sequence #, EncryptionKey{
	 * data }, hash { Sequence #, data}
	 * 
	 * @throws CertificateEncodingException
	 * @throws IOException
	 */
	private void handshakeServer() throws CertificateEncodingException, IOException {
		// Begin the handshake, send Certificate to server
		clientOut.write(clientCertificate.getEncoded());
		clientOut.flush();

		// Sets up Private Key for Client
		ClientDecryption decrypt = null;
		try {
			decrypt = new ClientDecryption();
		} catch (NoSuchAlgorithmException e1) {
			System.out.println("Unable to get RSA for KeyFactory");
			return;
		} catch (InvalidKeySpecException e2) {
			System.out.println("Unable to use PKCS8");
			return;
		} catch (IOException e) {
			System.out.println("Private Key File failed");
			return;
		}

		// Sets up Server Certificate
		// if encryption fails, there was a bad Certificate.
		ClientEncryption encrypt = null;
		try {
			encrypt = new ClientEncryption(clientIn);
		} catch (CertificateException e) {
			System.out.println("Server Certificate failed");
			throw new CertificateEncodingException();
		}

		// decrypt.getR1() will decrypt R1 from the Server
		// encrypt.encryptR2ToServer() will send R2 to the server
		// If encrypt fails, throws exception
		if (!encrypt.encryptR2ToServer(clientOut, decrypt.getR1(clientIn))) {
			System.out.println("Bad R1");
			throw new IOException();
		}

		// Hash the first 3 messages for comparison
		encrypt.hashFirst3Messages(clientPublicKey, clientOut);

		// Confirm the hashes match
		if (!decrypt.confirmHash1(encrypt.hash1, clientIn)) {
			System.out.println("Unable to have secure connection. Retry");
			throw new IOException();
		}

		// Creates the second hash and sends to Server
		encrypt.createSendHash2(clientPublicKey, clientOut);

		System.out.println("Secure connection with Server Established");
		
		//Makes 4 session keys, currently only 2 are used
		encrypt.makeSessionKeys();
		
		if (!decrypt.readFile(clientIn, encrypt.serverAuth, encrypt.serverSecret)) {
			System.out.println("BAD FILE READ: unable to save file");
			return;
		}
		System.out.println("File saved. Connection closed");
	}

}

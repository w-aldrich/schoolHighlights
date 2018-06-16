package server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class ServerEncryption {

	public PublicKey clientPublicKey;
	private byte[] r1;
	private byte[] r2;
	private byte[] hash1;
	private byte[] masterSecret;
	private SecretKey serverSecret;
	private SecretKey clientSecret;
	private SecretKey serverAuth;
	private SecretKey clientAuth;

	/**
	 * This constructor sets up the client public key and gets the first nonce
	 * 
	 * @param clientInput,
	 *            the Client Input Stream
	 * @throws CertificateException
	 */
	ServerEncryption(InputStream clientInput) throws CertificateException {
		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		Certificate clientCert = cf.generateCertificate(clientInput);
		clientPublicKey = clientCert.getPublicKey();
		System.out.println("Client Public key recieved");
	}

	/**
	 * Sends a nonce encrypted with the client's public key to the client
	 * 
	 * @param serverCert,
	 *            the Server Certificate
	 * @param clientOutput,
	 *            the Client Output Stream
	 * @return true if it was able to complete
	 * @return false if it fails.
	 */
	boolean sendCertR1(Certificate serverCert, OutputStream clientOutput) {
		SecureRandom random = new SecureRandom();
		byte secureBytes[] = new byte[20];
		random.nextBytes(secureBytes);
		r1 = secureBytes;

		try {
			Cipher clientPubCipher = Cipher.getInstance("RSA");
			clientPubCipher.init(Cipher.ENCRYPT_MODE, clientPublicKey);
			byte[] encryptedR1 = clientPubCipher.doFinal(secureBytes);
			clientOutput.write(serverCert.getEncoded());
			clientOutput.write(encryptedR1);
			clientOutput.flush();
			System.out.println("Sent R1");
		} catch (NoSuchAlgorithmException e) {
			System.out.println("Cipher could not create RSA (R1)");
			return false;
		} catch (NoSuchPaddingException e) {
			System.out.println("Incorrect padding for cipher (R1)");
			return false;
		} catch (InvalidKeyException e) {
			System.out.println("Incorrect key type for nonce (R1)");
			return false;
		} catch (IllegalBlockSizeException e) {
			System.out.println("Incorrect block size for cipher (R1)");
			return false;
		} catch (BadPaddingException e) {
			System.out.println("Bad padding for cipher (R1)");
			return false;
		} catch (CertificateEncodingException e) {
			System.out.println("Bad server certificate (R1)");
			return false;
		} catch (IOException e) {
			System.out.println("Unable to write to client (R1)");
			return false;
		}
		return true;
	}

	/**
	 * Set R2 for later use, also creates masterSecret Key
	 * 
	 * @param r2
	 *            the byte[] that is a 20 byte nonce
	 * 
	 * @return true if successfully created
	 * @return false if unsuccessful
	 */
	boolean setR2(byte[] r2) {
		if (r2 == null) {
			return false;
		}
		this.r2 = r2;
		masterSecret = new byte[r1.length];
		for (int i = 0; i < r1.length; i++) {
			masterSecret[i] = (byte) (r1[i] ^ r2[i]);
		}
		assert (masterSecret != null);
		assert (masterSecret != r1);
		assert (masterSecret != r2);
		return true;
	}

	/**
	 * Sends the first hash of the handshake hashes the client public key, the
	 * server public key R1(nonce) R2(nonce) and the master secret
	 * 
	 * The first hash protocal is as follows SHA1{ClientPublicKey, ServerPublicKey,
	 * nonce1, nonce2}
	 * 
	 * @param serverPublicKey,
	 *            the public key of the Server
	 * @param clientOutput,
	 *            the OutputStream of the client
	 * 
	 * @return true if it could send
	 * @return false if it fails
	 */
	boolean sendHashFirst3Messages(PublicKey serverPublicKey, OutputStream clientOutput) {
		try {
			MessageDigest sha = MessageDigest.getInstance("SHA-1");
			byte[] clientPubKeyToByte = clientPublicKey.getEncoded();
			byte[] serverPubToByte = serverPublicKey.getEncoded();

			// Create an array with the correct length
			byte[] threeMessages = new byte[clientPubKeyToByte.length + serverPubToByte.length + r1.length + r2.length
					+ masterSecret.length];
			int length = 0;

			// Copy all the elements over
			System.arraycopy(clientPubKeyToByte, 0, threeMessages, length, clientPubKeyToByte.length);
			length += clientPubKeyToByte.length;
			System.arraycopy(serverPubToByte, 0, threeMessages, length, serverPubToByte.length);
			length += serverPubToByte.length;
			System.arraycopy(r1, 0, threeMessages, length, r1.length);
			length += r1.length;
			System.arraycopy(r2, 0, threeMessages, length, r2.length);
			length += r2.length;
			System.arraycopy(masterSecret, 0, threeMessages, length, masterSecret.length);
			assert (threeMessages != null);

			// Digest
			hash1 = sha.digest(threeMessages);
			String server = "SERVER";
			clientOutput.write(hash1);
			clientOutput.write(server.getBytes());
			clientOutput.flush();
		} catch (NoSuchAlgorithmException e) {
			System.out.println("Could not get SHA-1");
			return false;
		} catch (IOException e) {
			System.out.println("Issue Writing hash1 to Client");
			return false;
		}
		System.out.println("Sent hash1");
		assert (hash1 != null);
		return true;
	}

	/**
	 * Creates the second hash to compare in ServerDecryption This should match what
	 * the client sends to the server
	 * 
	 * @param serverPublicKey,
	 *            the public key of the Server
	 * @return the completed hash //Will return null if digest fails
	 */
	byte[] createHash2(PublicKey serverPublicKey) {
		byte[] hash2 = null;
		byte[] clientPubKeyToByte = clientPublicKey.getEncoded();
		byte[] serverPubToByte = serverPublicKey.getEncoded();
		String server = "SERVER";

		// Create space for the hash
		byte[] fourMessages = new byte[clientPubKeyToByte.length + serverPubToByte.length + r1.length + r2.length
				+ hash1.length + masterSecret.length + server.getBytes().length];
		int length = 0;

		// Copy the elements for the hash
		System.arraycopy(clientPubKeyToByte, 0, fourMessages, 0, clientPubKeyToByte.length);
		length += clientPubKeyToByte.length;
		System.arraycopy(serverPubToByte, 0, fourMessages, length, serverPubToByte.length);
		length += serverPubToByte.length;
//		System.arraycopy(r1, 0, fourMessages, length, r1.length);
//		length += r1.length;
		System.arraycopy(r2, 0, fourMessages, length, r2.length);
		length += r2.length;
		System.arraycopy(hash1, 0, fourMessages, length, hash1.length);
		length += hash1.length;
		System.arraycopy(server.getBytes(), 0, fourMessages, length, server.getBytes().length);
		length += server.getBytes().length;
		System.arraycopy(masterSecret, 0, fourMessages, length, masterSecret.length);

		// Digest
		try {
			MessageDigest sha = MessageDigest.getInstance("SHA-1");
			hash2 = sha.digest(fourMessages);
		} catch (NoSuchAlgorithmException e) {
			System.out.println("Cannot SHA-1 hash2");
			return null;
		}

		assert (hash2 != null);
		return hash2;
	}

	/**
	 * Will create the session keys for the remaining session
	 * 
	 * @return true if it can create the session keys
	 * @return false if it fails
	 */
	boolean makeSessionKeys() {

		try {
			SecureRandom sudoRandom = SecureRandom.getInstance("SHA1PRNG");
			sudoRandom.setSeed(masterSecret);

			KeyGenerator generator = KeyGenerator.getInstance("DESede");
			generator.init(sudoRandom);

			serverSecret = generator.generateKey();
			clientSecret = generator.generateKey();
			serverAuth = generator.generateKey();
			clientAuth = generator.generateKey();

			return true;
		} catch (NoSuchAlgorithmException e) {
			System.out.println("Unable to create sessionKeys");
			return false;
		}
	}

	/**
	 * Sends the given file (HARDCODED in the function) to the client Will encrypt
	 * with the serverSecret key
	 * 
	 * The protocol is as follows
	 * 
	 * Sequence Number, Encrypted{ Data }, SHA-1{ Sequence Number, Data}
	 * 
	 * @param clientOut,
	 *            the output stream of the Client
	 * @return true if it could send
	 * @return false if it fails
	 */
	boolean sendFile(OutputStream clientOut) {
		try {
			Cipher encryptionCipher = Cipher.getInstance("DESede");
			encryptionCipher.init(Cipher.ENCRYPT_MODE, serverSecret);
			String file = "test.png";
			File f = new File(file);

			// read in the file
			FileInputStream fis = new FileInputStream(f);
			byte[] fileToSend = new byte[(int) f.length()];
			fis.read(fileToSend);
			fis.close();

			// Send the first message AuthenticationKey{ File Length, File Type }
			clientOut.write(authenticationInformation(file, f));

			// This number will say how many times is needed to loop
			double checkLoopNumber = f.length() / 1024.0;
			// System.out.println("CHECKLOOPNUMBER: " + checkLoopNumber);

			int loopTimes = 0;
			// If it can loop an even amount of times it will
			// else it will loop an additional time for extra data
			if (checkLoopNumber != 0 && checkLoopNumber % 1 == 0) {
//				System.out.println("Original");
				loopTimes = (int) checkLoopNumber;
			} else {
//				System.out.println("Plus 1");
				loopTimes = (int) checkLoopNumber + 1;
			}

//			 System.out.println("LOOPING: " + loopTimes);
			int posInFile = 0;
			for (int i = 0; i < loopTimes; i++) {
				// The seqence number is the current loop
				byte[] seqNumber = ByteBuffer.allocate(4).putInt(i).array();
				byte[] dataToSend = new byte[1024];

				// This will correctly fill how much data needs to be sent
				if (posInFile + 1024 > fileToSend.length) {
					System.arraycopy(fileToSend, posInFile, dataToSend, 0, fileToSend.length - posInFile);
				} else {
					System.arraycopy(fileToSend, posInFile, dataToSend, 0, 1024);
					posInFile += 1024;
				}

				// Create a byte array to hold the hash
				byte[] toHash = new byte[seqNumber.length + dataToSend.length];

				// Copy information to be hashed
				System.arraycopy(seqNumber, 0, toHash, 0, seqNumber.length);
				System.arraycopy(dataToSend, 0, toHash, seqNumber.length, dataToSend.length);

				// Digest information
				MessageDigest sha = MessageDigest.getInstance("SHA-1");
				byte[] hash = sha.digest(toHash);

				// Encrypt the Data to send
				byte[] encryptedData = encryptionCipher.doFinal(dataToSend);
				// System.out.println("Encrypted data length: " + encryptedData.length);

				// Create Space for everything to send
				byte[] message = new byte[seqNumber.length + encryptedData.length + hash.length];

				// Copy everything to be sent over
				System.arraycopy(seqNumber, 0, message, 0, seqNumber.length);
				System.arraycopy(encryptedData, 0, message, seqNumber.length, encryptedData.length);
				System.arraycopy(hash, 0, message, (seqNumber.length + encryptedData.length), hash.length);

				// Send message
				clientOut.write(message);
			}
			clientOut.flush();
			return true;
		} catch (NoSuchAlgorithmException e) {
			System.out.println("Bad Algorithm");
		} catch (NoSuchPaddingException e) {
			System.out.println("Bad padding");
		} catch (InvalidKeyException e) {
			System.out.println("Bad Key");
		} catch (FileNotFoundException e) {
			System.out.println("Could not load file to send");
		} catch (IOException e) {
			System.out.println("Could not read from file or write to Client");
		} catch (IllegalBlockSizeException e) {
			System.out.println("Bad Block Size");
		} catch (BadPaddingException e) {
			System.out.println("Bad Padding");
		}

		return false;
	}

	/**
	 * Creates the Authentication information Protocol is as follows
	 * 
	 * AuthenticationKey { File Length, File Type }
	 * 
	 * 
	 * @param fileName,
	 *            The name of the File, will give Type information
	 * @param file,
	 *            The file, used to grab the length
	 *            
	 * @return The encrypted Authentication information
	 * 
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	private byte[] authenticationInformation(String fileName, File file) throws NoSuchAlgorithmException,
			NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		
		//This will allocate bytes for a long
		ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
		buffer.putLong(file.length());
		
		//Grab the file extension
		String fileTypeString = fileName.substring(fileName.length() - 3);
		byte[] fileType = fileTypeString.getBytes();
		
		//Encrypt the Information above
		Cipher authenticationCipher = Cipher.getInstance("DESede");
		authenticationCipher.init(Cipher.ENCRYPT_MODE, serverAuth);
		byte[] information = new byte[buffer.array().length + fileType.length];
		System.arraycopy(buffer.array(), 0, information, 0, buffer.array().length);
		System.arraycopy(fileType, 0, information, buffer.array().length, fileType.length);
		byte[] encrypted = authenticationCipher.doFinal(information);
		
		
		return encrypted;
	}

}

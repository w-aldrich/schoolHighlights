package client;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class ClientDecryption {

	private PrivateKey clientPrivateKey;

	/**
	 * This will set up decryption for the rest of the session. It also Will
	 * generate the private key for the Client
	 * 
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 */
	ClientDecryption() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
		// Start grabbing private key from computer
		File f = new File("javaClientPrivate.der");
		// read in the file
		FileInputStream fis = new FileInputStream(f);
		byte[] byteForPrivKey = new byte[(int) f.length()];
		fis.read(byteForPrivKey);
		fis.close();
		// Key Factory tells what kind of encryption to use
		KeyFactory kf = KeyFactory.getInstance("RSA");
		// This is what kind of key we are using (This is specified when making the key
		// in terminal)
		PKCS8EncodedKeySpec keySpecPKCS8 = new PKCS8EncodedKeySpec(byteForPrivKey);
		// Generate the key from the key factory
		clientPrivateKey = kf.generatePrivate(keySpecPKCS8);
	}

	/**
	 * Decrypts R1 from the server
	 * 
	 * @param clientIn,
	 *            The InputStream for the Client
	 * @return R1 from the server, or null if it fails.
	 */
	byte[] getR1(InputStream clientIn) {
		byte[] r1 = null;

		try {
			byte[] getR1 = new byte[256];
			clientIn.read(getR1);
			Cipher decryptR1 = Cipher.getInstance("RSA");
			decryptR1.init(Cipher.DECRYPT_MODE, clientPrivateKey);
			byte[] decryptedR1 = decryptR1.doFinal(getR1);
			r1 = decryptedR1;
		} catch (IOException e) {
			System.out.println("IO Excption Client R1 ");
			return null;
		} catch (NoSuchAlgorithmException e) {
			System.out.println("Algorithm Failed Client R1");
			return null;
		} catch (NoSuchPaddingException e) {
			System.out.println("No Padding Client R1");
			return null;
		} catch (InvalidKeyException e) {
			System.out.println("Bad Key Client R1");
			return null;
		} catch (IllegalBlockSizeException e) {
			System.out.println("Illegal block Client R1");
			return null;
		} catch (BadPaddingException e) {
			System.out.println("Bad Padding Client R1");
			return null;
		}

		System.out.println("Retrieved R1");
		assert (r1 != null);
		return r1;
	}

	/**
	 * This will confirm that the hash that the Server sends back matches what we
	 * expect it to. We compute this hash in the ClientEncryption class
	 * 
	 * @param hash1,
	 *            hash computed from the ClientEncryption class hashFirst3Messages()
	 *            method
	 * @param clientIn,
	 *            the InputStream from the client
	 * @return true if the hashes match
	 * @return false if the hashes dont match
	 */
	boolean confirmHash1(byte[] hash1, InputStream clientIn) {
		byte[] clientHash2 = new byte[hash1.length];
		try {
			clientIn.read(clientHash2);
			assert (clientHash2 != null);
			String serverString = "SERVER";
			byte[] serverbyte = serverString.getBytes();
			byte[] server = new byte[serverbyte.length];
			clientIn.read(server);
			for (int i = 0; i < server.length; i++) {
				if (serverbyte[i] != server[i]) {
					System.out.println("NOT SERVER");
					return false;
				}
			}
			for (int i = 0; i < hash1.length; i++) {
				if (hash1[i] != clientHash2[i]) {
					System.out.println("BAD HASH");
					return false;
				}
			}
		} catch (IOException e) {
			System.out.println("IOException confirmHash1");
			return false;
		}
		System.out.println("Confirmed Hash1");
		return true;
	}

	
	/**
	 * This will read the File sent from the server
	 * 
	 * @param clientIn, the InputStream from the client
	 * @param serverAuth, the SecretKey for authentication for the server
	 * @param serverSecret, the SecretKey for encryption for the server
	 * @return true if it succeeds
	 * @return false if reading the file fails
	 */
	boolean readFile(InputStream clientIn, SecretKey serverAuth, SecretKey serverSecret) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte[] authentication = new byte[16];
		byte[] fileLength = new byte[8];
		byte[] fileType = new byte[3];

		try {
			clientIn.read(authentication);
			Cipher c = Cipher.getInstance("DESede");
			c.init(Cipher.DECRYPT_MODE, serverAuth);
			byte[] info = c.doFinal(authentication);
			System.arraycopy(info, 0, fileLength, 0, 8);
			System.arraycopy(info, 8, fileType, 0, 3);
		} catch (IOException e2) {
			return false;
		} catch (NoSuchAlgorithmException e) {
			return false;
		} catch (NoSuchPaddingException e) {
			return false;
		} catch (InvalidKeyException e) {
			return false;
		} catch (IllegalBlockSizeException e) {
			return false;
		} catch (BadPaddingException e) {
			return false;
		}

		ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
		buffer.put(fileLength);
		buffer.flip();// need flip
		long fileLengthLong = buffer.getLong();

		String fileExtention = new String(fileType);

		double checkLoopNumber = fileLengthLong / 1024.0;
		int loopTimes = 0;
		if (checkLoopNumber != 0 && checkLoopNumber % 1 == 0) {
			loopTimes = (int) checkLoopNumber;
		} else {
			loopTimes = (int) checkLoopNumber + 1;
		}
		byte[] seqNumber = new byte[4];
		byte[] data = new byte[1032];
		byte[] hash = new byte[20];
		byte[] compareHash = new byte[20];
		ByteBuffer seqBuffer = ByteBuffer.allocate(4);
		MessageDigest sha = null;
		try {
			sha = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		int posInFile = 0;
		for (int i = 0; i < loopTimes; i++) {
			try {
//				System.out.println(i);
				clientIn.read(seqNumber);
				clientIn.read(data);
				clientIn.read(hash);
				seqBuffer = ByteBuffer.wrap(seqNumber);
				if (seqBuffer.getInt() != i) {
					return false;
				}

				Cipher c = Cipher.getInstance("DESede");
				c.init(Cipher.DECRYPT_MODE, serverSecret);
				// System.out.println(data.length);
				byte[] decrypt = c.doFinal(data);
				byte[] writeFile = null;
				// System.out.println(fileLengthLong);
				if (posInFile + 1024 > fileLengthLong) {
					writeFile = new byte[(int) (fileLengthLong - posInFile)];
					System.arraycopy(decrypt, 0, writeFile, 0, (int) (fileLengthLong - posInFile));
				} else {
					writeFile = new byte[1024];
					// System.out.println(posInFile);
					System.arraycopy(decrypt, 0, writeFile, 0, 1024);
					posInFile += 1024;
				}
				// System.out.println(new String(writeFile));
				// System.out.println(writeFile.length);
				byte[] clientHash = new byte[1028];
				byte[] padding = new byte[1028 - writeFile.length - 4];
				System.arraycopy(seqNumber, 0, clientHash, 0, 4);
				System.arraycopy(writeFile, 0, clientHash, 4, writeFile.length);
				System.arraycopy(padding, 0, clientHash, writeFile.length + 4, padding.length);
				compareHash = sha.digest(clientHash);
				for (int j = 0; j < 20; j++) {
					if (compareHash[j] != hash[j]) {
						System.out.println("BAD HASH");
						return false;
					}
				}
				bos.write(writeFile);
			} catch (NoSuchAlgorithmException e) {
				return false;
			} catch (NoSuchPaddingException e) {
				return false;
			} catch (InvalidKeyException e) {
				return false;
			} catch (IllegalBlockSizeException e) {
				return false;
			} catch (BadPaddingException e) {
				return false;
			} catch (IOException e) {
				System.out.println("Server shut down early.");
				return false;
			}

		}

		try {
			FileOutputStream fos = new FileOutputStream(new File("test." + fileExtention));
			fos.write(bos.toByteArray());
			fos.flush();
			fos.close();
		} catch (FileNotFoundException e) {
			return false;
		} catch (IOException e) {
			return false;
		}

		return true;
	}

}

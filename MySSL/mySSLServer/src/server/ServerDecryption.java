package server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class ServerDecryption {

	PrivateKey serverPrivateKey;

	/**
	 * Constructor for the Server Decryption. When created gets and saves the
	 * server's private key
	 * @throws IOException 
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeySpecException 
	 */
	ServerDecryption() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
		// Start grabbing private key from computer
		File f = new File("javaServerPrivate.der");
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
		serverPrivateKey = kf.generatePrivate(keySpecPKCS8);
	}

	/**
	 * Decodes the second nonce in the handshake. Should be encrypted with the
	 * server public key
	 * 
	 * @param clientInput
	 * @return nonce as byte array
	 */
	byte[] decodeR2(InputStream clientInput) {
		byte[] r2 = null;
		try {
			byte[] getR2 = new byte[256];
			clientInput.read(getR2);
			Cipher decryptR2 = Cipher.getInstance("RSA");
			decryptR2.init(Cipher.DECRYPT_MODE, serverPrivateKey);
			byte[] decryptedR2 = decryptR2.doFinal(getR2);
			r2 = decryptedR2;
		} catch (IOException e) {
			System.out.println("IO Excption Client R2 ");
			return null;
		} catch (NoSuchAlgorithmException e) {
			System.out.println("Algorithm Failed Client R2");
			return null;
		} catch (NoSuchPaddingException e) {
			System.out.println("No Padding Client R2");
			return null;
		} catch (InvalidKeyException e) {
			System.out.println("Bad Key Client R2");
			return null;
		} catch (IllegalBlockSizeException e) {
			System.out.println("Illegal block Client R2");
			return null;
		} catch (BadPaddingException e) {
			System.out.println("Bad Padding R2");
			return null;
		}

		assert (r2 != null);
		System.out.println("Server recieved R2");
		return r2;
	}

	/**
	 * Verify that the hash received is the same as the one computed
	 * 
	 * @param hash2, the second hash computed from the server
	 * @param clientInput, the InputStream from the client
	 * @return true if correct hash
	 * @return false if incorrect hash
	 */
	boolean verifyHash2(byte[] hash2, InputStream clientInput) {
		if(hash2 == null) {
			return false;
		}
		byte[] clientHash2 = new byte[hash2.length];
		try {
			clientInput.read(clientHash2);
			assert (clientHash2 != null);
		} catch (IOException e) {
			System.out.println("Could not read the client hash2");
			e.printStackTrace();
		}
		for (int i = 0; i < hash2.length; i++) {
			if (hash2[i] != clientHash2[i]) {
				return false;
			}
		}
		System.out.println("Secure connection with client Established");
		return true;
	}

}

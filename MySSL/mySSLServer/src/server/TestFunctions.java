package server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import org.junit.jupiter.api.Test;

class TestFunctions {

	//https://stackoverflow.com/questions/8434428/get-public-key-from-private-in-java
	//https://examples.javacodegeeks.com/core-java/io/fileinputstream/read-file-in-byte-array-with-fileinputstream/
	 
	@Test 
	void test1() { 
			try {
				//Certificate Factory tells what kind of certificate it is
				CertificateFactory cf = CertificateFactory.getInstance("X.509");
				//grab the certificate from the computer
				Certificate certificate = cf.generateCertificate(new FileInputStream("serverSelfSignedCert.cert"));
				//create the public key from the certificate
				PublicKey publicKey = certificate.getPublicKey();
				
				//Start grabbing private key from computer
				File f = new File("javaServerPrivate.der");
				//read in the file
				FileInputStream fis = new FileInputStream(f);
				byte [] byteForPrivKey = new byte[(int) f.length()];
				fis.read(byteForPrivKey);
				fis.close();

				//Key Factory tells what kind of encryption to use
				KeyFactory kf = KeyFactory.getInstance("RSA");
				//This is what kind of key we are using (This is specified when making the key in terminal)
				PKCS8EncodedKeySpec keySpecPKCS8 = new PKCS8EncodedKeySpec(byteForPrivKey);
				//Generate the key from the key factory
				PrivateKey privKey = kf.generatePrivate(keySpecPKCS8);
				
				//Create RSA encryption and decryption ciphers
				Cipher encryptCipher = Cipher.getInstance("RSA");
				encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);
				Cipher decryptCipher = Cipher.getInstance("RSA");
				decryptCipher.init(Cipher.DECRYPT_MODE, privKey);
				
				//Encrypt the string
				String testingEncryption = "Holy Shit! it works!!!";
				byte[] encrypted = encryptCipher.doFinal(testingEncryption.getBytes());
				
				String encryptedString = new String(encrypted);
				System.out.println(encryptedString);
				
				//Decrypt the string
				byte[] decrypted = decryptCipher.doFinal(encrypted);
				
				String didItWork = new String(decrypted);
				System.out.println(didItWork);
			} catch (CertificateException e) {
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (InvalidKeySpecException e) {
				e.printStackTrace();
			} catch (NoSuchPaddingException e) {
				e.printStackTrace();
			} catch (InvalidKeyException e) {
				e.printStackTrace();
			} catch (IllegalBlockSizeException e) {
				e.printStackTrace();
			} catch (BadPaddingException e) {
				e.printStackTrace();
			}
	}
	
	@Test
	void test2() {
		try {
			File f = new File("javaServerPrivate.der");
			//read in the file
			FileInputStream fis = new FileInputStream(f);
			byte [] byteForPrivKey = new byte[(int) f.length()];
			fis.read(byteForPrivKey);
			fis.close();

			//Key Factory tells what kind of encryption to use
			KeyFactory kf = KeyFactory.getInstance("RSA");
			//This is what kind of key we are using (This is specified when making the key in terminal)
			PKCS8EncodedKeySpec keySpecPKCS8 = new PKCS8EncodedKeySpec(byteForPrivKey);
			//Generate the key from the key factory
			PrivateKey privKey = kf.generatePrivate(keySpecPKCS8);
			
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
			SecureRandom createKeys = new SecureRandom(privKey.getEncoded());
			keyGen.initialize(2048, createKeys);
			KeyPair encryptionPair = keyGen.generateKeyPair();
			PrivateKey tryDecrypt = encryptionPair.getPrivate();
			PublicKey tryEncrypt = encryptionPair.getPublic();
			
			Cipher encryptCipher = Cipher.getInstance("RSA");
			encryptCipher.init(Cipher.ENCRYPT_MODE, tryEncrypt);
			Cipher decryptCipher = Cipher.getInstance("RSA");
			decryptCipher.init(Cipher.DECRYPT_MODE, tryDecrypt);
			
			//Encrypt the string
			String testingEncryption = "Holy Shit! it works!!!";
			byte[] encrypted = encryptCipher.doFinal(testingEncryption.getBytes());
			
			String encryptedString = new String(encrypted);
			System.out.println(encryptedString);
			
			//Decrypt the string
			byte[] decrypted = decryptCipher.doFinal(encrypted);
			
			String didItWork = new String(decrypted);
			System.out.println(didItWork);
			
		} catch (NoSuchAlgorithmException e) {
			System.out.println("Unable to create sessionKeys");
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	void test3() throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		byte[] masterSecret = new byte[10];
		for(int i = 0; i < 10; i++) {
			masterSecret[i] = (byte) i;
		}
		SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
		random.setSeed(masterSecret); // s is the master secret

		KeyGenerator keyGenerator = KeyGenerator.getInstance("DESede");
		keyGenerator.init(random);

		SecretKey bobsAuthKey = keyGenerator.generateKey();
		SecretKey alicsAuthKey = keyGenerator.generateKey();
		SecretKey bobsEncKey = keyGenerator.generateKey();
		SecretKey alicesEncKey = keyGenerator.generateKey();
		
		Cipher c = Cipher.getInstance("DESede");
		c.init(Cipher.ENCRYPT_MODE, bobsEncKey);
		String blah = "BLAH";
		byte[] encrypted = c.doFinal(blah.getBytes());
		Cipher d = Cipher.getInstance("DESede");
		c.init(Cipher.DECRYPT_MODE, bobsEncKey);
		byte[] decrypted = c.doFinal(encrypted);
		String finalString = new String(decrypted);
		System.out.println(finalString);
	}
	

}

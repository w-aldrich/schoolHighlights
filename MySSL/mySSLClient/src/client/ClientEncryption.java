package client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class ClientEncryption {

	PublicKey serverPublicKey;
	byte[] r1;
	byte[] r2;
	byte[] masterSecret;
	byte[] hash1;
	SecretKey serverSecret;
	private SecretKey clientSecret;
	SecretKey serverAuth;
	private SecretKey clientAuth;

	ClientEncryption(InputStream clientIn) throws CertificateException {
		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		Certificate clientCert = cf.generateCertificate(clientIn);
		serverPublicKey = clientCert.getPublicKey();
	}

	boolean encryptR2ToServer(OutputStream clientOut, byte[] r1) {
		if (r1 == null) {
			System.out.println("Bad R1");
			return false;
		}
		this.r1 = r1;
		try {
			Cipher encryptR2 = Cipher.getInstance("RSA");
			encryptR2.init(Cipher.ENCRYPT_MODE, serverPublicKey);
			SecureRandom random = new SecureRandom();
			byte secureBytes[] = new byte[20];
			random.nextBytes(secureBytes);
			r2 = secureBytes;
			secureBytes = encryptR2.doFinal(secureBytes);
			clientOut.write(secureBytes);
			clientOut.flush();
			System.out.println("Sent R2");
			setMasterSecret();
		} catch (NoSuchAlgorithmException e) {
			System.out.println("Could not create RSA (R2)");
			return false;
		} catch (NoSuchPaddingException e) {
			System.out.println("Could not pad RSA (R2)");
			return false;
		} catch (InvalidKeyException e) {
			System.out.println("Invalid key (R2)");
			return false;
		} catch (IllegalBlockSizeException e) {
			System.out.println("Illegal Block Size RSA (R2)");
			return false;
		} catch (BadPaddingException e) {
			System.out.println("Bad Padding RSA (R2)");
			return false;
		} catch (IOException e) {
			System.out.println("Could not write R2 to server");
			return false;
		}
		return true;

	}

	void setMasterSecret() {
		masterSecret = new byte[r1.length];
		for (int i = 0; i < r1.length; i++) {
			masterSecret[i] = (byte) (r1[i] ^ r2[i]);
		}
		assert (masterSecret != null);
		assert (masterSecret != r1);
		assert (masterSecret != r2);
	}

	void hashFirst3Messages(PublicKey clientPublicKey, OutputStream clientOut) {
		try {
			MessageDigest sha = MessageDigest.getInstance("SHA-1");
			byte[] clientPubKeyToByte = clientPublicKey.getEncoded();
			byte[] serverPubToByte = serverPublicKey.getEncoded();
			byte[] threeMessages = new byte[clientPubKeyToByte.length + serverPubToByte.length + r1.length + r2.length
					+ masterSecret.length];
			int length = 0;
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
			hash1 = sha.digest(threeMessages);
		} catch (NoSuchAlgorithmException e) {
			System.out.println("Could not get SHA-1");
			e.printStackTrace();
		}

		assert (hash1 != null);
	}

	void createSendHash2(PublicKey clientPublicKey, OutputStream clientOut) {
		byte[] hash2 = null;
		byte[] clientPubKeyToByte = clientPublicKey.getEncoded();
		byte[] serverPubToByte = serverPublicKey.getEncoded();
		String server = "SERVER";
		byte[] fourMessages = new byte[clientPubKeyToByte.length + serverPubToByte.length + r1.length + r2.length
				+ hash1.length + masterSecret.length + server.getBytes().length];
		int length = 0;
		System.arraycopy(clientPubKeyToByte, 0, fourMessages, 0, clientPubKeyToByte.length);
		length += clientPubKeyToByte.length;
		System.arraycopy(serverPubToByte, 0, fourMessages, length, serverPubToByte.length);
		length += serverPubToByte.length;
		System.arraycopy(r1, 0, fourMessages, length, r1.length);
		length += r1.length;
		System.arraycopy(r2, 0, fourMessages, length, r2.length);
		length += r2.length;
		System.arraycopy(hash1, 0, fourMessages, length, hash1.length);
		length += hash1.length;
		System.arraycopy(server.getBytes(), 0, fourMessages, length, server.getBytes().length);
		length += server.getBytes().length;
		System.arraycopy(masterSecret, 0, fourMessages, length, masterSecret.length);
		try {
			MessageDigest sha = MessageDigest.getInstance("SHA-1");
			hash2 = sha.digest(fourMessages);
			assert (hash2 != null);
			String client = "CLIENT";
			clientOut.write(hash2);
			clientOut.write(client.getBytes());
			clientOut.flush();
			System.out.println("Sent hash2");
		} catch (NoSuchAlgorithmException e) {
			System.out.println("Cannot get SHA-1 for hash2");
		} catch (IOException e) {
			System.out.println("Cannot write hash2 to client");
		}
	}

	boolean makeSessionKeys() {

		try {
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
			random.setSeed(masterSecret); // s is the master secret

			KeyGenerator keyGenerator = KeyGenerator.getInstance("DESede");
			keyGenerator.init(random);

			serverSecret = keyGenerator.generateKey();
			clientSecret = keyGenerator.generateKey();
			serverAuth = keyGenerator.generateKey();
			clientAuth = keyGenerator.generateKey();

			return true;
		} catch (NoSuchAlgorithmException e) {
			System.out.println("Unable to create sessionKeys");
			return false;
		}
	}
}

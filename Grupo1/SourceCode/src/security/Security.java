package security;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.SealedObject;
import javax.crypto.spec.SecretKeySpec;

import bank.Pair;
import message.Message;
import message.SecureMessage;

public class Security {
	
	public static final String SECURE_ALGORITHM_FOR_SYMETRIC_KEY = "AES";

	public static final int SECURE_BIT_SIZE_FOR_SYMETRIC_KEY = 256;


	//HAND SHAKE
	public static Key handShakeClient(PublicKey serverPk, boolean isStore, ObjectInputStream in, ObjectOutputStream out) throws IOException, ClassNotFoundException {
		//gen session key and send it encrypted with server pk
		Key sessionKey = getSecureRandomKey("AES", 256);
		out.writeObject(seal(sessionKey, serverPk));
		
		//receive server confirmation
		SealedObject so = (SealedObject) in.readObject();
		SecureMessage sm = (SecureMessage) unseal(so, sessionKey);
		Message m = sm.getMessage();
		
		//send type of client
		Message temp = new Message();
		//isto significa que estamos a informar que somos ou nao um client
		temp.setStatus(isStore);
		SecureMessage s = new SecureMessage(temp);
		out.writeObject(seal(s,sessionKey));
		
		if(m != null) {
			System.out.println("status: " + m.getStatus());
			return sessionKey;
		} 
		return null;
	}
	
	
	public static Pair handShakeServer(PrivateKey serverPr, ObjectInputStream in, ObjectOutputStream out) throws ClassNotFoundException, IOException {
		//receive session key and decrypted
		SealedObject so = (SealedObject) in.readObject();
		Key sessionKey = (Key) unseal(so, serverPr);
		
		//send confirmation message
		Message m = new Message();
		m.setStatus(true);
		SecureMessage s = new SecureMessage(m);
		out.writeObject(seal(s,sessionKey));
		
		//receive client confirmation
		SealedObject confirmation = (SealedObject) in.readObject();
		SecureMessage confirmationSecureMessage = (SecureMessage) unseal(confirmation, sessionKey);
		Message confirmationMessage = confirmationSecureMessage.getMessage();
		
		if(confirmationMessage.getStatus()) {
			return new Pair(sessionKey, true);
		}
		else {
			return new Pair(sessionKey, false);
		}
	}
	
	
	//KEY GENERATION
	public static KeyPair genAssymetricKeys() throws NoSuchAlgorithmException {
		KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
		kpg.initialize(2048);
		return kpg.generateKeyPair();
	}

	public static Key getSecureRandomKey(String cipher, int keySize) {
		byte[] secureRandomKeyBytes = new byte[keySize / 8];
		SecureRandom secureRandom = new SecureRandom();
		secureRandom.nextBytes(secureRandomKeyBytes);
		return new SecretKeySpec(secureRandomKeyBytes, cipher);
	}
	
	
	//ENCRYPTION
	public static SealedObject seal(Serializable object, Key key) {
		try {
			Cipher cipher = Cipher.getInstance(key.getAlgorithm());
			cipher.init(Cipher.ENCRYPT_MODE, key);
			return new SealedObject(object, cipher);
		} catch (Exception e) {
			System.err.println("Error sealing object");
		}
		return null;
	}


	public static Serializable unseal(SealedObject object, Key key) {
		try {
			Cipher cipher = Cipher.getInstance(key.getAlgorithm());
			cipher.init(Cipher.DECRYPT_MODE, key);
			return (Serializable)object.getObject(cipher);
		} catch (Exception e) {
			System.err.println("Error unsealing object");
		}
		return null;
	}



	//HASH
	public static byte[] hashSecretK(byte [] k) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		md.update(k);
		return md.digest(); 

	}
}

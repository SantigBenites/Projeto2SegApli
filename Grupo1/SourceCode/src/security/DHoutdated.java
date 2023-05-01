package security;

import java.io.Serializable;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.SealedObject;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.SecretKeySpec;


public class DHoutdated {  

	//DIFFE-HEALMAN
	public static BigInteger genDHG() {
		return BigInteger.probablePrime(512, new SecureRandom());
	}

	public static BigInteger genDHP() {
		return BigInteger.probablePrime(512, new SecureRandom());
	}

	public static KeyPair genDHKeyPair(DHParameterSpec params) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException {
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DH");
		keyGen.initialize(params);
		keyGen.initialize(512);
		return keyGen.generateKeyPair();
	}

	public static byte [] genDHSecretK(PrivateKey secret, PublicKey pk) throws NoSuchAlgorithmException, InvalidKeyException {
		KeyAgreement ka = KeyAgreement.getInstance("DH");
		ka.init(secret);
		ka.doPhase(pk, true);
		return ka.generateSecret();
	}

	public static SecretKeySpec genSymetricKey(byte [] k) {
		return new SecretKeySpec(k, 0, 32, "AES");
	}

	//ASSYMETRIC KEYS
	public static KeyPair genAssymetricKeys() throws NoSuchAlgorithmException {
		KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
		kpg.initialize(2048);
		return kpg.generateKeyPair();
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
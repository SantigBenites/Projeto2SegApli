
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.json.simple.JSONObject;

import javax.crypto.spec.IvParameterSpec;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;



public class MBeC {
	public static final String ALGORITHM = "RSA";
	
	public static PublicKey publicKey ;
    public static PrivateKey privateKey;
    public static SecretKey operationKey;
	
	static String host;
	static int port = 0;

	public static String account;
	public static String userFile;
	public static String bankFile;
	public static String operation;
	public static String vccFile;
	
	
	public static byte[] initializationVector;
	
	public static String value;
	
	
	public byte[] generateNonce() {
        long timestamp = System.currentTimeMillis();
        byte[] nonce = new byte[8];
        nonce[0] = (byte) (timestamp >>> 56);
        nonce[1] = (byte) (timestamp >>> 48);
        nonce[2] = (byte) (timestamp >>> 40);
        nonce[3] = (byte) (timestamp >>> 32);
        nonce[4] = (byte) (timestamp >>> 24);
        nonce[5] = (byte) (timestamp >>> 16);
        nonce[6] = (byte) (timestamp >>> 8);
        nonce[7] = (byte) timestamp;
        return nonce;
    }
	
	public int checkPort() {
		if(port < 1024 || port > 65535)
			return 130;
		else 
			return 0;
	}
	
	
	public int checkIp() {
		
		String[] ip_split = host.split("\\.");
		
		if(ip_split.length == 0)
			return 130;
		
		for(String x: ip_split) {
			Integer i = Integer.parseInt(x);
			
			if(i < 0 || i > 255)
				return 130;
			
		}
		
		return 0;
	}
	
	
	
	public int checkAccount() {
		String reg = "^[0-9]*$";
		
		if(account.matches(reg))
			return 0;
		else
			return 130;
	}
	
	public int checkFileName(String name) {
		String reg = "^[a-z_.0-9-]*$";
		
		if ((name.length() > 1 && name.length() <= 127) && name.matches(reg))
			return 0;
		else
			return 130;
	}
	
	
	public int checkNumeric() {
		String regInt =  "(0|[1-9][0-9]*)";
		String regDec = "[0-9]{2}";
		
		if(!value.contains("."))
			return 130;
		
		String[] split = value.split("\\.");
		

		
		if(split[0].matches(regInt) && split[1].matches(regDec))
			return 0;
		else 
			return 130;
		
		
	}
	
	
	public String chooseVccFile() {
		File folder = new File(System.getProperty("user.dir"));
		File[] files = folder.listFiles();
		
		String selected = null;
		
		ArrayList<String> cardFilesNames = new ArrayList<String>();
		
		for(File f: files) {
		
			String[] x = f.getName().split("\\.");
			
			if(x.length>=2 && x[1].equals("card") && x[0].length() != 0) {
				cardFilesNames.add(x[0]);
			}

		}
		
		for(String h: cardFilesNames) {
			if(selected == null || Integer.parseInt(selected.split("_")[1].split("\\.")[0]) < Integer.parseInt(h.split("_")[1].split("\\.")[0])) {
				selected = h;
			}
			
		}
		
		return selected;
	}
	
	
	
	public byte[] encryptPub(PublicKey pub, byte[] inputData)
            throws Exception {


        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, pub);

        byte[] encryptedBytes = cipher.doFinal(inputData);

        return encryptedBytes;
    }

    public byte[] decrypt(PrivateKey priv, byte[] inputData)
            throws Exception {

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, priv);

        byte[] decryptedBytes = cipher.doFinal(inputData);

        return decryptedBytes;
    }
    
    public byte[] createInitializationVector()
    {
 
        // Used with encryption
        byte[] initializationVector = new byte[16];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(initializationVector);
        return initializationVector;
    }
    
    public byte[] encryptSecret(byte[] plainText, SecretKey secretKey, byte[] initializationVector)
            throws Exception
        {

    	String AES_CIPHER_ALGORITHM = "AES/CBC/PKCS5PADDING";
    	
    	Cipher cipher
                = Cipher.getInstance(
                    AES_CIPHER_ALGORITHM);
     
            IvParameterSpec ivParameterSpec
                = new IvParameterSpec(
                    initializationVector);
     
            cipher.init(Cipher.ENCRYPT_MODE,
                        secretKey,
                        ivParameterSpec);
     
            return cipher.doFinal(plainText);
        }
    
    
    public byte[] decryptSecret(
            byte[] cipherText,
            SecretKey secretKey,
            byte[] initializationVector)
            throws Exception
        {
    		String AES_CIPHER_ALGORITHM = "AES/CBC/PKCS5PADDING";
            Cipher cipher
                = Cipher.getInstance(
                    AES_CIPHER_ALGORITHM);
     
            IvParameterSpec ivParameterSpec
                = new IvParameterSpec(
                    initializationVector);
     
            cipher.init(
                Cipher.DECRYPT_MODE,
                secretKey,
                ivParameterSpec);
     
            byte[] result = cipher.doFinal(cipherText);
     
            return result;
        }
        
    
	public KeyPair generateKeyPair()
	            throws NoSuchAlgorithmException, NoSuchProviderException {

	        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ALGORITHM);

	        SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");

	        // 512 is keysize
	        keyGen.initialize(2048, random);

	        KeyPair generateKeyPair = keyGen.generateKeyPair();
	        return generateKeyPair;
	}
	
	
	public byte[] objectToByteArray(Object obj) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(bos);
		
		oos.writeObject(obj);
		oos.flush();
		
		return bos.toByteArray();
	}
	
	public String getHash(byte[] inputToHash) throws NoSuchAlgorithmException, IOException {
		MessageDigest md = MessageDigest.getInstance("SHA-256");

		for(byte b: inputToHash) {
			md.update(b);
		}

		byte[] bytes = md.digest();

		StringBuilder sb = new StringBuilder();
		for(int i=0; i< bytes.length ;i++)
		{
			sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
		}

		return sb.toString();
	}
	
	public PublicKey bytesToPublicKey(byte[] keyBytes) throws Exception {
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(spec);
    }
    
    public PrivateKey bytesToPrivateKey(byte[] keyBytes) throws Exception {
    	PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(spec);
    }
    
    public SecretKey bytesToSecretKey(byte[] keyBytes) throws Exception {
    	return new SecretKeySpec(keyBytes, 0, keyBytes.length, "AES");
    }
    
    
    //Challenges
    public String[] createChallenge() {
		ArrayList<String> operations = new ArrayList<String>();
		
		
		int min = 1;
		int max = 99999;
		
		operations.add("+");
		operations.add("-");
		operations.add("*");
		operations.add("/");
		
		int st_number = (int) (Math.random() * (max - min +1) + min);
		int snd_number = (int) (Math.random() * (max - min +1) + min);
		
				
		switch (operations.get((int) (Math.random() * (3 - 0 +1) + 0))) {
		case "*": {
			double result = st_number * snd_number;
			
			StringBuilder sb = new StringBuilder();
			sb.append(st_number);
			sb.append(" * ");
			sb.append(snd_number);
			
			String[] x = {sb.toString(), String.valueOf(result)};
			
			return x;
		}
		case "+": {
			double result = st_number + snd_number;
			
			StringBuilder sb = new StringBuilder();
			sb.append(st_number);
			sb.append(" + ");
			sb.append(snd_number);
			
			String[] x = {sb.toString(), String.valueOf(result)};
			
			return x;
		}
		case "/": {
			double result = (double) st_number / snd_number;
			
			StringBuilder sb = new StringBuilder();
			sb.append(st_number);
			sb.append(" / ");
			sb.append(snd_number);
			
			String[] x = {sb.toString(), String.valueOf(result)};
			
			return x;
		}
		case "-": {
			double result = st_number - snd_number;
			
			StringBuilder sb = new StringBuilder();
			sb.append(st_number);
			sb.append(" - ");
			sb.append(snd_number);
			
			String[] x = {sb.toString(), String.valueOf(result)};
			
			return x;
		}
		default:
			throw new IllegalArgumentException("Unexpected value: " + operations.get((int) (Math.random() * (max - min +1) + min)));
		}
		
		
	}
	
	public double resolveChallenge(String input) {
		String[] params = input.split(" ");
		
		switch (params[1]) {
		case "*": {
			double result = Integer.parseInt(params[0]) * Integer.parseInt(params[2]);
			
			return result;
		}
		case "+": {
			double result = Integer.parseInt(params[0]) + Integer.parseInt(params[2]);
			
			return result;
		}
		case "/": {
			double result = (double) Integer.parseInt(params[0]) / Integer.parseInt(params[2]);
			
			return result;
		}
		case "-": {
			double result = Integer.parseInt(params[0]) - Integer.parseInt(params[2]);
			
			return result;
		}
		default:
			return -1;
		}
	}
	
	

}

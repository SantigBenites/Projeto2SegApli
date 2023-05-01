package bank;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;

import java.security.SecureRandom;


import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import org.json.simple.JSONObject;

public class ClientThread extends Thread{
	private static final String ALGORITHM = "RSA";

	private static int i = 1;
	
	private Socket clientSocket;
	private PrivateKey privateKey;
	private BankServer bank;
	private PublicKey clientPub;
	private boolean clientAuthenticated;
	private int port;
	private String host;
	
	
	private SecretKey opKey;
	private static byte[] initializationVector;
	
	public ClientThread(Socket client, PrivateKey privateKey, BankServer bank, int port, String host) {
		this.clientSocket=client;
		this.privateKey = privateKey;
		this.bank = bank;
		this.clientAuthenticated = false;
		this.host = host;
		this.port = port;
	}

	public void run() {
		try{
			
			ObjectInputStream dIn = new ObjectInputStream(clientSocket.getInputStream());
			ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
			

	        
	        	//Primeira fase de auth
				byte[] message = (byte[]) dIn.readObject();
				
				
				String separator = "#!--!#";
				String separator2 = "!#--#!";
				
				String separatorNonce = "!#--!#";


				byte[] unencrypted = decrypt(privateKey, message);
				
				
				String h = new String(unencrypted);

				String[] x0 = h.split(separator2);
				
				if(!getHash(x0[0].getBytes()).equals(x0[1])) {
					
					JSONObject obj = new JSONObject();
					
					byte[] ansNonce = encryptSecret(obj.toJSONString().getBytes(), opKey, initializationVector);
					
					out.writeObject(ansNonce);
					out.close();
				}
								
				String[] x = x0[0].split(separator);
				
				String client = x[0];
								
					String challengeToSolve = x[1];
					
					byte[] clientPub = Base64.getDecoder().decode(x[2]);
					
					this.clientPub = bytesToPublicKey(clientPub);
					
					//Segunda fase de auth
					

					
					String solution = String.valueOf(resolveChallenge(challengeToSolve));
					
					String[] newChallengeInfo = createChallenge();
					
					String solution2 = newChallengeInfo[1];
					
					String newChallengeForClient = newChallengeInfo[0];
					
					StringBuilder sb = new StringBuilder();
					
					sb.append("bank");
					sb.append(separator);
					sb.append(solution);
					sb.append(separator);
					sb.append(newChallengeForClient);
					
					String hash2 = getHash(sb.toString().getBytes());
					
					sb.append(separator2);
					sb.append(hash2);
					
					out.writeObject(encryptPub(this.clientPub, sb.toString().getBytes()));
					
					//Terceira fase de auth
					
					byte[] thirdMessage = (byte[]) dIn.readObject();
					
					
					byte[] unencryptedThird = decrypt(privateKey, thirdMessage);
					
					String h1 = new String(unencryptedThird);
					
					String x10[] = h1.split(separator2);
					
					if(!getHash(x10[0].getBytes()).equals(x10[1])) {
						JSONObject obj = new JSONObject();
						
						byte[] ansNonce = encryptSecret(obj.toJSONString().getBytes(), opKey, initializationVector);
						
						out.writeObject(ansNonce);
						out.close();
					}
										
					String[] x1 = x10[0].split(separator);
								
					String possibleSolution = x1[1];
					
					
					if(possibleSolution.equals(solution2)) {
						
						//Quarta fase de auth
						
						
						this.opKey = createAESKey();
						initializationVector = createInitializationVector();
						
						
						
						StringBuilder sb3 = new StringBuilder();
						sb3.append("bank");
						sb3.append(separator);
						sb3.append(Base64.getEncoder().encodeToString(this.opKey.getEncoded()));
						sb3.append(separator2);
						sb3.append(Base64.getEncoder().encodeToString(initializationVector));
						String hash4 = getHash(sb3.toString().getBytes());
						
						sb3.append(separator2);
						sb3.append(hash4);
						
						this.clientAuthenticated = true;
						
						out.writeObject(encryptPub(this.clientPub, sb3.toString().getBytes()));
					}else {
						StringBuilder sb2 = new StringBuilder();
						sb2.append("bank");
						sb2.append(separator);
						sb2.append("Resposta errada");
						
						String hashError = getHash(sb2.toString().getBytes());
						
						sb2.append(separator2);
						sb2.append(hashError);
						
						out.writeObject(encryptPub(this.clientPub, sb2.toString().getBytes()));
					}
					
					
					
					if(this.clientAuthenticated) {

										
						byte[] cont = (byte[]) dIn.readObject();
						
						
						byte[] unencryptedRequest = decryptSecret(cont, opKey, initializationVector);
										
						String content = new String(unencryptedRequest);
						
						String[] po = content.split(separatorNonce);
						
						
						
						if(bank.checkNonce(po[1])) {
							
							JSONObject obj = new JSONObject();
							
							byte[] ansNonce = encryptSecret(obj.toJSONString().getBytes(), opKey, initializationVector);
							
							out.writeObject(ansNonce);
							out.close();
							
						}
						String[] sep = po[0].split(separator2);
						
						if(!getHash(sep[0].getBytes()).equals(sep[1])) {
							JSONObject obj = new JSONObject();
							
							byte[] ansNonce = encryptSecret(obj.toJSONString().getBytes(), opKey, initializationVector);
							
							out.writeObject(ansNonce);
							out.close();
						}
						
						//byte[] unencryptedMessage = decryptSecret(this.opKey, content);
						
						String proc = new String(sep[0]);
						
						
						String[] stuffs = proc.split(" ");
						
				
						if (stuffs[0].equals("-n")) {

							String ans = this.bank.createUser(stuffs);
							
							if(ans.length() > 2)
								System.out.println(ans);
							
							byte[] ans1 = encryptSecret(ans.getBytes(), opKey, initializationVector);
							
							out.writeObject(ans1);
							out.close();
						}
						
						else if (stuffs[0].equals("-d")) {
							String ans = this.bank.deposit(stuffs);
							
							if(ans.length() > 2)
								System.out.println(ans);
							
							byte[] ans1 = encryptSecret(ans.getBytes(), opKey, initializationVector);
							
							out.writeObject(ans1);
							out.close();
						}
						
						
						else if (stuffs[0].equals("-c")) {
							String ans = this.bank.createVCC(stuffs, port, host);
							
							if(ans.length() > 2)
								System.out.println(ans);
							
							byte[] ans1 = encryptSecret(ans.getBytes(), opKey, initializationVector);
							
							out.writeObject(ans1);
							out.close();

						}
						
						else if (stuffs[0].equals("-g")) {
							String ans = this.bank.balance(stuffs);
							
							if(ans.length() > 2)
								System.out.println(ans);
							
							byte[] ans1 = encryptSecret(ans.getBytes(), opKey, initializationVector);
							
							out.writeObject(ans1);
							out.close();

						} 
						else if(stuffs[0].equals("-m")) {
							
							String ans = this.bank.checkVCC(stuffs);
							
							if(ans.length() > 2)
								System.out.println(ans);
							
							byte[] ans1 = encryptSecret(ans.getBytes(), opKey, initializationVector);
							
							out.writeObject(ans1);
							out.close();
						}


					}
				//}
				

		} catch (Exception e) {
			System.out.println("protocol_error");
		}
		
	}
	
	public static byte[] encryptPub(PublicKey pub, byte[] inputData)
            throws Exception {


        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, pub);

        byte[] encryptedBytes = cipher.doFinal(inputData);

        return encryptedBytes;
    }

    public static byte[] decrypt(PrivateKey priv, byte[] inputData)
            throws Exception {

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, priv);

        byte[] decryptedBytes = cipher.doFinal(inputData);

        return decryptedBytes;
    }
    
    public static byte[] createInitializationVector()
    {
 
        // Used with encryption
        byte[] initializationVector
            = new byte[16];
        SecureRandom secureRandom
            = new SecureRandom();
        secureRandom.nextBytes(initializationVector);
        return initializationVector;
    }
    
    public static byte[] encryptSecret(byte[] plainText, SecretKey secretKey, byte[] initializationVector)
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
    
    
    public static byte[] decryptSecret(
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
     

            
            byte[] result
                = cipher.doFinal(cipherText);
     

            
            return result;
        }
    
    public static PublicKey bytesToPublicKey(byte[] keyBytes) throws Exception {
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(spec);
    }
	
    
    
    private static String[] createChallenge() {
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
	
	private static double resolveChallenge(String input) {
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
	
	
	public static SecretKey createAESKey()
	        throws Exception
	    {
	 
			String AES = "AES";
		
	        // Creating a new instance of
	        // SecureRandom class.
	        SecureRandom securerandom
	            = new SecureRandom();
	 
	        // Passing the string to
	        // KeyGenerator
	        KeyGenerator keygenerator
	            = KeyGenerator.getInstance(AES);
	 
	        // Initializing the KeyGenerator
	        // with 256 bits.
	        keygenerator.init(256, securerandom);
	        SecretKey key = keygenerator.generateKey();
	        return key;
	    }
	
	public static String getHash(byte[] inputToHash) throws NoSuchAlgorithmException, IOException {
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
    
}

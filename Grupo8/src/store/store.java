package store;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
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
import java.security.spec.X509EncodedKeySpec;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.json.simple.JSONObject;


public class store {
	static String bankHost = "127.0.0.1";
	static int bankPort = 0;
	
	private static final String ALGORITHM = "RSA";

	
	private static PublicKey publicKey;
	private static PrivateKey privateKey;
    private static SecretKey operationKey;

    private static String bankFile = null;
	
	
	private static String account = "store";
	
	private static byte[] initializationVector;
	
	public static void main(String[] args) {
		
		int port = 0;
		
		KeyPair generateKeyPair;
		try {
			generateKeyPair = generateKeyPair();
		
	    publicKey = generateKeyPair.getPublic();
	    privateKey = generateKeyPair.getPrivate();
		
		
		StringBuilder sb = new StringBuilder();
		
		for(String b: args) {
			sb.append(b);
		}
		
		if(sb.toString().length() > 4096) {
			System.out.println("ERROR: 135");
			System.exit(135);
		}
		
		
		String[] teste = sb.toString().split("-");
	
		if(teste.length == 1) {
			bankFile = "bank.auth";
			port = 5000;
		} else if(teste.length == 3) {
			for(String t: teste) {
				if(t.length() == 0)
					continue;
				if(t.charAt(0) == 'p') {
					try {
						port = Integer.parseInt(t.substring(1));
						
						if(checkPort(port) == 135) {
							System.out.println("ERROR: 135");
							System.exit(135);
						}
					} catch (Exception e) {
						System.out.println("ERROR: 135");
						System.exit(135);

					}
				} else if(t.charAt(0) == 's') {
					try {
						bankFile = t.substring(1);
						
						if(checkFileName(bankFile) == 135) {
							System.out.println("ERROR: 135");
							System.exit(135);
						}
					} catch (Exception e) {
						System.out.println("ERROR: 135");
						System.exit(135);

					}
				} else {
					System.out.println("ERROR: 135");
					System.exit(135);
				}
			}
		} else if(teste.length == 2) {
			for(String t: teste) {
				if(t.length() == 0)
					continue;
				if(t.charAt(0) == 'p') {
					try {
						port = Integer.parseInt(t.substring(1));
						bankFile = "bank.auth";
						
						if(checkPort(port) == 135) {
							System.out.println("ERROR: 135");
							System.exit(135);
						}
						
						if(checkFileName(bankFile) == 135) {
							System.out.println("ERROR: 135");
							System.exit(135);
						}
					} catch (Exception e) {
						System.out.println("ERROR: 135");
						System.exit(135);

					}
				} else if(t.charAt(0) == 's') {
					try {
						bankFile = t.substring(1);
						port = 5000;
						
						if(checkPort(port) == 135) {
							System.out.println("ERROR: 135");
							System.exit(135);
						}
						
						if(checkFileName(bankFile) == 135) {
							System.out.println("ERROR: 135");
							System.exit(135);
						}
					} catch (Exception e) {
						System.out.println("ERROR: 135");
						System.exit(135);

					}
				} else {
					System.out.println("ERROR: 135");
					System.exit(135);
				}
			}
		} else {
			System.out.println("ERROR: 135");
			System.exit(135);
		}
		
		
		ServerSocket server = null;
		try {			
			server = new ServerSocket(port);
			server.setReuseAddress(true);
			while(true) {
				System.out.println("Store Ativa, à espera de conecções!");
				Socket client = server.accept();
				System.out.println("Conectou");
				clientHandler clientSock = new clientHandler(client);
				
				new Thread(clientSock).start();
			}
			
		} catch (Exception e) {
			System.out.println("ERROR: 135");
			System.exit(135);
		}
		server.close();
		} catch (NoSuchAlgorithmException | NoSuchProviderException e) {
			System.out.println("ERROR: 135");
			System.exit(135);
		} catch (IOException e) {
			System.out.println("ERROR: 63");
			System.exit(63);
		}
	}
	
	private static String connec(String req) {
		try (Socket socket = new Socket(bankHost,bankPort)) {
			ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
			ObjectInputStream in = new ObjectInputStream(socket.getInputStream()); 
			
			int resp = authenticate1(out, in);
			if(resp == 0) {
				
				String hash = getHash(req.getBytes());
				StringBuilder t = new StringBuilder();
				
				String separator2 = "!#--#!";
				String separatorNonce = "!#--!#";

				
				t.append(req);
				t.append(separator2);
				t.append(hash);
				t.append(separatorNonce);
				t.append(generateNonce());
				
				
				if(operationKey != null) {
					
					byte[] encryptRequest = encryptSecret(t.toString().getBytes(StandardCharsets.UTF_8), operationKey, initializationVector);
					
										
					out.writeObject(encryptRequest);
					
					System.out.println("À espera de resposta...");
					
					byte[] ans = (byte[]) in.readObject();
					
					String ans1 = new String(decryptSecret(ans, operationKey, initializationVector));
					
					if(ans1.length() < 3) {
						System.out.println("ERROR 130");
					} else {
						System.out.println(ans1);
					}
					
					return ans1;
						
				}
			}

			return "";
			
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		JSONObject obj = new JSONObject();
		return obj.toJSONString();
	}
	
	private static int authenticate1(ObjectOutputStream out, ObjectInputStream in) {
		File bankFile1 = new File(bankFile);
		FileInputStream bankIn;
		
		if(!bankFile1.exists()) {
			System.out.println("ERROR: 130");
			System.exit(130);
		}
		
		
		try {
			
			//read bank pub key
			bankIn = new FileInputStream(bankFile1);
			byte [] bankPubKeyByte = bankIn.readAllBytes();
			
			PublicKey bankPubKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(bankPubKeyByte));
			
			//1 - envia identificaão, challenge e a sua chave pública -> encriptado com chave publica de banco
			String[] challengeInfo = createChallenge();
			
			String challenge = challengeInfo[0];
			String challengeSolution = challengeInfo[1];
			
			String separator = "#!--!#";
			String separator2 = "!#--#!";
			
			StringBuilder sb = new StringBuilder();
			sb.append(account);
			sb.append(separator);
			sb.append(challenge);
			sb.append(separator);
			sb.append(Base64.getEncoder().encodeToString(publicKey.getEncoded()));
			
			String hash1 = getHash(sb.toString().getBytes());
			
			sb.append(separator2);
			sb.append(hash1);
			
			
			out.writeObject(encryptPub(bankPubKey, sb.toString().getBytes()));
			
			//2 - recebe do banco a resposta ao desafio e um novo desafio -> encriptado com chave publica do cliente
			byte[] stResponse = (byte[]) in.readObject();

			byte[] unencryptedStResponse = decrypt(privateKey, stResponse);
			
			String h = new String(unencryptedStResponse);
			
			String[] x0 = h.split(separator2);
			
			
			if(!getHash(x0[0].getBytes()).equals(x0[1])) {
				JSONObject obj = new JSONObject();
				
				byte[] ansNonce = encryptSecret(obj.toJSONString().getBytes(), operationKey, initializationVector);
				
				out.writeObject(ansNonce);
				out.close();
			}
			
			
			
			String[] x = x0[0].split(separator);
			
			
			if(challengeSolution.equals(x[1])) {
				
				//3 - envia resposta do desafio -> encriptada com chave publica do banco
				
				String challengeToSolve = x[2];
				
				String solution = String.valueOf(resolveChallenge(challengeToSolve));

				
				StringBuilder sb1 = new StringBuilder();
				sb1.append(account);
				sb1.append(separator);
				sb1.append(solution);
				
				String hash3 = getHash(sb1.toString().getBytes());
				
				sb1.append(separator2);
				sb1.append(hash3);
				
				out.writeObject(encryptPub(bankPubKey, sb1.toString().getBytes()));
				
				
				//4 - recebe chave simetrica a utilizar futuramente -> encriptada com chave publica do cliente
				
				byte[] lastResponse = (byte []) in.readObject();
				
				byte[] unencryptedLastResponse = decrypt(privateKey, lastResponse);
				
				String last = new String(unencryptedLastResponse);
				
				String[] lastHash = last.split(separator2);
				
				
				initializationVector = Base64.getDecoder().decode(lastHash[1]);
				
				
				String[] last1 = lastHash[0].split(separator);
				
				
				byte[] opKeyByte = Base64.getDecoder().decode(last1[1]);
				
				operationKey = bytesToSecretKey(opKeyByte);
				
								
				return 0;
				
			}else {
								
				StringBuilder sb2 = new StringBuilder();
				sb2.append(account);
				sb2.append(separator);
				sb2.append("Resposta errada");
				
				String hashError = getHash(sb2.toString().getBytes());
				
				sb2.append(separator2);
				sb2.append(hashError);
		
				out.writeObject(encryptPub(bankPubKey, sb2.toString().getBytes()));
				
				return -2;
			}
			
			

			
			
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;
	}
	
	
	public static SecretKey bytesToSecretKey(byte[] keyBytes) throws Exception {
    	return new SecretKeySpec(keyBytes, 0, keyBytes.length, "AES");
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
     
            byte[] result = cipher.doFinal(cipherText);
     
            return result;
        }
	
	
	public static KeyPair generateKeyPair()
            throws NoSuchAlgorithmException, NoSuchProviderException {

        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ALGORITHM);

        SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");

        // 512 is keysize
        keyGen.initialize(2048, random);

        KeyPair generateKeyPair = keyGen.generateKeyPair();
        return generateKeyPair;
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
	
	public static int checkPort(int port) {
		if(port < 1024 || port > 65535)
			return 135;
		else 
			return 0;
	}
    
    public static int checkFileName(String name) {
		String reg = "^[a-z_.0-9-]*$";
		
		if ((name.length() > 1 && name.length() <= 127) && name.matches(reg))
			return 0;
		else
			return 135;
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
	
	
	public static byte[] generateNonce() {
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

	
	private static class clientHandler implements Runnable{
			
			private final Socket clientSocket;
			
			public clientHandler(Socket client) {
				this.clientSocket=client;
			}
	
			@Override
			public void run() {
				try{
					
					
					
					ObjectInputStream dIn = new ObjectInputStream(clientSocket.getInputStream());

					
					String message = (String) dIn.readObject();
					
					String separatorIp = "#ip#";
					
					String[] data = message.split(separatorIp);
					
					String[] ipPort = data[1].split(":");
					
					bankHost = ipPort[0];
					bankPort = Integer.parseInt(ipPort[1]);
					

					ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
					
					byte[] resp = connec(data[0]).getBytes();
					
					out.writeObject(resp);
			/*
					if (stuffs[0].equals("-m")) {
						String req = "-check " + stuffs[1] + " " + stuffs[2] + " " + stuffs[3] ;
						connec(req);
						
					}
				*/	
				} catch (Exception e) {
					// TODO: handle exception
				}
				
			}
			
		}
}

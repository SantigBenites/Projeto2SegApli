package Client;
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
	private static final String ALGORITHM = "RSA";
	
	private static PublicKey publicKey ;
    private static PrivateKey privateKey;
    private static SecretKey operationKey;
	
	static String host;
	static int port = 0;

	private static String account;
	private static String userFile;
	private static String bankFile;
	private static String operation;
	private static String vccFile;
	
	
	private static byte[] initializationVector;
	
	private static String value;
	public static void main(String[] args) {
			    	    
	    StringBuilder sb = new StringBuilder();

		
		for(String x: args) {
			sb.append(x);
		}

		if(sb.toString().length() > 4096) {
			System.out.println("ERROR: 130");
			System.exit(130);
		}
		
	    
	    String[] teste = sb.toString().split("-");
	    
		
	    boolean allow = false;
	    
	    if(teste.length > 7) {
	    	System.out.println("ERROR: 130");
    		System.exit(130);
	    }
	    
	    if(teste.length < 3) {
	    	for(String j: teste) {
	    		if(j.length() == 0)
	    			continue;
	    		if(j.charAt(0) == 'm')
	    			allow = true;
	    	}

	    	
	    } else {
	    	allow = true;
	    }
	    
	    if(allow) {
	    	for(String t: teste) {
	    		
	    		if(t.length() == 0)
	    			continue;
	    		
	    		if(t.charAt(0) == 'a') {
	    			account = t.substring(1);
	    			
	    			if(checkAccount() == 130) {
	    				System.out.println("ERROR: 130");
    					System.exit(130);
	    			}
	    			
	    		} else if(t.charAt(0) == 'n' || t.charAt(0) == 'd' || t.charAt(0) == 'c' || t.charAt(0) == 'g' || t.charAt(0) == 'm') {
	    			operation = String.valueOf(t.charAt(0));
	    			if(!operation.equals(String.valueOf('g'))) {
	    				value = t.substring(1);
	    				if(checkNumeric() == 130) {
	    					System.out.println("ERROR: 130");
	    					System.exit(130);
	    				}
	    			}
	    		} else if(t.charAt(0) == 's') {
	    			bankFile = t.substring(1);
	    			if(checkFileName(bankFile) == 130) {
	    				System.out.println("ERROR: 130");
    					System.exit(130);
	    			}
	    		} else if(t.charAt(0) == 'i') {
	    			host = t.substring(1);
	    			
	    			if(checkIp() == 130) {
	    				System.out.println("ERROR: 130");
    					System.exit(130);
	    			}
	    		} else if(t.charAt(0) == 'p') {
	    			port = Integer.parseInt(t.substring(1));
	    			
	    			if(checkPort() == 130) {
	    				System.out.println("ERROR: 130");
    					System.exit(130);
	    			}
	    		} else if(t.charAt(0) == 'u') {
	    			userFile = t.substring(1);
	    			if(checkFileName(userFile) == 130) {
	    				System.out.println("ERROR: 130");
    					System.exit(130);
	    			}
	    		} else if(t.charAt(0) == 'v') {
	    			vccFile = t.substring(1);
	    			
	    			if(checkFileName(vccFile) == 130) {
	    				System.out.println("aqui 1");
	    				System.out.println("ERROR: 130");
    					System.exit(130);
	    			}
	    			
	    			File f1 = new File(vccFile);
    				if(!f1.exists()) {
    					System.out.println("ERROR: 130");
    					System.exit(130);
    				}
	    			
	    			String ac = t.substring(1);
	    			String[] a = ac.split("_");
	    			account = a[0];
	    			
	    			if(checkAccount() == 130) {
	    				System.out.println("aqui 2");
	    				System.out.println("ERROR: 130");
    					System.exit(130);
	    			}
	    		}
	    	}
	    	
	    	if(operation == null) {
	    		System.out.println("ERROR: 130");
	    		System.exit(130);
	    	} else {
	    		if(!operation.equals(String.valueOf('m'))) {
	    			if(account == null) {
	    				System.out.println("ERROR: 130");
	    	    		System.exit(130);
	    			}
	    			if(userFile == null) {
	    				userFile = account + ".user";
	    				
		    			if(checkFileName(userFile) == 130) {
		    				System.out.println("ERROR: 130");
	    					System.exit(130);
		    			}
	    			}  			
		    		if(bankFile == null) {
		    			bankFile = "bank.auth";
			    		
		    			if(checkFileName(bankFile) == 130) {
		    				System.out.println("ERROR: 130");
	    					System.exit(130);
		    			}
		    		}
		    			
	    		} else {
	    			if(vccFile == null) {
	    				vccFile = chooseVccFile();
	    				if(vccFile == null) {
	    					System.out.println("ERROR: 130");
		    	    		System.exit(130);
	    				}
	    				if(checkFileName(vccFile) == 130) {
		    				System.out.println("ERROR: 130");
	    					System.exit(130);
		    			}
	    				
	    			}
	    		}
	    		if(host == null) {
	    			host = "127.0.0.1";
	    			
	    			if(checkIp() == 130) {
	    				System.out.println("ERROR: 130");
    					System.exit(130);
	    			}
	    		}
	    			
	    		
	    			
	    		if(port == 0 && !operation.equals("m")) {
	    			port = 3000;
	    			
	    			if(checkPort() == 130) {
	    				System.out.println("ERROR: 130");
    					System.exit(130);
	    			}
	    		}
	    		else {
	    			port = 5000;
	    			
	    			if(checkPort() == 130) {
	    				System.out.println("ERROR: 130");
    					System.exit(130);
	    			}
	    		}
	    			
	    	}
	    }
	    
	    
	    Socket socket;
		try {
			socket = new Socket(host, port);
		
	    
	    if(!operation.equals("m")) {
	    	File userFile1 = new File(userFile);
		    if (userFile1.exists()) {
			        
			    FileInputStream file = new FileInputStream(userFile1);
				ObjectInputStream objIn = new ObjectInputStream(file);
				publicKey = bytesToPublicKey((byte[]) objIn.readObject());
				privateKey = bytesToPrivateKey((byte[]) objIn.readObject());
				
				    
				objIn.close();
		    }
	    }
	    
	    
		if(operation.equals("n")) {
			 if(value == null) {
					System.out.println("ERROR: 130");
					System.exit(130);
				}
			
			if(Double.parseDouble(value) < 15.0) {
				System.out.println("ERROR: 130");
				System.exit(130);
			}
			
			File newuser = new File(userFile);
			if(newuser.exists()) {
				System.out.println("ERROR: 130");
				System.exit(130);
			}
		      if (newuser.createNewFile()) {
		        
		        KeyPair generateKeyPair = generateKeyPair();
			    publicKey = generateKeyPair.getPublic();
			    privateKey = generateKeyPair.getPrivate();
		        
		        FileOutputStream file = new FileOutputStream(newuser);
			    ObjectOutputStream objOut = new ObjectOutputStream(file);
			    
			    objOut.writeObject(publicKey.getEncoded());
			    objOut.writeObject(privateKey.getEncoded());
		      } 		      
		    
		     
			double valor = Double.parseDouble(value);
			String req = "-" + operation + " " + account + " " + valor;
			
			connec(socket, req);
			
		}
		
		else if(operation.equals("d")) {
			if(value == null) {
				System.out.println("ERROR: 130");
				System.exit(130);
			}
			double valor = Double.parseDouble(value);
			
			if(valor <= 0.0) {
				System.out.println("ERROR: 130");
				System.exit(130);
			}
			String[] t = userFile.split("\\.");
			
			
			if(!t[0].equals(account)) {
				System.out.println("ERROR: 130");
				System.exit(130);
			}
			
			String req = "-" + operation + " " + account + " " + valor;
			connec(socket, req);
		}
		
		else if(operation.equals("c")) {
			 if(value == null) {
					System.out.println("ERROR: 130");
					System.exit(130);
				}
			
			double valor = Double.parseDouble(value);
			
			if(valor <= 0.0) {
				System.out.println("ERROR: 130");
				System.exit(130);
			}
			
			String[] t = userFile.split("\\.");
			
			if(!t[0].equals(account)) {
				System.out.println("ERROR: 130");
				System.exit(130);
			}
			
			String req = "-" + operation + " " + account + " " + valor;
			connec(socket, req);
		}

		else if(operation.equals("g")) {
			
			String[] t = userFile.split("\\.");
			
			if(!t[0].equals(account)) {
				System.out.println("ERROR: 130");
				System.exit(130);
			}
			
			String req = "-" + operation + " " + account;
			connec(socket, req);
		}
		
		else if(operation.equals("m")) {
			
			 if(value == null) {
					System.out.println("ERROR: 130");
					System.exit(130);
				}
			
			double valor = Double.parseDouble(value);
			
			if(valor <= 0.0) {
				System.out.println("ERROR: 130");
				System.exit(130);
			}
			
			
			Socket socket1 = new Socket(host, port);
			
			String req = "-" + operation + " " + account + " " + vccFile + " " + value ;
			connec(socket1, req);
		}
		} catch (IOException e) {
			System.out.println("ERROR: 63");
			System.exit(63);
		} catch (ClassNotFoundException e) {
			System.out.println("ERROR: 130");
			System.exit(130);
		} catch (InterruptedException e) {
			System.out.println("ERROR: 63");
			System.exit(63);
		} catch (NoSuchAlgorithmException e) {
			System.out.println("ERROR: 130");
			System.exit(130);
		} catch (NoSuchProviderException e) {
			System.out.println("ERROR: 130");
			System.exit(130);
		} catch (Exception e) {
			System.out.println("ERROR: 130");
			System.exit(130);
		}

	}
	
	/*
	 * 
	 * 1 - envia identificaão, challenge e a sua chave pública -> encriptado com chave publica de banco
	 * 2 - recebe do banco a resposta ao desafio e um novo desafio -> encriptado com chave publica do cliente
	 * 3 - envia resposta do desafio -> encriptada com chave publica do banco
	 * 4 - recebe chave simetrica a utilizar futuramente -> encriptada com chave publica do cliente
	 * 
	 * TODO: No futuro, há mais coisas a circular como por exemplo a variavel a utilizar na funcao de hash
	 * 
	 */
	private static int authenticate(ObjectOutputStream out, ObjectInputStream in) {
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
//				
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
	
	
	private static void connec(Socket socket, String req) throws InterruptedException, IOException, ClassNotFoundException {
				
		ObjectOutputStream out;
		ObjectInputStream in;
		try {
			out = new ObjectOutputStream(socket.getOutputStream());
			
			if(operation.equals("m")) {
				
				BufferedReader reader = new BufferedReader(new FileReader(vccFile));
				
				String user = reader.readLine();
				String bankHost = reader.readLine();
				String bankPort = reader.readLine();
				
				
				String req1 = req + "#ip#" + bankHost + ":" + bankPort;
				
				
				out.writeObject(req1);
				
				System.out.println("À espera de resposta...");
				
				in = new ObjectInputStream(socket.getInputStream());
				
				byte[] ans = (byte[]) in.readObject();
				
				String ans1 = new String(ans);
				
				if(ans1.length() < 3) {
					System.out.println("ERROR 130");
					System.exit(130);
				} else {
					System.out.println(ans1);
					System.exit(0);
				}
				
			}else {
				in = new ObjectInputStream(socket.getInputStream());
				if(authenticate(out, in) == 0) {
					
					
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
							System.exit(130);
						} else {
							System.out.println(ans1);
							System.exit(0);

						}
							
					}
				
					
				}
			}
			
			
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	
	public static int checkPort() {
		if(port < 1024 || port > 65535)
			return 130;
		else 
			return 0;
	}
	
	
	public static int checkIp() {
		
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
	
	
	
	public static int checkAccount() {
		String reg = "^[0-9]*$";
		
		if(account.matches(reg))
			return 0;
		else
			return 130;
	}
	
	public static int checkFileName(String name) {
		String reg = "^[a-z_.0-9-]*$";
		
		if ((name.length() > 1 && name.length() <= 127) && name.matches(reg))
			return 0;
		else
			return 130;
	}
	
	
	public static int checkNumeric() {
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
	
	
	public static String chooseVccFile() {
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
	
	
	public static byte[] objectToByteArray(Object obj) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(bos);
		
		oos.writeObject(obj);
		oos.flush();
		
		return bos.toByteArray();
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
	
	public static PublicKey bytesToPublicKey(byte[] keyBytes) throws Exception {
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(spec);
    }
    
    public static PrivateKey bytesToPrivateKey(byte[] keyBytes) throws Exception {
    	PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(spec);
    }
    
    public static SecretKey bytesToSecretKey(byte[] keyBytes) throws Exception {
    	return new SecretKeySpec(keyBytes, 0, keyBytes.length, "AES");
    }
    
    
    //Challenges
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
	
	

}

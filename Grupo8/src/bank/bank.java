package bank;


import java.io.File;
import java.io.FileOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;

import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;


public class bank {

	private static final String ALGORITHM = "RSA";

	private static PrivateKey privateKey;
	private static BankServer bank;
	
	
	public static void main(String[] args) throws Exception {
		
		String bankFile = null;
		int port = 0;
		String host = InetAddress.getByName("localhost").getHostAddress();
		
		
		StringBuilder sb = new StringBuilder();
		
		for(String b: args) {
			sb.append(b);
		}

		if(sb.toString().length() > 4096) {
			System.out.println("ERROR: 125");
			System.exit(125);
		}
		
		String[] teste = sb.toString().split("-");
	
		if(teste.length == 1) {
			bankFile = "bank.auth";
			port = 3000;
		} else if(teste.length == 3) {
			for(String t: teste) {
				if(t.length() == 0)
					continue;
				if(t.charAt(0) == 'p') {
					try {
						port = Integer.parseInt(t.substring(1));
						
						if(checkPort(port) == 125) {
							System.out.println("ERROR: 125");
							System.exit(125);
						}
						
						
					} catch (Exception e) {
						System.out.println("ERROR: 125");
						System.exit(125);

					}
				} else if(t.charAt(0) == 's') {
					try {
						bankFile = t.substring(1);
						
						if(checkFileName(bankFile) == 125) {
							System.out.println("ERROR: 125");
							System.exit(125);
						}
					} catch (Exception e) {
						System.out.println("ERROR: 125");
						System.exit(125);

					}
				} else {
					System.out.println("ERROR: 125");
					System.exit(125);
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
						
						if(checkFileName(bankFile) == 125) {
							System.out.println("ERROR: 125");
							System.exit(125);
						}
						
						if(checkPort(port) == 125) {
							System.out.println("ERROR: 125");
							System.exit(125);
						}
						
					} catch (Exception e) {
						System.out.println("ERROR: 125");
						System.exit(125);

					}
				} else if(t.charAt(0) == 's') {
					try {
						bankFile = t.substring(1);
						port = 3000;
						
						if(checkFileName(bankFile) == 125) {
							System.out.println("ERROR: 125");
							System.exit(125);
						}
						
						if(checkPort(port) == 125) {
							System.out.println("ERROR: 125");
							System.exit(125);
						}
						
					} catch (Exception e) {
						System.out.println("ERROR: 125");
						System.exit(125);

					}
				} else {
					System.out.println("ERROR: 125");
					System.exit(125);
				}
			}
		} else {
			System.out.println("ERROR: 125");
			System.exit(125);
		}

		bank = new BankServer();
		
		KeyPair generateKeyPair = generateKeyPair();
		
        PublicKey pub = generateKeyPair.getPublic();
        privateKey = generateKeyPair.getPrivate();
        
       

		byte[] publicKey = pub.getEncoded();
		
		//insert pub key on auth file
        File authFile = new File(bankFile);
        
        if(authFile.exists()) {
        	System.out.println("ERROR: 125");
        	return;
        }
        FileOutputStream file = new FileOutputStream(authFile);
        file.write(publicKey);
        file.close();
        
        
		ServerSocket server = null;
		try {		
			server = new ServerSocket(port);
			//server.setReuseAddress(true);
			while(true) {
				System.out.println("Banco Ativo, à espera de conecções!");
				Socket client = server.accept();
				System.out.println("Conectou");
				ClientThread cliente = new ClientThread(client, privateKey, bank, port, host);
				cliente.start();
			}
			
		} catch (Exception e) {
			System.out.println("protocol_error");
		}
		server.close();
	}
	
	
	public static KeyPair generateKeyPair()
	            throws NoSuchAlgorithmException, NoSuchProviderException {

	        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ALGORITHM);

	        SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");

	        // 512 is keysize
	        keyGen.initialize(4096, random);

	        KeyPair generateKeyPair = keyGen.generateKeyPair();
	        return generateKeyPair;
	}

	

	
	
	////////PARA AUTH FILE
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
        
    /////ATE AQUI
	
    
    public static int checkPort(int port) {
		if(port < 1024 || port > 65535)
			return 125;
		else 
			return 0;
	}
    
    public static int checkFileName(String name) {
		String reg = "^[a-z_.0-9-]*$";
		
		if ((name.length() > 1 && name.length() <= 127) && name.matches(reg))
			return 0;
		else
			return 125;
	}
    
}







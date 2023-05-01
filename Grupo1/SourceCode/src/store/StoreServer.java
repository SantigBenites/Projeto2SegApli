package store;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.Key;
import java.security.PublicKey;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.crypto.SealedObject;
import javax.crypto.spec.DHParameterSpec;

import message.SecureMessage;
import security.Security;

public class StoreServer extends Thread {
	
	private static final int ERROR_PROTOCOL = 63;
	
	private ObjectInputStream inClient;
	private ObjectOutputStream outClient;
	private ObjectInputStream inBank;
	private ObjectOutputStream outBank;
	
	private String ipBankAddress;
	private int bkPort;
	
	private Socket socketClient;
	private Socket socketBank;

	private PublicKey serverPK;
	private Key sessionKey;

	
	
	public StoreServer(Socket socketClient,DHParameterSpec paramsDH, PublicKey serverPK) {
		this.ipBankAddress = "127.0.0.1";
    	this.bkPort = 3000;
		this.socketClient = socketClient;
		this.serverPK = serverPK;
		
		try {	
			this.inClient = new ObjectInputStream(socketClient.getInputStream());
			this.outClient = new ObjectOutputStream(socketClient.getOutputStream());	
			
		} catch (IOException e) {
			// error setting objectStream
		}		
	}
	
	@Override
 	public void run() {
		try {	
			SealedObject keyClientBank = (SealedObject) inClient.readObject();
			SealedObject secureMessage = (SealedObject) inClient.readObject();
			withdraw(keyClientBank, secureMessage);		
			
		} catch (Exception e) {
			//error in readObject, do nothing
		} 
		
		
	}
	
	public SecureMessage receiveSafe() {
		try {
			SealedObject obj = (SealedObject) inBank.readObject();
			SecureMessage sm = (SecureMessage) Security.unseal(obj, this.sessionKey);

			if(sm != null && sm.verifyIntegrityAndTimeout()) {
				return sm;
			}

		} catch (ClassNotFoundException | IOException e) {
			System.exit(ERROR_PROTOCOL);
		}
		return null;

	}
	
	private Object readTimeoutHandler() {
		ExecutorService executor = Executors.newCachedThreadPool();
		Callable<Object> task = new Callable<Object>() {
			public Object call() {
				SecureMessage m = receiveSafe();
				
				if(m != null) {
					return m;
				}
				return null;
			}
		};
		Future<Object> future = executor.submit(task);
		try {
			return future.get(10, TimeUnit.SECONDS); 
		} catch (TimeoutException ex) {
			System.exit(ERROR_PROTOCOL);
		} catch (InterruptedException e) {
			// handle the interrupts
		} catch (ExecutionException e) {
			// handle other exceptions
		} finally {
			// do nothing
		}
		return null;
	}
	
	public void withdraw(SealedObject keyClientBank, SealedObject secureMessage) {
		try {
			connectBank(this.ipBankAddress, this.bkPort);
			//reencaminhar pedido para o servidor
            this.outBank.writeObject(keyClientBank);
            this.outBank.writeObject(secureMessage);

        
        	SecureMessage obj = (SecureMessage) readTimeoutHandler();
        	System.out.println(obj.getMessage().getResponse());
        	outClient.writeObject(obj.getMessage().getResponseSafe());
           
		} catch (Exception e) {
			stopConnection();
		}
		
	}
	
	public void connectBank(String host, int port) {		
		try {	
			this.socketBank = new Socket(host, port);
		} catch (IOException e) {
			System.exit(ERROR_PROTOCOL);
		}
		try {
			this.outBank = new ObjectOutputStream(socketBank.getOutputStream());	
			this.inBank = new ObjectInputStream(socketBank.getInputStream());
			this.sessionKey = Security.handShakeClient(this.serverPK, true, this.inBank, this.outBank);

		}catch(IOException | ClassNotFoundException e) {
			System.exit(ERROR_PROTOCOL);
			
		}
	}
	
	
	
	
	public void stopConnection() {
        try {
			inClient.close();
			outClient.close();
	        this.socketClient.close();
	        inBank.close();
			outBank.close();
	        this.socketBank.close();
		} catch (IOException e) {
			//sockets already closed
		}
    }

}

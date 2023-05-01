package bank;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.Key;
import java.security.KeyPair;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.crypto.SealedObject;
import javax.crypto.spec.DHParameterSpec;

import org.json.simple.JSONObject;

import mbec.Operation;
import message.Message;
import message.SecureMessage;
import security.Security;
import user.Card;
import user.CardDTO;
import user.User;

public class BankServer extends Thread {

	private ObjectInputStream in;
	private ObjectOutputStream out;
	private Map<Integer, User> usersMap;
	private KeyPair keysRSA;
	private Socket socket;
	private Key sessionKey;
	private Boolean isStore;


	public BankServer(Socket s, Map<Integer, User> usersMap, KeyPair keysRSA, DHParameterSpec params) {
		this.socket = s;
		this.usersMap = usersMap;
		this.keysRSA = keysRSA;
		try {	
			this.in = new ObjectInputStream(s.getInputStream());
			this.out = new ObjectOutputStream(s.getOutputStream());	

			//handshake with client

			Pair pair = Security.handShakeServer(this.keysRSA.getPrivate(), this.in, this.out);
			this.sessionKey = pair.getKey();
			this.isStore = pair.getValue();

		} catch (IOException | ClassNotFoundException e) {
			// error setting handshake or objectStream
			System.err.println("Error setting handshake or objectStream");
		}		
	}


	////////////////////////////////////////////////////////////////

	private void newAccount(Message received) {
		int account = Integer.parseInt(received.getAccount());
		double initialBalance = received.getBalance();
		int pin = received.getPin();
		User user = new User();
		user.setAccount(account);
		user.setBalance(initialBalance);
		user.setPin(pin);


		// Insert user in users map
		this.usersMap.put(account, user);

		// Insert data into JSONObject
		JSONObject obj = new JSONObject();    
		obj.put("account", account);    
		obj.put("initial_balance", initialBalance);

		//send response
		Message m = new Message();
		m.setResponse(obj);
		sendSafe(m);

		stopConnection();
	}

	private void deposit(Message received) {
		int account = Integer.parseInt(received.getAccount());
		double amount = received.getAmount();
		int pin = received.getPin();

		User user = usersMap.get(account);

		if(user != null && pin == user.getPin()) {
			user.deposit(amount);

			// Insert data into JSONObject
			JSONObject obj = new JSONObject();
			obj.put("deposit", amount);
			obj.put("account", account);      

			//send response
			Message m = new Message();
			m.setResponse(obj);
			sendSafe(m);

			stopConnection();
		}else {
			stopConnection();
		}

	}

	private void getBalance(Message received) {
		int account = Integer.parseInt(received.getAccount());
		int pin = received.getPin();

		User user = usersMap.get(account);

		if(user != null && pin == user.getPin()) {
			double balance = user.getBalance();
			// Insert data into JSONObject
			JSONObject obj = new JSONObject();
			obj.put("balance", balance);
			obj.put("account", account);

			//send response
			Message m = new Message();
			m.setResponse(obj);
			sendSafe(m);

			System.out.println(obj);
			stopConnection();
		}else {
			stopConnection();
		}
	}

	private void createCard(Message received) {
		int account = Integer.parseInt(received.getAccount());
		double amount = received.getAmount();
		int pin = received.getPin();

		User user = usersMap.get(account);

		if(user != null && pin == user.getPin() && user.getBalance() >= amount) {
			JSONObject obj = user.createCard(amount);
			if(obj != null) {
				Message m = new Message();
				m.setResponse(obj);
				sendSafe(m);
			}
		}
		stopConnection();
	}

	public void withdraw() {
		SecureMessage request = null;
		SealedObject keyClientBankSO = null;
		Key keyClientBank = null;
		try {
			keyClientBankSO = (SealedObject) in.readObject(); 
			keyClientBank = (Key) Security.unseal(keyClientBankSO, this.keysRSA.getPrivate());

			SealedObject secureMessageSO = (SealedObject) in.readObject();
			request = (SecureMessage) Security.unseal(secureMessageSO, keyClientBank);
		} catch (Exception e ) {
			//catch, do nothing
		}

		Message received = null;
		if(request != null) {
			received = request.getMessage();
		}

		if(received != null && keyClientBank != null) {

			double amount = received.getAmount();
			CardDTO cardDTO = received.getCardDTO();
			int account = cardDTO.getAccount();

			if(this.usersMap.containsKey(account)) {
				User user = this.usersMap.get(account);
				Card cardObtained = user.getCard();
				if(cardObtained != null) {
					if(cardDTO.getAccount() == cardObtained.getAccount() && cardDTO.getAmount() == cardObtained.getAmount() 
							&& cardDTO.getCardName().equals(cardObtained.getCardName())) {
						if(cardObtained.isActive() && cardObtained.getAmount() >= amount) {

							user.withdraw(amount);
							cardObtained.disable();
							JSONObject obj = new JSONObject();
							obj.put("vcc_file", cardObtained.getCardName());
							obj.put("vcc_amount_used", amount);

							System.out.println("withdraw");
							//send response
							Message m = new Message();
							m.setResponse(obj);
							SecureMessage sm = new SecureMessage(m);
							m.setResponseSafe(Security.seal(sm, keyClientBank));

							sendSafe(m);

							System.out.println(obj);
						}	
					}
				}	
			}
		}
		stopConnection();	
	}


	////////////////////////////////////////////////////////////////


	@Override
	public void run() {
		try {	
			if(isStore) {
				withdraw();			
			}
			else {
				Message received = (Message) readTimeoutHandler();
				if(received != null) {
					Operation operation = received.getOperation();
					if(operation.equals(Operation.N)) {
						newAccount(received);
					}else if(operation.equals(Operation.D)) {
						deposit(received);
					}else if(operation.equals(Operation.G)) {
						getBalance(received);
					}else if(operation.equals(Operation.C)) {
						createCard(received);
					}
				}
			}
		} catch (Exception e) {
			//do nothing if exception, exit would end main thread
			System.err.println("Error making operation");
		} 
	}



	private Object readTimeoutHandler() {
		ExecutorService executor = Executors.newCachedThreadPool();
		Callable<Object> task = new Callable<Object>() {
			public Object call() {
				return receiveSafe();
			}
		};
		Future<Object> future = executor.submit(task);
		try {
			return future.get(10, TimeUnit.SECONDS); 
		} catch (TimeoutException ex) {
			System.out.println("protocol_error");
			stopConnection();
		} catch (InterruptedException e) {
			// handle the interrupts
		} catch (ExecutionException e) {
			// handle other exceptions
		} finally {
			//do nothing
		}
		return null;
	}





	public void sendSafe(Message m) {
		SecureMessage sm = new SecureMessage(m);		
		SealedObject obj = Security.seal(sm, this.sessionKey);
		try {
			this.out.writeObject(obj);

		} catch (IOException e) {
			// error sending safe message, do nothing
		}
	}

	public Message receiveSafe() {
		try {
			SealedObject obj = (SealedObject) in.readObject();
			SecureMessage sm = (SecureMessage) Security.unseal(obj, this.sessionKey);

			if(sm != null && sm.verifyIntegrityAndTimeout()) {
				return sm.getMessage();
			}

		} catch (ClassNotFoundException | IOException e) {
			// error receiving safe message, do nothing
		}
		return null;


	}


	public void stopConnection() {
		try {
			in.close();
			out.close();
			this.socket.close();
		} catch (IOException e) {
			//catch if socket already closed
		}
	}

}

package mbec;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;

import javax.crypto.SealedObject;

import org.apache.commons.validator.routines.InetAddressValidator;
import org.json.simple.JSONObject;

import files.FileManager;
import message.Message;
import message.SecureMessage;
import security.Security;
import user.CardDTO;



public class MBeC {

	private static final int ERROR_CODE = 130;
	private static final int ERROR_PROTOCOL = 63;
	private static final int MINIMUM_BALANCE = 15;
	private static final int MINIMUM_AMOUNT = 0;
	private static final int MAXIMUM_PIN_VALUE = 9001;

	private static final int SUCCESSFUL_EXIT = 0;

	private static final String FILE_NAME = "([_\\-\\.0-9a-z]){1,127}";
	private static final String PORT = "(102[4-9]|10[3-9]\\d|1[1-9]\\d{2}|[2-9]\\d{3}|[1-5]\\d{4}|6[0-4]\\d{3}|65[0-4]\\d{2}|655[0-2]\\d|6553[0-5])";
	private static final String AMMOUNT = "(\\d|[1-9]\\d{1,7}|1\\d{8}|2[0-8]\\d{7}|29[0-3]\\d{6}|294[0-8]\\d{5}|2949[0-5]\\d{4}|29496[0-6]\\d{3}|294967[01]\\d{2}|2949672[0-8]\\d|29496729[0-5]).([0-9]{0,2})";

	private ObjectInputStream in;
	private ObjectOutputStream out;

	private String authFile;
	private String ipAddress;
	private String ipStoreAddress;
	private int port;
	private int stPort;
	private String userFile;
	private String account;
	private double balance;
	private double amount;
	private Operation operation;
	private String virtualCreditCardFile;
	private String sequentialNumber;
	private Socket socket;

	private PublicKey serverPK;
	private Key sessionKey;

	public MBeC(String[] args) {
		iniciarAtributos();
		checkArgs(args);
		readAuthFile();
	}


	public void iniciarAtributos() {
		this.authFile = "bank.auth";
		this.ipAddress = "127.0.0.1";
		this.port = 3000;
		this.amount = 0.0;
		this.balance = 0.0;
		this.sequentialNumber = "0";
	}

	public void connect(String host, int port, boolean handshake) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException {		
		try {	
			this.socket = new Socket(host, port);
		} catch (IOException e) {
			// catch if host does not exist in that port
			System.exit(ERROR_PROTOCOL);
		}
		try {
			this.out = new ObjectOutputStream(socket.getOutputStream());	
			this.in = new ObjectInputStream(socket.getInputStream());

			//Hand shake
			if(handshake) {
				this.sessionKey = Security.handShakeClient(this.serverPK, false, this.in, this.out);
			}

		} catch(IOException | ClassNotFoundException e) {
			// error in objectsStream creation
			System.exit(ERROR_PROTOCOL);

		}
	}


	//////////////////////////////////////////////////////////////////////
	private void deposit() {
		try {
			File userFile = new File(this.userFile);
			if(this.amount > MINIMUM_AMOUNT && userFile.exists()) {
				String pin = readPinFromFile(userFile);
				connect(this.ipAddress, this.port, true);

				//send request
				Message message = new Message();
				message.setOperation(operation);
				message.setAmount(this.amount);
				message.setAccount(this.account);
				message.setPin(Integer.parseInt(pin));
				sendSafe(message);

				//receive response
				JSONObject obj = (JSONObject) readTimeoutHandler();
				if(obj != null) {
					System.out.println(obj);
				}
				else {
					stopConnection();
					System.exit(ERROR_CODE);
				}

				stopConnection();

			}
			else {
				System.exit(ERROR_CODE);
			}
		} catch (Exception e) {
			// any exception, program should exit
			System.exit(ERROR_CODE);
		}
	}

	private void newAccount() {
		try {
			File userFile = new File(this.userFile);
			if(this.balance >= MINIMUM_BALANCE && !userFile.exists()) {
				int pin = createUserFile();
				connect(this.ipAddress, this.port, true);

				//send request
				Message message = new Message();
				message.setOperation(operation);
				message.setBalance(this.balance);
				message.setAccount(this.account);
				message.setPin(pin);
				sendSafe(message);

				//receive response
				JSONObject obj = (JSONObject) readTimeoutHandler();
				if(obj != null) {
					System.out.println(obj);
				}
				else {
					stopConnection();
					System.exit(ERROR_CODE);
				}

				stopConnection();

			}
			else {
				System.exit(ERROR_CODE);
			}
		} catch (Exception e) {
			// any exception, program should exit
			System.exit(ERROR_CODE);
		}


	}

	private void getBalance() {
		try {
			File userFile = new File(this.userFile);
			if(userFile.exists()) {
				String pin = readPinFromFile(userFile);
				connect(this.ipAddress, this.port, true);

				//send request
				Message message = new Message();
				message.setOperation(operation);
				message.setAccount(this.account);
				message.setPin(Integer.parseInt(pin));
				sendSafe(message);

				//receive request
				JSONObject obj = (JSONObject) readTimeoutHandler();
				System.out.println(obj);

				stopConnection();
			}
			else {
				System.exit(ERROR_CODE);
			}
		} catch (Exception e) {
			// any exception, program should exit
			System.exit(ERROR_CODE);
		}
	}

	private void createCard() {
		try {
			File userFile = new File(this.userFile);
			if(userFile.exists()) {
				String pin = readPinFromFile(userFile);
				connect(this.ipAddress, this.port, true);

				//send request
				Message message = new Message();
				message.setOperation(operation);
				message.setAmount(this.amount);
				message.setAccount(this.account);
				message.setPin(Integer.parseInt(pin));
				sendSafe(message);

				//receive responce
				JSONObject obj = (JSONObject) readTimeoutHandler();
				if(obj != null) {
					CardDTO.writeCardDTOToFile(obj);
					System.out.println(obj);
				}else {
					stopConnection();
					System.exit(ERROR_CODE);
				}
				stopConnection();
			}
			else {
				System.exit(ERROR_CODE);
			}
		} catch (Exception e) {
			// any exception, program should exit
			System.exit(ERROR_CODE);
		}
	}

	//STORE
	public void withdraw() {
		try {
			CardDTO card = CardDTO.readFromFile(this.virtualCreditCardFile);
			if(card.getAmount() > 0 && this.amount <= card.getAmount()) {
				this.sessionKey = Security.getSecureRandomKey(Security.SECURE_ALGORITHM_FOR_SYMETRIC_KEY, Security.SECURE_BIT_SIZE_FOR_SYMETRIC_KEY);
				connect(this.ipAddress, this.port, false);
				Message message = new Message();
				message.setOperation(operation);
				message.setCardDTO(card);
				message.setAmount(this.amount);

				sendSafeObject(this.sessionKey, this.serverPK);
				sendSafe(message, this.sessionKey);

				JSONObject obj = (JSONObject) readTimeoutHandler();
				if(obj != null) {
					System.out.println(obj);
				}
				else {
					stopConnection();
					System.exit(ERROR_CODE);
				}
				stopConnection();
			}
			else {
				System.exit(ERROR_CODE);
			}
		} catch (Exception e) {
			// any exception, program should exit
			System.exit(ERROR_CODE);
		}

	}

	//////////////////////////////////////////////////////////////////////

	public void makeOperation() {
		if(this.operation.equals(Operation.N)) {
			newAccount();
		}else if(this.operation.equals(Operation.D)) {
			deposit();
		}else if(this.operation.equals(Operation.G)) {
			getBalance();
		}else if(this.operation.equals(Operation.C)) {
			createCard();
		}else if(this.operation.equals(Operation.M)) {
			withdraw();
		}
		System.exit(SUCCESSFUL_EXIT);
	}

	private void writeToFile(File file, String string) {
		try(FileWriter myWriter = new FileWriter(file);){
			myWriter.write(string);
			myWriter.close();
		}catch(IOException e) {
			System.err.println("Error writing string to file");
		}
	}

	private String readPinFromFile(File file) {
		Path path = Paths.get(file.getPath());
		try {
			return Files.readAllLines(path, StandardCharsets.UTF_8).get(0);
		} catch (IOException e) {
			// error reading pin file
			System.exit(ERROR_CODE);
		}
		return null;
	}

	public int createUserFile() {
		try {
			File userFileTemp = new File(this.userFile);
			if(userFileTemp.createNewFile()) {
				//do nothing
			}

			SecureRandom random = new SecureRandom();
			int pin = random.nextInt(MAXIMUM_PIN_VALUE) + 999;
			writeToFile(userFileTemp, String.valueOf(pin));
			return pin;
		} catch (Exception e) {
			System.err.println("Error creating userfile");
		}
		return -1;
	}

	public void checkArgsAux() {

		try{
			Integer.parseInt(account);
		}catch(NumberFormatException e) {
			System.exit(ERROR_CODE);
		}
		InetAddressValidator validator = InetAddressValidator.getInstance();
		if (!validator.isValidInet4Address(ipAddress)) {
			System.exit(ERROR_CODE);
		}
		if(!Pattern.matches(FILE_NAME, userFile) || !Pattern.matches(FILE_NAME, authFile) || !Pattern.matches(FILE_NAME, virtualCreditCardFile)) {
			System.exit(ERROR_CODE);
		}
		if(!Pattern.matches(PORT, String.valueOf(port))) {
			System.exit(ERROR_CODE);
		}
		if(!Pattern.matches(AMMOUNT, String.valueOf(amount)) || !Pattern.matches(AMMOUNT, String.valueOf(balance))) {
			System.exit(ERROR_CODE);
		}
	}

	public void checkArgs(String[] args) {
		int length = 0;
		for(String str : args){
			length += str.length();
		}
		if(length > 4096) {
			System.exit(ERROR_CODE);
		}
		StringBuilder sb = new StringBuilder();	
		for (String string : args) {
			boolean hasCommand = string.contains("-s") || string.contains("-i") || string.contains("-p") || string.contains("-u") || string.contains("-a") ||
					string.contains("-n") || string.contains("-d") || string.contains("-c") || string.contains("-g") || string.contains("-v") || string.contains("-m");

			if(hasCommand && string.length() > 2){
				aux(sb, string);
			}
			else {
				sb.append(string);
				sb.append(" ");
			}
		}
		String[] result = sb.toString().split(" ");
		for (int i = 0; i < result.length; i+=2) {
			if(result[i].equals("-s")) {
				this.authFile = result[i+1];
			}
			else if(result[i].equals("-i")) {
				this.ipAddress = result[i+1];
			}
			else if(result[i].equals("-p")) {
				this.port = Integer.parseInt(result[i+1]);
			}
			else if(result[i].equals("-u")) {
				this.userFile = result[i+1];
			}
			else if(result[i].equals("-a")) {
				this.account = result[i+1];
				this.virtualCreditCardFile = this.account + "_" + this.sequentialNumber; 
			}
			else if(result[i].equals("-n")) {
				this.balance = Double.parseDouble(result[i+1]);
				this.operation = Operation.N;
			}
			else if(result[i].equals("-d")) {
				this.amount = Double.parseDouble(result[i+1]);
				this.operation = Operation.D;
			}
			else if(result[i].equals("-c")) {
				this.amount = Double.parseDouble(result[i+1]);
				this.operation = Operation.C;
			}
			else if(result[i].equals("-g")) {
				this.operation = Operation.G;
			}
			else if(result[i].equals("-v")) {
				this.virtualCreditCardFile = result[i+1];
			}
			else if(result[i].equals("-m")) {
				this.amount = Double.parseDouble(result[i+1]);
				this.operation = Operation.M;
				this.port = 5000;
			}	 
		}
		checkArgsAux();

	}

	public boolean aux(StringBuilder sb, String string) {
		sb.append(string.substring(0, 2));
		sb.append(" ");
		sb.append(string.substring(2));
		sb.append(" ");
		return true;
	}

	public void printAttributes() {
		System.out.println("Status Atributos:");
		System.out.println("authFile: " + this.authFile);
		System.out.println("ipBankAddress: " + this.ipAddress);
		System.out.println("ipStoreAddress: "+ this.ipStoreAddress);
		System.out.println("bkPort: " + this.port);
		System.out.println("stPort: " + this.stPort);
		System.out.println("userFile: " +this.userFile);
		System.out.println("account: " + this.account);
		System.out.println("balance: " + this.balance);
		System.out.println("amount: " + this.amount);
		System.out.println("operation: " + this.operation);
		System.out.println("virtualCreditCardFile: " + virtualCreditCardFile);
		System.out.println("sequentialNumber: " + this.sequentialNumber);
	}

	public void stopConnection() {
		try {
			in.close();
			out.close();
			this.socket.close();
		} catch (IOException e) {
			// exception if sockets already closed.
		}
	}

	//////////////////////////////////////////////////////////////////////////
	public void readAuthFile() {
		PublicKey pk = (PublicKey) FileManager.readObjectFromFile(this.authFile);
		if(pk != null) {
			this.serverPK = pk;
		} else {
			System.exit(ERROR_CODE);
		}
	}

	public void sendSafe(Message m) {
		SecureMessage sm = new SecureMessage(m);
		try {
			SealedObject obj = Security.seal(sm, this.sessionKey);
			this.out.writeObject(obj);

		} catch (IOException e) {
			System.exit(ERROR_PROTOCOL);
		}
	}

	public void sendSafeObject(Serializable obj, Key key) {
		try {
			SealedObject o = Security.seal(obj, key);
			this.out.writeObject(o);

		} catch (IOException e) {
			// error sending safe message
			System.exit(ERROR_PROTOCOL);
		}
	}

	public void sendSafe(Message m, Key key) {
		SecureMessage sm = new SecureMessage(m);
		try {
			SealedObject obj = Security.seal(sm, key);
			this.out.writeObject(obj);

		} catch (IOException e) {
			// error sending safe message
			System.exit(ERROR_PROTOCOL);
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
			// error receiving safe message
			System.exit(ERROR_PROTOCOL);
		}
		return null;

	}

	private Object readTimeoutHandler() {
		ExecutorService executor = Executors.newCachedThreadPool();
		Callable<Object> task = new Callable<Object>() {
			public Object call() {
				Message m = receiveSafe();
				if(m != null) {
					return m.getResponse();
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

	////////////////////////////////////////////////////////
	public static void main(String[] args) {
		MBeC mbec = new MBeC(args);
		mbec.makeOperation();

	}

}

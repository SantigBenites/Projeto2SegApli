package bank;

import java.io.IOException;
import java.net.ServerSocket;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.crypto.spec.DHParameterSpec;

import files.FileManager;
import security.Security;
import user.User;

public class Bank {
	
	private static final int ERROR_CODE = 125;
	
	private static String REGEX_PATTERN = "((-p(\\s*)(102[4-9]|10[3-9]\\d|1[1-9]\\d{2}|[2-9]\\d{3}|[1-5]\\d{4}|6[0-4]\\d{3}|65[0-4]\\d{2}|655[0-2]\\d|6553[0-5])|-s(\\s*)[_\\-\\.0-9a-z]{1,127})(\\s*)){0,2}";
	private String authFile;
	private int port;
	private Map<Integer, User> usersMap;
	private KeyPair keysRSA;
	private DHParameterSpec paramsDH;
	private static ServerSocket ss;
	
	
	
	public static void main(String[] args) throws Exception {
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
	        public void run() {
	            System.out.println("Shutting down ...");
	            //System.exit(SUCCESSUL_EXIT);
	        }
	    });
		Bank bank = new Bank(args);
		bank.receiveRequests();
    }
	
	public Bank(String[] args) {
		port = 3000;
		authFile = "bank.auth";
		checkArgs(args);
		this.usersMap = new HashMap<>();
		generateAuthFile();
	}
	
	
	//CHECK ARGS
	private void checkArgs(String[] args) {	
		String str = String.join(" ", args);
		if(!Pattern.matches(REGEX_PATTERN, str)) {
			System.exit(125);
		}
		
		StringBuilder sb = new StringBuilder();
		boolean sawS = false;
		boolean sawP = false;
		for (String string : args) {
			if(string.contains("-p") && string.length() > 2 && !sawP) {
				sb.append(string.substring(0, 2));
				sb.append(" ");
				sb.append(string.substring(2));
				sb.append(" ");
				sawP = true;
			}else if(string.contains("-s") && string.length() > 2 && !sawS){
				sb.append(string.substring(0, 2));
				sb.append(" ");
				sb.append(string.substring(2));
				sb.append(" ");
				sawS = true;
			}else {
				if(string.contains("-s")) {
					sawS = true;
				}else if(string.contains("-p")) {
					sawP = true;
				}
				sb.append(string);
				sb.append(" ");
			}
		}
		String[] result = sb.toString().split(" ");
		for (int i = 0; i < result.length; i+=2) {
			if(result[i].equals("-p")) {
				this.port = Integer.parseInt(result[i+1]);
			} else if(result[i].equals("-s")) {
				this.authFile = result[i+1];
			}
		}
		checkArgsAux();
	}
	
	private void checkArgsAux() {
		if(this.port < 1024 || this.port > 65535) {
			System.exit(125);
		}
		if(this.authFile.equals(".") || this.authFile.equals("..") 
				|| this.authFile.length() < 1 || this.authFile.length() > 127){
					System.exit(ERROR_CODE);
				}
	}
	
	
	
	//DEAL WITH REQUESTS
	private void receiveRequests() {
		try {	
        	ss = new ServerSocket(this.port);
        	while (true) {
        		BankServer bs = new BankServer(ss.accept(), this.usersMap, this.keysRSA, this.paramsDH);
        		bs.start();
        	}
        } catch (IOException e) {
        	// Catch if port is already being used
        	System.exit(ERROR_CODE);
        }
	}
	
	//GENERATE AUTH-FILE
	private void generateAuthFile() {
		try {
			
			this.keysRSA = Security.genAssymetricKeys();
			
			//file already exists
			if(!FileManager.writeObjectToFile(this.keysRSA.getPublic(), this.authFile)) {
				System.exit(ERROR_CODE);
			}
			
		} catch (NoSuchAlgorithmException e) {
			//Key will always exist
		}
				
	}


}



package store;

import java.net.ServerSocket;
import java.security.PublicKey;
import java.util.regex.Pattern;

import javax.crypto.spec.DHParameterSpec;

import files.FileManager;

public class Store {
	
	private static final int ERROR_CODE = 63;
	private static final int INPUT_ERROR = 125;
	
	private static String REGEX_PATTERN = "((-p(\\s*)(102[4-9]|10[3-9]\\d|1[1-9]\\d{2}|[2-9]\\d{3}|[1-5]\\d{4}|6[0-4]\\d{3}|65[0-4]\\d{2}|655[0-2]\\d|6553[0-5])|-s(\\s*)[_\\-\\.0-9a-z]{1,127})(\\s*)){0,2}";
	private int port;
	private String authFile;
	
	private DHParameterSpec paramsDH;
	private PublicKey serverPK;
	
	public void checkArgsAux() {
		if(this.port < 1024 || this.port > 65535) {
			System.exit(INPUT_ERROR);
		}
		if(this.authFile.equals(".") || this.authFile.equals("..") 
				|| this.authFile.length() < 1 || this.authFile.length() > 127){
					System.exit(INPUT_ERROR);
				}
	}
	
	public void checkArgs(String[] args) {
		String str = String.join(" ", args);
		if(!Pattern.matches(REGEX_PATTERN, str)) {
			System.exit(INPUT_ERROR);
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
			}else if(result[i].equals("-s")) {
				this.authFile = result[i+1];
			}
		}
		checkArgsAux();
	}
	
	public  void receiveRequests() {
		try {	
        	ServerSocket ss = new ServerSocket(this.port);
        	while (true) {
        		StoreServer bs = new StoreServer(ss.accept(), this.paramsDH, this.serverPK);
        		bs.start();
        	}

        } catch (Exception e) {
        	// error setting server socket 
        	System.exit(ERROR_CODE);
        }
	}
	
	public void readAuthFile() {
		PublicKey pk = (PublicKey) FileManager.readObjectFromFile(this.authFile);
		if(pk != null) {
			this.serverPK = pk;

		} else {
			System.exit(ERROR_CODE);
		}
	}
	
	public Store(String[] args) {
		this.port = 5000;
		this.authFile = "bank.auth";
		checkArgs(args);
		readAuthFile();
	}

	public static void main(String[] args) {
		Runtime.getRuntime().addShutdownHook(new Thread() {
	        public void run() {
	            System.out.println("Shutting down ...");
	            //System.exit(SUCCESSUL_EXIT);
	        }
	    });
		Store store = new Store(args);
		store.receiveRequests();

	}

}

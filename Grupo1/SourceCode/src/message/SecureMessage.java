package message;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.MessageDigest;
import java.util.Arrays;


public class SecureMessage implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -897335675102554325L;
	private byte[] messageHashed;
	private Message message;
	
	
	public SecureMessage(Message message) {
		this.message = message;
	    this.messageHashed = hashMessage(this.message);

	}
	
	public boolean verifyIntegrityAndTimeout() {
		return verifyTime() && verifyIntegrity();
	}
	
	
	public boolean verifyTime() {
		return this.message.verifyTime();
	}
	
	public boolean verifyIntegrity() {
		byte[] temp = hashMessage(this.message);
		return Arrays.equals(this.messageHashed, temp);
	}
	

	public byte[] hashMessage(Message message) {
		try {
			
			byte[] mensagemInBytes = objectToByteArray(message);
			MessageDigest md = MessageDigest.getInstance("SHA-256");
		    md.update(mensagemInBytes);
		    return md.digest(); 
		    
		} catch (Exception e) {
			// never enter in this catch
		}
		return null;
	}
	
	
	
	public static byte[] objectToByteArray(Message message) {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(message);
			oos.flush();
			return bos.toByteArray();
		} catch (Exception e) {
			// catch if error in writeObject
			System.err.println("Error writing object");
		}
		return null;
		
	}
	
	public Object byteArrayToObject(byte[] data) {
		try {
			ByteArrayInputStream bis = new ByteArrayInputStream(data);
			ObjectInputStream ois = new ObjectInputStream(bis);
			return ois.readObject();
		} catch (Exception e) {
			// catch if error in readObject
			System.err.println("Error reading object");
		}
		return null;
	}
	
	public Message getMessage() {
		if(verifyIntegrityAndTimeout()) {		
			return this.message;
		}
		return null;
	}

}

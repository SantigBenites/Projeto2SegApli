package bank;

import java.io.Serializable;
import java.security.Key;

public class Pair implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Key key;
	private Boolean value;
	
	public Pair(Key key, Boolean value) {
		this.key = key;
		this.value = value;
	}

	public Key getKey() {
		return key;
	}

	public Boolean getValue() {
		return value;
	}

}

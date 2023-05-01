package message;

import java.io.Serializable;

public class SmallMessage implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1480995577904686188L;
	private byte [] pkBytes;

	public byte [] getPkBytes() {
		return pkBytes;
	}

	public void setPkBytes(byte [] pkBytes) {
		this.pkBytes = pkBytes;
	}
}
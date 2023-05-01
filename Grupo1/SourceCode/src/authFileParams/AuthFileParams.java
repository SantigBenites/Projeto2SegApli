package authFileParams;

import java.io.Serializable;
import java.math.BigInteger;
import java.security.PublicKey;

public class AuthFileParams implements Serializable {

	private static final long serialVersionUID = 8681845349003463445L;
	private BigInteger p;
	private BigInteger g;
	private PublicKey serverPK;
	
	public AuthFileParams(BigInteger g, BigInteger p, PublicKey serverPK) {
		this.p = p;
		this.g = g;
		this.serverPK = serverPK;		
	}

	public PublicKey getServerPK() {
		return this.serverPK;
	}

	public BigInteger getP() {
		return this.p;
	}

	public BigInteger getG() {
		return this.g;
	}
	
	
}

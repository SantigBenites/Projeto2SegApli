package message;

import java.io.Serializable;
import java.security.Key;
import java.util.Calendar;

import javax.crypto.SealedObject;

import org.json.simple.JSONObject;

import mbec.Operation;
import user.CardDTO;

public class Message implements Serializable {
	
	/**
	 *
	 */
	private static final long serialVersionUID = -6761437517046961453L;

	/**
	 *
	 */


	private Operation operation;
	
	private double balance;
	
	private String account;
	
	private double amount;
	
	private int pin;
	
	private CardDTO card;
	
	private Calendar timestamp;

	private boolean status;
	
	private JSONObject response;
	
	private SealedObject responseSafe;
	
	private Key sessionKey;
	
	
	public Message() {
		this.timestamp = Calendar.getInstance();

	}
	
	public boolean verifyTime() {
		long a = this.timestamp.getTime().getTime();
		long b = Calendar.getInstance().getTime().getTime();
		
		long diff = Math.abs(b-a);
		
		return diff < 2500;
	}
	
	public void setOperation(Operation operation) {
		this.operation = operation;
	}
	
	public Operation getOperation() {
		return this.operation;
	}

	public double getBalance() {
		return balance;
		
	}

	public void setBalance(double balance) {
		this.balance = balance;
		
	}

	public String getAccount() {
		return account;
		
	}

	public void setAccount(String account) {
		this.account = account;
		
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public int getPin() {
		return pin;
	}

	public void setPin(int pin) {
		this.pin = pin;
	}

	public CardDTO getCardDTO() {
		return card;
		
	}

	public void setCardDTO(CardDTO card) {
		this.card = card;
		
	}

	public boolean getStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	public JSONObject getResponse() {
		return response;
	}

	public void setResponse(JSONObject response) {
		this.response = response;
	}

	public Key getSessionKey() {
		return sessionKey;
		
	}

	public void setSessionKey(Key sessionKey) {
		this.sessionKey = sessionKey;
		
	}

	public SealedObject getResponseSafe() {
		return responseSafe;
		
	}

	public void setResponseSafe(SealedObject responseSafe) {
		this.responseSafe = responseSafe;
		
	}
	

	

	
	
}

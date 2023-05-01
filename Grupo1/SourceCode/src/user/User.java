package user;

import org.json.simple.JSONObject;

public class User {
	private int account;
	private double balance;
	private int pin;
	private Card card;
	private int sequenceNumber;
	
	public User() {	
		sequenceNumber = 0;
	}
	
	public void deposit(double amount) {
		this.balance = this.balance + amount;
	}
	
	public void withdraw(double amount) {
		this.balance = this.balance - amount;
	}

	public int getAccount() {
		return account;
	}

	public void setAccount(int account) {
		this.account = account;
	}

	public double getBalance() {
		return balance;
	}

	public void setBalance(double balance) {
		this.balance = balance;
	}

	public int getPin() {
		return pin;
	}

	public void setPin(int pin) {
		this.pin = pin;
	}
	
	public JSONObject createCard(double amount) {
		if(this.card != null && this.card.isActive()) {
			return null;
		}else {
			this.card = new Card();
			card.setAmount(amount);
			card.setAccount(account);
			card.setCardName(String.valueOf(account) + "_" + String.valueOf(sequenceNumber) + ".card");
			sequenceNumber++;
			card.activate();
			return card.getJSONObject();
		}
	}
	
	public Card getCard() {
		return this.card;
	}
	
}

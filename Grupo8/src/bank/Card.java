package bank;

public class Card {

	private boolean active;
	private double amount;
	private String user;
	private int seq;
	
	public Card(double amount, String user, int seq) {
		this.user = user;
		this.amount = amount;
		this.active = true;
		this.seq = seq;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}
	
	public int getSeq() {
		return this.seq;
	}
	
	public String toString() {
		return "Card n: " + this.seq + ", from: " + this.user + ", amount: " + this.amount + ", active: " + this.active;
	}
}

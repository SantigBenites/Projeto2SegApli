package user;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.json.simple.JSONObject;

public class Card {
	private String cardName;
	private double amount;
	private int account;
	private boolean isActive;

	public Card() {

	}

	public String getCardName() {
		return cardName;
	}

	public void setCardName(String cardName) {
		this.cardName = cardName;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public boolean isActive() {
		return isActive;
	}

	public void activate() {
		this.isActive = true;
	}

	public void disable() {
		this.isActive = false;
	}

	public int getAccount() {
		return account;
	}

	public void setAccount(int account) {
		this.account = account;
	}
	
	public JSONObject getJSONObject() {
		JSONObject obj = new JSONObject();
		obj.put("vcc_amount", amount);
		obj.put("account", account);  
		obj.put("vcc_file", cardName);
		return obj;
	}

	public JSONObject writeToFile() {
		// Insert data into JSONObject
		JSONObject obj = new JSONObject();
		obj.put("vcc_amount", amount);
		obj.put("account", account);  
		obj.put("vcc_file", cardName);
		
		try {
			File file = new File(cardName);
			if(file.createNewFile()) {
				//System.out.println("[SUCCESS] User file: " + cardName + ".card" + " created with success.");
			}
			
			try(FileWriter myWriter = new FileWriter(file);){
				myWriter.write(obj.toString());
			    myWriter.close();
			}catch(IOException e) {
				e.printStackTrace();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return obj;
	}
}
	

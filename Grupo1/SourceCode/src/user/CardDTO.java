package user;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class CardDTO implements Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = 668073119210052634L;

	private String cardName;
	private double amount;
	private int account;

	public CardDTO() {

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

	public int getAccount() {
		return account;
	}

	public void setAccount(int account) {
		this.account = account;
	}
	
	public static CardDTO readFromFile(String fileName) {
		try {
			Path path = Paths.get(fileName);
	        String linha = Files.readAllLines(path, StandardCharsets.UTF_8).get(0);
	        JSONParser parser = new JSONParser();
	        JSONObject json = (JSONObject) parser.parse(linha);
	        CardDTO card = new CardDTO();
	        card.setAccount(((Long) json.get("account")).intValue());
	        card.setAmount((Double) json.get("vcc_amount"));
	        card.setCardName((String) json.get("vcc_file"));
	        return card;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
		
	}
	
	public static JSONObject writeCardDTOToFile(JSONObject obj) {
		try {
			File file = new File((String) obj.get("vcc_file"));
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
	

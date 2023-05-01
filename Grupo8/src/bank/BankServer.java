package bank;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.lang.*;
import java.security.KeyStore.Entry;

import org.json.simple.JSONObject;

public class BankServer {
	
	public int seqNumber = 1;
	private Map<Integer, String> users;
	private Map<Integer, Card> cards;
	
	private ArrayList<String> nonces = new ArrayList<String>();

	
	public BankServer() {
		this.cards = new HashMap<Integer, Card>();
		this.users = new HashMap<Integer, String>();
	}
	
	
	public String createUser(String[] stuffs) throws Exception {
		if (!users.containsKey(Integer.parseInt(stuffs[1])) && Double.parseDouble(stuffs[2]) >= 15) {
			users.put(Integer.parseInt(stuffs[1]), stuffs[2]);
			
			JSONObject obj = new JSONObject();
			obj.put("initial_balance", Double.parseDouble(stuffs[2]));
			obj.put("account", stuffs[1]);
			
			return obj.toJSONString();
		}else {
			System.out.println("Error 130");
			
			JSONObject obj = new JSONObject();
			return obj.toJSONString();
		}
	
	}
	
	public String deposit(String[] stuffs) throws Exception {
		if (users.containsKey(Integer.parseInt(stuffs[1])) && Double.parseDouble(stuffs[2]) > 0) {
			String current = users.get(Integer.parseInt(stuffs[1]));
			double current1 = Double.parseDouble(current);
			double novo = current1 + Double.parseDouble(stuffs[2]);
			users.replace(Integer.parseInt(stuffs[1]), String.valueOf(novo));
			
			JSONObject obj = new JSONObject();
			obj.put("deposit", Double.parseDouble(stuffs[2]));
			obj.put("account", stuffs[1]);
			
			return obj.toJSONString();
			
		}else {
			System.out.println("Error 130");
			
			JSONObject obj = new JSONObject();
			return obj.toJSONString();
		}

	}


	public String createVCC(String[] stuffs, int port, String host) throws Exception {
		if (!cards.containsKey(Integer.parseInt(stuffs[1])) && Double.parseDouble(stuffs[2]) > 0) {
			
			if(Double.parseDouble(this.users.get(Integer.parseInt(stuffs[1]))) < Double.parseDouble(stuffs[2])) {
				System.out.println("Error 130");
				
				JSONObject obj = new JSONObject();
				return obj.toJSONString();
			}
			Card c = new Card(Double.parseDouble(stuffs[2]), stuffs[1], seqNumber);
			
			cards.put(Integer.parseInt(stuffs[1]), c);
			
			if(cards.get(Integer.parseInt(stuffs[1])) == null) {
				System.out.println("Error 130");
				
				JSONObject obj = new JSONObject();
				return obj.toJSONString();
			}
			File newCard = new File(stuffs[1] + "_" + seqNumber + ".card");
			
			
		      try {
				if (newCard.createNewFile()) {
					FileOutputStream f = new FileOutputStream(newCard);
					BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(f));
					
					bw.write(stuffs[1]);
					bw.newLine();
					
					bw.write(host);
					bw.newLine();
					
					bw.write(String.valueOf(port));
					bw.newLine();
					
					bw.write(stuffs[2]);
					
					bw.flush();
					bw.close();
					
					newCard.renameTo(new File("../Client/" + stuffs[1] + "_" + seqNumber + ".card"));
					
					JSONObject obj = new JSONObject();
					obj.put("vcc_file", stuffs[1] + "_" + seqNumber + ".card");
					obj.put("vcc_amount", Double.parseDouble(stuffs[2]));
					obj.put("account", stuffs[1]);
					
					this.seqNumber++;

					
					return obj.toJSONString();
					
				  } else {
					  System.out.println("Error 130");
						
						JSONObject obj = new JSONObject();
						return obj.toJSONString();
				  }
			} catch (IOException e) {


				e.printStackTrace();
			}
		      
		      System.out.println("Error 130");
				
				JSONObject obj = new JSONObject();
				return obj.toJSONString();
		      
		}else {
			System.out.println("Error 130");
			
			JSONObject obj = new JSONObject();
			return obj.toJSONString();
		}

		
	}


	public String balance(String[] stuffs) throws Exception {
		if (users.containsKey(Integer.parseInt(stuffs[1]))) {
			
			double saldo = Double.parseDouble(users.get(Integer.parseInt(stuffs[1])));
			
			JSONObject obj = new JSONObject();
			
			obj.put("balance", saldo);
			obj.put("account", stuffs[1]);
			
			return obj.toJSONString();
		}else {
			System.out.println("Error 130");
			
			JSONObject obj = new JSONObject();
			return obj.toJSONString();
		}
			
	}


	public String checkVCC(String[] stuffs) throws NumberFormatException, ParseException {
		
		
		String[] parts = stuffs[2].split("_");

		String[] parts2 = parts[1].split("\\.");
		
		int seq2 = Integer.parseInt(parts2[0]);
		
		
		if(Double.parseDouble(users.get(Integer.parseInt(stuffs[1]))) < Double.parseDouble(stuffs[3])) {
			System.out.println("Error 130");
			
			JSONObject obj = new JSONObject();
			return obj.toJSONString();
		}
		
		if (stuffs[1].equals(parts[0])) {
			if (cards.containsKey(Integer.parseInt(stuffs[1])) && cards.get(Integer.parseInt(stuffs[1])).getSeq() == seq2 ) {
				double amount = cards.get(Integer.parseInt(stuffs[1])).getAmount();
				if (amount >= Double.parseDouble(stuffs[3])) {
					cards.remove(Integer.parseInt(stuffs[1]));
					
					double old = Double.parseDouble(users.get(Integer.parseInt(stuffs[1])));
					double rem = Double.parseDouble(stuffs[3]);
					users.replace(Integer.parseInt(stuffs[1]), String.valueOf(old - rem));
					
					
					JSONObject obj = new JSONObject();
					
					obj.put("vcc_file", stuffs[2]);
					obj.put("vcc_amount_used", stuffs[3]);
					
					
					return obj.toJSONString();
				}
				
			}
		}
		
		
		System.out.println("Error 130");
		
		JSONObject obj = new JSONObject();
		return obj.toJSONString();
	}
	
	
	public boolean checkNonce(String nonce) {
		return this.nonces.contains(nonce);
	}


	public void addNonce(String string) {
		this.nonces.add(string);	
		
	}
	
	
	
}

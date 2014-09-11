package com.badlogic.gdx.pay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PurchaseManagerConfig {

	// available store names (not even sure what all those are!?)
	public static final String STORE_NAME_GOOGLE = "Google";
	public static final String STORE_NAME_AMAZON = "Amazon";
	public static final String STORE_NAME_SAMSUNG = "Samsung";
	public static final String STORE_NAME_NOKIA = "Nokia";
	public static final String STORE_NAME_SLIDEME = "SlideME";
	public static final String STORE_NAME_APTOIDE = "Aptoide";
	public static final String STORE_NAME_APPLAND = "Appland";
	public static final String STORE_NAME_YANDEX = "Yandex";

	private List<Offer> offers;
	
	private Map<String, String> storeKeys;
	
	
	public PurchaseManagerConfig() {
		offers = new ArrayList<Offer>(16);
		storeKeys = new HashMap<String, String>(16);
	}
	
	public void addOffer(Offer offer) {
		offers.add(offer);
	}
	
	public Offer getOffer(String identifier) {
		// search matching offer and return it
		for (int i = 0; i < offers.size(); i++) {
			if (offers.get(i).getIdentifier().equals(identifier)) {
				return offers.get(i);
			}
		}
		
		// no matching offer found
		return null;
	}
	
	public Offer getOffer(int index) {
		return offers.get(index);
	}
	
	public int getOfferCount() {
		return offers.size();
	}
	
	public void addStoreKey(String storeName, String publicKey) {
		storeKeys.put(storeName, publicKey);
	}
	
	public String getStoreKey(String storeName) {
		return storeKeys.get(storeName);
	}
	
	public Map<String, String> getStoreKeys() {
		return storeKeys;
	}
}

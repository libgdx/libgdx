package com.badlogic.gdx.pay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PurchaseManagerConfig {

	// available store names (not even sure what all those are!?)
	public static final String STORE_NAME_ANDROID_GOOGLE = "GooglePlay";
	public static final String STORE_NAME_ANDROID_AMAZON = "Amazon";
	public static final String STORE_NAME_ANDROID_SAMSUNG = "Samsung";
	public static final String STORE_NAME_ANDROID_NOKIA = "Nokia";
	public static final String STORE_NAME_ANDROID_SLIDEME = "SlideME";
	public static final String STORE_NAME_ANDROID_APTOIDE = "Aptoide";
	public static final String STORE_NAME_ANDROID_APPLAND = "Appland";
	public static final String STORE_NAME_ANDROID_YANDEX = "Yandex";
	public static final String STORE_NAME_IOS_APPLE = "AppleiOS";
	public static final String STORE_NAME_DESKTOP_APPLE = "AppleMac";
	public static final String STORE_NAME_GWT_GOOGLEWALLET = "GwtGoogleWallet";
	
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

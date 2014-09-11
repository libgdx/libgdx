
package com.badlogic.gdx.pay;

import java.util.HashMap;
import java.util.Map;

public class Offer {

	private OfferType type;

	private String identifier;
	private Map<String, String> identifierForStores;

	public Offer (OfferType type, String identifier) {
		this.type = type;
		this.identifier = identifier;
		this.identifierForStores = new HashMap<String, String>(16);
	}

	public OfferType getType () {
		return type;
	}

	public void setType (OfferType type) {
		this.type = type;
	}

	public String getIdentifier () {
		return identifier;
	}

	public void setIdentifier (String identifier) {
		this.identifier = identifier;
	}

	public String getIdentifierForStore (String storeName) {
		return identifierForStores.get(storeName);
	}

	public void putIdentifierForStore (String storeName, String identifierForStore) {
		identifierForStores.put(storeName, identifierForStore);
	}
}

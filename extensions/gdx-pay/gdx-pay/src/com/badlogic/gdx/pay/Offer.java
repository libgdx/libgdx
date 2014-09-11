
package com.badlogic.gdx.pay;

import java.util.HashMap;
import java.util.Map;

public class Offer {

	private OfferType type;

	private String identifier;
	private Map<String, String> identifierForStores = new HashMap<String, String>(16);

	public OfferType getType () {
		return type;
	}

	public Offer setType (OfferType type) {
		this.type = type;
		
		// and return this for chaining
		return this;
	}

	public String getIdentifier () {
		return identifier;
	}

	public Offer setIdentifier (String identifier) {
		this.identifier = identifier;
		
		// and return this for chaining
		return this;
	}

	public String getIdentifierForStore (String storeName) {
		return identifierForStores.get(storeName);
	}

	public Offer putIdentifierForStore (String storeName, String identifierForStore) {
		identifierForStores.put(storeName, identifierForStore);
		
		// and return this for chaining
		return this;
	}
}

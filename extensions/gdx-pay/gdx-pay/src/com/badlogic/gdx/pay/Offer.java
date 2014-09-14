
package com.badlogic.gdx.pay;

import java.util.HashMap;
import java.util.Map;

/** A product offer that can be purchased. */
public class Offer {

	/** The offer type. */
	private OfferType type;

	/** The default identifier that is used to identify purchases and also serves as default for stores where no specific identifier
	 * has been set. */
	private String identifier;
	/** Store specific identifiers. For simplicity it's probably best to not set one but use the default identifier instead. */
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

	public Map<String, String> getIdentifierForStores () {
		return identifierForStores;
	}

	public String getIdentifierForStore (String storeName) {
		String identifier = identifierForStores.get(storeName);
		if (identifier != null) {
			return identifier;
		} else {
			// we use our default
			return this.identifier;
		}
	}

	public Offer putIdentifierForStore (String storeName, String identifierForStore) {
		identifierForStores.put(storeName, identifierForStore);

		// and return this for chaining
		return this;
	}
}

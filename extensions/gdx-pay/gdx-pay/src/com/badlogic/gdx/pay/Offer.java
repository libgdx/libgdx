/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

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

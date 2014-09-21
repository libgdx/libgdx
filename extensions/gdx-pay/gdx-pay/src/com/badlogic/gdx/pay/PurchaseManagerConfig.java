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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** This configuration is set in your "core"-project and will be passed on to the active InApp store automatically.
 * @author noblemaster */
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
	public static final String STORE_NAME_ANDROID_OUYA = "OUYA";
	public static final String STORE_NAME_IOS_APPLE = "AppleiOS";
	public static final String STORE_NAME_DESKTOP_APPLE = "AppleMac";
	public static final String STORE_NAME_GWT_GOOGLEWALLET = "GwtGoogleWallet";

	/** Our list of purchaseable products. */
	private List<Offer> offers;

	/** The store-specific parameters where needed. */
	private Map<String, Object> storeParams;

	public PurchaseManagerConfig () {
		offers = new ArrayList<Offer>(16);
		storeParams = new HashMap<String, Object>(16);
	}

	public void addOffer (Offer offer) {
		offers.add(offer);
	}

	public Offer getOffer (String identifier) {
		// search matching offer and return it
		for (int i = 0; i < offers.size(); i++) {
			if (offers.get(i).getIdentifier().equals(identifier)) {
				return offers.get(i);
			}
		}

		// no matching offer found
		return null;
	}

	public Offer getOffer (int index) {
		return offers.get(index);
	}

	public int getOfferCount () {
		return offers.size();
	}

	/** Adds a parameter for a store.
	 * 
	 * @param storeName The name of the store.
	 * @param param The store parameters to use. This could be a string or byte-array etc. depending on what that store needs to
	 *           initialize. */
	public void addStoreParam (String storeName, Object param) {
		storeParams.put(storeName, param);
	}

	/** Returns parameters for a store.
	 * 
	 * @param storeName The name of the store.
	 * @return The store parameters or null if there where none. This could be a string or byte-array etc. depending on what that
	 *         store needs to initialize. */
	public Object getStoreParam (String storeName) {
		return storeParams.get(storeName);
	}
}

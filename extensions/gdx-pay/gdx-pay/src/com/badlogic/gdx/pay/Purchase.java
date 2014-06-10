/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.pay;

import java.util.Date;

/** An item purchased via libGDX In-App payment system (IAP).
 * <p>
 * The item identifier/SKU matches the item id in the IAP service. Please note that Valve/Steam expects an integer value for the
 * item identifier (which is somewhat restrictive) while other services such as Google Play, Amazon or iOS expect a textual
 * identifier. So, to ensure cross-platform compatibility simply prefix "item_" before the number for stores that expect a textual
 * identifier. For an item with identifier 1401 register the item identifier in the corresponding store page as follows:
 * <ul>
 * <li>Steam (just a number!): 1401
 * <li>Google Play/Amazon/iOS/Mac OS X: "item_1401"
 * </ul>
 * 
 * @author noblemaster */
public final class Purchase {

	/** Item identifier/SKU number. */
	public String identifier;

	/** Is set to true if the purchase is valid (non-expired/non-refunded) or false if the purchase has been refunded or has
	 * expired, e.g. subscription. */
	public boolean valid;

	/** A unique transaction ID. */
	public String transactionId;
	/** The original transaction date which never changes. */
	public Date transactionDate;
	/** A transaction-receipt for optional postback validation on a server if available. null if not available. */
	public String transactionReceipt;

	/** Creates a new purchase.
	 * 
	 * @param identifier The item identifier/SKU number. */
	public Purchase (String identifier, boolean valid, String transactionId, Date transactionDate, String transactionReceipt) {
		this.identifier = identifier;

		// true for a valid purchase.
		this.valid = valid;

		// the transaction information
		this.transactionId = transactionId;
		this.transactionDate = transactionDate;
		this.transactionReceipt = transactionReceipt;
	}

	/** The item identifier/SKU that matches the item id in the IAP service. Please note that Valve/Steam expects an integer value
	 * for the item identifier (which is somewhat restrictive). */
	public String getIdentifier () {
		return identifier;
	}

	/** Is set to true if the purchase is valid (non-expired/non-refunded) or false if the purchase has been refunded or has
	 * expired, e.g. subscription.
	 * <p>
	 * Note: consumable items (such as depleting health packs) have to be managed by the application and are not handled by the
	 * libGDX payment API. This is the standard for payment APIs such as Google Play or iTunes. We might add a higher-level
	 * Inventory class in the future to simplify consumable items, the raw libGDX payment API however is not responsible for
	 * keeping track of consumable items. */
	public boolean isValid () {
		return valid;
	}

	/** The original transaction identifier which is unique for each purchase (doesn't change). It represents an unique ID for the
	 * purchase on the corresponding IAP system. */
	public String getTransactionId () {
		return transactionId;
	}

	/** The original transaction date, i.e. when the product was first purchased (doesn't change). */
	public Date getTransactionDate () {
		return transactionDate;
	}

	/** A transaction receipt that can be used for postback validation if you have setup a server to make sure the purchased item is
	 * genuine. Returns null if not available. */
	public String getTransactionReceipt () {
		return transactionReceipt;
	}
}

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

/** An transaction for an item purchased via libGDX In-App payment system (IAP).
 * 
 * @author noblemaster */
public final class Transaction {

	/** Item identifier/SKU number. */
	private String identifier;

	/** The store name. */
	private String storeName;
	/** A unique order ID. */
	private String orderId;

	/** The original purchase time in milliseconds since the epoch (Jan 1, 1970). */
	private Date purchaseTime;
	/** The title/info for the purchase (or null for unknown). E.g. "Purchased: 100 Coins". */
	private String purchaseText;
	/** How much was originally charged in the lowest denomination (or -1 for unknown). E.g. if the cost was USD 4.99, then this
	 * field contains 499. */
	private int purchaseCost;
	/** The ISO 4217 currency code for price (or null for unknown). For example, if price is specified in British pounds sterling
	 * then this field is "GBP". */
	private String purchaseCostCurrency;

	/** The original refund/cancellation time or null for non-refunded. Might not be accurate if it cannot be determined. */
	private Date reversalTime;
	/** The title/info for the refund (or null for unknown). E.g. "Refunded" or "Cancelled". */
	private String reversalText;

	/** The original data string from the purchase (or null for unknown). */
	private String transactionData;
	/** A signature for the purchase data string for validation of the data (or null for unknown). */
	private String transactionDataSignature;

	/** The item identifier/SKU that matches our item id in the IAP service. */
	public String getIdentifier () {
		return identifier;
	}

	public void setIdentifier (String identifier) {
		this.identifier = identifier;
	}

	/** Returns one of the store names as defined in PurchaseManagerConfig. */
	public String getStoreName () {
		return storeName;
	}

	public void setStoreName (String storeName) {
		this.storeName = storeName;
	}

	/** The original transaction identifier which is unique for each purchase (doesn't change). It represents an unique ID for the
	 * purchase on the corresponding store. */
	public String getOrderId () {
		return orderId;
	}

	public void setOrderId (String orderId) {
		this.orderId = orderId;
	}

	/** Returns true if the order is considered valid, i.e. in purchased state (non-refunded/cancelled). */
	public boolean isPurchased () {
		return reversalTime == null;
	}

	/** The original purchase time in milliseconds since the epoch (Jan 1, 1970). */
	public Date getPurchaseTime () {
		return purchaseTime;
	}

	public void setPurchaseTime (Date purchaseTime) {
		this.purchaseTime = purchaseTime;
	}

	/** The title/info for the purchase (or null for unknown). E.g. "Purchased: 100 Coins". */
	public String getPurchaseText () {
		return purchaseText;
	}

	public void setPurchaseText (String purchaseText) {
		this.purchaseText = purchaseText;
	}

	/** How much was originally charged in the lowest denomination (or null for unknown). E.g. if the cost was USD 4.99, then this
	 * field contains 499. */
	public int getPurchaseCost () {
		return purchaseCost;
	}

	public void setPurchaseCost (int purchaseCost) {
		this.purchaseCost = purchaseCost;
	}

	/** The ISO 4217 currency code for price (or null for unknown). For example, if price is specified in British pounds sterling
	 * then this field is "GBP". */
	public String getPurchaseCostCurrency () {
		return purchaseCostCurrency;
	}

	public void setPurchaseCostCurrency (String purchaseCostCurrency) {
		this.purchaseCostCurrency = purchaseCostCurrency;
	}

	/** The original refund/cancellation time in milliseconds since the epoch (Jan 1, 1970) or null for non-refunded. */
	public Date getReversalTime () {
		return reversalTime;
	}

	public void setReversalTime (Date reversalTime) {
		this.reversalTime = reversalTime;
	}

	/** The title/info for the refund (or null for unknown). E.g. "Refunded" or "Cancelled". */
	public String getReversalText () {
		return reversalText;
	}

	public void setReversalText (String reversalText) {
		this.reversalText = reversalText;
	}

	public String getTransactionData () {
		return transactionData;
	}

	public void setTransactionData (String transactionData) {
		this.transactionData = transactionData;
	}

	/** The original data string from the purchase (or null for unknown). */
	public String getTransactionDataSignature () {
		return transactionDataSignature;
	}

	/** A signature for the purchase data string for validation of the data (or null for unknown). */
	public void setTransactionDataSignature (String transactionDataSignature) {
		this.transactionDataSignature = transactionDataSignature;
	}
}

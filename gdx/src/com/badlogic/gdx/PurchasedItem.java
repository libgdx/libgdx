package com.badlogic.gdx;

import java.util.Date;

/**
 * An item purchased via IAP.
 * 
 * @author noblemaster
 */
public class PurchasedItem {

	/** The item identifier that matches the item id in the IAP service. */
	public String identifier;
  
	/** 
	 * Is set to true if the purchase is valid (non-expired/non-refunded) or false if the purchase
	 * has been refunded or has expired, e.g. subscription (?). 
	 */
	public boolean valid;

	/** 
	 * The original transaction identifier which is unique for each purchase (doesn't change). 
	 * It represents an unique ID for the purchase on the corresponding IAP system.
	 */
	public String transactionId;
	
	/** 
	 * The original transaction date, i.e. when the product was first purchased (doesn't change). 
	 */
	public Date transactionDate;
	
	/** 
	 *  A transaction receipt that can be used for postback validation if you have setup a server
	 *  to make sure the purchased item is genuine.  
	 */
	public String transactionReceipt;
}

package com.badlogic.gdx;

/**
 * The IAP purchase validator (server). Please note the validator is only needed if you 
 * have setup a server and would like to verify items that have been purchased. The 
 * validator will perform a post-back validation to the corresponding IAP service to
 * verify if a purchase is genuine.
 * <p>
 * Please note you will have one validator for each IAP service. I.e. 1 validator for 
 * Google Play, 1 validator for iOS etc.
 * <p>
 * 1. How to setup the validator
 * <pre>
 *   MyServer myServer = ... // store the validator somewhere on your server
 *   myServer.setPurchaseValidator(new SomePurchaseValidator());
 * </pre>
 * <p>
 * 2. How to validate an item
 * <pre>
 *   PurchaseValidator validator = myServer.getPurchaseValidator();
 *   PurchasedItem item = ... // the item to validate; received over the web
 *   validator.validate(item, new PurchaseValidator.Listener() {
 *      public void handleValidation(PurchasedItem item, boolean valid) {
 *        if (valid) {
 *           ... // process the item (e.g. add to your database)
 *        }
 *      }
 *   });
 * </pre>
 * 
 * @author noblemaster
 */
public interface PurchaseValidator {

	public static interface Listener {
	
		/**
		 * Will be called once an item has been validated.
		 * 
		 * @param item  The item that was validated.
		 * @param valid  Is set to true if the item could be validated to be genuine.
		 */
		public void handleValidation(PurchasedItem item, boolean valid);
	}
	
	/**
	 * Will perform a post-back validation for an item.
	 * 
	 * @param item  The item to validate.
	 * @param listener  The result listener.
	 */
	public void validate(PurchasedItem item, Listener listener);
}

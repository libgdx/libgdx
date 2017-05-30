package com.badlogic.gdx;

/**
 * An IAP purchase manager (client). Items for purchase are referenced by an item identifier string. Make sure
 * to register the same identifier on all the IAP services desired for easy porting. The same identifier 
 * should be registered in the IAP item setup screen of each IAP service (Google Play IAP, Amazon IAP, iOS IAP,
 * Apple Mac Store IAP etc).
 * <p>
 * Please note due to limitations by the various IAP services you need to manage identifiers on your own.
 * There is no way for example to retrieve the IAP item list on Google Play. There is also no way to 
 * store icons, downloadable content etc. in Google Play as well as some other IAP services. Icons and
 * downloadable content have to be either integrated into your application or served by a separate server
 * that you setup. Your application is responsible to display the items for purchase.
 * <p>
 * 1. How to Setup IAP in your Application
 * <pre>
 *   // platform-dependent code (setup the purchase manager in your code)
 *   MyGame myGame = ... // for example your game class (or store the manager somewhere else)
 *   myGame.setPurchaseManager(new SomePurchaseManager(parameters));
 *   
 *   // platform-independent code (register an listener - REQUIRED!)
 *   PurchaseManager manager = myGame.getPurchaseManager();
 *   manager.register(new PurchaseManager.Listener() {
 *     public void handleRequestPurchase(PurchasedItem item) {
 *       // handle successful purchases...
 *     }
 *     public void handleRequestPurchaseFailure(String identifier, Failure e) {
 *       // handle failed purchases due to e.g. non-existing identifier/network problems; display dialog!
 *     }
 *     public void handleRestore(PurchasedItem[] items) {
 *       // handle restores...
 *     }
 *     public void handleRestoreFailure(Failure e) {
 *       // handle restore failures due to e.g. network problems; display dialog!
 *     }
 *   });
 * </pre>
 * <p>
 * 2. How to Make a Purchase
 * <pre>
 *   // platform-independent code (purchase)
 *   PurchaseManager manager = myGame.getPurchaseManager();
 *   String identifier = ...   // your product identifier that is registered in the IAP service
 *   manager.purchase(identifier);
 * </pre>
 * Will call the listener once completed.
 * <p>
 * 3. How to Restore Purchases (e.g. new device/re-install - REQUIRED: display [Restore] button)
 * <pre>
 *   // platform-independent code (restore purchases)
 *   PurchaseManager manager = myGame.getPurchaseManager();
 *   manager.restore();
 * </pre>
 * Will call the listener once completed.
 * 
 * @author noblemaster
 */
public interface PurchaseManager {

  public static interface Listener {
    
    /**
     * The item that was successfully purchased.
     * 
     * @param item  The purchased item.
     */
    public void handleRequestPurchase(PurchasedItem item);
    
    /**
     * Called when a purchase failed.
     * 
     * @param identifier  The item that was tried to be purchased.
     * @param e  The error, e.g. network outage, or invalid identifier.
     */
    public void handleRequestPurchaseFailure(String identifier, Throwable e);

    /**
     * All the items that were purchased.
     * 
     * @param items  The purchased items.
     */
    public void handleRestore(PurchasedItem[] items);
    
    /**
     * Called when a restore failed.
     * 
     * @param e  The error, e.g. network outage.
     */
    public void handleRestoreFailure(Throwable e);
  }
  
  /**
   * Returns true if the IAP service is ready and enabled to process purchases.
   * 
   * @return  True for available; false if disabled/not connected to network etc.
   */
  public boolean available();
              
  /**
   * Register a transaction listener. 
   * 
   * @param listener  The listener.
   */
  public void register(Listener listener);
  
  /**
   * Request to purchase an item. 
   * <p>
   * Either handleRequestPurchase(...) or handleRequestPurchaseFailure(...) is called after completion.
   * 
   * @param identifier  The item to purchase.
   */
  public void requestPurchase(String identifier);
  
  /**
   * Request to restore the items purchased. Please note that iOS requires a [Restore] button that is 
   * visible to users. This is also helpful if something in payment processing has failed due to a network 
   * interruption, crash, re-install, new device install etc.
   * <p>
   * Either handleRestore(...) or handleRestoreFailure(...) is called after completion.
   */
  public void restore();
}

package com.badlogic.gdx;

/**
 * An item purchased via IAP.
 * 
 * @author noblemaster
 */
public interface PurchasedItem {

  /** The item status. */
  public static enum Information {
  
    /** Valid: purchased and non-expired/non-refunded etc. - aka still valid. */
    VALID, 
    /** Invalid: e.g. refunded item, expired subscription, etc. */
    INVALID;
  }
  
  /**
   * The item identifier.
   * 
   * @return  The item identifier.
   */
  public String getIdentifier();
      
  /**
   * The original transaction identifier.
   * 
   * @return  The transaction identifier which is unique for each purchase.
   */
  public String getTransation();
  
  /**
   * Returns the item status.
   * 
   * @return  The item status if valid (purchased/non-expired etc.) or invalid (expired/refunded etc.).
   */
  public Information getInformation();
}

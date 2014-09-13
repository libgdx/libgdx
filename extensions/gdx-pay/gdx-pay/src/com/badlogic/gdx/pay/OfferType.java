
package com.badlogic.gdx.pay;

public enum OfferType {

	/** A consumable can be purchased multiple times and diminishes once used (e.g. virtual money, coins). */
	CONSUMABLE,
	/** An entitlement can only be purchased once (e.g. one time upgrade). Basically a non-consumable. */
	ENTITLEMENT;
}

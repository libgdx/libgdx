
package com.badlogic.gdx.pay;

import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;

/** Our purchase system to make InApp payments.
 * 
 * @author noblemaster */
public final class PurchaseSystem {

	/** The actual purchase manager or null if none was available. */
	private static PurchaseManager manager = null;

	private PurchaseSystem () {
		// private to prevent instantiation
	}

	/** Registers a new purchase manager. */
	public static void setManager (PurchaseManager manager) {
		PurchaseSystem.manager = manager;
	}

	/** Returns true if there is a purchase manager available. */
	public static boolean hasManager () {
		return manager != null;
	}

	/** Returns the registered manager or null for none. */
	public static PurchaseManager getManager () {
		return manager;
	}

	/** Returns the store name or null for none. */
	public static String storeName() {
		if (manager != null) {
			return manager.storeName();
		}
		else {
			return null;
		}
	}
	
	/** Installs a purchase observer. */
	public static void install (PurchaseObserver observer, PurchaseManagerConfig config) {
		if (manager != null) {
			manager.install(observer, config);
		} else {
			observer.handleInstallError(new GdxRuntimeException("No purchase manager was available."));
		}
	}

	/** Returns true if the purchase system is installed and ready to go. */
	public static boolean installed() {
		if (manager != null) {
			return manager.installed();
		} else {
			return false;
		}
	}
	
	/** Disposes the purchase manager if there was one. */
	public static void dispose () {
		if (manager != null) {
			if (manager instanceof Disposable) {
				((Disposable)manager).dispose();
			}
			manager = null;
		}
	}

	/** Executes a purchase. */
	public static void purchase (PurchaseListener listener, String identifier) {
		if (manager != null) {
			manager.purchase(listener, identifier);
		} else {
			throw new GdxRuntimeException("No purchase manager was found.");
		}
	}

	/** Asks to restore previous purchases. Results are returned to the observer. */
	public static void purchaseRestore () {
		if (manager != null) {
			manager.purchaseRestore();
		} else {
			throw new GdxRuntimeException("No purchase manager was found.");
		}
	}
}

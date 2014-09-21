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
	public static String storeName () {
		if (manager != null) {
			return manager.storeName();
		} else {
			return null;
		}
	}

	/** Installs a purchase observer. */
	public static void install (PurchaseObserver observer, PurchaseManagerConfig config) {
		if (manager != null) {
			manager.install(observer, config);
		} else {
			observer.handleInstallError(new RuntimeException("No purchase manager was available."));
		}
	}

	/** Returns true if the purchase system is installed and ready to go. */
	public static boolean installed () {
		if (manager != null) {
			return manager.installed();
		} else {
			return false;
		}
	}

	/** Disposes the purchase manager if there was one. */
	public static void dispose () {
		if (manager != null) {
			manager.dispose();
			manager = null;
		}
	}

	/** Executes a purchase. */
	public static void purchase (PurchaseListener listener, String identifier) {
		if (manager != null) {
			manager.purchase(listener, identifier);
		} else {
			throw new RuntimeException("No purchase manager was found.");
		}
	}

	/** Asks to restore previous purchases. Results are returned to the observer. */
	public static void purchaseRestore () {
		if (manager != null) {
			manager.purchaseRestore();
		} else {
			throw new RuntimeException("No purchase manager was found.");
		}
	}
}

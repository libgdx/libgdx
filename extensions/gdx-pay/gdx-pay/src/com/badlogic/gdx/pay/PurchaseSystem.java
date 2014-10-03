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

import java.lang.reflect.Method;

/** Our purchase system to make InApp payments.
 * 
 * @author noblemaster */
public final class PurchaseSystem {

	private static final String TAG = "IAP";

	/** The actual purchase manager or null if none was available. */
	private static PurchaseManager manager = null;

	/** We try to locate a suitable store via Java reflection. */
	static {
		// obtain the Gdx class
		try {
			Class<?> gdxClazz = Class.forName("com.badlogic.gdx.Gdx");
			Class<?> gdxLifecycleListenerClazz = Class.forName("com.badlogic.gdx.LifecycleListener");
			Class<?> gdxAndroidEventListenerClazz = Class.forName("com.badlogic.gdx.backends.android.AndroidEventListener");
			Object gdxAppObject = gdxClazz.getField("app").get(null);
			Method gdxAppLogMethod = gdxAppObject.getClass().getMethod("log", String.class, String.class);
			Method gdxAppLogMethodT = gdxAppObject.getClass().getMethod("log", String.class, String.class, Throwable.class);
			Method gdxAppAddLifecycleListenerMethod = gdxAppObject.getClass().getMethod("addLifecycleListener", gdxLifecycleListenerClazz);
			Method gdxAppAddAndroidEventListenerMethod = gdxAppObject.getClass().getMethod("addAndroidEventListener", gdxAndroidEventListenerClazz);
			
			// check if we are on Android
			boolean android;
			try {
				// this will crash if we are not on android!
				Class<?> androidAppClazz = Class.forName("com.badlogic.gdx.backends.android.AndroidApplication");
				android = true;
			} catch (Exception e) {
				// we appear not to be on Android
				android = false;
			}
			if (android) {
				try {
					// look for gdx-pay-android and if it exists, instantiate it (gdx-pay jars need to be in place)
					Class<?> iapClazz = Class.forName("com.badlogic.gdx.pay.android.IAP");
					Class<?> activityClazz = Class.forName("android.app.Activity");
					Class<?> intentClazz = Class.forName("android.content.Intent");
					int requestCode = 1032; // requestCode for onActivityResult for purchases (could go into PurchaseManagerConfig)
					Object iap = iapClazz.getConstructor(activityClazz, int.class).newInstance(gdxAppObject, requestCode);

					// add a listener for Lifecycle events
					gdxAppAddLifecycleListenerMethod.invoke(gdxAppObject, iap);
					
					// add a listener for Android Events events
					gdxAppAddAndroidEventListenerMethod.invoke(gdxAppObject, iap);

					// notify of success
					gdxAppLogMethod.invoke(gdxAppObject, TAG, "IAP: gdx-pay successfully instantiated.");
				} catch (Exception e) {
					// some jar files appear to be missing
					gdxAppLogMethodT.invoke(gdxAppObject, TAG,
						"IAP: Error creating IAP for Android (are the gdx-pay**.jar files installed?).", e);
				}
			}
		} catch (Exception e) {
			// we appear not to be on libGDX!
		}
	}

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
	public static void purchase (String identifier) {
		if (manager != null) {
			manager.purchase(identifier);
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

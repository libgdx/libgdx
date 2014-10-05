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

/** An IAP purchase manager (client). Items for purchase are referenced by an item identifier integer value. Make sure to register
 * the same identifier on all the IAP services desired for easy porting. The same identifier should be registered in the IAP item
 * setup screen of each IAP service (Google Play IAP, Amazon IAP, iOS IAP, Apple Mac Store IAP, Steam etc). For stores that
 * support textual item identifiers (most of them except Steam) prefix a "item_" before the item identifier number.
 * <p>
 * Please note due to limitations by the various IAP services you need to manage identifiers on your own. It is not possible for
 * example to retrieve the IAP item list for all the stores. It is also not possible to store icons, downloadable content etc. in
 * some IAP services. Icons and downloadable content have to be either integrated into your application or served by a separate
 * server that you setup. Your application is responsible to display the items for purchase.
 * <p>
 * 1. Copy the needed JAR files into your Application to make this all work:
 * <ul>
 * <li>Required: gdx-pay.jar (into "core"/libs)
 * <li>Required for Android: gdx-pay-android.jar (into "android"/libs)
 * <li>Optional for Android: gdx-pay-android-openiab.jar (for Google Play, Amazon etc.)
 * <li>Optional for Android: gdx-pay-android-ouya.jar (for OUYA)
 * <li>Required for iOS: gdx-pay-iosrobovm-apple (for iOS/Apple Store)
 * <li>...
 * </ul>
 * <p>
 * 2. For Android, the correct purchase manager is located automatically if the jar files are available (via Java reflection). No
 * code changes are needed within your Android project. In your "core" project is where it all happens: Have a look at
 * PayTest.java to see how it works.
 * <p>
 * 
 * @author noblemaster */
public interface PurchaseManager {

	/** Returns the store name. */
	public String storeName ();

	/** Registers a purchase observer which handles installs of apps on a new device or aborted purchases from a previous session
	 * that were not yet handled by the application. The observer is called for all unfinished transactions. The observer is also
	 * called for refunds of previous purchased items.
	 * <p>
	 * Registering an observer is required. If no observer is registered the call to purchase an item will fail with a runtime
	 * exception to teach you lesson to always remember to set a purchase observer. The purchase observer is needed to make sure
	 * all purchases have been handled and served to the customer.
	 * 
	 * @param observer The observer which is called whenever purchases have to be handled by the application as well as when the
	 *           store has been installed. */
	public void install (PurchaseObserver observer, PurchaseManagerConfig config);

	/** Returns true if the purchase manager is installed (non-disposed) and ready to go. */
	public boolean installed ();

	/** Disposes the purchase manager. */
	public void dispose ();

	/** Requests to purchase an item. The listener will always be called once the purchase has either completed or failed.
	 * <p>
	 * Note: a GDX runtime exception if throw if you have not registered a purchase observer.
	 * @param identifier The item to purchase. */
	public void purchase (String identifier);

	/** Restores existing purchases. */
	public void purchaseRestore ();

	@Override
	public String toString ();
}

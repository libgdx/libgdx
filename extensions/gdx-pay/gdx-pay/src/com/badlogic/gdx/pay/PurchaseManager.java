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
 * 1. Copy the Needed JAR Files into your Application
 * <ul>
 * <li>Required: gdx-pay.jar (into your libs/ folder in "core")
 * <li>Optional: gdx-pay-google-android.jar (into your libs/ folder in your Android project if you are deploying to Google Play)
 * <li>Optional: gdx-pay-amazon-android.jar (into your libs/ folder in your Android project if you are deploying to Amazon)
 * <li>Optional: gdx-pay-steam-desktop.jar (into your libs/ folder in your Desktop project if you are deploying to Valve/Steam)
 * <li>Optional: etc.
 * </ul>
 * <p>
 * 2. How to Setup IAP in your Application
 * <pre>
 * // platform-dependent code (setup the purchase manager in your code, e.g. Google Play/Android)
 * MyGame myGame = ... // for example your game class (or store the manager somewhere else)
 * myGame.setPurchaseManager(new PurchaseManagerGoogleAndroid(...));
 * 
 * // platform-independent code (register an observer e.g. in libGDX's ApplicationListener - REQUIRED!)
 * PurchaseManager manager = myGame.getPurchaseManager();
 * manager.register(new PurchaseObserver() {
 *   public void handleRestore (Purchase purchase) {
 *     if (purchase.valid) {
 *       // handle an un-handled but successful purchase 
 *       ...
 *     }
 *     else {
 *       // handle a refund or purchased that was not completed by a user after all
 *       ...
 *     }
 *   }
 *   public void handleFailure (Throwable e) {
 *     // handle purchase manager problems: display error to user
 *   }
 * });
 * </pre>
 * <p>
 * 3. How to Make a Purchase
 * <pre>
 * // platform-independent code (purchase)
 * PurchaseManager manager = myGame.getPurchaseManager();
 * int identifier = ... // your product identifier that is registered in the IAP service
 * manager.purchase(new PurchaseListener() {
 *   public void handlePurchase (Purchase purchase) {
 *     if (purchase.valid) {
 *       // handle a successful purchase, i.e. deliver the purchase to the user
 *       ...
 *     }
 *     else {
 *       // optional handling: this part can be ignored: the user simply didn't go through with the purchase
 *     }
 *   }
 *   public void handleAborted (Throwable e) {
 *     // output the error to the user
 *     ...
 *   }
 * }, identifier);
 * </pre>
 * 
 * @author noblemaster */
public interface PurchaseManager {

	/** Registers a purchase observer which handles installs of apps on a new device or aborted purchases from a previous session
	 * that were not yet handled by the application. The observer is called for all unfinished transactions. The observer is also
	 * called for refunds of previous purchased items.
	 * <p>
	 * Registering an observer is required. If no observer is registered the call to purchase an item will fail with a runtime
	 * exception to teach you lesson to always remember to set a purchase observer. The purchase observer is needed to make sure
	 * all purchases have been handled and served to the customer.
	 * 
	 * @param observer The observer which is called whenever purchases have to be handled by the application. */
	public void register (PurchaseObserver observer); 

	/** Requests to purchase an item. The listener will always be called once the purchase has either completed or failed.
	 * <p>
	 * Note: a GDX runtime exception if throw if you have not registered a purchase observer.
	 * 
	 * @param listener The listener that will be called with the purchase information. If the listener finishes without throwing an
	 *           exception the purchases is considered handled by the application. If the listener is (a) never called or (b) the
	 *           listener itself throws an exception, the purchase observer will again later report the purchase information again.
	 * @param identifier The item to purchase. */
	public void purchase (PurchaseListener listener, int identifier);
}

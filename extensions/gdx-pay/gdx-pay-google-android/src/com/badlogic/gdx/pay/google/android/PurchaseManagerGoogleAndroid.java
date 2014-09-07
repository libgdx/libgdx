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

package com.badlogic.gdx.pay.google.android;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.android.vending.billing.IInAppBillingService;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.pay.Purchase;
import com.badlogic.gdx.pay.PurchaseListener;
import com.badlogic.gdx.pay.PurchaseManager;
import com.badlogic.gdx.utils.Disposable;

/** The purchase manager implementation for Google Play on Android.
 * <p>
 * To use InApp on Google Play do the following:
 * <ul>
 * <li>AndroidManifest.xml: add &lt;uses-permission android:name="com.android.vending.BILLING" /&gt;
 * <li>AndroidApplication/member variables: add "PurchaseManager manager;"
 * <li>AndroidApplication.onCreate(...): add "manager = new PurchaseManagerGoogleAndroid(this);"
 * <li>AndroidApplication.onDispose(): add "manager.dispose(); manager = null;"
 * <li>AndroidApplication.onActivityResult(...): add "manager.onActivityResult(requestCode, resultCode, data);"
 * <ul>
 * 
 * @author noblemaster */
public class PurchaseManagerGoogleAndroid implements PurchaseManager, Disposable {

	public static final int GDX_PAY_GOOGLE_ANDROID_REQUEST_CODE = 0x0000FF01;

	/** Google IAP version to use. */
	private static final int API_VERSION = 3;

	/** The activity. */
	private Activity activity;

	/** The service. */
	IInAppBillingService mService;
	/** The service connection. */
	ServiceConnection mServiceConn = new ServiceConnection() {
		@Override
		public void onServiceDisconnected (ComponentName name) {
			// NOTE: do not call Log.d(...), otherwise the service disconnect silenty crashes... :-/
			mService = null;
		}

		@Override
		public void onServiceConnected (ComponentName name, IBinder service) {
			Log.d(PurchaseManagerGoogleAndroid.this.toString(), "Service Connected.");
			mService = IInAppBillingService.Stub.asInterface(service);
		}
	};

	private Map<String, PurchaseListener> listeners = new HashMap<String, PurchaseListener>(4);
	private int listenerIndex = 1;

	public PurchaseManagerGoogleAndroid (Activity activity) {
		this.activity = activity;

		// bind the service
		Log.d(toString(), "Service Connecting...");
		activity.bindService(new Intent("com.android.vending.billing.InAppBillingService.BIND"), mServiceConn,
			Context.BIND_AUTO_CREATE);
	}

	@Override
	public void dispose () {
		// we destroy/unbind the service (required!)
		if (mService != null) {
			Log.d(toString(), "Service Shutdown!");
			activity.unbindService(mServiceConn);

			// reset to null
			activity = null;
			mService = null;
		}
	}

	@Override
	public void register (PurchaseListener listener) {
		// TODO: implement!
	}

	@Override
	public void purchase (PurchaseListener listener, String identifier) {
		// are we in test-mode: if yes, use custom android identifiers!
		if (identifier.equals(Purchase.IDENTIFIER_TEST_SUCCESS)) {
			identifier = "android.test.purchased";
		} else if (identifier.equals(Purchase.IDENTIFIER_TEST_FAILURE_CANCELED)) {
			identifier = "android.test.canceled";
		} else if (identifier.equals(Purchase.IDENTIFIER_TEST_FAILURE_REFUNDED)) {
			identifier = "android.test.refunded";
		} else if (identifier.equals(Purchase.IDENTIFIER_TEST_FAILURE_INVALIDIDENTIFIER)) {
			identifier = "android.test.item_unavailable";
		}

		// store the listener
		String developerPayload = "gdx-pay-google-android-listener-" + listenerIndex++;
		listeners.put(developerPayload, listener);

		// create the bundle (with purchase info)
		Bundle buyIntentBundle;
		try {
			buyIntentBundle = mService.getBuyIntent(API_VERSION, activity.getPackageName(), identifier, "inapp", developerPayload);
		} catch (RemoteException e) {
			// forward the error
			listener.handleError(e);
			return;
		}

		// verify response code is good
		int buyIntentBundleResponseCode = buyIntentBundle.getInt("RESPONSE_CODE");
		if (buyIntentBundleResponseCode != 0) {
			if ((buyIntentBundleResponseCode == 7) && (identifier.equals("android.test.purchased"))) {
				// consume product if we have the test-case (breaks otherwise!)
				final Activity activity = this.activity;
				new Thread(new Runnable() {
					public void run () {
						try {
							Bundle ownedItems = mService.getPurchases(API_VERSION, activity.getPackageName(), "inapp", null);
							ArrayList<String> purchaseDataList = ownedItems.getStringArrayList("INAPP_PURCHASE_DATA_LIST");
							for (String purchaseData : purchaseDataList) {
								JSONObject jo = null;
								String ownedIdentifier = null;
								try {
									jo = new JSONObject(purchaseData);
									ownedIdentifier = jo.getString("productId");
								} catch (JSONException e) {
									// output the parsing error
									throw new RuntimeException(e);
								}
								if (ownedIdentifier.equals("android.test.purchased")) {
									String purchaseToken = jo.optString("token", jo.optString("purchaseToken"));

									// consume purchaseToken, handling any errors
									int consumeResult = mService.consumePurchase(API_VERSION, activity.getPackageName(), purchaseToken);
									if (consumeResult != 0) {
										// bad response code!
										throw new RuntimeException("Bad response code for consumePurchase(...):" + consumeResult);
									}
								}
							}
						} catch (RemoteException e) {
							// forward the error
							throw new RuntimeException(e);
						}
					}
				}).start();

				// try to create the buy bundle again --> should work now because consumed!?
				try {
					buyIntentBundle = mService.getBuyIntent(API_VERSION, activity.getPackageName(), identifier, "inapp",
						developerPayload);
				} catch (RemoteException e) {
					// forward the error
					listener.handleError(e);
					return;
				}
			} else {
				// bad response code!
				listener.handleError(new RuntimeException("Bad response code for getBuyIntent(...):" + buyIntentBundleResponseCode));
				return;
			}
		}

		// make the purchase (result will be returned in onActivityResult)
		PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
		try {
			activity.startIntentSenderForResult(pendingIntent.getIntentSender(), GDX_PAY_GOOGLE_ANDROID_REQUEST_CODE, new Intent(),
				Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0));
		} catch (SendIntentException e) {
			// forward the error
			listener.handleError(e);
			return;
		}
	}

	public void onActivityResult (int requestCode, int resultCode, Intent data) {
		// our request code?
		if (requestCode == GDX_PAY_GOOGLE_ANDROID_REQUEST_CODE) {
			// handle the data
			if (resultCode == Activity.RESULT_OK) {
				// parse the data
				int responseCode = data.getIntExtra("RESPONSE_CODE", 0);
				String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
				String dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE");

				// Google IAP data is returned as JSON object
				try {
					// purchase data
					JSONObject jo = new JSONObject(purchaseData);
					String identifier = jo.getString("productId");
					boolean valid = Integer.parseInt(jo.getString("purchaseState")) == 0; // 0==purchased!
					final String transactionId = jo.getString("purchaseToken");
					Date transactionDate = new Date(Long.parseLong(jo.getString("purchaseTime")));
					String transactionReceipt = null;

					// consume item directly if it's our test-case (needs to run in separate thread!)!
					if (identifier.equals("android.test.purchased")) {
						final Activity activity = this.activity;
						new Thread(new Runnable() {
							public void run () {
								try {
									int consumeResult = mService.consumePurchase(API_VERSION, activity.getPackageName(), transactionId);
									if (consumeResult != 0) {
										// bad response code!
										throw new RuntimeException("Bad response code for consumePurchase(...):" + consumeResult);
									}
								} catch (RemoteException e) {
									// forward the error
									throw new RuntimeException(e);
								}
							}
						}).start();
					}

					// are we in test-mode: if yes, use custom android identifiers!
					if (identifier.equals("android.test.purchased")) {
						identifier = Purchase.IDENTIFIER_TEST_SUCCESS;
					} else if (identifier.equals("android.test.canceled")) {
						identifier = Purchase.IDENTIFIER_TEST_FAILURE_CANCELED;
					} else if (identifier.equals("android.test.refunded")) {
						identifier = Purchase.IDENTIFIER_TEST_FAILURE_REFUNDED;
					} else if (identifier.equals("android.test.item_unavailable")) {
						identifier = Purchase.IDENTIFIER_TEST_FAILURE_INVALIDIDENTIFIER;
					}

					// obtain listener from payload
					String developerPayload = jo.getString("developerPayload");
					final PurchaseListener listener = listeners.remove(developerPayload);

					// notify via listener
					if (listener == null) {
						// output developer payload error
						Gdx.app.error("", "Wrong developerPayload string (cannot find listener).");
					} else {
						// build purchase object
						Purchase purchase = new Purchase(identifier, valid, transactionId, transactionDate, transactionReceipt);

						// output result
						listener.handlePurchase(purchase);
					}
				} catch (JSONException e) {
					// output the parsing error
					Gdx.app.error("", "Error parsing IAP JSON data.", e);
				}
			}
		}
	}

	@Override
	public String toString () {
		return "IAPGoogleAndroid";
	}
}

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

package com.badlogic.gdx.pay.android.openiab;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.onepf.oms.OpenIabHelper;
import org.onepf.oms.appstore.googleUtils.IabHelper;
import org.onepf.oms.appstore.googleUtils.IabResult;
import org.onepf.oms.appstore.googleUtils.Inventory;

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
import com.badlogic.gdx.pay.PurchaseManagerConfig;
import com.badlogic.gdx.pay.Transaction;
import com.badlogic.gdx.pay.PurchaseListener;
import com.badlogic.gdx.pay.PurchaseManager;
import com.badlogic.gdx.pay.PurchaseObserver;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;

/** The purchase manager implementation for Android via <a href="http://www.onepf.org/openiab">OpenIAB</a>. Supported stores
 * include:
 * <ul>
 * <li>Google Play
 * <li>Amazon
 * <li>Samsung Apps
 * <li>Nokia
 * <li>Open Store
 * <li>SlideME
 * <li>Aptoide
 * </ul>
 * Please note that Fortumo is not included.
 * <p>
 * To integrate on Android do the following:
 * <ul>
 * <li>AndroidManifest.xml: add the required permissions (see <a href="https://github.com/onepf/OpenIAB">OpenIAB on GitHub</a>).
 * <li>proguard.cfg: add the required proguard settings (see <a href="https://github.com/onepf/OpenIAB">OpenIAB on GitHub</a>).
 * <li>AndroidApplication/member variables: add "PurchaseManagerAndroidOpenIAB manager;"
 * <li>AndroidApplication.onCreate(...): add "manager = new PurchaseManagerAndroidOpenIAB(this);"
 * <li>AndroidApplication.onDispose(): add "manager.dispose(); manager = null;"
 * <li>AndroidApplication.onActivityResult(...): add "manager.onActivityResult(requestCode, resultCode, data);"
 * <ul>
 * 
 * @author noblemaster */
public class PurchaseManagerAndroidOpenIAB implements PurchaseManager, Disposable {

	/** Debug tag for logging. */
	private static final String TAG = "GdxPay/OpenIAB";
	
	/** Our Android activity. */
	private Activity activity;
	/** The request code to use for onActivityResult (arbitrary chosen). */
	private int requestCode;

	/** Our OpenIAB helper class through which we get access to the various markets. */
	private OpenIabHelper helper;

	public PurchaseManagerAndroidOpenIAB (Activity activity) {
		this(activity, 1001); // NOTE: requestCode here is an arbitrarily chosen number!
	}

	public PurchaseManagerAndroidOpenIAB (Activity activity, int requestCode) {
		this.activity = activity;

		// the request code for onActivityResult
		this.requestCode = requestCode;
	}

	@Override
	public String storeName () {
		// return the correct store name
		String storeName = helper.getConnectedAppstoreName();
		if (storeName.equals(OpenIabHelper.NAME_GOOGLE)) {
			return PurchaseManagerConfig.STORE_NAME_GOOGLE;
		}
		else if (storeName.equals(OpenIabHelper.NAME_AMAZON)) {
			return PurchaseManagerConfig.STORE_NAME_AMAZON;
		}
		else if (storeName.equals(OpenIabHelper.NAME_SAMSUNG)) {
			return PurchaseManagerConfig.STORE_NAME_SAMSUNG;
		}
		else if (storeName.equals(OpenIabHelper.NAME_NOKIA)) {
			return PurchaseManagerConfig.STORE_NAME_NOKIA;
		}
		else if (storeName.equals(OpenIabHelper.NAME_SLIDEME)) {
			return PurchaseManagerConfig.STORE_NAME_SLIDEME;
		}
		else if (storeName.equals(OpenIabHelper.NAME_APTOIDE)) {
			return PurchaseManagerConfig.STORE_NAME_APTOIDE;
		}
		else if (storeName.equals(OpenIabHelper.NAME_APPLAND)) {
			return PurchaseManagerConfig.STORE_NAME_APPLAND;
		}
		else if (storeName.equals(OpenIabHelper.NAME_YANDEX)) {
			return PurchaseManagerConfig.STORE_NAME_YANDEX;
		}
		else {
			// we should get here: the correct store should always be mapped!
			Gdx.app.error(TAG, "Store name could not be mapped: " + storeName);
			return storeName;
		}
	}

	@Override
	public void install (final PurchaseObserver observer, PurchaseManagerConfig config) {
		// build the OpenIAB options
		config!xxx();

		// start OpenIAB
		helper = new OpenIabHelper(activity, options);
		helper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
			public void onIabSetupFinished (IabResult result) {
				if (!result.isSuccess()) {
					helper = null;
					observer.handleInstallError(new GdxRuntimeException("Problem setting up in-app billing: " + result));
					return;
				}
				Gdx.app.log(TAG, "OpenIAB successfully initialized.");
			}
		});
	}

	@Override
	public boolean installed () {
		return helper != null;
	}

	@Override
	public void dispose () {
		// dispose OpenIAB and underlying store
		helper.dispose();
		helper = null;
	}

	@Override
	public void purchase (final PurchaseListener listener, final String identifier) {
		// make a purchase
		helper.launchPurchaseFlow(activity, identifier, requestCode, new IabHelper.OnIabPurchaseFinishedListener() {
			@Override
			public void onIabPurchaseFinished (IabResult result, org.onepf.oms.appstore.googleUtils.Purchase purchase) {
				// parse transaction data
				xxx();

				// forward result to listener
				listener.handlePurchase(transaction);

				// if the listener doesn't throw an error, we consume as needed
				if (configuration.getOffer(identifier).getType() == OfferType.CONSUMABLE) {
					// it's a consumable, so we consume right away!
					helper.consumeAsync(purchase, new IabHelper.OnConsumeFinishedListener() {
						@Override
						public void onConsumeFinished (org.onepf.oms.appstore.googleUtils.Purchase purchase, IabResult result) {
							if (!result.isSuccess()) {
								// NOTE: we should only rarely have an exception due to e.g. network outages etc.
								Gdx.app.error(TAG, "Error while consuming: " + result);
							}
						}
					});
				}
			}
		});
	}

	@Override
	public void purchaseRestore () {
		// ask for purchase restore
		helper.queryInventoryAsync(new IabHelper.QueryInventoryFinishedListener() {
			@Override
			public void onQueryInventoryFinished (IabResult result, Inventory inventory) {
				xxx();

			}
		});
	}

	public void onActivityResult (int requestCode, int resultCode, Intent data) {
		// forwards activities to OpenIAB for processing
		helper.handleActivityResult(requestCode, resultCode, data);
	}

	@Override
	public String toString () {
		return "OpenIAB/" + helper.getConnectedAppstoreName();
	}
}

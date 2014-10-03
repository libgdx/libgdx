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

package com.badlogic.gdx.pay.android.ouya;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONException;
import org.json.JSONObject;

import tv.ouya.console.api.CancelIgnoringOuyaResponseListener;
import tv.ouya.console.api.OuyaEncryptionHelper;
import tv.ouya.console.api.OuyaErrorCodes;
import tv.ouya.console.api.OuyaFacade;
import tv.ouya.console.api.OuyaResponseListener;
import tv.ouya.console.api.Product;
import tv.ouya.console.api.Purchasable;
import tv.ouya.console.api.Receipt;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ParseException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.pay.PurchaseManager;
import com.badlogic.gdx.pay.PurchaseManagerConfig;
import com.badlogic.gdx.pay.PurchaseObserver;
import com.badlogic.gdx.pay.Transaction;

/** The purchase manager implementation for OUYA.
 * <p>
 * Include the gdx-pay-android-ouya.jar for this to work (plus gdx-pay-android.jar). Also update the "uses-permission" settings in
 * AndroidManifest.xml and your proguard settings.
 * 
 * @author just4phil */
public class PurchaseManagerAndroidOUYA implements PurchaseManager {

	/** Debug tag for logging. */
	private static final String TAG = "GdxPay/OUYA";
	private static final boolean LOGDEBUG = true;
	private static final boolean SHOWTOASTS = true;
	private static final int LOGTYPELOG = 0;
	private static final int LOGTYPEERROR = 1;

	/** Our Android activity. */
	Activity activity;

	/** The registered observer. */
	PurchaseObserver observer;
	/** The configuration. */
	PurchaseManagerConfig config;

	/** the ouya helper */
	OuyaFacade ouyaFacade;

	/** The OUYA cryptographic key for the application */
	PublicKey ouyaPublicKey;
	String applicationKeyPath;
	List<Purchasable> productIDList; 	// --- This is the set of OUYA product IDs which our app knows about
	final Map<String, Product> ouyaOutstandingPurchaseRequests = new HashMap<String, Product>();
	ReceiptListener myOUYAreceiptListener = new ReceiptListener();
	List<Receipt> mReceiptList; 		// the list of purchased items, sorted
	ArrayList<Product> productList = new ArrayList<Product>();
	Purchasable purchasable; 			// for a concrete purchase
	Product OUYApurchaseProduct;
//	com.badlogic.gdx.pay.PurchaseListener appPurchaseListener; // this is the listener from the app that will be informed after a purchase

	// ------- for Toasts (debugging) -----
	String toastText;
	int duration;

	// --------------------------------------------------

	public PurchaseManagerAndroidOUYA (Activity activity, int requestCode) {
		this.activity = activity;
	}

	/** Used by IAP.java to determine if we are running on OUYA hardware :). */
	public static final boolean isRunningOnOUYAHardware () {
		// NOTE: - this would be nice but doesn't work yet (i.e. crashes before ouyaFacade.init(...) is called)
		// - promised to be fixed in the next SDK release...
		// - see for details: http://forums.ouya.tv/discussion/3772/bug-limit-in-isrunningonouyasupportedhardware
		// return OuyaFacade.getInstance().isRunningOnOUYAHardware();

		// let's determine if we are on OUYA-hardware by this hack...
		String name = android.os.Build.MODEL.toLowerCase();
		return (name.contains("ouya") || name.contains("mojo") || name.contains("m.o.j.o"));
	}

	@Override
	public String storeName () {
		return PurchaseManagerConfig.STORE_NAME_ANDROID_OUYA;
	}

	@Override
	public void install (final PurchaseObserver observer, PurchaseManagerConfig config) {
		this.observer = observer;
		this.config = config;

		// Obtain applicationKey and developer ID. Pass in as follows:
		// -------------------------------------------------------------------------
		//		config.addStoreParam(
		//			PurchaseManagerConfig.STORE_NAME_ANDROID_OUYA, 
		//			new Object[] { OUYA_DEVELOPERID_STRING, applicationKeyPathSTRING });
		// -------------------------------------------------------------------------

		Object[] configuration = (Object[])config.getStoreParam(PurchaseManagerConfig.STORE_NAME_ANDROID_OUYA);
		String developerID = (String)configuration[0];
		applicationKeyPath = (String)configuration[1]; // store our OUYA applicationKey-Path!
		
		ouyaFacade = OuyaFacade.getInstance();
		ouyaFacade.init((Context)activity, developerID);

		// --- copy all available products to the list of purchasables
		productIDList = new ArrayList<Purchasable>(config.getOfferCount());
		for (int i = 0; i < config.getOfferCount(); i++) {
			productIDList.add(new Purchasable(config.getOffer(i).getIdentifier()));
		}

		// Create a PublicKey object from the key data downloaded from the developer portal.
		try {
			// Read in the key.der file (downloaded from the developer portal)
			FileHandle fHandle = Gdx.files.internal(applicationKeyPath);
			byte[] applicationKey = fHandle.readBytes();

			X509EncodedKeySpec keySpec = new X509EncodedKeySpec(applicationKey);
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			ouyaPublicKey = keyFactory.generatePublic(keySpec);
			showMessage(LOGTYPELOG, "succesfully created publicKey");

			// ---- request the productlist ---------
			requestProductList();

			// notify of successful initialization
			observer.handleInstall();

		} catch (Exception e) {
			// notify about the problem
			showMessage(LOGTYPEERROR, "Problem setting up in-app billing: Unable to create encryption key");
			observer.handleInstallError(new RuntimeException(
				"Problem setting up in-app billing: Unable to create encryption key: " + e));
		}
	}

	// ----- Handler --------------------

	Handler handler = new HandlerExtension();

	final static int showToast = 0;
	final static int requestOUYAproducts = 1;
	final static int requestOUYApurchase = 2;
	final static int requestPurchaseRestore = 3;

	final class HandlerExtension extends Handler {

		@Override
		public void handleMessage (Message msg) {

			switch (msg.what) {

			case requestOUYAproducts:
				ouyaFacade.requestProductList(productIDList, productListListener);
				break;

			case requestOUYApurchase:
				ouyaFacade.requestPurchase(purchasable, new PurchaseListener(OUYApurchaseProduct));
				break;

			case requestPurchaseRestore:
				ouyaFacade.requestReceipts(myOUYAreceiptListener);
				break;

			case showToast:
				Toast toast = Toast.makeText(activity, toastText, duration);
				toast.show();
				break;
			}
		}
	}

	// ------------------------------------------------
	/** Request the receipts from the users previous purchases from the server. */
	public void requestPurchaseRestore () {
		handler.sendEmptyMessage(requestPurchaseRestore);
	}

	/** Request the available products from the server. */
	public void requestProductList () {
		handler.sendEmptyMessage(requestOUYAproducts);
	}

	/** make a purchase */
	@Override
	public void purchase (String identifier) {
		// String payload = null;

		OUYApurchaseProduct = getProduct(identifier);

		if (OUYApurchaseProduct != null) {
			try {
				requestPurchase(OUYApurchaseProduct);
				handler.sendEmptyMessage(requestOUYApurchase);

			} catch (UnsupportedEncodingException e) {
				observer.handlePurchaseError(e);
				e.printStackTrace();
			} catch (GeneralSecurityException e) {
				observer.handlePurchaseError(e);
				e.printStackTrace();
			} catch (JSONException e) {
				observer.handlePurchaseError(e);
				e.printStackTrace();
			}
		} else {
			showMessage(LOGTYPEERROR, "There has been a Problem with your Internet connection. Please try again later");
			observer.handlePurchaseError(new RuntimeException(
				"There has been a Problem with your Internet connection. Please try again later"));
		}
	}

	// -------------------------------------------------------------

	/** The callback for when the list of user receipts has been requested. */
	public class ReceiptListener implements OuyaResponseListener<String> {
		/** Handle the successful fetching of the data for the receipts from the server.
		 * 
		 * @param receiptResponse The response from the server. */
		@Override
		public void onSuccess (String receiptResponse) {
			OuyaEncryptionHelper helper = new OuyaEncryptionHelper();
			List<Receipt> receipts = null;
			try {
				JSONObject response = new JSONObject(receiptResponse);
				if (response.has("key") && response.has("iv")) {
					receipts = helper.decryptReceiptResponse(response, ouyaPublicKey);
				} else
					receipts = helper.parseJSONReceiptResponse(receiptResponse);

			} catch (ParseException e) {
				observer.handleRestoreError(e);
				throw new RuntimeException(e);
			} catch (JSONException e) {
				observer.handleRestoreError(e);
				throw new RuntimeException(e);
			} catch (GeneralSecurityException e) {
				observer.handleRestoreError(e);
				throw new RuntimeException(e);
			} catch (IOException e) {
				observer.handleRestoreError(e);
				throw new RuntimeException(e);
			} catch (java.text.ParseException e) {
				observer.handleRestoreError(e);
				e.printStackTrace();
			}
			Collections.sort(receipts, new Comparator<Receipt>() {
				@Override
				public int compare (Receipt lhs, Receipt rhs) {
					return rhs.getPurchaseDate().compareTo(lhs.getPurchaseDate());
				}
			});

			mReceiptList = receipts;
			List<Transaction> transactions = new ArrayList<Transaction>(mReceiptList.size());

			for (int i = 0; i < mReceiptList.size(); i++) {
				transactions.add(convertToTransaction(mReceiptList.get(i)));
			}
			// send inventory to observer
			observer.handleRestore(transactions.toArray(new Transaction[transactions.size()]));

			// ========= not sure if this is needed?? ---- shouldnt this be the task of the app ??

			// if the observer above didn't throw an error, we consume all consumeables as needed
			// for (int i = 0; i < mReceiptList.size(); i++) {
				// Receipt receipt = mReceiptList.get(i);
				// Offer offer = config.getOffer(receipt.getIdentifier());
					// if (offer == null) {
						// Gdx.app.debug(TAG, "Offer not found for: " + receipt.getIdentifier());
					// }
					// else if (offer.getType() == OfferType.CONSUMABLE) {
						//
						// Gdx.app.log("TODO", "we have to consume incoming receipts?!");
						//
						// // it's a consumable, so we consume right away!
						// helper.consumeAsync(purchase, new IabHelper.OnConsumeFinishedListener() {
							// @Override
							// public void onConsumeFinished (Purchase purchase, IabResult result) {
							// if (!result.isSuccess()) {
								// // NOTE: we should only rarely have an exception due to e.g. network outages etc.
								// Gdx.app.error(TAG, "Error while consuming: " + result);
							// }
						// }
					// });
				// }
			// }
		}

		@Override
		public void onCancel () {
//			observer.handlePurchaseCanceled();	 // this is minor relevant
			showMessage(LOGTYPELOG, "receiptlistener: user canceled");
		}

		@Override
		public void onFailure (int arg0, String arg1, Bundle arg2) {
			// observer.handleRestoreError(e);
			showMessage(LOGTYPEERROR, "receiptlistener: onFailure!");
		}
	}

	// ----------------------------

	OuyaResponseListener<ArrayList<Product>> productListListener = new CancelIgnoringOuyaResponseListener<ArrayList<Product>>() {

		@Override
		public void onSuccess (ArrayList<Product> products) {
			productList = products;
			showMessage(LOGTYPELOG, "successfully loaded productlist. " + productList.size() + " products found");

		}

		@Override
		public void onFailure (int errorCode, String errorMessage, Bundle errorBundle) {
			productList = null;
			showMessage(LOGTYPEERROR, "failed to load productlist!");
		}
	};

	// ---------------------------

	/** search for a specific product by identifier */
	public Product getProduct (String identifier) {
		Product returnProduct = null;
		for (int i = 0; i < productList.size(); i++) {
			if (productList.get(i).getIdentifier().equals(identifier)) {
				returnProduct = productList.get(i);
				break;
			}
		}
		return returnProduct;
	}

	// ------------------------------------------------

	public void requestPurchase (final Product product) throws GeneralSecurityException, UnsupportedEncodingException,
		JSONException {
		SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");

		// This is an ID that allows you to associate a successful purchase with
		// it's original request. The server does nothing with this string except
		// pass it back to you, so it only needs to be unique within this instance
		// of your app to allow you to pair responses with requests.
		String uniqueId = Long.toHexString(sr.nextLong());

		JSONObject purchaseRequest = new JSONObject();
		purchaseRequest.put("uuid", uniqueId);
		purchaseRequest.put("identifier", product.getIdentifier());
		// purchaseRequest.put("testing", "true"); // !!!! This value is only needed for testing, not setting it results in a live purchase
		String purchaseRequestJson = purchaseRequest.toString();

		byte[] keyBytes = new byte[16];
		sr.nextBytes(keyBytes);
		SecretKey key = new SecretKeySpec(keyBytes, "AES");

		byte[] ivBytes = new byte[16];
		sr.nextBytes(ivBytes);
		IvParameterSpec iv = new IvParameterSpec(ivBytes);

		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding", "BC");
		cipher.init(Cipher.ENCRYPT_MODE, key, iv);
		byte[] payload = cipher.doFinal(purchaseRequestJson.getBytes("UTF-8"));

		cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", "BC");
		cipher.init(Cipher.ENCRYPT_MODE, ouyaPublicKey);
		byte[] encryptedKey = cipher.doFinal(keyBytes);

		purchasable = new Purchasable(product.getIdentifier(), Base64.encodeToString(encryptedKey, Base64.NO_WRAP),
			Base64.encodeToString(ivBytes, Base64.NO_WRAP), Base64.encodeToString(payload, Base64.NO_WRAP));

		synchronized (ouyaOutstandingPurchaseRequests) {
			ouyaOutstandingPurchaseRequests.put(uniqueId, product);
		}
	}

	// -----------------------------------------------------------

	/** The callback for when the user attempts to purchase something. We're not worried about the user cancelling the purchase so
	 * we extend CancelIgnoringOuyaResponseListener, if you want to handle cancelations differently you should extend
	 * OuyaResponseListener and implement an onCancel method.
	 * 
	 * @see tv.ouya.console.api.CancelIgnoringOuyaResponseListener
	 * @see tv.ouya.console.api.OuyaResponseListener#onCancel() */
	private class PurchaseListener implements OuyaResponseListener<String> {
		/** The ID of the product the user is trying to purchase. This is used in the onFailure method to start a re-purchase if the
		 * user wishes to do so. */
		private Product mProduct;

		PurchaseListener (final Product product) {
			mProduct = product;
		}

		/** Handle a successful purchase.
		 * 
		 * @param result The response from the server. */
		@Override
		public void onSuccess (String result) {
			Product product = null;
			Product storedProduct = null;
			String id;
			try {
				OuyaEncryptionHelper helper = new OuyaEncryptionHelper();

				JSONObject response = new JSONObject(result);
				if (response.has("key") && response.has("iv")) {
					id = helper.decryptPurchaseResponse(response, ouyaPublicKey);
					synchronized (ouyaOutstandingPurchaseRequests) {
						storedProduct = ouyaOutstandingPurchaseRequests.remove(id);
						// showMessage("PurchaseListener: looks good ....");
					}
					if (storedProduct == null || !storedProduct.getIdentifier().equals(mProduct.getIdentifier())) {
						showMessage(LOGTYPEERROR, "Purchased product is not the same as purchase request product");
						onFailure(OuyaErrorCodes.THROW_DURING_ON_SUCCESS,
							"Purchased product is not the same as purchase request product", Bundle.EMPTY);
						return;
					}
				} else {
					product = new Product(new JSONObject(result));
					if (!mProduct.getIdentifier().equals(product.getIdentifier())) {
						showMessage(LOGTYPEERROR, "Purchased product is not the same as purchase request product");
						onFailure(OuyaErrorCodes.THROW_DURING_ON_SUCCESS,
							"Purchased product is not the same as purchase request product", Bundle.EMPTY);
						return;
					}
				}
			} catch (ParseException e) {
				observer.handlePurchaseError(e);
				onFailure(OuyaErrorCodes.THROW_DURING_ON_SUCCESS, e.getMessage(), Bundle.EMPTY);
			} catch (JSONException e) {
				observer.handlePurchaseError(e);
				onFailure(OuyaErrorCodes.THROW_DURING_ON_SUCCESS, e.getMessage(), Bundle.EMPTY);
				return;
			} catch (IOException e) {
				observer.handlePurchaseError(e);
				onFailure(OuyaErrorCodes.THROW_DURING_ON_SUCCESS, e.getMessage(), Bundle.EMPTY);
				return;
			} catch (GeneralSecurityException e) {
				observer.handlePurchaseError(e);
				onFailure(OuyaErrorCodes.THROW_DURING_ON_SUCCESS, e.getMessage(), Bundle.EMPTY);
				return;
			} catch (java.text.ParseException e) {
				observer.handlePurchaseError(e);
				e.printStackTrace();
				return;
			}

			// evrything is ok ...
			// purchaseRestore(); // check for purchases ..... would work but is not intended here

			if (storedProduct != null) {
				// convert product to transaction
				Transaction trans = convertPurchasedProductToTransaction(storedProduct);

				// inform the listener
				observer.handlePurchase(trans);
			} else {
				// appPurchaseListener.handlePurchaseError(e);
				showMessage(LOGTYPEERROR, "PurchaseListener: storedProduct == null!");
			}
		}

		/** Handle a failure. Because displaying the receipts is not critical to the application we just show an error message rather
		 * than asking the user to authenticate themselves just to start the application up.
		 * 
		 * @param errorCode An HTTP error code between 0 and 999, if there was one. Otherwise, an internal error code from the Ouya
		 *           server, documented in the {@link OuyaErrorCodes} class.
		 * 
		 * @param errorMessage Empty for HTTP error codes. Otherwise, a brief, non-localized, explanation of the error.
		 * 
		 * @param optionalData A Map of optional key/value pairs which provide additional information. */
		@Override
		public void onFailure (int errorCode, String errorMessage, Bundle optionalData) {
			// TODO: inform observer 
			showMessage(LOGTYPEERROR, "PurchaseListener: onFailure :(");
		}

		@Override
		public void onCancel () {
			observer.handlePurchaseCanceled();
			showMessage(LOGTYPELOG, "PurchaseListener: onCancel ...");
		}
	}

	// ---------------------------------------------

	@Override
	public void purchaseRestore () {
		handler.sendEmptyMessage(requestPurchaseRestore);
	}

	// --------------------------------------------

	/** Converts a product to our transaction object. */
	Transaction convertPurchasedProductToTransaction (Product product) {

		// build the transaction from the purchase object
		Transaction transaction = new Transaction();
		transaction.setIdentifier(product.getIdentifier());
		transaction.setStoreName(storeName());
		// transaction.setOrderId(receipt.getOrderId());
		transaction.setPurchaseTime(new Date());
		// transaction.setPurchaseText(skuDetails != null ? "Purchased: " + skuDetails.getTitle() : "Purchased");
		// GeneratedDate - when the receipt was created
		// Gamer - the gamer that purchased the product
		// UUID - the identifier of the gamer that purchased the product
		// transaction.setPurchaseCost(-1); // TODO: GdxPay: impl. parsing of COST + CURRENCY via skuDetails.getPrice()!
		// transaction.setPurchaseCostCurrency(null);

		// if (purchase.getPurchaseState() != 0) {
		// order has been refunded or cancelled
		// transaction.setReversalTime(new Date());
		// transaction.setReversalText(purchase.getPurchaseState() == 1 ? "Cancelled" : "Refunded");
		// } else {
		// still valid!
		// transaction.setReversalTime(null);
		// transaction.setReversalText(null);
		// }

		// transaction.setTransactionData(purchase.getOriginalJson());
		// transaction.setTransactionDataSignature(purchase.getSignature());

		showMessage(LOGTYPELOG, "converted purchased product to transaction.");
		return transaction;
	}

	/** Converts a purchase to our transaction object. */
	Transaction convertToTransaction (Receipt receipt) {

		// build the transaction from the purchase object
		Transaction transaction = new Transaction();
		transaction.setIdentifier(receipt.getIdentifier());
		transaction.setStoreName(storeName());
		// transaction.setOrderId(receipt.getOrderId());
		transaction.setPurchaseTime(receipt.getPurchaseDate());
		// transaction.setPurchaseText(skuDetails != null ? "Purchased: " + skuDetails.getTitle() : "Purchased");
		// GeneratedDate - when the receipt was created
		// Gamer - the gamer that purchased the product
		// UUID - the identifier of the gamer that purchased the product
		// transaction.setPurchaseCost(-1); // TODO: GdxPay: impl. parsing of COST + CURRENCY via skuDetails.getPrice()!
		// transaction.setPurchaseCostCurrency(null);

		// if (purchase.getPurchaseState() != 0) {
		// order has been refunded or cancelled
		// transaction.setReversalTime(new Date());
		// transaction.setReversalText(purchase.getPurchaseState() == 1 ? "Cancelled" : "Refunded");
		// } else {
		// still valid!
		// transaction.setReversalTime(null);
		// transaction.setReversalText(null);
		// }

		// transaction.setTransactionData(purchase.getOriginalJson());
		// transaction.setTransactionDataSignature(purchase.getSignature());

		showMessage(LOGTYPELOG, "converted receipt to transaction.");
		return transaction;
	}

	public void onActivityResult (int requestCode, int resultCode, Intent data) {
		// forwards activities to OpenIAB for processing
		// this is only relevant for android
	}

	@Override
	public String toString () {
		return "OUYA";
	}

	void showMessage (final int type, final String message) {
		if (LOGDEBUG) {
			if (type == LOGTYPELOG) Log.d(TAG, message);
			if (type == LOGTYPEERROR) Log.e(TAG, message);
		}
		if (SHOWTOASTS) {
			if (type == LOGTYPELOG) showToast(message);
			if (type == LOGTYPEERROR) showToast("error: " + message);
		}
	}

	// ---- saves the toast text and displays it
	void showToast (String toastText) {
		this.duration = Toast.LENGTH_SHORT;
		this.toastText = toastText;
		handler.sendEmptyMessage(showToast);
	}

	@Override
	public boolean installed () {
		return ouyaFacade != null;
	}

	@Override
	public void dispose () {
		if (ouyaFacade != null) {
			ouyaFacade.shutdown();
			ouyaFacade = null;

			productIDList = null;
			productList = null;
			
			// remove observer and config as well
			observer = null;
			config = null;

			showMessage(LOGTYPELOG, "disposed all the OUYA IAP stuff.");
		}
	}
}

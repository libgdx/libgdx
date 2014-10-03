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

package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.pay.Offer;
import com.badlogic.gdx.pay.OfferType;
import com.badlogic.gdx.pay.PurchaseManagerConfig;
import com.badlogic.gdx.pay.PurchaseObserver;
import com.badlogic.gdx.pay.PurchaseSystem;
import com.badlogic.gdx.pay.Transaction;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Timer;

/** Performs some tests for InApp payments.
 * 
 * @author davebaol */
public class PayTest extends GdxTest {

	String message;
	
	BitmapFont font;
	SpriteBatch batch;

	@Override
	public void create () {
		font = new BitmapFont();
		batch = new SpriteBatch();

		message = "Testing InApp System:\n";

		// test the purchase manager if there is one (if you use the default APK install it should find Google!)
		if (PurchaseSystem.hasManager()) {
			// build our purchase configuration: all your products and types need to be listed here
			final String IAP_TEST_CONSUMEABLE = "com.badlogic.gdx.tests.pay.consumeable";		
			PurchaseManagerConfig config = new PurchaseManagerConfig();
			config.addOffer(new Offer().setType(OfferType.CONSUMABLE)
				                        .setIdentifier(IAP_TEST_CONSUMEABLE)
			                           .putIdentifierForStore(PurchaseManagerConfig.STORE_NAME_ANDROID_GOOGLE, "android.test.purchased"));
			
			// install the observer
			PurchaseSystem.install(new PurchaseObserver() {					
				@Override
				public void handleRestore (Transaction[] transactions) {
					// keep note of our purchases
					message(" - totally " + transactions.length + " purchased products\n");
					for (int i = 0; i < transactions.length; i++) {
						message("   . " + transactions[i].getIdentifier() + "\n");
					}
				}
				@Override
				public void handleRestoreError (Throwable e) {
					message(" - error during purchase manager restore: " + e + "\n");

					// throw error
					throw new GdxRuntimeException(e);				
				}				
				@Override
				public void handleInstall () {
					message(" - purchase manager installed: " + PurchaseSystem.storeName() + ".\n");
				}
				@Override
				public void handleInstallError (Throwable e) {
					message(" - error installing purchase manager: " + e + "\n");

					// throw error
					throw new GdxRuntimeException(e);				
				}
				@Override
				public void handlePurchase (Transaction transaction) {
					message(" - purchased: " + transaction.getIdentifier() + "\n");
					
					// dispose the purchase system
					Gdx.app.postRunnable(new Runnable() {		
						@Override
						public void run () {					
							message(" - disposing the purchase manager.\n");
							PurchaseSystem.dispose();
							message("Testing InApp System: COMPLETED\n");
						}
					});
				}
				@Override
				public void handlePurchaseError (Throwable e) {
					message(" - error purchasing: " + e + "\n");
					// throw error
					throw new GdxRuntimeException(e);				
				}
				@Override
				public void handlePurchaseCanceled () {
					// TODO Auto-generated method stub
					
				}
			}, config);
					
			// restore purchases!
			Timer.schedule(new Timer.Task() {		
				@Override
				public void run () {					
					message(" - do a restore to check inventory\n");
					PurchaseSystem.purchaseRestore();
				}
			}, 2.0f);
			
			// try to make a new purchase
			Timer.schedule(new Timer.Task() {		
				@Override
				public void run () {					
					message(" - purchasing: " + IAP_TEST_CONSUMEABLE + ".\n");
					PurchaseSystem.purchase(IAP_TEST_CONSUMEABLE);
				}
			}, 4.0f);
		}
		else {
			message(" - no purchase manager found.\n");
		}			
	}
	
	synchronized void message(final String message) {
		Gdx.app.postRunnable(new Runnable() {
			public void run() {
				PayTest.this.message += message;
			}
		});
	}
	
	@Override
	public void render () {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.begin();
		font.drawMultiLine(batch, message, 20, Gdx.graphics.getHeight() - 20);
		batch.end();
	}

	@Override
	public void dispose () {
		batch.dispose();
		font.dispose();
	}
}

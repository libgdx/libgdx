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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.pay.Offer;
import com.badlogic.gdx.pay.OfferType;
import com.badlogic.gdx.pay.PurchaseManagerConfig;
import com.badlogic.gdx.pay.Transaction;
import com.badlogic.gdx.pay.PurchaseListener;
import com.badlogic.gdx.pay.PurchaseManager;
import com.badlogic.gdx.pay.PurchaseObserver;
import com.badlogic.gdx.pay.PurchaseSystem;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.I18NBundle;

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

		try {
			message = "Testing InApp System:\n";

			// test the purchase manager if there is one
			if (PurchaseSystem.hasManager()) {
				message += " - purchase manager: " + PurchaseSystem.storeName() + ".\n";

				// 0. build our purchase configuration
				PurchaseManagerConfig config = new PurchaseManagerConfig();
				config.addOffer(new Offer().setType(OfferType.ENTITLEMENT)
					                        .setIdentifier("com.badlogic.gdx.tests.pay.entitlement01")
				                           .putIdentifierForStore(PurchaseManagerConfig.STORE_NAME_GOOGLE, "android.test.purchased"));
				
				// 1. install the observer
				PurchaseSystem.install(new PurchaseObserver() {					
					@Override
					public void handleRestore (Transaction[] purchases) {
						// TODO Auto-generated method stub
						
					}
					@Override
					public void handleRestoreError (Throwable e) {
						// TODO Auto-generated method stub
						
					}				
					@Override
					public void handleInstall () {
						message += " - purchase manager successfully installed.\n";

						// TODO Auto-generated method stub
						
					}
					@Override
					public void handleInstallError (Throwable e) {
						message += " - error installing purchase manager: " + e + "\n";

						// TODO Auto-generated method stub
						
					}				
				}, config);
				xxx();
				
				// 2. query the inventory (this should only be called for (a) new installs and (b) broken inventory data)
				PurchaseSystem.purchaseRestore();
				xxx();
				
				// 3. try to make a purchase
				xxx();

				// 4. dispose the purchase system
				PurchaseSystem.dispose();
				xxx();
			}
			else {
				message += " - no purchase manager found.\n";
			}			
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
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

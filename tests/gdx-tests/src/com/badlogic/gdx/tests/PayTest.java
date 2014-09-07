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
import com.badlogic.gdx.pay.Purchase;
import com.badlogic.gdx.pay.PurchaseListener;
import com.badlogic.gdx.pay.PurchaseManager;
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
			message = "Testing InApp Managers... (" + IAP.getManagerCount() + " total)\n";

			// test all managers
			for (int i = 0; i < IAP.getManagerCount(); i++) {
				// testing the managers
				final PurchaseManager manager = IAP.getManager(i);
				manager.purchase(new PurchaseListener() {					
					@Override
					public void handlePurchase (Purchase purchase) {
						// output success message
						message += "Manager \"" + manager.toString() + "\": Purchase sucessful for " + purchase.identifier; 
					}					
					@Override
					public void handleError (Throwable e) {
						// we should never receive an error for this type of purchase
						throw new RuntimeException(e);
					}
				}, Purchase.IDENTIFIER_TEST_SUCCESS);
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

	public static final class IAP {

		private static List<PurchaseManager> managers;

		private IAP () {
			// private constructor to prevent instantiation
		}

		public static void init(PurchaseManager... managers) {
			IAP.managers = Arrays.asList(managers);
		}
		
		public static void dispose() {
			if (managers != null) {
				// dispose all managers
				for (int i = 0; i < managers.size(); i++) {
					PurchaseManager manager = managers.get(i);
					if (manager instanceof Disposable) {
						((Disposable)manager).dispose();
					}
				}
				
				// clear the list
				IAP.managers = null;
			}
		}

		/** Returns the purchase manager for the given index. */
		public static PurchaseManager getManager (int index) {
			return managers.get(index);
		}

		/** Returns the number of purchase managers available. */
		public static int getManagerCount () {
			if (managers == null) {
				return 0;
			}
			else {
				return managers.size();
			}
		}
	}
}

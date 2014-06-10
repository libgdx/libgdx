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

import com.badlogic.gdx.pay.PurchaseListener;
import com.badlogic.gdx.pay.PurchaseManager;

/** The purchase manager implementation for Google Play on Android.
 * 
 * @author noblemaster */
public class PurchaseManagerGoogleAndroid implements PurchaseManager {

	public PurchaseManagerGoogleAndroid (String publicKey) {
		// TODO: initialize the app with it's public key (I guess that's how it still works for Google Play???)
	}

	@Override
	public void register (PurchaseListener listener) {
		// TODO: implement!
	}

	@Override
	public void purchase (PurchaseListener listener, String identifier) {
		// TODO: implement!
	}
}

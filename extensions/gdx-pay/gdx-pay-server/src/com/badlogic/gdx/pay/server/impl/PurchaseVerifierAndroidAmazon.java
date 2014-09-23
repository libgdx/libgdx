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

package com.badlogic.gdx.pay.server.impl;

import com.badlogic.gdx.pay.PurchaseManagerConfig;
import com.badlogic.gdx.pay.Transaction;
import com.badlogic.gdx.pay.server.PurchaseVerifier;

/** Purchase verifier for Amazon. Return true if the purchase appears valid.
 * @author noblemaster */
public class PurchaseVerifierAndroidAmazon implements PurchaseVerifier {

	private String developerSecret;
	
	/**
	 * Constructs the purchase-verifier for purchases made via Amazon.
	 * 
	 * @param developerSecret  The shared secret for your Amazon developer account.
	 */
	public PurchaseVerifierAndroidAmazon(String developerSecret) {
		this.developerSecret = developerSecret;
	}
	
	@Override
	public String storeName () {
		return PurchaseManagerConfig.STORE_NAME_ANDROID_AMAZON;
	}

	@Override
	public boolean isValid (Transaction transaction) {
		// TODO: implement...
		return true;
	}
}

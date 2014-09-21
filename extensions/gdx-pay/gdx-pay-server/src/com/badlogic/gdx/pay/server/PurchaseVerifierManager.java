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

package com.badlogic.gdx.pay.server;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.pay.Transaction;

/** Verifies if a purchase is valid by e.g. doing a post-back validation on a server. Place the following two jar-files onto your
 * server (you won't need any other libGDX libraries on your server, all dependencies to libGDX have been removed for easy
 * integration):
 * <ul>
 * <li>gdx-pay.jar
 * <li>gdx-pay-server.jar
 * </ul>
 * <p>
 * How to integrate in your server:
 * 
 * <pre>
 * // create a manager which returns "true" by default  
 * PurchaseVerifierManager verifier = new PurchaseVerifierManager(true);
 * 
 * // add the various purchase verifiers
 * verifier.addVerifier(new PurchaseVerifierAndroidGoogle(...));
 * verifier.addVerifier(new PurchaseVerifierAndroidAmazon(...));
 * verifier.addVerifier(new PurchaseVerifierAndroidOUYA(...));
 * verifier.addVerifier(new PurchaseVerifieriOSApple(...));
 * ...
 * 
 * // verify a purchase
 * if (verifier.isValid(transaction)) {
 *   // transaction appears valid
 *   ... add to DB etc. ...
 * }
 * else {
 *   // transaction appears bogus
 *   ... punish user ...
 * }
 * </pre>
 * 
 * IMPORTANT: this code runs on your SERVER! Don't use on your client-application (not secure).
 * 
 * @author noblemaster */
public class PurchaseVerifierManager {

	/** Default if no verifier was found for a store. */
	private boolean defaultIfNoVerifierFound;

	/** The verifier implementations. */
	private Map<String, PurchaseVerifier> verifiers;

	public PurchaseVerifierManager () {
		this(false);
	}

	public PurchaseVerifierManager (boolean defaultIfNoVerifierFound) {
		this.defaultIfNoVerifierFound = defaultIfNoVerifierFound;
		this.verifiers = new HashMap<String, PurchaseVerifier>(16);
	}

	public void addVerifier (PurchaseVerifier verifier) {
		verifiers.put(verifier.storeName(), verifier);
	}

	public void removeVerifier (PurchaseVerifier verifier) {
		verifiers.remove(verifier.storeName());
	}

	/** Returns true if a transaction is deemed valid.
	 * <p>
	 * IMPORTANT: will return "defaultIfNoVerifierFound" if no verifier was found for the given transaction.
	 * 
	 * @param transaction The transaction to verify.
	 * @return True for considered valid. */
	public boolean isValid (Transaction transaction) {
		// find the verifier and verify via verifier if a purchase is valid
		PurchaseVerifier verifier = verifiers.get(transaction.getStoreName());
		if (verifier == null) {
			return defaultIfNoVerifierFound;
		} else {
			return verifier.isValid(transaction);
		}
	}
}

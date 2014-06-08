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

/** Listens to purchase changes.
 * @author noblemaster */
public interface PurchaseObserver {

	/** Will be called for all items that need to be restored because of (a) a new device install, (b) a purchase that wasn't
	 * handled yet by the application or (c) a purchase that was refunded.
	 * 
	 * @param purchase The purchased item that needs to be handled. The "valid" field will indicate the status of the purchase i.e.
	 *           false if refunded or true for purchases that are not handled yet. */
	public void handleRestore (Purchase purchase);

	/** Called when there is a problem with the purchase manager.
	 * 
	 * @param e Any errors such as problems with the purchase manager itself, problems connecting to the IAP system, problems with
	 *           restoring purchases etc. */
	public void handleFailure (Throwable e);
}

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

package com.badlogic.gdx.pay.ios.apple;

import com.badlogic.gdx.pay.PurchaseManager;
import com.badlogic.gdx.pay.PurchaseManagerConfig;
import com.badlogic.gdx.pay.PurchaseObserver;

/** The purchase manager implementation for Apple's iOS IAP system.
 * 
 * @author noblemaster */
public class PurchaseManageriOSApple implements PurchaseManager {

	@Override
	public String storeName () {
		return PurchaseManagerConfig.STORE_NAME_IOS_APPLE;
	}

	@Override
	public void install (PurchaseObserver observer, PurchaseManagerConfig config) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean installed () {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void dispose () {
		// TODO Auto-generated method stub

	}

	@Override
	public void purchase (String identifier) {
		// TODO Auto-generated method stub

	}

	@Override
	public void purchaseRestore () {
		// TODO Auto-generated method stub

	}
}

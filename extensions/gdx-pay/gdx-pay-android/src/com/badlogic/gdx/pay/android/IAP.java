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

package com.badlogic.gdx.pay.android;

import java.lang.reflect.Method;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.badlogic.gdx.LifecycleListener;
import com.badlogic.gdx.backends.android.AndroidEventListener;
import com.badlogic.gdx.pay.PurchaseManager;
import com.badlogic.gdx.pay.PurchaseSystem;

/** The IAP system for Android supporting the following stores:
 * <ul>
 * <li>via gdx-pay-android-openiab.jar: Google Play
 * <li>via gdx-pay-android-openiab.jar: Amazon
 * <li>via gdx-pay-android-openiab.jar: Samsung Apps
 * <li>via gdx-pay-android-openiab.jar: Nokia
 * <li>via gdx-pay-android-openiab.jar: Open Store
 * <li>via gdx-pay-android-openiab.jar: SlideME
 * <li>via gdx-pay-android-openiab.jar: Aptoide
 * <li>via gdx-pay-android-ouya.jar: Ouya
 * </ul>
 * <p>
 * To integrate into your Android project do the following:
 * <ul>
 * <li>1. add the jar-files to your project's lib directory as follows (IAP will work automatically once the files are present):
 * <ul>
 * <li>gdx-pay.jar: This goes into your "core"/lib project.
 * <li>gdx-pay-android.jar: ALWAYS include within your Android/lib directory to support the IAP systems below!
 * <li>gdx-pay-android-openiab.jar: into Android/lib if you want to support the stores listed from above mentioning OpenIAB.
 * <li>gdx-pay-android-ouya.jar: into Android/lib if you want to support OUYA.
 * </ul>
 * <li>2. AndroidManifest.xml: add the required permissions (i.e. "uses-permission...").
 * <li>3. proguard.cfg: add the required proguard settings for each store you want to support (see: TODO).
 * </ul>
 * Please note that no code changes for Android are necessary. As soon as you place the jar files everything will work out of the
 * box (instantiated via reflection).
 * 
 * @author noblemaster */
public class IAP implements LifecycleListener, AndroidEventListener {

	/** Debug tag for logging. */
	private static final String TAG = "GdxPay/IAP";

	/** AndroidApplication will call this dynamically via reflection. We try to find installed IAP systems (jar-files present). If
	 * yes, we use them.
	 * 
	 * @param activity The AndroidApplication activity.
	 * @param requestCode The request code to use in case they are needed (not all stores need them). */
	public IAP (Activity activity, int requestCode) {
		// are we on OUYA-hardware?
		try {
			Class<?> ouyaClazz = Class.forName("com.badlogic.gdx.pay.android.ouya.PurchaseManagerAndroidOUYA");
		   Method method = ouyaClazz.getMethod("isRunningOnOUYAHardware");
		   if ((Boolean)method.invoke(ouyaClazz)) {	
		   	// we are running on OUYA: let's set the purchase manager and be done with it!
		   	PurchaseSystem.setManager((PurchaseManager)ouyaClazz.getConstructor(Activity.class, int.class).newInstance(activity, requestCode));
		   	return;
		   }
		} catch (Exception e) {
			Log.d(TAG, "Failed to locate purchase manager for OUYA-IAP (gdx-pay-android-ouya.jar file not installed)", e);
		}

		// let's go with OpenIAB instead if we can find it...
		try {
			Class<?> iabClazz = Class.forName("com.badlogic.gdx.pay.android.openiab.PurchaseManagerAndroidOpenIAB");
		   PurchaseSystem.setManager((PurchaseManager)iabClazz.getConstructor(Activity.class, int.class).newInstance(activity, requestCode));
		} catch (Exception e) {
			Log.d(TAG, "Failed to locate purchase manager for OpenIAB-IAP (gdx-pay-android-openiab.jar file not installed)", e);
		}
	}

	@Override
	public void onActivityResult (int requestCode, int resultCode, Intent data) {
		// forward to corresponding Android IAP-system
		PurchaseManager manager = PurchaseSystem.getManager();
		if (manager != null) {
			try {
				// this might fail which is OK! --> some implementations will not require this...
				Method method = manager.getClass().getMethod("onActivityResult", int.class, int.class, Intent.class);
				method.invoke(manager, requestCode, resultCode, data);
			} catch (Exception e) {
				Log.d(TAG, "Failed to invoke onActivityResult(...) on purchase manager.", e);
			}
		}
	}

	@Override
	public void pause () {
		// not used ...
	}

	@Override
	public void resume () {
		// not used ...
	}

	@Override
	public void dispose () {
		// dispose the purchase system
		PurchaseSystem.dispose();
	}
}

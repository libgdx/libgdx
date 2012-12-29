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

package com.badlogic.gdx.backends.android;

import android.content.Context;
import android.provider.Settings.Secure;

import com.badlogic.gdx.DeviceInfo;

/** @author xoppa */
public class AndroidDeviceInfo implements DeviceInfo {
	public final Context context;
	public AndroidDeviceInfo(final Context context) {
		this.context = context;
	}
	/** {@inheritDoc} */
	@Override
	public String getManufacturer () {
		return android.os.Build.MANUFACTURER;
	}

	/** {@inheritDoc} */
	@Override
	public String getBrand () {
		return android.os.Build.BRAND;
	}

	/** {@inheritDoc} */
	@Override
	public String getDevice () {
		return android.os.Build.DEVICE;
	}
	
	/** {@inheritDoc} */
	@Override
	public String getProduct () {
		return android.os.Build.PRODUCT;
	}

	/** {@inheritDoc} */
	@Override
	public String getModel () {
		return android.os.Build.MODEL;
	}

	/** {@inheritDoc} */
	@Override
	public String getSerial () {
		return context == null ? "" : Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
		// For API 9 and above:
		// return android.os.Build.SERIAL;
	}

	/** {@inheritDoc} */
	@Override
	public String getVersion () {
		return android.os.Build.VERSION.RELEASE;
	}
}
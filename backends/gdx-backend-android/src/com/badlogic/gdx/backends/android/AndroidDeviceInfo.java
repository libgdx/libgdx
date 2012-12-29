package com.badlogic.gdx.backends.android;

import android.content.Context;
import android.provider.Settings.Secure;

import com.badlogic.gdx.DeviceInfo;

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
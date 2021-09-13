
package com.badlogic.gdx.backends.android;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.display.DisplayManager;

@SuppressLint("NewApi")
public class DefaultDisplayRotationHelper implements DisplayRotationHelper, DisplayManager.DisplayListener {

	private boolean refreshNeeded;
	private final Context mContext;

	public DefaultDisplayRotationHelper (Context context) {
		mContext = context;
	}

	public void onSurfaceChanged () {
		refreshNeeded = true;
	}

	public void onResume () {
		mContext.getSystemService(DisplayManager.class).registerDisplayListener(this, null);
	}

	public void onPause () {
		mContext.getSystemService(DisplayManager.class).unregisterDisplayListener(this);
	}

	@Override
	public void onDisplayAdded (int displayId) {
	}

	@Override
	public void onDisplayRemoved (int displayId) {
	}

	@Override
	public void onDisplayChanged (int displayId) {
		refreshNeeded = true;
	}

	public boolean getAndResetRefreshNeeded () {
		boolean isRefreshNeeded = refreshNeeded;
		refreshNeeded = false;
		return isRefreshNeeded;
	}

}

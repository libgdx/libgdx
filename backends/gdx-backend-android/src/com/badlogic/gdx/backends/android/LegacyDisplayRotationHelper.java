package com.badlogic.gdx.backends.android;

public class LegacyDisplayRotationHelper implements DisplayRotationHelper {

	private boolean refreshNeeded;

	@Override
	public boolean getAndResetRefreshNeeded () {
		boolean isRefreshNeeded = refreshNeeded;
		refreshNeeded = false;
		return isRefreshNeeded;
	}

	@Override
	public void onSurfaceChanged () {
		refreshNeeded = true;
	}

	@Override
	public void onResume () {
	}

	@Override
	public void onPause () {
	}
}

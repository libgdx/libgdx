
package com.dozingcatsoftware.bouncy;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.surfaceview.FillResolutionStrategy;

public class BouncyAndroid extends AndroidApplication {
	/** Called when the activity is first created. */
	@Override public void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initialize(new Bouncy(), false, new FillResolutionStrategy(), 16);
	}
}

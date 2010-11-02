
package com.badlogic.gdxinvaders;

import android.content.pm.ActivityInfo;
import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;

public class GdxInvadersAndroid extends AndroidApplication {
	/** Called when the activity is first created. */
	@Override public void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		initialize(new GdxInvaders(),false, 20);		
	}
}

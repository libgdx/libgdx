
package com.badlogic.gdx.tests.android;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;

public class AlphaTest extends AndroidApplication {
	public void onCreate (Bundle bundle) {
		super.onCreate(bundle);
		initialize(false);
		getGraphics().setRenderListener(new com.badlogic.gdx.tests.AlphaTest());
	}
}


package com.badlogic.gdx.tests.android;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.tests.utils.GdxTests;

public class GdxTestActivity extends AndroidApplication {

	public void onCreate (Bundle bundle) {
		super.onCreate(bundle);
		
		Bundle extras = getIntent().getExtras();
		String testName = (String)extras.get("test");
		
		GdxTest test = GdxTests.newTest(testName);
		initialize(test.needsGL20());
		getGraphics().setRenderListener(test);
	}
}

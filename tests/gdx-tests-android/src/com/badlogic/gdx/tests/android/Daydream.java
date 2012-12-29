package com.badlogic.gdx.tests.android;

import android.annotation.TargetApi;
import android.util.Log;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.backends.android.AndroidDaydream;
import com.badlogic.gdx.tests.MeshShaderTest;

@TargetApi(17)
public class Daydream extends AndroidDaydream {
	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		
		Log.i("Andrew", "hi");

		AndroidApplicationConfiguration cfg = new AndroidApplicationConfiguration();
		cfg.useGL20 = true;

		setInteractive(false);

		ApplicationListener app = new MeshShaderTest();
		initialize(app, cfg);
	}
}
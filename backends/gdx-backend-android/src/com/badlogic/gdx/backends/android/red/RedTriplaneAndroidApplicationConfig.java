
package com.badlogic.gdx.backends.android.red;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

public class RedTriplaneAndroidApplicationConfig {

	private ApplicationListener gdx_listener;
	private AndroidApplicationConfiguration android_config;

	public void setGdxListener (ApplicationListener gdx_listener) {
		this.gdx_listener = gdx_listener;
	}

	public void setAndroidApplicationConfig (AndroidApplicationConfiguration android_config) {
		this.android_config = android_config;
	}

	public ApplicationListener getGdxListener () {
		return gdx_listener;
	}

	public AndroidApplicationConfiguration getAndroidApplicationConfig () {
		return android_config;
	}

}

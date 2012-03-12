
package com.badlogic.gdx.tests.gwt.client;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;
import com.badlogic.gdx.tests.gwt.GwtInputTest;

public class GwtTestStarter extends GwtApplication {

	@Override
	public GwtApplicationConfiguration getConfig () {
		return new GwtApplicationConfiguration(640, 640);
	}

	@Override
	public ApplicationListener getApplicationListener () {
		return new GwtInputTest();
	}
}

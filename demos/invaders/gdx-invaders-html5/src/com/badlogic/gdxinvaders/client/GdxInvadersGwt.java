package com.badlogic.gdxinvaders.client;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;
import com.badlogic.gdxinvaders.GdxInvaders;

public class GdxInvadersGwt extends GwtApplication {
	@Override
	public GwtApplicationConfiguration getConfig () {
		GwtApplicationConfiguration config = new GwtApplicationConfiguration(480, 320);
//		config.useDebugGL = true;
		return config;
	}

	@Override
	public ApplicationListener getApplicationListener () {
		return new GdxInvaders();
	}
}

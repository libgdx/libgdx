package com.badlogic.cuboc.client;

import com.badlogic.cubocy.Cubocy;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;

public class CubocGwt extends GwtApplication {
	@Override
	public GwtApplicationConfiguration getConfig () {
		GwtApplicationConfiguration config = new GwtApplicationConfiguration(480, 320);
		config.useDebugGL = true;
		return config;
	}

	@Override
	public ApplicationListener getApplicationListener () {
		return new Cubocy();
	}
}

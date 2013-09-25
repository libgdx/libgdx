package com.badlogic.gdx.tests.net;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.tests.utils.GdxTest;

/**
 * Demonstrates how to open a browser and load
 * a specific URL.
 * @author badlogic
 *
 */
public class OpenBrowserExample extends GdxTest {
	@Override
	public void create() {
		Gdx.net.openURI("http://libgdx.badlogicgames.com");
	}
}

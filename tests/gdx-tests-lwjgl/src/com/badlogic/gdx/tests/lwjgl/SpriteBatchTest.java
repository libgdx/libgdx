
package com.badlogic.gdx.tests.lwjgl;

import com.badlogic.gdx.backends.desktop.LwjglApplication;

public class SpriteBatchTest {
	public static void main (String[] argv) {
		LwjglApplication app = new LwjglApplication("SpriteBatch Test", 480, 320, false);
		app.getGraphics().setRenderListener(new com.badlogic.gdx.tests.SpriteBatchTest());
	}
}

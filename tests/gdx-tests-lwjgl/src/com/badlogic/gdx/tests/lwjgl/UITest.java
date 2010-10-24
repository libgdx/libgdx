
package com.badlogic.gdx.tests.lwjgl;

import com.badlogic.gdx.backends.desktop.LwjglApplication;

public class UITest {
	public static void main (String[] argv) {
		LwjglApplication app = new LwjglApplication("UI Test", 480, 320, false);
		app.getGraphics().setRenderListener(new com.badlogic.gdx.tests.UITest());
	}
}

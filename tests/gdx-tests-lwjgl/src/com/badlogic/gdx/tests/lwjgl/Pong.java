
package com.badlogic.gdx.tests.lwjgl;

import com.badlogic.gdx.backends.desktop.LwjglApplication;

public class Pong {
	public static void main (String[] argv) {
		LwjglApplication app = new LwjglApplication("Pong", 480, 320, false);
		app.getGraphics().setRenderListener(new com.badlogic.gdx.tests.Pong());
	}
}

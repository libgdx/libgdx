
package com.badlogic.gdx.tests.lwjgl;

import com.badlogic.gdx.backends.desktop.LwjglApplication;

public class ObjTest {
	public static void main (String[] argv) {
		LwjglApplication app = new LwjglApplication("Obj Test", 480, 320, false);
		app.getGraphics().setRenderListener(new com.badlogic.gdx.tests.ObjTest());
	}
}

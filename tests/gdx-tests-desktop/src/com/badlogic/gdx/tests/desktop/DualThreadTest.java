
package com.badlogic.gdx.tests.desktop;

import com.badlogic.gdx.backends.desktop.JoglApplication;

public class DualThreadTest {
	public static void main (String[] argv) {
		JoglApplication app = new JoglApplication("Dual Thread Test", 480, 320, false);
		com.badlogic.gdx.tests.DualThreadTest test = new com.badlogic.gdx.tests.DualThreadTest();
		app.setApplicationListener(test);
		app.getGraphics().setRenderListener(test);
	}
}

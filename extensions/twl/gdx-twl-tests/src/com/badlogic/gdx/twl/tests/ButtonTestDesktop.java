
package com.badlogic.gdx.twl.tests;

import com.badlogic.gdx.backends.desktop.JoglApplication;

public class ButtonTestDesktop {
	public static void main (String[] argv) {
		JoglApplication app = new JoglApplication("Button Test", 480, 320, false);
		app.getGraphics().setRenderListener(new ButtonTest());
	}
}


package com.badlogic.gdx.twl.tests;

import com.badlogic.gdx.backends.desktop.JoglApplication;

public class TwlTestDesktop {
	public static void main (String[] argv) {
		JoglApplication app = new JoglApplication("Twl Test", 480, 320, false);
		app.getGraphics().setRenderListener(new TwlTest());
	}
}

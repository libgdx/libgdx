
package com.badlogic.gdx.tests.desktop;

import com.badlogic.gdx.backends.desktop.JoglApplication;

public class WaterRipples {
	public static void main (String[] argv) {
		JoglApplication app = new JoglApplication("Water Ripples", 480, 320, false);
		app.getGraphics().setRenderListener(new com.badlogic.gdx.tests.WaterRipples());
	}
}

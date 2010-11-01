
package com.badlogic.gdx.twl.tests;

import com.badlogic.gdx.backends.jogl.JoglApplication;

public class ButtonTestDesktop {
	public static void main (String[] argv) {
		new JoglApplication(new ButtonTest(), "Button Test", 480, 320, false);
	}
}

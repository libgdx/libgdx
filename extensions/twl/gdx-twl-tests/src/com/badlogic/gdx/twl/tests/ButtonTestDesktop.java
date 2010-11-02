
package com.badlogic.gdx.twl.tests;

import com.badlogic.gdx.backends.desktop.LwjglApplication;

public class ButtonTestDesktop {
	public static void main (String[] argv) {
		new LwjglApplication(new ButtonTest(), "Button Test", 480, 320, false);
	}
}

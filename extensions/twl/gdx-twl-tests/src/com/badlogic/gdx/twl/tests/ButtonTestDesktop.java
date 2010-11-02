
package com.badlogic.gdx.twl.tests;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;

public class ButtonTestDesktop {
	public static void main (String[] argv) {
		new LwjglApplication(new ButtonTest(), "Button Test", 480, 320, false);
	}
}

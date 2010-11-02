
package com.badlogic.gdx.twl.tests;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;

public class TextAreaTestDesktop {
	public static void main (String[] argv) {
		new LwjglApplication(new TextAreaTest(), "Twl Test", 480, 320, false);		
	}
}

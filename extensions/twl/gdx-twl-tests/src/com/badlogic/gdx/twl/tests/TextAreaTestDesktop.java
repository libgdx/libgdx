
package com.badlogic.gdx.twl.tests;

import com.badlogic.gdx.backends.jogl.JoglApplication;

public class TextAreaTestDesktop {
	public static void main (String[] argv) {
		new JoglApplication(new TextAreaTest(), "Twl Test", 480, 320, false);		
	}
}

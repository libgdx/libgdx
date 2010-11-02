
package com.badlogic.gdx.tests.lwjgl;

import com.badlogic.gdx.backends.desktop.LwjglApplet;
import com.badlogic.gdx.tests.Pong;

public class LwjglAppletTest extends LwjglApplet {
	public LwjglAppletTest () {
		super(new Pong(), false);
	}
}

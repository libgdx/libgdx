
package com.dozingcatsoftware.bouncy;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;

public class BouncyDesktop {
	public static void main (String[] argv) {
		new LwjglApplication(new Bouncy(), "Bouncy", 320, 480, true);
	}
}

package com.dozingcatsoftware.bouncy;

import com.badlogic.gdx.backends.jogl.JoglApplication;

public class BouncyDesktop {
	public static void main(String[] argv) {
		new JoglApplication(new Bouncy(), "Bouncy", 320, 480, false);
	}
}

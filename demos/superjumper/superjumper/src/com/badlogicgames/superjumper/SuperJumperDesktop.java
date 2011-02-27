package com.badlogicgames.superjumper;

import com.badlogic.gdx.backends.jogl.JoglApplication;

public class SuperJumperDesktop {
	public static void main(String[] argv) {
		new JoglApplication(new SuperJumper(), "Super Jumper", 320, 480, false);
	}
}

package com.badlogic.cubocy;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;

public class CubocDesktop {
	public static void main (String[] argv) {
		new LwjglApplication(new Cubocy(), "Cubocy", 480, 320, false);
	}
}

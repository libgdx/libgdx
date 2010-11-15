package com.badlogic.gdx.tests.lwjgl;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;

public class LwjglDebugStarter {
	public static void main(String[] argv) {
		new LwjglApplication(new com.badlogic.gdx.tests.VBOVATest(), "UI Test", 480, 320, false);
	}
}

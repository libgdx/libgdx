package com.badlogic.gdx.tests.lwjgl;

import com.badlogic.gdx.backends.desktop.LwjglApplicationNew;

public class LwjglDebugStarter {
	public static void main(String[] argv) {
		new LwjglApplicationNew(new com.badlogic.gdx.tests.UITest(), "UI Test", 480, 320, false);
	}
}

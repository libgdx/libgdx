package com.badlogic.gdx.physics.bullet;

import com.badlogic.gdx.utils.SharedLibraryLoader;

public class Bullet {
	/**
	 * Loads the native Bullet native library and initializes the gdx-bullet
	 * extension.
	 */
	public static void init() {
	    new SharedLibraryLoader().load("gdx-bullet");
	}
}

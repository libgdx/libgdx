
package com.badlogic.cubocy;

import com.badlogic.cubocy.screens.GameScreen;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;

public class Cubocy extends Game {
	public static void main (String[] argv) {
		new LwjglApplication(new Cubocy(), "Cubocy", 480, 320, false);
	}

	@Override
	public void create () {
		setScreen(new GameScreen(this));
	}
}

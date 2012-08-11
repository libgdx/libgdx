
package com.badlogic.cubocy;

import com.badlogic.cubocy.screens.GameScreen;
import com.badlogic.gdx.Game;

public class Cubocy extends Game {
	@Override
	public void create () {
		setScreen(new GameScreen(this));
	}
}

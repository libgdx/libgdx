package de.swagner.paxbritannica;

import com.badlogic.gdx.Game;

import de.swagner.paxbritannica.mainmenu.MainMenu;

public class PaxBritannica extends Game {
	@Override 
	public void create () {
		setScreen(new MainMenu(this));
	}
}

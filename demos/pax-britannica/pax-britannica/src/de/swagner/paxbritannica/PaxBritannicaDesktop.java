package de.swagner.paxbritannica;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;

public class PaxBritannicaDesktop {
	public static void main(String[] args) {
		new LwjglApplication(new PaxBritannica(),
				"Pax Britannica", 1024, 550,false);
	}
}

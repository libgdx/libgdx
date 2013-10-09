package de.swagner.paxbritannica.factory;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class PlayerProduction extends FactoryProduction {
		
	public PlayerProduction(int id, Vector2 position, Vector2 facing) {
		super(id, position, facing);
	}
	
	@Override
	public void draw(SpriteBatch spriteBatch) {
		thrust();
		turn(1);
		super.draw(spriteBatch);
	}

	
}

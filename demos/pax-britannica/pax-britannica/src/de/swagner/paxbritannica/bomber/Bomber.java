package de.swagner.paxbritannica.bomber;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import de.swagner.paxbritannica.GameInstance;
import de.swagner.paxbritannica.Resources;
import de.swagner.paxbritannica.Ship;

public class Bomber extends Ship {

	public BomberAI ai = new BomberAI(this);

	public Bomber(int id, Vector2 position, Vector2 facing) {
		super(id, position, facing);

		turnSpeed = 45f;
		accel = 45.0f;
		hitPoints = 440;
		
		switch (id) {
		case 1:
			this.set(Resources.getInstance().bomberP1);
			break;
		case 2:
			this.set(Resources.getInstance().bomberP2);
			break;
		case 3:
			this.set(Resources.getInstance().bomberP3);
			break;
		default:
			this.set(Resources.getInstance().bomberP4);
			break;
		}
		this.setOrigin(this.getWidth()/2, this.getHeight()/2);
	}

	@Override
	public void draw(SpriteBatch spriteBatch) {
		ai.update();
		
		super.draw(spriteBatch);
	}

	public void shoot(int approach) {
		 Vector2 bombFacing = new Vector2().set(facing).rotate(90*approach);
		 GameInstance.getInstance().bullets.add(new Bomb(id, collisionCenter, bombFacing));
		
	}

}

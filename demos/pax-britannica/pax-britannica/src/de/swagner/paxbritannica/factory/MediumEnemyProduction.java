package de.swagner.paxbritannica.factory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

import de.swagner.paxbritannica.GameInstance;
import de.swagner.paxbritannica.Ship;
import de.swagner.paxbritannica.bomber.Bomber;
import de.swagner.paxbritannica.fighter.Fighter;
import de.swagner.paxbritannica.frigate.Frigate;

public class MediumEnemyProduction extends FactoryProduction {

	int action_index = 0;
	float timeToHold = 0;
	float accumulated_frames = 0;
	float frames_to_wait = 0;
	int script_index = 0;
	float delta;

	int action = -1;

	int enemyFighters = 0;
	int enemyBombers = 0;
	int enemyFrigates = 0;
	int ownFighters = 0;
	int ownBombers = 0;
	int ownFrigates = 0;
	
	public MediumEnemyProduction(int id, Vector2 position, Vector2 facing) {
		super(id, position, facing);
	}

	@Override
	public void draw(SpriteBatch spriteBatch) {
		delta = Math.min(0.06f, Gdx.graphics.getDeltaTime());

		super.draw(spriteBatch);

		accumulated_frames += 30 * delta;

		if (production.currentBuildingUnit != action && action >-1) {
			button_held = true;
		} else {
			button_held = false;
			next_action();
		}

		thrust();
		turn(1);
	}

	public void next_action() {
		action = -1;		
		enemyFighters = 0;
		enemyBombers = 0;
		enemyFrigates = 0;
		ownFighters = 0;
		ownBombers = 0;
		ownFrigates = 0;				
		accumulated_frames = 0;
		timeToHold = 0;


		for (Ship fighter : GameInstance.getInstance().fighters) {
			if(fighter.id != this.id) {
				if(((Fighter) fighter).ai.target != null && ((Fighter) fighter).ai.target.id == this.id) {
					enemyFighters++;
				}
			}
			else ownFighters++;
		}
		
		for (Ship bomber : GameInstance.getInstance().bombers) {
			if(bomber.id != this.id) {
				if(((Bomber) bomber).ai.target != null && ((Bomber) bomber).ai.target.id == this.id) {
					enemyBombers++;
				}
			}
			else ownBombers++;
		}
		
		for (Ship frigate : GameInstance.getInstance().frigates) {
			if(frigate.id != this.id) {
				if(((Frigate) frigate).ai.target != null && ((Frigate) frigate).ai.target.id == this.id) {
					enemyFrigates++;
				}
			}
			else ownFrigates++;
		}
		
		// what to do
		if(resourceAmount < 80) action = -1;
		else if(ownFighters < 5) action = 0;
		else if(ownBombers < 4) action = 1;
		else if(ownFrigates < 3) action = 2;
		else action = 3;
	}

}

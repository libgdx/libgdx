package de.swagner.paxbritannica.fighter;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import de.swagner.paxbritannica.Ship;
import de.swagner.paxbritannica.Targeting;

public class FighterAI {
	// shot range
	private float shot_range = 200;

	// try to stay this far away when you're out of ammo
	private float run_distance = 200;

	// true when we've shot everything and want to make a distance, false means
	// we're approaching to attack
	private boolean running = false;

	public Ship target;
	private boolean on_screen = true;
	
	//recycle vars
	Vector2 to_target = new Vector2();

	private Fighter fighter;

	public FighterAI(Fighter fighter) {
		this.fighter = fighter;
		retarget();
	}

	public void retarget() {
		target = Targeting.getNearestOfType(fighter, 1);
		if (target == null) {
			target = Targeting.getNearestOfType(fighter, 0);
		}
		if (target == null) {
			target = Targeting.getNearestOfType(fighter, 2);
		}
		if (target == null) {
			target = Targeting.getNearestOfType(fighter, 3);
		}
	}

	public void update() {
		// if we go from on to off screen, retarget
		boolean new_on_screen = Targeting.onScreen(fighter.collisionCenter);
		if (on_screen && !new_on_screen || target == null || !target.alive || MathUtils.random() < 0.005f) {
			retarget();
		}
		on_screen = new_on_screen;

		if (target != null) {
			to_target.set(target.collisionCenter.x - fighter.collisionCenter.x, target.collisionCenter.y - fighter.collisionCenter.y);
			float dist_squared = to_target.dot(to_target);

			if (running) {
				// run away until you have full ammo and are far enough away
				boolean too_close = dist_squared < Math.pow(run_distance, 2);
				// if you're too close to the target then turn away
				if (too_close) {
					fighter.goAway(target.collisionCenter, true);
				} else {
					fighter.thrust();
				}

				if (!fighter.isEmpty() && !too_close) {
					running = false;
				}
			} else {
				// go towards the target and attack!
				fighter.goTowards(target.collisionCenter, true);

				// maybe shoot
				if (fighter.isReadyToShoot()) {
					if (dist_squared <= shot_range * shot_range && to_target.dot(fighter.facing) > 0 && Math.pow(to_target.dot(fighter.facing), 2) > 0.97 * dist_squared) {
						fighter.shoot();
					}
				}

				// if out of shots then run away
				if (fighter.isEmpty()) {
					running = true;
				}
			}
		}
	}
}

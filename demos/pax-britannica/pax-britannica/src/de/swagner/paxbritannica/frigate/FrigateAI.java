package de.swagner.paxbritannica.frigate;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import de.swagner.paxbritannica.Ship;
import de.swagner.paxbritannica.Targeting;

public class FrigateAI {
	private Vector2 target_fuzzy_pos = new Vector2();
	private boolean stopping = false;
	
	public Ship target;

	private Frigate frigate;

	public FrigateAI(Frigate frigate) {
		this.frigate = frigate;
	}

	public void retarget() {
		target = Targeting.getNearestOfType(frigate, 0);
		if (target == null) {
			target = Targeting.getNearestOfType(frigate, 1);
		}
		if (target == null) {
			target = Targeting.getNearestOfType(frigate, 2);
		}
		if (target == null) {
			target = Targeting.getNearestOfType(frigate, 3);
		}	
		
		if (target != null) {
			Vector2 random = new Vector2(MathUtils.cos((float) ((MathUtils.random() * MathUtils.PI * 2) * Math.sqrt(MathUtils.random()))),
										MathUtils.sin((float) ((MathUtils.random() * MathUtils.PI * 2) * Math.sqrt(MathUtils.random()))));
			target_fuzzy_pos.set(target.collisionCenter).add(random.scl(250));
		}
	}

	public void update() {
		if (target == null || !target.alive || MathUtils.random() < 0.001f) {
			retarget();
		}

		if (target != null) {
			float target_distance = target.collisionCenter.dst(frigate.collisionCenter);
			float speed_square = frigate.velocity.dot(frigate.velocity);

			if (frigate.isReadyToShoot() && speed_square > 0) {
		      stopping = true;
			} else if(frigate.isEmpty()) {
		      stopping = false;
			}

		    if(!stopping) {
		      if(target_distance < 150) {
		        //not too close!
		        frigate.goAway(target_fuzzy_pos, true);
		      } else {
		        frigate.goTowards(target_fuzzy_pos, true);
		      }
		    }
		    
		    // Shoot when not moving and able to fire
		    if(!frigate.isEmpty() && speed_square < 0.1) {
		        frigate.shoot();
		    }
			
		}
	}
}


package com.badlydrawngames.veryangryrobots.mobiles;

import com.badlydrawngames.general.Config;
import com.badlydrawngames.veryangryrobots.Assets;

public class PlayerShot extends BaseShot {

	private static final float SHOT_SPEED = Config.asFloat("PlayerShot.speed", 31.25f);

	public PlayerShot () {
		width = Assets.playerShotWidth;
		height = Assets.playerShotHeight;
		setShotSpeed(SHOT_SPEED);
	}
}

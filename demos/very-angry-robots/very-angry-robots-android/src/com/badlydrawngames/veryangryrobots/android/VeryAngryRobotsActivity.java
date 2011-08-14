
package com.badlydrawngames.veryangryrobots.android;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlydrawngames.veryangryrobots.VeryAngryRobotsGame;

public class VeryAngryRobotsActivity extends AndroidApplication {
	/** Called when the activity is first created. */
	@Override
	public void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		config.useCompass = false;
		config.useAccelerometer = false;
		config.useGL20 = false;
		VeryAngryRobotsGame game = new VeryAngryRobotsGame();
		initialize(game, config);
	}
}

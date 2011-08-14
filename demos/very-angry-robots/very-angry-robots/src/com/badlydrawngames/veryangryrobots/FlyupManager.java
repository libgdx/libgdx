
package com.badlydrawngames.veryangryrobots;

import com.badlogic.gdx.utils.Array;

public class FlyupManager implements ScoringEventListener {

	private static final int MAX_FLYUPS = 16;

	final Array<Flyup> flyups;
	private int index;

	public FlyupManager () {
		flyups = new Array<Flyup>(MAX_FLYUPS);
		for (int i = 0; i < MAX_FLYUPS; i++) {
			flyups.add(new Flyup());
		}
		index = 0;
	}

	@Override
	public void onScoringEvent (float x, float y, int points) {
		flyups.get(index).spawn(x, y, points);
		if (++index == MAX_FLYUPS) {
			index = 0;
		}
	}

	public void update (float delta) {
		for (Flyup flyup : flyups) {
			if (flyup.active) {
				flyup.update(delta);
			}
		}
	}
}

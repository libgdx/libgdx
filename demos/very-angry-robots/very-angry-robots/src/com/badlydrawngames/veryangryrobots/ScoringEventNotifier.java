
package com.badlydrawngames.veryangryrobots;

import com.badlogic.gdx.utils.Array;

public class ScoringEventNotifier implements ScoringEventListener {

	private final Array<ScoringEventListener> listeners;

	public ScoringEventNotifier () {
		listeners = new Array<ScoringEventListener>();
	}

	public void addListener (ScoringEventListener listener) {
		listeners.add(listener);
	}

	@Override
	public void onScoringEvent (float x, float y, int points) {
		for (ScoringEventListener listener : listeners) {
			listener.onScoringEvent(x, y, points);
		}
	}
}

/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

 
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

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

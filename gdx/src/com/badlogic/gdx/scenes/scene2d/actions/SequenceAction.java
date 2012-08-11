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

package com.badlogic.gdx.scenes.scene2d.actions;

/** Executes a number of actions one at a time.
 * @author Nathan Sweet */
public class SequenceAction extends ParallelAction {
	private int index;

	public boolean act (float delta) {
		if (index >= actions.size) return true;
		if (actions.get(index).act(delta)) {
			index++;
			if (index > actions.size) return true;
		}
		return false;
	}

	public void restart () {
		super.restart();
		index = 0;
	}
}

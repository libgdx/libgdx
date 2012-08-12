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

import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;

/** Executes a number of actions at the same time.
 * @author Nathan Sweet */
public class ParallelAction extends Action {
	Array<Action> actions = new Array(4);
	private boolean complete;

	public boolean act (float delta) {
		if (complete) return true;
		complete = true;
		Array<Action> actions = this.actions;
		for (int i = 0, n = actions.size; i < n; i++)
			if (!actions.get(i).act(delta)) complete = false;
		return complete;
	}

	public void restart () {
		complete = false;
		Array<Action> actions = this.actions;
		for (int i = 0, n = actions.size; i < n; i++)
			actions.get(i).restart();
	}

	public void reset () {
		super.reset();
		actions.clear();
	}

	public void addAction (Action action) {
		actions.add(action);
		if (actor != null) action.setActor(actor);
	}

	public void setActor (Actor actor) {
		Array<Action> actions = this.actions;
		for (int i = 0, n = actions.size; i < n; i++)
			actions.get(i).setActor(actor);
		super.setActor(actor);
	}

	public Array<Action> getActions () {
		return actions;
	}

	public String toString () {
		StringBuilder buffer = new StringBuilder(64);
		buffer.append(super.toString());
		buffer.append('(');
		Array<Action> actions = this.actions;
		for (int i = 0, n = actions.size; i < n; i++) {
			if (i > 0) buffer.append(", ");
			buffer.append(actions.get(i));
		}
		buffer.append(')');
		return buffer.toString();
	}
}

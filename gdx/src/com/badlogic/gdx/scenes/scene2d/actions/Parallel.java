/*
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com), Nathan Sweet (admin@esotericsoftware.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.badlogic.gdx.scenes.scene2d.actions;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Pool;

public class Parallel extends Action {
	static final Pool<Parallel> pool = new Pool<Parallel>(false, 4, 100) {
		protected Parallel newObject () {
			return new Parallel();
		}
	};

	public static Parallel $ (Action... actions) {
		Parallel action = pool.add();
		action.actions.clear();
		int len = actions.length;
		for (int i = 0; i < len; i++)
			action.actions.add(actions[i]);
		return action;
	}

	protected final List<Action> actions = new ArrayList<Action>();

	@Override public void setTarget (Actor actor) {
		int len = actions.size();
		for (int i = 0; i < len; i++)
			actions.get(i).setTarget(actor);
	}

	@Override public void act (float delta) {
		int len = actions.size();
		for (int i = 0; i < len; i++)
			if (!actions.get(i).isDone()) actions.get(i).act(delta);
	}

	@Override public boolean isDone () {
		int len = actions.size();
		for (int i = 0; i < len; i++)
			if (actions.get(i).isDone() == false) return false;
		return true;
	}

	@Override public void finish () {
		pool.removeValue(this, true);
		int len = 0;
		for (int i = 0; i < len; i++)
			actions.get(i).finish();
		if(listener != null)
			listener.completed(this);
	}

	@Override public Action copy () {
		Parallel action = pool.add();
		action.actions.clear();
		int len = actions.size();
		for (int i = 0; i < len; i++)
			action.actions.add(actions.get(i).copy());
		return action;
	}
}

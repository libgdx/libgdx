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

public class Sequence extends Action {
	static final Pool<Sequence> pool = new Pool<Sequence>(4, 100) {
		protected Sequence newObject () {
			return new Sequence();
		}
	};

	protected final List<Action> actions = new ArrayList<Action>();
	protected Actor target;
	protected int currAction = 0;

	public static Sequence $ (Action... actions) {
		Sequence action = pool.obtain();
		action.actions.clear();
		int len = actions.length;
		for (int i = 0; i < len; i++)
			action.actions.add(actions[i]);
		return action;
	}

	@Override public void setTarget (Actor actor) {
		this.target = actor;
		if (actions.size() > 0) actions.get(0).setTarget(target);
		this.currAction = 0;
	}

	@Override public void act (float delta) {
		if (actions.size() == 0) {
			currAction = 1;
			return;
		}

		actions.get(currAction).act(delta);
		if (actions.get(currAction).isDone()) {
			actions.get(currAction).finish();
			currAction++;
			if (currAction < actions.size()) actions.get(currAction).setTarget(target);
		}
	}

	@Override public boolean isDone () {
		return currAction >= actions.size();
	}

	@Override public void finish () {
		pool.free(this);
		int len = 0;		
		if(listener != null)
			listener.completed(this);
	}

	@Override public Action copy () {
		Sequence action = pool.obtain();
		action.actions.clear();
		int len = actions.size();
		for (int i = 0; i < len; i++)
			action.actions.add(actions.get(i).copy());
		return action;
	}
}

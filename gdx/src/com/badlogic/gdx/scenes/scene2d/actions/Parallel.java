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
import com.badlogic.gdx.scenes.scene2d.CompositeAction;

public class Parallel extends CompositeAction {

	static final ActionResetingPool<Parallel> pool = new ActionResetingPool<Parallel>(4, 100) {
		@Override
		protected Parallel newObject () {
			return new Parallel();
		}
	};

	protected boolean[] finished;
	protected Actor target = null;

	public static Parallel $ (Action... actions) {
		Parallel parallel = pool.obtain();
		parallel.actions.clear();
		if (parallel.finished == null || parallel.finished.length < actions.length)
			parallel.finished = new boolean[actions.length];
		int len = actions.length;
		for (int i = 0; i < len; i++)
			parallel.finished[i] = false;
		len = actions.length;
		for (int i = 0; i < len; i++)
			parallel.actions.add(actions[i]);
		return parallel;
	}

	@Override
	public void setTarget (Actor actor) {
		this.target = actor;
		int len = actions.size();
		for (int i = 0; i < len; i++)
			actions.get(i).setTarget(actor);
	}

	@Override
	public void act (float delta) {
		int len = actions.size();
		boolean allDone = true;
		Action action;
		for (int i = 0; i < len; i++) {
			action = actions.get(i);
			if (!action.isDone()) {
				action.act(delta);
				allDone = false;
			} else {
				if (!finished[i]) {
					action.finish();
					finished[i] = true;
					allDone &= finished[i];
				}
			}
		}
		if (allDone) callActionCompletedListener();
	}

	@Override
	public boolean isDone () {
		int len = actions.size();
		for (int i = 0; i < len; i++)
			if (actions.get(i).isDone() == false) return false;
		return true;
	}

	@Override
	public void finish () {
		pool.free(this);
		int len = actions.size();
		for (int i = 0; i < len; i++) {
			if (!finished[i]) actions.get(i).finish();
		}
		super.finish();
	}

	@Override
	public Action copy () {
		Parallel parallel = pool.obtain();
		parallel.actions.clear();
		if (parallel.finished == null || parallel.finished.length < actions.size())
			parallel.finished = new boolean[actions.size()];
		int len = actions.size();
		for (int i = 0; i < len; i++)
			parallel.finished[i] = false;
		len = actions.size();
		for (int i = 0; i < len; i++)
			parallel.actions.add(actions.get(i).copy());
		return parallel;
	}

	@Override
	public Actor getTarget () {
		return target;
	}
}

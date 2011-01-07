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

import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Pool;

public class Forever extends Action {
	static final Pool<Forever> pool = new Pool<Forever>(false, 4, 100) {
		protected Forever newObject () {
			return new Forever();
		}
	};

	protected Action action;
	protected Actor target;

	public static Forever $ (Action action) {
		Forever forever = pool.add();
		forever.action = action;
		return forever;
	}

	@Override public void setTarget (Actor actor) {
		action.setTarget(actor);
		target = actor;
	}

	@Override public void act (float delta) {
		action.act(delta);
		if (action.isDone()) {
			Action oldAction = action;
			action = action.copy();
			oldAction.finish();
			action.setTarget(target);
		}
	}

	@Override public boolean isDone () {
		return false;
	}

	@Override public void finish () {
		pool.removeValue(this, true);
		action.finish();
		if(listener != null)
			listener.completed(this);
	}

	@Override public Action copy () {
		return $(action.copy());
	}

}

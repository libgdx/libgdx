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

public class Delay extends Action {
	static final Pool<Delay> pool = new Pool<Delay>(false, 4, 100) {
		protected Delay newObject () {
			return new Delay();
		}
	};

	protected float taken;
	protected float duration;
	protected Action action;

	public static Delay $ (Action action, float duration) {
		Delay delay = pool.add();
		delay.duration = duration;
		delay.action = action;
		return delay;
	}

	@Override public void setTarget (Actor actor) {
		action.setTarget(actor);
		this.taken = 0;
	}

	@Override public void act (float delta) {
		taken += delta;
		if (taken > duration) action.act(delta);
	}

	@Override public boolean isDone () {
		return taken > duration && action.isDone();
	}

	@Override public void finish () {
		pool.removeValue(this, true);
		if(listener != null)
			listener.completed(this);
	}

	@Override public Action copy () {
		return $(action.copy(), duration);
	}
}

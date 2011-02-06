/*
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com), Nathan Sweet (admin@esotericsoftware.com), Moritz Post
 * (moritzpost@gmail.com)
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
import com.badlogic.gdx.scenes.scene2d.AnimationAction;
import com.badlogic.gdx.utils.Pool;

public class FadeTo extends AnimationAction {

	private static final Pool<FadeTo> pool = new Pool<FadeTo>(4, 100) {
		@Override protected FadeTo newObject () {
			return new FadeTo();
		}
	};

	protected float toAlpha = 0;
	protected float startAlpha;
	protected float deltaAlpha = 0;

	public static FadeTo $ (float alpha, float duration) {
		FadeTo action = pool.obtain();
		action.toAlpha = Math.min(Math.max(alpha, 0.0f), 1.0f);
		action.duration = duration;
		action.invDuration = 1 / duration;
		action.listener = null;
		return action;
	}

	@Override public void setTarget (Actor actor) {
		this.target = actor;
		this.startAlpha = this.target.color.a;
		this.deltaAlpha = toAlpha - this.target.color.a;
		this.taken = 0;
		this.done = false;
	}

	@Override public void act (float delta) {
		float alpha = createInterpolatedAlpha(delta);
		if (done) {
			target.color.a = toAlpha;
		} else {
			float val = startAlpha + deltaAlpha * alpha;
			target.color.a = Math.min(Math.max(val, 0.0f), 1.0f);
		}
	}

	@Override public void finish () {
		super.finish();
		pool.free(this);
	}

	@Override public Action copy () {
		return $(toAlpha, duration);
	}
}

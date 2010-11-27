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
import com.badlogic.gdx.utils.Pool.PoolObjectFactory;

public class FadeTo implements Action {
	static final Pool<FadeTo> pool = new Pool<FadeTo>(new PoolObjectFactory<FadeTo>() {
		@Override public FadeTo createObject () {
			return new FadeTo();
		}
	}, 100);

	protected float toAlpha = 0;
	protected float startAlpha;
	protected float deltaAlpha = 0;
	protected float duration;
	protected float invDuration;
	protected float taken = 0;
	protected Actor target;
	protected boolean done;

	public static FadeTo $ (float alpha, float duration) {
		FadeTo action = pool.newObject();
		if (alpha < 0) alpha = 0;
		if (alpha > 1) alpha = 1;
		action.toAlpha = alpha;
		action.duration = duration;
		action.invDuration = 1 / duration;
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
		taken += delta;
		if (taken >= duration) {
			taken = duration;
			done = true;
		}

		float alpha = taken * invDuration;
		target.color.a = startAlpha + deltaAlpha * alpha;
	}

	@Override public boolean isDone () {
		return done;
	}

	@Override public void finish () {
		pool.free(this);
	}

	@Override public Action copy () {
		return $(toAlpha, duration);
	}
}

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

public class ScaleTo extends AnimationAction {

	private static final Pool<ScaleTo> pool = new Pool<ScaleTo>(4, 100) {
		@Override protected ScaleTo newObject () {
			return new ScaleTo();
		}
	};

	protected float scaleX;
	protected float scaleY;
	protected float startScaleX;
	protected float startScaleY;
	protected float deltaScaleX;
	protected float deltaScaleY;

	public static AnimationAction $ (float scaleX, float scaleY, float duration) {
		ScaleTo action = pool.obtain();
		action.scaleX = scaleX;
		action.scaleY = scaleY;
		action.duration = duration;
		action.invDuration = 1 / duration;
		action.listener = null;
		return action;
	}

	@Override public void setTarget (Actor actor) {
		this.target = actor;
		this.startScaleX = target.scaleX;
		this.deltaScaleX = scaleX - target.scaleX;
		this.startScaleY = target.scaleY;
		this.deltaScaleY = scaleY - target.scaleY;
		this.taken = 0;
		this.done = false;
	}

	@Override public void act (float delta) {
		float alpha = createInterpolatedAlpha(delta);
		if (done) {
			target.scaleX = scaleX;
			target.scaleY = scaleY;
		} else {
			target.scaleX = startScaleX + deltaScaleX * alpha;
			target.scaleY = startScaleY + deltaScaleY * alpha;
		}
	}

	@Override public void finish () {
		super.finish();
		pool.free(this);
	}

	@Override public Action copy () {
		return $(scaleX, scaleY, duration);
	}
}

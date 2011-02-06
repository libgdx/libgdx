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

public class FadeOut extends AnimationAction {

	private static final Pool<FadeOut> pool = new Pool<FadeOut>(4, 100) {
		@Override protected FadeOut newObject () {
			return new FadeOut();
		}
	};

	protected float startAlpha = 0;
	protected float deltaAlpha = 0;

	public static FadeOut $ (float duration) {
		FadeOut action = pool.obtain();
		action.duration = duration;
		action.invDuration = 1 / duration;
		action.listener = null;
		return action;
	}

	@Override public void setTarget (Actor actor) {
		this.target = actor;
		this.target.color.a = 1;
		this.startAlpha = 1;
		this.deltaAlpha = -1;
		this.taken = 0;
		this.done = false;
	}

	@Override public void act (float delta) {
		float alpha = createInterpolatedAlpha(delta);
		if (done) {
			target.color.a = 0.0f;
		} else {
			target.color.a = startAlpha + deltaAlpha * alpha;
		}
	}

	@Override public void finish () {
		super.finish();
		pool.free(this);
	}

	@Override public Action copy () {
		return $(duration);
	}
}

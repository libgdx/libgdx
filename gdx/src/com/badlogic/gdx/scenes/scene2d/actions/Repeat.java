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
import com.badlogic.gdx.scenes.scene2d.TemporalAction;

public class Repeat extends TemporalAction {
	static final ActionResetingPool<Repeat> pool = new ActionResetingPool<Repeat>(4, 100) {
		@Override protected Repeat newObject () {
			return new Repeat();
		}
	};

	protected int times;
	protected int finishedTimes;

	public static Repeat $ (Action action, int times) {
		Repeat repeat = pool.obtain();
		repeat.action = action;
		repeat.times = times;
		return repeat;
	}

	@Override public void reset () {
		super.reset();
		finishedTimes = 0;
		listener = null;
	}

	@Override public void setTarget (Actor actor) {
		action.setTarget(actor);
		target = actor;
	}

	@Override public void act (float delta) {
		action.act(delta);
		if (action.isDone()) {
			finishedTimes++;
			if (finishedTimes < times) {
				Action oldAction = action;
				action = action.copy();
				oldAction.finish();
				action.setTarget(target);
			}
		}
	}

	@Override public boolean isDone () {
		return finishedTimes >= times;
	}

	@Override public void finish () {
		pool.free(this);
		action.finish();
		super.finish();
	}

	@Override public Action copy () {
		return $(action.copy(), times);
	}

	@Override public Actor getTarget () {	
		return target;
	}
}

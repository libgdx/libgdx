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

/** Removes an action from an actor.
 * @author Nathan Sweet */
public class RemoveAction extends Action {
	private Actor targetActor;
	private Action action;

	public boolean act (float delta) {
		(targetActor != null ? targetActor : actor).removeAction(action);
		return true;
	}

	public Actor getTargetActor () {
		return targetActor;
	}

	/** Sets the actor to remove an action from. If null (the default), the {@link #getActor() actor} will be used. */
	public void setTargetActor (Actor actor) {
		this.targetActor = actor;
	}

	public Action getAction () {
		return action;
	}

	public void setAction (Action action) {
		this.action = action;
	}

	public void reset () {
		super.reset();
		targetActor = null;
		action = null;
	}
}

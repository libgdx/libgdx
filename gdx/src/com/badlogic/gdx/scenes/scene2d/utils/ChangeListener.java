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

package com.badlogic.gdx.scenes.scene2d.utils;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;

/** Listener for {@link ChangeEvent}.
 * @author Nathan Sweet */
abstract public class ChangeListener implements EventListener {
	public boolean handle (Event event) {
		if (!(event instanceof ChangeEvent)) return false;
		changed((ChangeEvent)event, event.getTarget());
		return false;
	}

	/** @param actor The event target, which is the actor that emitted the change event. */
	abstract public void changed (ChangeEvent event, Actor actor);

	/** Fired when something in an actor has changed. This is a generic event, exactly what changed in an actor will vary.
	 * @author Nathan Sweet */
	static public class ChangeEvent extends Event {
	}
}

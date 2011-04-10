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

public class Remove extends Action {
	private static final ActionResetingPool<Remove> pool = new ActionResetingPool<Remove>(4, 100) {
		@Override protected Remove newObject () {
			return new Remove();
		}
	};
		
	protected Actor target;
	protected boolean removed = false;	
	
	static public Remove $() {
		Remove remove = pool.obtain();
		remove.removed = false;
		remove.target = null;
		return remove;
	}
	
	@Override public void setTarget (Actor actor) {
		this.target = actor;
	}

	@Override public void act (float delta) {
		if(!removed) {
			target.markToRemove(true);
			removed = true;
		}
	}

	@Override public boolean isDone () {
		return removed;
	}

	@Override public Action copy () {
		return $();
	}

}

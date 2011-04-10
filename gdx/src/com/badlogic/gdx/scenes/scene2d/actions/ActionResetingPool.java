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
import com.badlogic.gdx.utils.Pool;

/**
 * A pool taking care of the {@link Action} life cycle and resets all its properties when obtained from this pool.
 * 
 * @author Moritz Post <moritzpost@gmail.com>
 * @param <T> the type action to manage
 */
abstract class ActionResetingPool<T extends Action> extends Pool<T> {

	public ActionResetingPool (int initialCapacity, int max) {
		super(initialCapacity, max);
	}

	@Override public T obtain () {
		T elem = super.obtain();
		elem.reset();
		return elem;
	}
}

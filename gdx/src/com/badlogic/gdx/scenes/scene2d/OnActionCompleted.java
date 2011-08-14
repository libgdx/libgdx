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

package com.badlogic.gdx.scenes.scene2d;

import com.badlogic.gdx.scenes.scene2d.actions.Repeat;

/** Listener interface called by an {@link Action} when it was completed. Use this with
 * {@link Action#setCompletionListener(OnActionCompleted)};
 * @author mzechner */
public interface OnActionCompleted {
	/** Called when the {@link Action} is completed. Note that this might get called multiple times in case the Action is part of a
	 * {@link Repeat} Action. If the Action is an {@link AnimationAction} then you can cast to this class and receive the targeted
	 * {link Actor} via {@link AnimationAction#getTarget()}.
	 * @param action the Action. */
	public void completed (Action action);
}

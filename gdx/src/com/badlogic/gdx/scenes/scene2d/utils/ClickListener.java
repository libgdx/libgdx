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

import com.badlogic.gdx.scenes.scene2d.InputEvent;

/** Detects a click on an actor. The touch must go down over the actor and go up over the actor or within the
 * {@link #setTapSquareSize(float) tap square} for the click to occur.
 * @author Nathan Sweet */
abstract public class ClickListener extends PressedListener {
	public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
		boolean validClick = isOver(event.getListenerActor(), x, y);
		if (validClick && pointer == 0 && button != getButton()) validClick = false;
		if (validClick) clicked(event, x, y);
		super.touchUp(event, x, y, pointer, button);
	}

	abstract public void clicked (InputEvent event, float x, float y);
}

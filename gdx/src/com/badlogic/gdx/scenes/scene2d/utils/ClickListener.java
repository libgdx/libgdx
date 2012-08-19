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
import com.badlogic.gdx.utils.TimeUtils;

/** Detects a click on an actor. The touch must go down over the actor and go up over the actor or within the
 * {@link #setTapSquareSize(float) tap square} for the click to occur. Double clicks can be detected using {@link #getTapCount()}.
 * @author Nathan Sweet */
abstract public class ClickListener extends PressedListener {
	private long tapCountInterval = (long)(0.4f * 1000000000l);
	private int tapCount;
	private long lastTapTime;

	public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
		boolean validClick = isOver(event.getListenerActor(), x, y);
		if (validClick && pointer == 0 && button != getButton()) validClick = false;
		if (validClick) {
			long time = TimeUtils.nanoTime();
			if (time - lastTapTime > tapCountInterval) tapCount = 0;
			tapCount++;
			lastTapTime = time;
			clicked(event, x, y);
		}
		super.touchUp(event, x, y, pointer, button);
	}

	abstract public void clicked (InputEvent event, float x, float y);

	/** @param tapCountInterval time in seconds that must pass for two touch down/up sequences to be detected as consecutive taps. */
	public void setTapCountInterval (float tapCountInterval) {
		this.tapCountInterval = (long)(tapCountInterval * 1000000000l);
	}

	/** Returns the number of taps within the tap count interval for the most recent click event. */
	public int getTapCount () {
		return tapCount;
	}
}

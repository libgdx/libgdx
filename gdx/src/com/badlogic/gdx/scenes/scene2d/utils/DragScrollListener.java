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
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;

/** Causes a scroll pane to scroll when a drag goes outside the bounds of the scroll pane.
 * @author Nathan Sweet */
public class DragScrollListener extends DragListener {
	private ScrollPane scroll;
	private Task scrollUp, scrollDown;

	public DragScrollListener (final ScrollPane scroll) {
		this.scroll = scroll;

		scrollUp = new Task() {
			public void run () {
				scroll.setScrollY(scroll.getScrollY() - 15);
			}
		};
		scrollDown = new Task() {
			public void run () {
				scroll.setScrollY(scroll.getScrollY() + 15);
			}
		};
	}

	public void drag (InputEvent event, float x, float y, int pointer) {
		if (y >= scroll.getHeight()) {
			scrollDown.cancel();
			if (!scrollUp.isScheduled()) Timer.schedule(scrollUp, 0.05f, 0.05f);
		} else if (y < 0) {
			scrollUp.cancel();
			if (!scrollDown.isScheduled()) Timer.schedule(scrollDown, 0.05f, 0.05f);
		} else {
			scrollUp.cancel();
			scrollDown.cancel();
		}
	}

	public void dragStop (InputEvent event, float x, float y, int pointer) {
		scrollUp.cancel();
		scrollDown.cancel();
	}
}

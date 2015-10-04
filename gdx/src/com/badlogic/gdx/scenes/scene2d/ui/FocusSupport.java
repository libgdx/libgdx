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

package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;

/**
 * Utilities for {@link Focusable} actors
 *
 * @author arcnor
 */
public final class FocusSupport {
	private FocusSupport() {}

	public static <T extends Actor & Focusable> T getNextFocusActor(Focusable focusable) {
		T result = focusable.getNextKeyFocusActor();
		while (result != null && result.isDisabled()) {
			result = result.getNextKeyFocusActor();
		}
		return result;
	}
	public static <T extends Actor & Focusable> T getPreviousFocusActor(Focusable focusable) {
		T result = focusable.getPreviousKeyFocusActor();
		while (result != null && result.isDisabled()) {
			result = result.getPreviousKeyFocusActor();
		}
		return result;
	}
}

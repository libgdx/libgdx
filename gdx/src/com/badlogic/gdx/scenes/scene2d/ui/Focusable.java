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
import com.badlogic.gdx.scenes.scene2d.utils.Disableable;

/**
 * Actors that extend this are capable of being focused and receive keyboard input
 *
 * @author arcnor
 */
public interface Focusable extends Disableable {
	/**
	 * Returns if focusing is disabled for this instance
	 */
	boolean isFocusTraversal();
	/**
	 * Enables or disables focusing for this instance
	 */
	void setFocusTraversal(boolean focusTraversal);

	/**
	 * Returns the actor that will be focused after pressing the "focus next actor" shortcut (i.e. TAB)
	 */
	<T extends Actor & Focusable> T getNextKeyFocusActor();

	/**
	 * Sets the actor that will be focused after pressing the "focus next actor" shortcut (i.e. TAB)
	 *
	 * @see Focusable#chainKeyFocusActor(Actor)
	 */
	<T extends Actor & Focusable> void setNextKeyFocusActor(T actor);
	/**
	 * Returns the actor that will be focused after pressing the "focus previous actor" shortcut (i.e. SHIFT+TAB)
	 */
	<T extends Actor & Focusable> T getPreviousKeyFocusActor();
	/**
	 * Sets the actor that will be focused after pressing the "focus previous actor" shortcut (i.e. SHIFT+TAB)
	 *
	 * @see Focusable#chainKeyFocusActor(Actor)
	 */
	<T extends Actor & Focusable> void setPreviousKeyFocusActor(T actor);

	/**
	 * Modifies this and {@code nextActor} to be chained. The {@code nextKeyFocusActor} for this instance will be {@code nextActor},
	 * and the {@code previousKeyFocusActor} for {@code nextActor} will be this instance
	 */
	<T extends Actor & Focusable> void chainKeyFocusActor(T nextActor);
}

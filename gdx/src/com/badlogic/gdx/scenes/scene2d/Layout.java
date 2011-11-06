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

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/** Provides methods for an actor to participate in layout and to provide a minimum, preferred, and maximum size.
 * @author Nathan Sweet */
public interface Layout {
	/** Computes and caches any information needed for drawing and, if this actor has children, positions and sizes each child.
	 * Usually this should not be called directly, instead {@link #validate()} should be used. */
	public void layout ();

	/** Invalidates this actor's layout, causing {@link #layout()} to be called the next time {@link #validate()} is called. This
	 * should be done when state changes in the actor that requires a layout but does not change the minimum, preferred, maximum,
	 * or actual size of the actor (meaning it does not affect the parent actor's layout). */
	public void invalidate ();

	/** Invalidates this actor and all its parents, calling {@link #invalidate()} on all involved actors. This method should be
	 * called when state changes in the actor that affects the minimum, preferred, maximum, or actual size of the actor (meaning it
	 * potentially affects the parent actor's layout). */
	public void invalidateHierarchy ();

	/** Ensures the actor has been laid out. Calls {@link #layout()} if {@link #invalidate()} has called since the last time
	 * {@link #validate()} was called. This method is usually called in {@link Actor#draw(SpriteBatch, float)} before drawing is
	 * performed. */
	public void validate ();

	/** Sizes this actor to its preferred width and height and, if its size was changed, calls {@link #invalidate()}. */
	public void pack ();

	public float getMinWidth ();

	public float getMinHeight ();

	public float getPrefWidth ();

	public float getPrefHeight ();

	public float getMaxWidth ();

	public float getMaxHeight ();
}

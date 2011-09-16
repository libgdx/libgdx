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

public interface Layout {
	/** Positions and sizes each child of this actor. Subsequent calls will not have any affect unless {@link #invalidate()} is
	 * called. */
	public void layout ();

	/** Invalidates the layout, forcing the next call to {@link #layout()} to relayout. If an actor is resized or otherwise changed
	 * in a way that affects its layout, {@link #invalidate()} should be called. */
	public void invalidate ();

	public float getMinWidth ();
	
	public float getMinHeight ();
	
	public float getPrefWidth ();

	public float getPrefHeight ();
	
	public float getMaxWidth ();
	
	public float getMaxHeight ();
}

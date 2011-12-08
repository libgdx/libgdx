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

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Layout;

/** An {@link Actor} that participates in layout and provides a minimum, preferred, and maximum size.
 * <p>
 * The default preferred size of a widget is 0 and this is almost always overridden by a subclass. The default minimum size
 * returns the preferred size, so a subclass may choose to return 0 if it wants to allow itself to be sized smaller. The default
 * maximum size is 0, which means no maximum size.
 * <p>
 * See {@link Layout} for details on how a widget should participate in layout. A widget's mutator methods should call
 * {@link #invalidate()} or {@link #invalidateHierarchy()} as needed.
 * @author mzechner
 * @author Nathan Sweet */
public abstract class Widget extends Actor implements Layout {
	private boolean needsLayout = true;

	/** Creates a new widget without a name. */
	public Widget () {
		super(null);
	}

	public Widget (String name) {
		super(name);
	}

	public float getMinWidth () {
		return getPrefWidth();
	}

	public float getMinHeight () {
		return getPrefHeight();
	}

	public float getPrefWidth () {
		return 0;
	}

	public float getPrefHeight () {
		return 0;
	}

	public float getMaxWidth () {
		return 0;
	}

	public float getMaxHeight () {
		return 0;
	}

	public void invalidate () {
		needsLayout = true;
	}

	public void validate () {
		if (!needsLayout) return;
		needsLayout = false;
		layout();
	}

	/** Returns true if the widget's layout has been {@link #invalidate() invalidated}. */
	public boolean needsLayout () {
		return needsLayout;
	}

	public void invalidateHierarchy () {
		invalidate();
		if (parent instanceof Layout) ((Layout)parent).invalidateHierarchy();
	}

	public void pack () {
		float newWidth = getPrefWidth();
		float newHeight = getPrefHeight();
		if (newWidth != width || newHeight != height) {
			width = newWidth;
			height = newHeight;
			invalidate();
			validate();
		}
	}

	/** If this method is overridden, the super method or {@link #validate()} should be called to ensure the widget is laid out. */
	public void draw (SpriteBatch batch, float parentAlpha) {
		validate();
	}

	public Actor hit (float x, float y) {
		return x > 0 && x < width && y > 0 && y < height ? this : null;
	}

	public void layout () {
	}

	public boolean touchDown (float x, float y, int pointer) {
		return false;
	}

	public void touchUp (float x, float y, int pointer) {
	}

	public void touchDragged (float x, float y, int pointer) {
	}
}

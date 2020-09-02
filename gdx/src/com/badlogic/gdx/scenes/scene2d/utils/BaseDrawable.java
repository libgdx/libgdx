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

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.Null;
import com.badlogic.gdx.utils.reflect.ClassReflection;

/** Drawable that stores the size information but doesn't draw anything.
 * @author Nathan Sweet */
public class BaseDrawable implements Drawable {
	private @Null String name;
	private float leftWidth, rightWidth, topHeight, bottomHeight, minWidth, minHeight;

	public BaseDrawable () {
	}

	/** Creates a new empty drawable with the same sizing information as the specified drawable. */
	public BaseDrawable (Drawable drawable) {
		if (drawable instanceof BaseDrawable) name = ((BaseDrawable)drawable).getName();
		leftWidth = drawable.getLeftWidth();
		rightWidth = drawable.getRightWidth();
		topHeight = drawable.getTopHeight();
		bottomHeight = drawable.getBottomHeight();
		minWidth = drawable.getMinWidth();
		minHeight = drawable.getMinHeight();
	}

	public void draw (Batch batch, float x, float y, float width, float height) {
	}

	public float getLeftWidth () {
		return leftWidth;
	}

	public void setLeftWidth (float leftWidth) {
		this.leftWidth = leftWidth;
	}

	public float getRightWidth () {
		return rightWidth;
	}

	public void setRightWidth (float rightWidth) {
		this.rightWidth = rightWidth;
	}

	public float getTopHeight () {
		return topHeight;
	}

	public void setTopHeight (float topHeight) {
		this.topHeight = topHeight;
	}

	public float getBottomHeight () {
		return bottomHeight;
	}

	public void setBottomHeight (float bottomHeight) {
		this.bottomHeight = bottomHeight;
	}

	public void setPadding (float topHeight, float leftWidth, float bottomHeight, float rightWidth) {
		setTopHeight(topHeight);
		setLeftWidth(leftWidth);
		setBottomHeight(bottomHeight);
		setRightWidth(rightWidth);
	}

	public float getMinWidth () {
		return minWidth;
	}

	public void setMinWidth (float minWidth) {
		this.minWidth = minWidth;
	}

	public float getMinHeight () {
		return minHeight;
	}

	public void setMinHeight (float minHeight) {
		this.minHeight = minHeight;
	}

	public void setMinSize (float minWidth, float minHeight) {
		setMinWidth(minWidth);
		setMinHeight(minHeight);
	}

	public @Null String getName () {
		return name;
	}

	public void setName (@Null String name) {
		this.name = name;
	}

	public @Null String toString () {
		if (name == null) return ClassReflection.getSimpleName(getClass());
		return name;
	}
}

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
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.Layout;
import com.badlogic.gdx.utils.SnapshotArray;

/** A group that lays out its children side by side in a single row. This can be easier than using {@link Table} when actors need
 * to be inserted in the middle of the group.
 * <p>
 * The preferred width is the sum of the children's preferred widths, plus spacing if set. The preferred height is the largest
 * preferred height of any child. The min size is the preferred size and the max size is 0 as <code>HorizontalGroup</code> can be
 * stretched to cover any area.
 * <p>
 * This UI widget does not support <code>Layout</code>able actors that return 0 as their preferred width. A fine example is
 * {@link Label} class with text wrapping turned on.
 * @author Nathan Sweet */
public class HorizontalGroup extends WidgetGroup {
	private float prefWidth, prefHeight;
	private boolean sizeInvalid = true;
	private int alignment;
	private boolean reverse, round = true;
	private float spacing;

	public HorizontalGroup () {
		setTouchable(Touchable.childrenOnly);
	}

	/** Sets the vertical alignment of the children. Default is center.
	 * @see Align */
	public void setAlignment (int alignment) {
		this.alignment = alignment;
	}

	/** If true, the children will be ordered from right to left rather than the default left to right. */
	public void setReverse (boolean reverse) {
		this.reverse = reverse;
	}

	public void invalidate () {
		super.invalidate();
		sizeInvalid = true;
	}

	private void computeSize () {
		sizeInvalid = false;
		SnapshotArray<Actor> children = getChildren();
		int n = children.size;
		prefWidth = spacing * (n - 1);
		prefHeight = 0;
		for (int i = 0; i < n; i++) {
			Actor child = children.get(i);
			if (child instanceof Layout) {
				Layout layout = (Layout)child;
				prefWidth += layout.getPrefWidth();
				prefHeight = Math.max(prefHeight, layout.getPrefHeight());
			} else {
				prefWidth += child.getWidth();
				prefHeight = Math.max(prefHeight, child.getHeight());
			}
		}
		if (round) {
			prefWidth = Math.round(prefWidth);
			prefHeight = Math.round(prefHeight);
		}
	}

	public void layout () {
		float spacing = this.spacing;
		float groupHeight = getHeight() > 0 ? getHeight() : getMinHeight();
		float x = reverse ? (getWidth() > 0 ? getWidth() : getMinWidth()) : 0;
		SnapshotArray<Actor> children = getChildren();
		for (int i = 0, n = children.size; i < n; i++) {
			Actor child = children.get(i);
			float width, height;
			if (child instanceof Layout) {
				Layout layout = (Layout)child;
				width = layout.getPrefWidth();
				height = layout.getPrefHeight();
			} else {
				width = child.getWidth();
				height = child.getHeight();
			}
			float y;
			if ((alignment & Align.bottom) != 0)
				y = 0;
			else if ((alignment & Align.top) != 0)
				y = groupHeight - height;
			else
				y = (groupHeight - height) / 2;
			if (reverse) x -= (width + spacing);
			child.setBounds(x, y, width, height);
			if (!reverse) x += (width + spacing);
		}
	}

	public float getPrefWidth () {
		if (sizeInvalid) computeSize();
		return prefWidth;
	}

	public float getPrefHeight () {
		if (sizeInvalid) computeSize();
		return prefHeight;
	}

	/** Sets the space between children. */
	public void setSpacing (float spacing) {
		this.spacing = spacing;
	}

	/** If true (the default), positions and sizes are rounded to integers. */
	public void setRound (boolean round) {
		this.round = round;
	}
}
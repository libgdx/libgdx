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
	private int align;
	private boolean reverse, round = true;
	private float spacing;
	private float padTop, padLeft, padBottom, padRight;
	private float fill;

	public HorizontalGroup () {
		setTouchable(Touchable.childrenOnly);
	}

	public void invalidate () {
		super.invalidate();
		sizeInvalid = true;
	}

	private void computeSize () {
		sizeInvalid = false;
		SnapshotArray<Actor> children = getChildren();
		int n = children.size;
		prefWidth = padLeft + padRight + spacing * (n - 1);
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
		prefHeight += padTop + padBottom;
		if (round) {
			prefWidth = Math.round(prefWidth);
			prefHeight = Math.round(prefHeight);
		}
	}

	public void layout () {
		float spacing = this.spacing, padBottom = this.padBottom;
		int align = this.align;
		boolean reverse = this.reverse, round = this.round;

		float groupHeight = getHeight() - padTop - padBottom;
		float x = !reverse ? padLeft : getWidth() - padRight + spacing;
		SnapshotArray<Actor> children = getChildren();
		for (int i = 0, n = children.size; i < n; i++) {
			Actor child = children.get(i);
			float width, height;
			if (child instanceof Layout) {
				Layout layout = (Layout)child;
				if (fill > 0)
					height = groupHeight * fill;
				else
					height = Math.min(layout.getPrefHeight(), groupHeight);
				height = Math.max(height, layout.getMinHeight());
				float maxHeight = layout.getMaxHeight();
				if (maxHeight > 0 && height > maxHeight) height = maxHeight;
				width = layout.getPrefWidth();
			} else {
				width = child.getWidth();
				height = child.getHeight();
				if (fill > 0) height *= fill;
			}

			float y = padBottom;
			if ((align & Align.top) != 0)
				y += groupHeight - height;
			else if ((align & Align.bottom) == 0) // center
				y += (groupHeight - height) / 2;

			if (reverse) x -= (width + spacing);
			if (round)
				child.setBounds(Math.round(x), Math.round(y), Math.round(width), Math.round(height));
			else
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

	/** If true (the default), positions and sizes are rounded to integers. */
	public void setRound (boolean round) {
		this.round = round;
	}

	/** The children will be ordered from right to left rather than the default left to right. */
	public HorizontalGroup reverse () {
		reverse(true);
		return this;
	}

	/** If true, the children will be ordered from right to left rather than the default left to right. */
	public HorizontalGroup reverse (boolean reverse) {
		this.reverse = reverse;
		return this;
	}

	public boolean getReverse () {
		return reverse;
	}

	/** Sets the space between children. */
	public HorizontalGroup space (float spacing) {
		this.spacing = spacing;
		return this;
	}

	public float getSpace () {
		return spacing;
	}

	/** Sets the padTop, padLeft, padBottom, and padRight to the specified value. */
	public HorizontalGroup pad (float pad) {
		padTop = pad;
		padLeft = pad;
		padBottom = pad;
		padRight = pad;
		return this;
	}

	public HorizontalGroup pad (float top, float left, float bottom, float right) {
		padTop = top;
		padLeft = left;
		padBottom = bottom;
		padRight = right;
		return this;
	}

	public HorizontalGroup padTop (float padTop) {
		this.padTop = padTop;
		return this;
	}

	public HorizontalGroup padLeft (float padLeft) {
		this.padLeft = padLeft;
		return this;
	}

	public HorizontalGroup padBottom (float padBottom) {
		this.padBottom = padBottom;
		return this;
	}

	public HorizontalGroup padRight (float padRight) {
		this.padRight = padRight;
		return this;
	}

	public float getPadTop () {
		return padTop;
	}

	public float getPadLeft () {
		return padLeft;
	}

	public float getPadBottom () {
		return padBottom;
	}

	public float getPadRight () {
		return padRight;
	}

	/** Sets the alignment of widgets within the horizontal group. Set to {@link Align#center}, {@link Align#top},
	 * {@link Align#bottom}, {@link Align#left}, {@link Align#right}, or any combination of those. */
	public HorizontalGroup align (int align) {
		this.align = align;
		return this;
	}

	/** Sets the alignment of widgets within the horizontal group to {@link Align#center}. This clears any other alignment. */
	public HorizontalGroup center () {
		align = Align.center;
		return this;
	}

	/** Sets {@link Align#top} and clears {@link Align#bottom} for the alignment of widgets within the horizontal group. */
	public HorizontalGroup top () {
		align |= Align.top;
		align &= ~Align.bottom;
		return this;
	}

	/** Sets {@link Align#bottom} and clears {@link Align#top} for the alignment of widgets within the horizontal group. */
	public HorizontalGroup bottom () {
		align |= Align.bottom;
		align &= ~Align.top;
		return this;
	}

	public int getAlign () {
		return align;
	}

	public HorizontalGroup fill () {
		fill = 1f;
		return this;
	}

	/** @param fill 0 will use pref width. */
	public HorizontalGroup fill (float fill) {
		this.fill = fill;
		return this;
	}

	public float getFill () {
		return fill;
	}
}

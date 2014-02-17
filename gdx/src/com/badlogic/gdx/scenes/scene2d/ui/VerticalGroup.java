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

/** A group that lays out its children on top of each other in a single column. This can be easier than using {@link Table} when
 * actors need to be inserted in the middle of the group.
 * <p>
 * The preferred width is the largest preferred width of any child. The preferred height is the sum of the children's preferred
 * heights, plus spacing between them if set. The min size is the preferred size and the max size is 0.
 * @author Nathan Sweet */
public class VerticalGroup extends WidgetGroup {
	private float prefWidth, prefHeight;
	private boolean sizeInvalid = true;
	private int align;
	private boolean reverse, round = true;
	private float spacing;
	private float padTop, padLeft, padBottom, padRight;
	private float fill;

	public VerticalGroup () {
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
		prefWidth = 0;
		prefHeight = padTop + padBottom + spacing * (n - 1);
		for (int i = 0; i < n; i++) {
			Actor child = children.get(i);
			if (child instanceof Layout) {
				Layout layout = (Layout)child;
				prefWidth = Math.max(prefWidth, layout.getPrefWidth());
				prefHeight += layout.getPrefHeight();
			} else {
				prefWidth = Math.max(prefWidth, child.getWidth());
				prefHeight += child.getHeight();
			}
		}
		prefWidth += padLeft + padRight;
		if (round) {
			prefWidth = Math.round(prefWidth);
			prefHeight = Math.round(prefHeight);
		}
	}

	public void layout () {
		float spacing = this.spacing, padLeft = this.padLeft;
		int align = this.align;
		boolean reverse = this.reverse, round = this.round;

		float groupWidth = getWidth() - padLeft - padRight;
		float y = reverse ? padBottom : getHeight() - padTop + spacing;
		SnapshotArray<Actor> children = getChildren();
		for (int i = 0, n = children.size; i < n; i++) {
			Actor child = children.get(i);
			float width, height;
			if (child instanceof Layout) {
				Layout layout = (Layout)child;
				if (fill > 0)
					width = groupWidth * fill;
				else
					width = Math.min(layout.getPrefWidth(), groupWidth);
				width = Math.max(width, layout.getMinWidth());
				float maxWidth = layout.getMaxWidth();
				if (maxWidth > 0 && width > maxWidth) width = maxWidth;
				height = layout.getPrefHeight();
			} else {
				width = child.getWidth();
				height = child.getHeight();
				if (fill > 0) width *= fill;
			}

			float x = padLeft;
			if ((align & Align.right) != 0)
				x += groupWidth - width;
			else if ((align & Align.left) == 0) // center
				x += (groupWidth - width) / 2;

			if (!reverse) y -= (height + spacing);
			if (round)
				child.setBounds(Math.round(x), Math.round(y), Math.round(width), Math.round(height));
			else
				child.setBounds(x, y, width, height);
			if (reverse) y += (height + spacing);
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

	/** The children will be ordered from bottom to top rather than the default top to bottom. */
	public VerticalGroup reverse () {
		reverse(true);
		return this;
	}

	/** If true, the children will be ordered from bottom to top rather than the default top to bottom. */
	public VerticalGroup reverse (boolean reverse) {
		this.reverse = reverse;
		return this;
	}

	public boolean getReverse () {
		return reverse;
	}

	/** Sets the space between children. */
	public VerticalGroup space (float spacing) {
		this.spacing = spacing;
		return this;
	}

	public float getSpace () {
		return spacing;
	}

	/** Sets the padTop, padLeft, padBottom, and padRight to the specified value. */
	public VerticalGroup pad (float pad) {
		padTop = pad;
		padLeft = pad;
		padBottom = pad;
		padRight = pad;
		return this;
	}

	public VerticalGroup pad (float top, float left, float bottom, float right) {
		padTop = top;
		padLeft = left;
		padBottom = bottom;
		padRight = right;
		return this;
	}

	public VerticalGroup padTop (float padTop) {
		this.padTop = padTop;
		return this;
	}

	public VerticalGroup padLeft (float padLeft) {
		this.padLeft = padLeft;
		return this;
	}

	public VerticalGroup padBottom (float padBottom) {
		this.padBottom = padBottom;
		return this;
	}

	public VerticalGroup padRight (float padRight) {
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

	/** Sets the alignment of widgets within the vertical group. Set to {@link Align#center}, {@link Align#top},
	 * {@link Align#bottom}, {@link Align#left}, {@link Align#right}, or any combination of those. */
	public VerticalGroup align (int align) {
		this.align = align;
		return this;
	}

	/** Sets the alignment of widgets within the vertical group to {@link Align#center}. This clears any other alignment. */
	public VerticalGroup center () {
		align = Align.center;
		return this;
	}

	/** Sets {@link Align#left} and clears {@link Align#right} for the alignment of widgets within the vertical group. */
	public VerticalGroup left () {
		align |= Align.left;
		align &= ~Align.right;
		return this;
	}

	/** Sets {@link Align#right} and clears {@link Align#left} for the alignment of widgets within the vertical group. */
	public VerticalGroup right () {
		align |= Align.right;
		align &= ~Align.left;
		return this;
	}

	public int getAlign () {
		return align;
	}

	public VerticalGroup fill () {
		fill = 1f;
		return this;
	}

	/** @param fill 0 will use pref width. */
	public VerticalGroup fill (float fill) {
		this.fill = fill;
		return this;
	}

	public float getFill () {
		return fill;
	}
}

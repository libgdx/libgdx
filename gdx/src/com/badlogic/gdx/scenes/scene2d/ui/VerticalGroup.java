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
 * heights. The min size is the preferred size and the max size is 0.
 * @author Nathan Sweet */
public class VerticalGroup extends WidgetGroup {
	private float prefWidth, prefHeight;
	private boolean sizeInvalid = true;
	private int alignment;
	private boolean reverse;
	private float spacing;

	public VerticalGroup () {
		setTouchable(Touchable.childrenOnly);
	}

	/** Sets the horizontal alignment of the children. Default is center.
	 * @see Align */
	public void setAlignment (int alignment) {
		this.alignment = alignment;
	}

	/** If true, the children will be ordered from bottom to top rather than the default top to bottom. */
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
		prefWidth = 0;
		prefHeight = spacing * (n - 1);
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
	}

	public void layout () {
		float spacing = this.spacing;
		float groupWidth = getWidth();
		float y = reverse ? 0 : getHeight();
		float dir = reverse ? 1 : -1;
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
			float x;
			if ((alignment & Align.left) != 0)
				x = 0;
			else if ((alignment & Align.right) != 0)
				x = groupWidth - width;
			else
				x = (groupWidth - width) / 2;
			if (!reverse) y += (height + spacing) * dir;
			child.setBounds(x, y, width, height);
			if (reverse) y += (height + spacing) * dir;
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
}

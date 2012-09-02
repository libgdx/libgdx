
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
		prefWidth = 0;
		SnapshotArray<Actor> children = getChildren();
		for (int i = 0, n = children.size; i < n; i++) {
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
			child.setBounds(x, y, width, height);
			y += height * dir;
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
}

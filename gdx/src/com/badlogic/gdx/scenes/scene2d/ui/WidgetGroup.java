
package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Layout;

/** @author Nathan Sweet */
public abstract class WidgetGroup extends Group implements Layout {
	private boolean needsLayout = true;

	public WidgetGroup () {
		super();
	}

	public WidgetGroup (String name) {
		super(name);
	}

	public float getMinWidth () {
		return getPrefWidth();
	}

	public float getMinHeight () {
		return getPrefHeight();
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
		layout();
		needsLayout = false;
	}

	public boolean needsLayout () {
		return needsLayout;
	}

	public void invalidateHierarchy () {
		invalidate();
		if (parent instanceof Layout) ((Layout)parent).invalidateHierarchy();
	}

	protected void childrenChanged () {
		invalidateHierarchy();
	}

	public void pack () {
		float newWidth = getPrefWidth();
		float newHeight = getPrefHeight();
		if (newWidth != width || newHeight != height) {
			width = newWidth;
			height = newHeight;
			invalidate();
		}
	}

	public void layout () {
	}

	/** If this method is overridden, the super method or {@link #validate()} should be called. */
	public void draw (SpriteBatch batch, float parentAlpha) {
		validate();
		super.draw(batch, parentAlpha);
	}
}

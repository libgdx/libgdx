
package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Layout;

/** A {@link Group} that participates in layout and provides a minimum, preferred, and maximum size.
 * <p>
 * The default preferred size of a widget group is 0 and this is almost always overridden by a subclass. The default minimum size
 * returns the preferred size, so a subclass may choose to return 0 if it wants to allow itself to be sized smaller. The default
 * maximum size is 0, which means no maximum size.
 * <p>
 * See {@link Layout} for details on how a widget group should participate in layout. A widget group's mutator methods should call
 * {@link #invalidate()} or {@link #invalidateHierarchy()} as needed. By default, invalidateHierarchy is called when child widgets
 * are added and removed.
 * @author Nathan Sweet */
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

	/** If this method is overridden, the super method or {@link #validate()} should be called to ensure the widget group is laid
	 * out. */
	public void draw (SpriteBatch batch, float parentAlpha) {
		validate();
		super.draw(batch, parentAlpha);
	}
}

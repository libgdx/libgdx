
package com.badlogic.gdx.scenes.scene2d.utils;

import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/** Drawable for a {@link NinePatch}.
 * <p>
 * The drawable sizes are set when the ninepatch is set, but they are separate values. Eg, {@link Drawable#getLeftWidth()} could
 * be set to more than {@link NinePatch#getLeftWidth()} in order to provide more space on the left than actually exists in the
 * ninepatch.
 * <p>
 * The min size is set to the ninepatch total size by default. It could be set to the left+right and top+bottom, excluding the
 * middle size, to allow the drawable to be sized down as small as possible.
 * @author Nathan Sweet */
public class NinePatchDrawable extends BaseDrawable {
	private NinePatch patch;

	public NinePatchDrawable () {
	}

	public NinePatchDrawable (NinePatch patch) {
		setPatch(patch);
	}

	public void draw (SpriteBatch batch, float x, float y, float width, float height) {
		patch.draw(batch, x, y, width, height);
	}

	public void setPatch (NinePatch patch) {
		this.patch = patch;
		setMinWidth(patch.getTotalWidth());
		setMinHeight(patch.getTotalHeight());
		setTopHeight(patch.getTopHeight());
		setRightWidth(patch.getRightWidth());
		setBottomHeight(patch.getBottomHeight());
		setLeftWidth(patch.getLeftWidth());
	}

	public NinePatch getPatch () {
		return patch;
	}
}

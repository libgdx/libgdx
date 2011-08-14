
package com.badlogic.gdx.graphics.g2d;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class NinePatch {
	public static final int TOP_LEFT = 0;
	public static final int TOP_CENTER = 1;
	public static final int TOP_RIGHT = 2;
	public static final int MIDDLE_LEFT = 3;
	public static final int MIDDLE_CENTER = 4;
	public static final int MIDDLE_RIGHT = 5;
	public static final int BOTTOM_LEFT = 6;
	public static final int BOTTOM_CENTER = 7;
	public static final int BOTTOM_RIGHT = 8;

	final TextureRegion[] patches;

	public NinePatch (Texture texture, int left, int right, int top, int bottom) {
		this(new TextureRegion(texture), left, right, top, bottom);
	}

	public NinePatch (TextureRegion region, int left, int right, int top, int bottom) {
		int middleWidth = region.getRegionWidth() - left - right;
		int middleHeight = region.getRegionHeight() - top - bottom;
		this.patches = new TextureRegion[] {new TextureRegion(region, 0, 0, left, top),
			new TextureRegion(region, left, 0, middleWidth, top), new TextureRegion(region, left + middleWidth, 0, right, top),
			new TextureRegion(region, 0, top, left, middleHeight), new TextureRegion(region, left, top, middleWidth, middleHeight),
			new TextureRegion(region, left + middleWidth, top, right, middleHeight),
			new TextureRegion(region, 0, top + middleHeight, left, bottom),
			new TextureRegion(region, left, top + middleHeight, middleWidth, bottom),
			new TextureRegion(region, left + middleWidth, top + middleHeight, right, bottom),};
	}

	public NinePatch (TextureRegion... patches) {
		if (patches.length != 9) throw new IllegalArgumentException("NinePatch needs nine TextureRegions");
		this.patches = patches;
		checkValidity();
	}

	private void checkValidity () {
		if (patches[BOTTOM_LEFT].getRegionWidth() != patches[TOP_LEFT].getRegionWidth()
			|| patches[BOTTOM_LEFT].getRegionWidth() != patches[MIDDLE_LEFT].getRegionWidth()) {
			throw new GdxRuntimeException("Left side patches must have the same width");
		}

		if (patches[BOTTOM_RIGHT].getRegionWidth() != patches[TOP_RIGHT].getRegionWidth()
			|| patches[BOTTOM_RIGHT].getRegionWidth() != patches[MIDDLE_RIGHT].getRegionWidth()) {
			throw new GdxRuntimeException("Right side patches must have the same width");
		}

		if (patches[BOTTOM_LEFT].getRegionHeight() != patches[BOTTOM_CENTER].getRegionHeight()
			|| patches[BOTTOM_LEFT].getRegionHeight() != patches[BOTTOM_RIGHT].getRegionHeight()) {
			throw new GdxRuntimeException("Bottom patches must have the same height");
		}

		if (patches[TOP_LEFT].getRegionHeight() != patches[TOP_CENTER].getRegionHeight()
			|| patches[TOP_LEFT].getRegionHeight() != patches[TOP_RIGHT].getRegionHeight()) {
			throw new GdxRuntimeException("Top patches must have the same height");
		}
	}

	public void draw (SpriteBatch batch, float x, float y, float width, float height) {
		float widthTopBottom = width - (patches[TOP_LEFT].getRegionWidth() + patches[TOP_RIGHT].getRegionWidth());
		float heightLeftRight = height - (patches[TOP_LEFT].getRegionHeight() + patches[BOTTOM_LEFT].getRegionHeight());
		float widthCenter = widthTopBottom;

		// bottom patches
		batch.draw(patches[BOTTOM_LEFT], x, y, patches[BOTTOM_LEFT].getRegionWidth(), patches[BOTTOM_LEFT].getRegionHeight());
		batch.draw(patches[BOTTOM_CENTER], x + patches[BOTTOM_LEFT].getRegionWidth(), y, widthCenter,
			patches[BOTTOM_CENTER].getRegionHeight());
		batch.draw(patches[BOTTOM_RIGHT], x + patches[BOTTOM_LEFT].getRegionWidth() + widthTopBottom, y,
			patches[BOTTOM_RIGHT].getRegionWidth(), patches[BOTTOM_RIGHT].getRegionHeight());

		y += patches[BOTTOM_LEFT].getRegionHeight();
		// center patches
		batch.draw(patches[MIDDLE_LEFT], x, y, patches[MIDDLE_LEFT].getRegionWidth(), heightLeftRight);
		batch.draw(patches[MIDDLE_CENTER], x + patches[MIDDLE_LEFT].getRegionWidth(), y, widthCenter, heightLeftRight);
		batch.draw(patches[MIDDLE_RIGHT], x + patches[MIDDLE_LEFT].getRegionWidth() + widthTopBottom, y,
			patches[MIDDLE_RIGHT].getRegionWidth(), heightLeftRight);

		// top patches
		y += heightLeftRight;
		batch.draw(patches[TOP_LEFT], x, y, patches[TOP_LEFT].getRegionWidth(), patches[TOP_LEFT].getRegionHeight());
		batch.draw(patches[TOP_CENTER], x + patches[TOP_LEFT].getRegionWidth(), y, widthCenter,
			patches[TOP_CENTER].getRegionHeight());
		batch.draw(patches[TOP_RIGHT], x + patches[TOP_LEFT].getRegionWidth() + widthTopBottom, y,
			patches[TOP_RIGHT].getRegionWidth(), patches[TOP_RIGHT].getRegionHeight());
	}

	public float getLeftWidth () {
		return patches[TOP_LEFT].getRegionWidth();
	}

	public float getRightWidth () {
		return patches[TOP_RIGHT].getRegionWidth();
	}

	public float getTopHeight () {
		return patches[TOP_RIGHT].getRegionHeight();
	}

	public float getBottomHeight () {
		return patches[BOTTOM_RIGHT].getRegionHeight();
	}

	public float getTotalHeight () {
		return getTopHeight() + getBottomHeight() + patches[MIDDLE_LEFT].getRegionHeight();
	}

	public float getTotalWidth () {
		return getLeftWidth() + getRightWidth() + patches[MIDDLE_CENTER].getRegionWidth();
	}
}

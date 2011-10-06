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

	private TextureRegion[] patches;

	private NinePatch () {
	}

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
			new TextureRegion(region, left + middleWidth, top + middleHeight, right, bottom)};
	}

	public NinePatch (TextureRegion region) {
		this.patches = new TextureRegion[] {
			//
			region, null, null, //
			null, null, null, //
			null, null, null //
		};
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
		float centerColumnX = x + getLeftWidth();
		float rightColumnX = x + width - getRightWidth();
		float middleRowY = y + getBottomHeight();
		float topRowY = y + height - getTopHeight();

		// Bottom row
		if (patches[BOTTOM_LEFT] != null) batch.draw(patches[BOTTOM_LEFT], x, y, centerColumnX - x, middleRowY - y);
		if (patches[BOTTOM_CENTER] != null)
			batch.draw(patches[BOTTOM_CENTER], centerColumnX, y, rightColumnX - centerColumnX, middleRowY - y);
		if (patches[BOTTOM_RIGHT] != null)
			batch.draw(patches[BOTTOM_RIGHT], rightColumnX, y, x + width - rightColumnX, middleRowY - y);

		// Middle row
		if (patches[MIDDLE_LEFT] != null) batch.draw(patches[MIDDLE_LEFT], x, middleRowY, centerColumnX - x, topRowY - middleRowY);
		if (patches[MIDDLE_CENTER] != null)
			batch.draw(patches[MIDDLE_CENTER], centerColumnX, middleRowY, rightColumnX - centerColumnX, topRowY - middleRowY);
		if (patches[MIDDLE_RIGHT] != null)
			batch.draw(patches[MIDDLE_RIGHT], rightColumnX, middleRowY, x + width - rightColumnX, topRowY - middleRowY);

		// Top row
		if (patches[TOP_LEFT] != null) batch.draw(patches[TOP_LEFT], x, topRowY, centerColumnX - x, y + height - topRowY);
		if (patches[TOP_CENTER] != null)
			batch.draw(patches[TOP_CENTER], centerColumnX, topRowY, rightColumnX - centerColumnX, y + height - topRowY);
		if (patches[TOP_RIGHT] != null)
			batch.draw(patches[TOP_RIGHT], rightColumnX, topRowY, x + width - rightColumnX, y + height - topRowY);
	}

	public float getLeftWidth () {
		if (patches[BOTTOM_LEFT] != null)
			return patches[BOTTOM_LEFT].getRegionWidth();
		else if (patches[MIDDLE_LEFT] != null)
			return patches[MIDDLE_LEFT].getRegionWidth();
		else if (patches[TOP_LEFT] != null) //
			return patches[TOP_LEFT].getRegionWidth();
		return 0;
	}

	public float getRightWidth () {
		if (patches[BOTTOM_RIGHT] != null)
			return patches[BOTTOM_RIGHT].getRegionWidth();
		else if (patches[MIDDLE_RIGHT] != null)
			return patches[MIDDLE_RIGHT].getRegionWidth();
		else if (patches[TOP_RIGHT] != null) //
			return patches[TOP_RIGHT].getRegionWidth();
		return 0;
	}

	public float getTopHeight () {
		if (patches[TOP_LEFT] != null)
			return patches[TOP_LEFT].getRegionHeight();
		else if (patches[TOP_CENTER] != null)
			return patches[TOP_CENTER].getRegionHeight();
		else if (patches[TOP_RIGHT] != null) //
			return patches[TOP_RIGHT].getRegionHeight();
		return 0;
	}

	public float getBottomHeight () {
		if (patches[BOTTOM_LEFT] != null)
			return patches[BOTTOM_LEFT].getRegionHeight();
		else if (patches[BOTTOM_CENTER] != null)
			return patches[BOTTOM_CENTER].getRegionHeight();
		else if (patches[BOTTOM_RIGHT] != null) //
			return patches[BOTTOM_RIGHT].getRegionHeight();
		return 0;
	}

	public float getTotalHeight () {
		float totalHeight = getTopHeight() + getBottomHeight();
		if (patches[MIDDLE_CENTER] != null) totalHeight += patches[MIDDLE_CENTER].getRegionHeight();
		return totalHeight;
	}

	public float getTotalWidth () {
		float totalWidth = getLeftWidth() + getRightWidth();
		if (patches[MIDDLE_CENTER] != null) totalWidth += patches[MIDDLE_CENTER].getRegionWidth();
		return totalWidth;
	}

	public TextureRegion[] getPatches () {
		return patches;
	}
}

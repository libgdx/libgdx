package com.badlogic.gdx.graphics.g2d;

import com.badlogic.gdx.graphics.Color;
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
	final Color color = new Color();
	
	public NinePatch(TextureRegion[] patches) {
		this.patches = patches;
		checkValidity();
	}
	
	private void checkValidity() {
		if(patches[BOTTOM_LEFT].getRegionWidth() != patches[TOP_LEFT].getRegionWidth() ||
		   patches[BOTTOM_LEFT].getRegionWidth() != patches[MIDDLE_LEFT].getRegionWidth()) {
			throw new GdxRuntimeException("Left side patches must have the same width");
		}
		
		if(patches[BOTTOM_RIGHT].getRegionWidth() != patches[TOP_RIGHT].getRegionWidth() ||
		   patches[BOTTOM_RIGHT].getRegionWidth() != patches[MIDDLE_RIGHT].getRegionWidth()) {
			throw new GdxRuntimeException("Right side patches must have the same width");
		}
		
		if(patches[BOTTOM_LEFT].getRegionHeight() != patches[BOTTOM_CENTER].getRegionHeight() ||
		   patches[BOTTOM_LEFT].getRegionHeight() != patches[BOTTOM_RIGHT].getRegionHeight()) {
			throw new GdxRuntimeException("Bottom patches must have the same height");
		}
		
		if(patches[TOP_LEFT].getRegionHeight() != patches[TOP_CENTER].getRegionHeight() ||
		   patches[TOP_LEFT].getRegionHeight() != patches[TOP_RIGHT].getRegionHeight()) {
			throw new GdxRuntimeException("Top patches must have the same height");
		}
	}
	
	public void setColor(Color color) {
		this.color.set(color);
	}
	
	public void setColor(float r, float g, float b, float a) {
		this.color.set(r, g, b, a);
	}
	
	public void draw(SpriteBatch batch, float x, float y, float width, float height) {
		draw(batch, x, y, width, height, 1);
	}
	
	public void draw(SpriteBatch batch, float x, float y, float width, float height, float alphaModulation) {
		float widthTopBottom = width - (patches[TOP_LEFT].getRegionWidth() + patches[TOP_RIGHT].getRegionWidth());
		float heightLeftRight = height - (patches[TOP_LEFT].getRegionHeight() + patches[BOTTOM_LEFT].getRegionHeight());				
		float widthCenter = widthTopBottom;		
		
		// bottom patches
		batch.draw(patches[BOTTOM_LEFT], x, y, patches[BOTTOM_LEFT].getRegionWidth(), patches[BOTTOM_LEFT].getRegionHeight());
		batch.draw(patches[BOTTOM_CENTER], x + patches[BOTTOM_LEFT].getRegionWidth(), y, widthCenter, patches[BOTTOM_CENTER].getRegionHeight());
		batch.draw(patches[BOTTOM_RIGHT], x + patches[BOTTOM_LEFT].getRegionWidth() + widthTopBottom, y, patches[BOTTOM_RIGHT].getRegionWidth(), patches[BOTTOM_RIGHT].getRegionHeight());
		
		y += patches[BOTTOM_LEFT].getRegionHeight(); 
		// center patches
		batch.draw(patches[MIDDLE_LEFT], x, y, patches[MIDDLE_LEFT].getRegionWidth(), heightLeftRight);
		batch.draw(patches[MIDDLE_CENTER], x + patches[MIDDLE_LEFT].getRegionWidth(), y, widthCenter, heightLeftRight);
		batch.draw(patches[MIDDLE_RIGHT], x + patches[MIDDLE_LEFT].getRegionWidth() + widthTopBottom, y, patches[MIDDLE_RIGHT].getRegionWidth(), heightLeftRight);
		
		
		// top patches
		y += heightLeftRight;
		batch.draw(patches[TOP_LEFT], x, y, patches[TOP_LEFT].getRegionWidth(), patches[TOP_LEFT].getRegionHeight());
		batch.draw(patches[TOP_CENTER], x + patches[TOP_LEFT].getRegionWidth(), y, widthCenter, patches[TOP_CENTER].getRegionHeight());
		batch.draw(patches[TOP_RIGHT], x + patches[TOP_LEFT].getRegionWidth() + widthTopBottom, y, patches[TOP_RIGHT].getRegionWidth(), patches[TOP_RIGHT].getRegionHeight());
	}
	
	public float getLeftWidth() {
		return patches[TOP_LEFT].getRegionWidth();
	}
	
	public float getRightWidth() {
		return patches[TOP_RIGHT].getRegionWidth();
	}
	
	public float getTopHeight() {
		return patches[TOP_RIGHT].getRegionHeight();
	}
	
	public float getBottomHeight() {
		return patches[BOTTOM_RIGHT].getRegionHeight();
	}

	public float getTotalHeight() {
		return getTopHeight() + getBottomHeight() + patches[MIDDLE_LEFT].getRegionHeight();
	}

	public float getTotalWidth() {
		return getLeftWidth() + getRightWidth() + patches[MIDDLE_CENTER].getRegionWidth();
	}
}

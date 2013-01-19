package com.badlogic.gdx.maps.objects;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapObject;

public class TextureMapObject extends MapObject {
	
	private float x;
	
	private float y;
	
	private TextureRegion textureRegion;

	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getY() {
		return y;
	}

	public void setY(float y) {
		this.y = y;
	}

	public TextureRegion getTextureRegion() {
		return textureRegion;
	}
	
	public TextureMapObject(TextureRegion textureRegion) {
		super();
		this.textureRegion = textureRegion;
	}
	
	public TextureMapObject(TextureMapObject copy) {
		super();
		this.x = copy.x;
		this.y = copy.y;
		this.textureRegion = copy.textureRegion;
	}
}

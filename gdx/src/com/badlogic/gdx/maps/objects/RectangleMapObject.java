package com.badlogic.gdx.maps.objects;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.math.Rectangle;

public class RectangleMapObject extends MapObject {
	
	private Rectangle rectangle;
	
	public Rectangle getRectangle() {
		return rectangle;
	}
	
	public RectangleMapObject(float x, float y, float width, float height) {
		super();
		rectangle = new Rectangle(x, y, width, height);
	}
	
}

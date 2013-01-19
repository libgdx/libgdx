package com.badlogic.gdx.maps.objects;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.math.Circle;

public class CircleMapObject extends MapObject {
	
	private Circle circle;
	
	public Circle getCircle() {
		return circle;
	}
	
	public CircleMapObject(float x, float y, int radius) {
		super();
		circle = new Circle(x, y, radius);
	}
	
	
}

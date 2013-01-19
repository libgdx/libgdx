/**
 * 
 */
package com.badlogic.gdx.maps.objects;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.math.Polygon;

public class PolygonMapObject extends MapObject {

	private Polygon polygon;
	
	public Polygon getPolygon() {
		return polygon;
	}
	
	public PolygonMapObject(float[] vertices) {
		super();
		polygon = new Polygon(vertices);
	}
	
}

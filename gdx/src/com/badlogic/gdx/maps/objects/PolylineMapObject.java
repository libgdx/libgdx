package com.badlogic.gdx.maps.objects;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.math.Polygon;

public class PolylineMapObject extends MapObject {

	private Polygon polygon;
	
	public Polygon getPolygon() {
		return polygon;
	}
	
	public void setPolygon(Polygon polygon) {
		this.polygon = polygon;
	}
	
	public PolylineMapObject() {
		this(new float[0]);
	}
	
	public PolylineMapObject(float[] vertices) {
		super();
		polygon = new Polygon(vertices);
	}

}

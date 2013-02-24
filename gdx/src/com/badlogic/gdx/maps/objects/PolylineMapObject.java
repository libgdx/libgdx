package com.badlogic.gdx.maps.objects;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.math.Polygon;

/**
 * @brief Represents polyline map objects
 */
public class PolylineMapObject extends MapObject {

	private Polygon polygon;
	
	/**
	 * @return polygon shape
	 */
	public Polygon getPolygon() {
		return polygon;
	}
	
	/**
	 * @param polygon new object's polygon shape
	 */
	public void setPolygon(Polygon polygon) {
		this.polygon = polygon;
	}
	
	/**
	 * Creates empty polyline 
	 */
	public PolylineMapObject() {
		this(new float[0]);
	}
	
	/**
	 * @param vertices polyline defining vertices (at least 3 because a polygon is used to represent it)
	 */
	public PolylineMapObject(float[] vertices) {
		super();
		polygon = new Polygon(vertices);
	}

}

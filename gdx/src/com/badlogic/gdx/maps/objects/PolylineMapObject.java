package com.badlogic.gdx.maps.objects;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.math.Polyline;

/**
 * @brief Represents polyline map objects
 */
public class PolylineMapObject extends MapObject {

	private Polyline polyline;
	
	/**
	 * @return polygon shape
	 */
	public Polyline getPolyline() {
		return polyline;
	}
	
	/**
	 * @param polyline new object's polyline shape
	 */
	public void setPolyline(Polyline polyline) {
		this.polyline = polyline;
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
		polyline = new Polyline(vertices);
	}

}

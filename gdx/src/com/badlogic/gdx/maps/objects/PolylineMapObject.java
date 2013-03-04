package com.badlogic.gdx.maps.objects;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.math.Polyline;

/**
 * @brief Represents polyline map objects
 */
public class PolylineMapObject extends MapObject {

	private Polyline polyline;
	
	/**
	 * @return polyline shape
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
	 * @param vertices polyline defining vertices
	 */
	public PolylineMapObject(float[] vertices) {
		polyline = new Polyline(vertices);
	}

	/**
	 * @param polyline the polyline
	 */
	public PolylineMapObject(Polyline polyline) {
		this.polyline = polyline;
	}
	
}

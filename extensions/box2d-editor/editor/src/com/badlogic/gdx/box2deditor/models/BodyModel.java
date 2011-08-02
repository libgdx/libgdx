package com.badlogic.gdx.box2deditor.models;

import com.badlogic.gdx.box2deditor.utils.VectorUtils;
import com.badlogic.gdx.math.Vector2;

/**
 *
 * @author Aurelien Ribon (aurelien.ribon@gmail.com)
 */
public class BodyModel {
	public static final BodyModel EMPTY = new BodyModel() {
		@Override public void set(Vector2[][] shapes, Vector2[][] polygons) {}
	};

	// -------------------------------------------------------------------------

	private Vector2[][] shapes;
	private Vector2[][] polygons;

	public void clearAll() {
		shapes = null;
		polygons = null;
	}

	public void set(Vector2[][] shapes, Vector2[][] polygons) {
		clearAll();
		this.shapes = VectorUtils.getCopy(shapes);
		this.polygons = VectorUtils.getCopy(polygons);
	}

	public Vector2[][] getShapes() {
		return VectorUtils.getCopy(shapes);
	}

	public Vector2[][] getPolygons() {
		return VectorUtils.getCopy(polygons);
	}
}

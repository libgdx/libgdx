package com.badlogic.gdx.box2deditor.models;

import com.badlogic.gdx.box2deditor.utils.ShapeUtils;
import com.badlogic.gdx.math.Vector2;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Aurelien Ribon (aurelien.ribon@gmail.com)
 */
public class ShapeModel {
    private final List<Vector2> points = new ArrayList<Vector2>();
	private boolean isClosed = false;

	public ShapeModel() {
	}

	public ShapeModel(Vector2[] points) {
		this.points.addAll(Arrays.asList(points));
	}

	public void addPoint(Vector2 p) {
		points.add(p);
	}

	public void addPoint(int idx, Vector2 p) {
		points.add(idx, p);
	}

	public void removePoint(Vector2 p) {
		points.remove(p);
	}

	public void removePoint(int idx) {
		points.remove(idx);
	}

	public void setPoints(Vector2[] points) {
		this.points.addAll(Arrays.asList(points));
	}

	public Vector2[] getPoints() {
		return points.toArray(new Vector2[points.size()]);
	}

	public Vector2 getPoint(int idx) {
		return points.get(idx);
	}

	public Vector2 getLastPoint() {
		return points.get(points.size()-1);
	}

	public void close() {
		isClosed = true;
		if (ShapeUtils.isPolygonCCW(points.toArray(new Vector2[points.size()])))
			Collections.reverse(points);
	}

	public boolean isClosed() {
		return isClosed;
	}

	public int getPointCount() {
		return points.size();
	}
}

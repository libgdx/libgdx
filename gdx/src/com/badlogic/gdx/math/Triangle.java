package com.badlogic.gdx.math;

import java.io.Serializable;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;

/** Encapsulates a 2D Triangle defined by it's three points (A, B and C)
 * @author vanniktech */
public class Triangle implements Serializable {
	private static final long serialVersionUID = 4947942567670252118L;

	/** x-coordinate for the first point(A) */
	public float x1;

	/** x-coordinate for the second point(B) */
	public float x2;

	/** x-coordinate for the third point(C) */
	public float x3;

	/** y-coordinate for the first point(A) */
	public float y1;

	/** y-coordinate for the second point(B) */
	public float y2;

	/** y-coordinate for the third point(C) */
	public float y3;

	/** Empty constructor for triangle */
	public Triangle () {

	}

	/** Constructs a triangle from a given triangle
	 * @param triangle */
	public Triangle (Triangle triangle) {
		this.x1 = triangle.x1;
		this.y1 = triangle.y1;
		this.x2 = triangle.x2;
		this.y2 = triangle.y2;
		this.x3 = triangle.x3;
		this.y3 = triangle.y3;
	}

	/** Constructs a triangle from the given x- and y coordinates of each point
	 * @param x1 x position of point A
	 * @param y1 y position of point A
	 * @param x2 x position of point B
	 * @param y2 y position of point B
	 * @param x3 x position of point C
	 * @param y3 y position of point C */
	public Triangle (float x1, float y1, float x2, float y2, float x3, float y3) {
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
		this.x3 = x3;
		this.y3 = y3;
	}

	/** @param x1 x position of point A
	 * @param y1 y position of point A
	 * @param x2 x position of point B
	 * @param y2 y position of point B
	 * @param x3 x position of point C
	 * @param y3 y position of point C
	 * @return this triangle for chaining */
	public Triangle set (float x1, float y1, float x2, float y2, float x3, float y3) {
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
		this.x3 = x3;
		this.y3 = y3;

		return this;
	}

	/** Sets the values of the given triangle to this triangle
	 * @param triangle
	 * @return this triangle for chaining */
	public Triangle set (Triangle triangle) {
		this.x1 = triangle.x1;
		this.y1 = triangle.y1;
		this.x2 = triangle.x2;
		this.y2 = triangle.y2;
		this.x3 = triangle.x3;
		this.y3 = triangle.y3;

		return this;
	}

	/** @return x-coordinate from point A */
	public float getX1 () {
		return x1;
	}

	/** Sets the x-coordinate of Point A
	 * @param x1
	 * @return this triangle for chaining */
	public Triangle setX1 (float x1) {
		this.x1 = x1;

		return this;
	}

	/** @return y-coordinate from point A */
	public float getY1 () {
		return y1;
	}

	/** Sets the y-coordinate of Point A
	 * @param y1
	 * @return this triangle for chaining */
	public Triangle setY1 (float y1) {
		this.y1 = y1;

		return this;
	}

	/** @return x-coordinate from point B */
	public float getX2 () {
		return x2;
	}

	/** Sets the x-coordinate of Point B
	 * @param x2
	 * @return this triangle for chaining */
	public Triangle setX2 (float x2) {
		this.x2 = x2;

		return this;
	}

	/** @return y-coordinate from point B */
	public float getY2 () {
		return y2;
	}

	/** Sets the y-coordinate of Point B
	 * @param y2
	 * @return this triangle for chaining */
	public Triangle setY2 (float y2) {
		this.y2 = y2;

		return this;
	}

	/** @return x-coordinate from point C */
	public float getX3 () {
		return x3;
	}

	/** Sets the x-coordinate of Point C
	 * @param x3
	 * @return this triangle for chaining */
	public Triangle setX3 (float x3) {
		this.x3 = x3;

		return this;
	}

	/** @return y-coordinate from point C */
	public float getY3 () {
		return y3;
	}

	/** Sets the y-coordinate of Point C
	 * @param y3
	 * @return this triangle for chaining */
	public Triangle setY3 (float y3) {
		this.y3 = y3;

		return this;
	}

	/** @return the Vector2 of Point A (x1 and y1) */
	public Vector2 getA (Vector2 v) {
		return v.set(x1, y1);
	}

	/** @return the Vector2 of Point B (x2 and y2) */
	public Vector2 getB (Vector2 v) {
		return v.set(x2, y2);

	}

	/** @return the Vector2 of Point C (x3 and y3) */
	public Vector2 getC (Vector2 v) {
		return v.set(x3, y3);
	}

	/** Checks if this triangle contains a point
	 * @param x x-coordinate
	 * @param y y-coordinate
	 * @return boolean whether it does contain or not */
	public boolean contains (float x, float y) {
		return Intersector.isPointInTriangle(x, y, this);
	}

	/** Checks if this triangle contains a point
	 * @param point
	 * @return boolean whether it does contain or not */
	public boolean contains (Vector2 point) {
		return Intersector.isPointInTriangle(point.x, point.y, x1, y1, x2, y2, x3, y3);
	}

	@Override
	public String toString () {
		return "Triangle [x1=" + x1 + ", y1=" + y1 + ", x2=" + x2 + ", y2=" + y2 + ", x3=" + x3 + ", y3=" + y3 + "]";
	}
}
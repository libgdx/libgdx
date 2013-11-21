package com.badlogic.gdx.math;

import java.io.Serializable;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;

/** Encapsulates a 2D Triangle defined by it's three points (A, B and C)
 * @author vanniktech */
public class Triangle implements Serializable {
	private static final long serialVersionUID = 4947942567670252118L;

	/** x-coordinate for the first point(A) */
	public float ax;

	/** x-coordinate for the second point(B) */
	public float bx;

	/** x-coordinate for the third point(C) */
	public float cx;

	/** y-coordinate for the first point(A) */
	public float ay;

	/** y-coordinate for the second point(B) */
	public float by;

	/** y-coordinate for the third point(C) */
	public float cy;

	/** Empty constructor for triangle */
	public Triangle () {

	}

	/** Constructs a triangle from a given triangle
	 * @param triangle */
	public Triangle (Triangle triangle) {
		this.ax = triangle.ax;
		this.ay = triangle.ay;
		this.bx = triangle.bx;
		this.by = triangle.by;
		this.cx = triangle.cx;
		this.cy = triangle.cy;
	}

	/** Constructs a triangle from the given x- and y coordinates of each point
	 * @param ax x position of point A
	 * @param ay y position of point A
	 * @param bx x position of point B
	 * @param by y position of point B
	 * @param cx x position of point C
	 * @param cy y position of point C */
	public Triangle (float ax, float ay, float bx, float by, float cx, float cy) {
		this.ax = ax;
		this.ay = ay;
		this.bx = bx;
		this.by = by;
		this.cx = cx;
		this.cy = cy;
	}

	/** @param ax x position of point A
	 * @param ay y position of point A
	 * @param bx x position of point B
	 * @param by y position of point B
	 * @param cx x position of point C
	 * @param cy y position of point C
	 * @return this triangle for chaining */
	public Triangle set (float ax, float ay, float bx, float by, float cx, float cy) {
		this.ax = ax;
		this.ay = ay;
		this.bx = bx;
		this.by = by;
		this.cx = cx;
		this.cy = cy;

		return this;
	}

	/** Sets the values of the given triangle to this triangle
	 * @param triangle
	 * @return this triangle for chaining */
	public Triangle set (Triangle triangle) {
		this.ax = triangle.ax;
		this.ay = triangle.ay;
		this.bx = triangle.bx;
		this.by = triangle.by;
		this.cx = triangle.cx;
		this.cy = triangle.cy;

		return this;
	}

	/** @return x-coordinate from point A */
	public float getAX () {
		return ax;
	}

	/** Sets the x-coordinate of Point A
	 * @param ax
	 * @return this triangle for chaining */
	public Triangle setAX (float ax) {
		this.ax = ax;

		return this;
	}

	/** @return y-coordinate from point A */
	public float getAy () {
		return ay;
	}

	/** Sets the y-coordinate of Point A
	 * @param ay
	 * @return this triangle for chaining */
	public Triangle setAy (float ay) {
		this.ay = ay;

		return this;
	}

	/** @return x-coordinate from point B */
	public float getBx () {
		return bx;
	}

	/** Sets the x-coordinate of Point B
	 * @param bx
	 * @return this triangle for chaining */
	public Triangle setBx (float bx) {
		this.bx = bx;

		return this;
	}

	/** @return y-coordinate from point B */
	public float getBy () {
		return by;
	}

	/** Sets the y-coordinate of Point B
	 * @param by
	 * @return this triangle for chaining */
	public Triangle setBy (float by) {
		this.by = by;

		return this;
	}

	/** @return x-coordinate from point C */
	public float getCx () {
		return cx;
	}

	/** Sets the x-coordinate of Point C
	 * @param cx
	 * @return this triangle for chaining */
	public Triangle setCx (float cx) {
		this.cx = cx;

		return this;
	}

	/** @return y-coordinate from point C */
	public float getCy () {
		return cy;
	}

	/** Sets the y-coordinate of Point C
	 * @param cy
	 * @return this triangle for chaining */
	public Triangle setCy (float cy) {
		this.cy = cy;

		return this;
	}

	/** @return the Vector2 of Point A (ax and ay) */
	public Vector2 getA (Vector2 v) {
		return v.set(ax, ay);
	}

	/** @return the Vector2 of Point B (bx and by) */
	public Vector2 getB (Vector2 v) {
		return v.set(bx, by);

	}

	/** @return the Vector2 of Point C (cx and cy) */
	public Vector2 getC (Vector2 v) {
		return v.set(cx, cy);
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
		return Intersector.isPointInTriangle(point.x, point.y, ax, ay, bx, by, cx, cy);
	}

	public float getArea() {
		return (ax * (by - cy) + bx * (cy - ay) + cx * (ay - by)) / 2;
	}

	public void getBaryCenter(Vector2 p, Vector2 baryCenter) {
		GeometryUtils.barycentric(p, new Vector2(ax, ay), new Vector2(bx, by), new Vector2(cx, cy), baryCenter);
	}

	public void getCircumCenter(Vector2 centroid) {
		GeometryUtils.triangleCentroid(ax, ay, bx, by, cx, cy, centroid);
	}

	public float getLengthA() {
		return this.getLength(bx, cx, by, cy);
	}

	public float getLengthB() {
		return this.getLength(cx, ax, cy, ay);
	}

	public float getLengthC() {
		return this.getLength(bx, ax, by, ay);
	}

	private float getLength(float x2, float x1, float y2, float y1) {
		return (float)Math.sqrt(Math.pow((x2 - x1), 2) + Math.pow((y2 - y1), 2));
	}

	public float getAngleA() {
		float a = getLengthA(), b = getLengthB(), c = getLengthC();
		return (float)Math.acos((b*b + c*c - a*a) / 2 * b * c);
	}

	public float getAngleB() {
		float a = getLengthA(), b = getLengthB(), c = getLengthC();
		return (float)Math.acos((a*a + c*c - b*b) / 2 * a * c);
	}

	public float getAngleC() {
		return 180.f - this.getAngleA() - getAngleB();
	}

	@Override
	public String toString () {
		return "Triangle [ax=" + ax + ", ay=" + ay + ", bx=" + bx + ", by=" + by + ", cx=" + cx + ", cy=" + cy + "]";
	}
}
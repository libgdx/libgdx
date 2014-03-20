/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.math.collision;

import java.io.Serializable;
import java.util.List;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

/** Encapsulates an axis aligned bounding box represented by a minimum and a maximum Vector. Additionally you can query for the
 * bounding box's center, dimensions and corner points.
 * 
 * @author badlogicgames@gmail.com */
public class BoundingBox implements Serializable {
	private static final long serialVersionUID = -1286036817192127343L;
	final Vector3 crn[] = new Vector3[8];
	public final Vector3 min = new Vector3();
	public final Vector3 max = new Vector3();
	final Vector3 cnt = new Vector3();
	final Vector3 dim = new Vector3();
	boolean crn_dirty = true;

	/** @return the center of the bounding box */
	public Vector3 getCenter () {
		return cnt;
	}

	protected void updateCorners () {
		if (!crn_dirty) return;

		crn[0].set(min.x, min.y, min.z);
		crn[1].set(max.x, min.y, min.z);
		crn[2].set(max.x, max.y, min.z);
		crn[3].set(min.x, max.y, min.z);
		crn[4].set(min.x, min.y, max.z);
		crn[5].set(max.x, min.y, max.z);
		crn[6].set(max.x, max.y, max.z);
		crn[7].set(min.x, max.y, max.z);
		crn_dirty = false;
	}

	/** @return the corners of this bounding box */
	public Vector3[] getCorners () {
		updateCorners();
		return crn;
	}

	/** @return The dimensions of this bounding box on all three axis */
	public Vector3 getDimensions () {
		return dim;
	}

	/** @return The minimum vector */
	public Vector3 getMin () {
		return min;
	}

	/** @return The maximum vector */
	public synchronized Vector3 getMax () {
		return max;
	}

	/** Constructs a new bounding box with the minimum and maximum vector set to zeros. */
	public BoundingBox () {
		crn_dirty = true;
		for (int l_idx = 0; l_idx < 8; l_idx++)
			crn[l_idx] = new Vector3();
		clr();
	}

	/** Constructs a new bounding box from the given bounding box.
	 * 
	 * @param bounds The bounding box to copy */
	public BoundingBox (BoundingBox bounds) {
		crn_dirty = true;
		for (int l_idx = 0; l_idx < 8; l_idx++)
			crn[l_idx] = new Vector3();
		this.set(bounds);
	}

	/** Constructs the new bounding box using the given minimum and maximum vector.
	 * 
	 * @param minimum The minimum vector
	 * @param maximum The maximum vector */
	public BoundingBox (Vector3 minimum, Vector3 maximum) {
		crn_dirty = true;
		for (int l_idx = 0; l_idx < 8; l_idx++)
			crn[l_idx] = new Vector3();
		this.set(minimum, maximum);
	}

	/** Sets the given bounding box.
	 * 
	 * @param bounds The bounds.
	 * @return This bounding box for chaining. */
	public BoundingBox set (BoundingBox bounds) {
		crn_dirty = true;
		return this.set(bounds.min, bounds.max);
	}

	/** Sets the given minimum and maximum vector.
	 * 
	 * @param minimum The minimum vector
	 * @param maximum The maximum vector
	 * @return This bounding box for chaining. */
	public BoundingBox set (Vector3 minimum, Vector3 maximum) {
		min.set(minimum.x < maximum.x ? minimum.x : maximum.x, minimum.y < maximum.y ? minimum.y : maximum.y,
			minimum.z < maximum.z ? minimum.z : maximum.z);
		max.set(minimum.x > maximum.x ? minimum.x : maximum.x, minimum.y > maximum.y ? minimum.y : maximum.y,
			minimum.z > maximum.z ? minimum.z : maximum.z);
		cnt.set(min).add(max).scl(0.5f);
		dim.set(max).sub(min);
		crn_dirty = true;
		return this;
	}

	/** Sets the bounding box minimum and maximum vector from the given points.
	 * 
	 * @param points The points.
	 * @return This bounding box for chaining. */
	public BoundingBox set (Vector3[] points) {
		this.inf();
		for (Vector3 l_point : points)
			this.ext(l_point);
		crn_dirty = true;
		return this;
	}

	/** Sets the bounding box minimum and maximum vector from the given points.
	 * 
	 * @param points The points.
	 * @return This bounding box for chaining. */
	public BoundingBox set (List<Vector3> points) {
		this.inf();
		for (Vector3 l_point : points)
			this.ext(l_point);
		crn_dirty = true;
		return this;
	}

	/** Sets the minimum and maximum vector to positive and negative infinity.
	 * 
	 * @return This bounding box for chaining. */
	public BoundingBox inf () {
		min.set(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
		max.set(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);
		cnt.set(0, 0, 0);
		dim.set(0, 0, 0);
		crn_dirty = true;
		return this;
	}

	/** Extends the bounding box to incorporate the given {@link Vector3}.
	 * 
	 * @param point The vector
	 * @return This bounding box for chaining. */
	public BoundingBox ext (Vector3 point) {
		crn_dirty = true;
		return this.set(min.set(min(min.x, point.x), min(min.y, point.y), min(min.z, point.z)),
			max.set(Math.max(max.x, point.x), Math.max(max.y, point.y), Math.max(max.z, point.z)));
	}

	/** Sets the minimum and maximum vector to zeros
	 * 
	 * @return This bounding box for chaining. */
	public BoundingBox clr () {
		crn_dirty = true;
		return this.set(min.set(0, 0, 0), max.set(0, 0, 0));
	}

	/** Returns whether this bounding box is valid. This means that min != max and min < max.
	 * 
	 * @return True in case the bounding box is valid, false otherwise */
	public boolean isValid () {
		return min.x < max.x && min.y < max.y && min.z < max.z;
	}

	/** Extends this bounding box by the given bounding box.
	 * 
	 * @param a_bounds The bounding box
	 * @return This bounding box for chaining. */
	public BoundingBox ext (BoundingBox a_bounds) {
		crn_dirty = true;
		return this.set(min.set(min(min.x, a_bounds.min.x), min(min.y, a_bounds.min.y), min(min.z, a_bounds.min.z)),
			max.set(max(max.x, a_bounds.max.x), max(max.y, a_bounds.max.y), max(max.z, a_bounds.max.z)));
	}

	/** Extends this bounding box by the given transformed bounding box.
	 * 
	 * @param bounds The bounding box
	 * @param transform The transformation matrix to apply to bounds, before using it to extend this bounding box.
	 * @return This bounding box for chaining. */
	public BoundingBox ext (BoundingBox bounds, Matrix4 transform) {
		bounds.updateCorners();
		for (Vector3 pnt : crn) {
			pnt.mul(transform);
			min.set(min(min.x, pnt.x), min(min.y, pnt.y), min(min.z, pnt.z));
			max.set(max(max.x, pnt.x), max(max.y, pnt.y), max(max.z, pnt.z));
		}
		crn_dirty = true;
		bounds.crn_dirty = true;
		return this.set(min, max);
	}

	/** Multiplies the bounding box by the given matrix. This is achieved by multiplying the 8 corner points and then calculating
	 * the minimum and maximum vectors from the transformed points.
	 * 
	 * @param matrix The matrix
	 * @return This bounding box for chaining. */
	public BoundingBox mul (Matrix4 matrix) {
		updateCorners();
		this.inf();
		for (Vector3 l_pnt : crn) {
			l_pnt.mul(matrix);
			min.set(min(min.x, l_pnt.x), min(min.y, l_pnt.y), min(min.z, l_pnt.z));
			max.set(max(max.x, l_pnt.x), max(max.y, l_pnt.y), max(max.z, l_pnt.z));
		}
		crn_dirty = true;
		return this.set(min, max);
	}

	/** Returns whether the given bounding box is contained in this bounding box.
	 * @param b The bounding box
	 * @return Whether the given bounding box is contained */
	public boolean contains (BoundingBox b) {
		return !isValid()
			|| (min.x <= b.min.x && min.y <= b.min.y && min.z <= b.min.z && max.x >= b.max.x && max.y >= b.max.y && max.z >= b.max.z);
	}

	/** Returns whether the given bounding box is intersecting this bounding box (at least one point in).
	 * @param b The bounding box
	 * @return Whether the given bounding box is intersected */
	public boolean intersects (BoundingBox b) {
		if (!isValid()) return false;

		// test using SAT (separating axis theorem)

		float lx = Math.abs(this.cnt.x - b.cnt.x);
		float sumx = (this.dim.x / 2.0f) + (b.dim.x / 2.0f);

		float ly = Math.abs(this.cnt.y - b.cnt.y);
		float sumy = (this.dim.y / 2.0f) + (b.dim.y / 2.0f);

		float lz = Math.abs(this.cnt.z - b.cnt.z);
		float sumz = (this.dim.z / 2.0f) + (b.dim.z / 2.0f);

		return (lx <= sumx && ly <= sumy && lz <= sumz);

	}

	/** Returns whether the given vector is contained in this bounding box.
	 * @param v The vector
	 * @return Whether the vector is contained or not. */
	public boolean contains (Vector3 v) {
		return min.x <= v.x && max.x >= v.x && min.y <= v.y && max.y >= v.y && min.z <= v.z && max.z >= v.z;
	}

	@Override
	public String toString () {
		return "[" + min + "|" + max + "]";
	}

	/** Extends the bounding box by the given vector.
	 * 
	 * @param x The x-coordinate
	 * @param y The y-coordinate
	 * @param z The z-coordinate
	 * @return This bounding box for chaining. */
	public BoundingBox ext (float x, float y, float z) {
		crn_dirty = true;
		return this.set(min.set(min(min.x, x), min(min.y, y), min(min.z, z)), max.set(max(max.x, x), max(max.y, y), max(max.z, z)));
	}

	static float min (float a, float b) {
		return a > b ? b : a;
	}

	static float max (float a, float b) {
		return a > b ? a : b;
	}
}

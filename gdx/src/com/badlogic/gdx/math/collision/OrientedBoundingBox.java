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

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

import java.io.Serializable;

public class OrientedBoundingBox implements Serializable {
	private static final long serialVersionUID = 3864065514676250557L;

	private final static Vector3 tmpVector = new Vector3();
	private final static Matrix4 tmpMatrix = new Matrix4();

	public final BoundingBox bounds = new BoundingBox();
	public final Matrix4 transform = new Matrix4();

	private Vector3[] vertices;
	private Vector3[] axes;

	/** Constructs a new oriented bounding box with the minimum and maximum vector set to zeros. */
	public OrientedBoundingBox () {
		bounds.clr();
	}

	/** Constructs a new oriented bounding box from the given bounding box.
	 *
	 * @param bounds The bounding box to copy */
	public OrientedBoundingBox (BoundingBox bounds) {
		this.bounds.set(bounds.min, bounds.max);
	}

	/** Constructs a new oriented bounding box from the given bounding box and transform.
	 *
	 * @param bounds The bounding box to copy
	 * @param transform The transformation matrix to copy */
	public OrientedBoundingBox (BoundingBox bounds, Matrix4 transform) {
		this.bounds.set(bounds.min, bounds.max);
		this.transform.set(transform);
	}

	public Vector3 getCorner000 (final Vector3 out) {
		return bounds.getCorner000(out).mul(transform);
	}

	public Vector3 getCorner001 (final Vector3 out) {
		return bounds.getCorner001(out).mul(transform);
	}

	public Vector3 getCorner010 (final Vector3 out) {
		return bounds.getCorner010(out).mul(transform);
	}

	public Vector3 getCorner011 (final Vector3 out) {
		return bounds.getCorner011(out).mul(transform);
	}

	public Vector3 getCorner100 (final Vector3 out) {
		return bounds.getCorner100(out).mul(transform);
	}

	public Vector3 getCorner101 (final Vector3 out) {
		return bounds.getCorner101(out).mul(transform);
	}

	public Vector3 getCorner110 (final Vector3 out) {
		return bounds.getCorner110(out).mul(transform);
	}

	public Vector3 getCorner111 (final Vector3 out) {
		return bounds.getCorner111(out).mul(transform);
	}

	/** Returns whether the given vector is contained in this oriented bounding box.
	 * @param v The vector
	 * @return Whether the vector is contained or not. */
	public boolean contains (Vector3 v) {
		return contains(v, tmpMatrix.set(transform).inv());
	}

	private boolean contains (Vector3 v, Matrix4 invTransform) {
		Vector3 localV = tmpVector.set(v).mul(invTransform);
		return bounds.contains(localV);
	}

	/** Returns whether the given bounding box is contained in this oriented bounding box.
	 * @param b The bounding box
	 * @return Whether the given bounding box is contained */
	public boolean contains (BoundingBox b) {
		tmpMatrix.set(transform).inv();
		return contains(b.getCorner000(tmpVector), tmpMatrix) && contains(b.getCorner001(tmpVector), tmpMatrix) &&
				contains(b.getCorner010(tmpVector), tmpMatrix) && contains(b.getCorner011(tmpVector), tmpMatrix) &&
				contains(b.getCorner100(tmpVector), tmpMatrix) && contains(b.getCorner101(tmpVector), tmpMatrix) &&
				contains(b.getCorner110(tmpVector), tmpMatrix) && contains(b.getCorner111(tmpVector), tmpMatrix);
	}

	/** Returns whether the given oriented bounding box is contained in this oriented bounding box.
	 * @param obb The oriented bounding box
	 * @return Whether the given oriented bounding box is contained */
	public boolean contains (OrientedBoundingBox obb) {
		tmpMatrix.set(transform).inv();
		return contains(obb.getCorner000(tmpVector), tmpMatrix) &&
				contains(obb.getCorner001(tmpVector), tmpMatrix) &&
				contains(obb.getCorner010(tmpVector), tmpMatrix) &&
				contains(obb.getCorner011(tmpVector), tmpMatrix) &&
				contains(obb.getCorner100(tmpVector), tmpMatrix) &&
				contains(obb.getCorner101(tmpVector), tmpMatrix) &&
				contains(obb.getCorner110(tmpVector), tmpMatrix) &&
				contains(obb.getCorner111(tmpVector), tmpMatrix);
	}

	/** Returns whether the given bounding box is intersecting this oriented bounding box (at least one point in).
	 * @param b The bounding box
	 * @return Whether the given bounding box is intersected */
	public boolean intersects (BoundingBox b) {
		Vector3[] aAxes = getAxes();
		Vector3[] bAxes = new Vector3[]{Vector3.X, Vector3.Y, Vector3.Z};

		Vector3[] allAxes = new Vector3[] {
				aAxes[0], aAxes[1], aAxes[2],
				bAxes[0], bAxes[1], bAxes[2],
				new Vector3(aAxes[0]).crs(bAxes[0]),
				new Vector3(aAxes[0]).crs(bAxes[1]),
				new Vector3(aAxes[0]).crs(bAxes[2]),
				new Vector3(aAxes[1]).crs(bAxes[0]),
				new Vector3(aAxes[1]).crs(bAxes[1]),
				new Vector3(aAxes[1]).crs(bAxes[2]),
				new Vector3(aAxes[2]).crs(bAxes[0]),
				new Vector3(aAxes[2]).crs(bAxes[1]),
				new Vector3(aAxes[2]).crs(bAxes[2])
		};

		Vector3[] aVertices = getVertices();
		Vector3[] bVertices = new Vector3[] {
				b.getCorner000(new Vector3()), b.getCorner001(new Vector3()), b.getCorner010(new Vector3()), b.getCorner011(new Vector3()),
				b.getCorner100(new Vector3()), b.getCorner101(new Vector3()), b.getCorner110(new Vector3()), b.getCorner111(new Vector3())
		};

		return Intersector.SAT.hasOverlap(allAxes, aVertices, bVertices);
	}

	/** Returns whether the given oriented bounding box is intersecting this oriented bounding box (at least one point in).
	 * @param obb The oriented bounding box
	 * @return Whether the given bounding box is intersected */
	public boolean intersects (OrientedBoundingBox obb) {
		Vector3[] aAxes = getAxes();
		Vector3[] bAxes = obb.getAxes();

		Vector3[] allAxes = new Vector3[] {
				aAxes[0], aAxes[1], aAxes[2],
				bAxes[0], bAxes[1], bAxes[2],
				new Vector3(aAxes[0]).crs(bAxes[0]),
				new Vector3(aAxes[0]).crs(bAxes[1]),
				new Vector3(aAxes[0]).crs(bAxes[2]),
				new Vector3(aAxes[1]).crs(bAxes[0]),
				new Vector3(aAxes[1]).crs(bAxes[1]),
				new Vector3(aAxes[1]).crs(bAxes[2]),
				new Vector3(aAxes[2]).crs(bAxes[0]),
				new Vector3(aAxes[2]).crs(bAxes[1]),
				new Vector3(aAxes[2]).crs(bAxes[2])
		};

		Vector3[] aVertices = getVertices();
		Vector3[] bVertices = obb.getVertices();

		return Intersector.SAT.hasOverlap(allAxes, aVertices, bVertices);
	}

	public Vector3[] getAxes() {
		if (axes == null) {
			axes = new Vector3[]{
					new Vector3(Vector3.X).mul(transform),
					new Vector3(Vector3.Y).mul(transform),
					new Vector3(Vector3.Z).mul(transform)
			};
		}
		return axes;
	}

	public Vector3[] getVertices() {
		if (vertices == null) {
			vertices = new Vector3[] {
					getCorner000(new Vector3()), getCorner001(new Vector3()), getCorner010(new Vector3()), getCorner011(new Vector3()),
					getCorner100(new Vector3()), getCorner101(new Vector3()), getCorner110(new Vector3()), getCorner111(new Vector3())
			};
		}

		return vertices;
	}

}

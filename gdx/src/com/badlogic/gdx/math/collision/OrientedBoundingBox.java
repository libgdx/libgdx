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

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

import java.io.Serializable;

public class OrientedBoundingBox implements Serializable {
	private static final long serialVersionUID = 3864065514676250557L;

	private final static Vector3 tmpVector = new Vector3();

	public final BoundingBox bounds = new BoundingBox();
	public final Matrix4 transform = new Matrix4();

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
		Vector3 localV = tmpVector.set(v).mul(transform);
		return bounds.contains(localV);
	}

	/** Returns whether the given bounding box is contained in this oriented bounding box.
	 * @param b The bounding box
	 * @return Whether the given bounding box is contained */
	public boolean contains (BoundingBox b) {
		return contains(b.getCorner000(tmpVector)) ||
				contains(b.getCorner001(tmpVector))||
				contains(b.getCorner010(tmpVector)) ||
				contains(b.getCorner011(tmpVector)) ||
				contains(b.getCorner100(tmpVector)) ||
				contains(b.getCorner101(tmpVector)) ||
				contains(b.getCorner110(tmpVector)) ||
				contains(b.getCorner111(tmpVector));
	}

}

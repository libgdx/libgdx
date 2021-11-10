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

	public final BoundingBox boundingBox = new BoundingBox();
	public final Matrix4 orientation = new Matrix4();

	/** Constructs a new oriented bounding box with the minimum and maximum vector set to zeros. */
	public OrientedBoundingBox () {
		boundingBox.clr();
		orientation.setToTranslation(0, 0, 0);
	}

	/** Constructs a new oriented bounding box from the given bounding box.
	 *
	 * @param bounds The bounding box to copy */
	public OrientedBoundingBox (BoundingBox bounds) {
		this.boundingBox.set(bounds);
	}

	/** Constructs the new oriented bounding box using the given minimum and maximum vector.
	 *
	 * @param minimum The minimum vector
	 * @param maximum The maximum vector */
	public OrientedBoundingBox (Vector3 minimum, Vector3 maximum) {
		this.boundingBox.set(minimum, maximum);
	}

	/** Sets the given bounding box and orientation.
	 *
	 * @param bounds The bounds.
	 * @param orientation The orientation.
	 * @return This oriented bounding box for chaining. */
	public OrientedBoundingBox set (BoundingBox bounds, Matrix4 orientation) {
		orientation.set(orientation);
		this.boundingBox.set(bounds.min, bounds.max);
		return this;
	}

	public Vector3 getCorner000 (final Vector3 out) {
		this.boundingBox.getCorner000(out);
		return out.mul(orientation);
	}

	public Vector3 getCorner001 (final Vector3 out) {
		this.boundingBox.getCorner001(out);
		return out.mul(orientation);
	}

	public Vector3 getCorner010 (final Vector3 out) {
		this.boundingBox.getCorner010(out);
		return out.mul(orientation);
	}

	public Vector3 getCorner011 (final Vector3 out) {
		this.boundingBox.getCorner011(out);
		return out.mul(orientation);
	}

	public Vector3 getCorner100 (final Vector3 out) {
		this.boundingBox.getCorner100(out);
		return out.mul(orientation);
	}

	public Vector3 getCorner101 (final Vector3 out) {
		this.boundingBox.getCorner101(out);
		return out.mul(orientation);
	}

	public Vector3 getCorner110 (final Vector3 out) {
		this.boundingBox.getCorner110(out);
		return out.mul(orientation);
	}

	public Vector3 getCorner111 (final Vector3 out) {
		this.boundingBox.getCorner111(out);
		return out.mul(orientation);
	}
}

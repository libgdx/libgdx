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

	public final BoundingBox bounds = new BoundingBox();
	public final Matrix4 transform = new Matrix4();

	/** Constructs a new oriented bounding box with the minimum and maximum vector set to zeros. */
	public OrientedBoundingBox () {
		bounds.clr();
	}

	public OrientedBoundingBox (BoundingBox bounds) {
		this.bounds.set(bounds.min, bounds.max);
	}

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

}

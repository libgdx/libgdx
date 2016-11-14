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

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.NumberUtils;

/** Encapsulates a 3D sphere with a center and a radius
 * 
 * @author badlogicgames@gmail.com */
public class Sphere implements Serializable {
	private static final long serialVersionUID = -6487336868908521596L;
	/** the radius of the sphere **/
	public float radius;
	/** the center of the sphere **/
	public final Vector3 center;

	private static final float PI_4_3 = MathUtils.PI * 4f / 3f;

	/** Constructs a sphere with the given center and radius
	 * @param center The center
	 * @param radius The radius */
	public Sphere (Vector3 center, float radius) {
		this.center = new Vector3(center);
		this.radius = radius;
	}

	/** @param sphere the other sphere
	 * @return whether this and the other sphere overlap */
	public boolean overlaps (Sphere sphere) {
		return center.dst2(sphere.center) < (radius + sphere.radius) * (radius + sphere.radius);
	}

	@Override
	public int hashCode () {
		final int prime = 71;
		int result = 1;
		result = prime * result + this.center.hashCode();
		result = prime * result + NumberUtils.floatToRawIntBits(this.radius);
		return result;
	}

	@Override
	public boolean equals (Object o) {
		if (this == o) return true;
		if (o == null || o.getClass() != this.getClass()) return false;
		Sphere s = (Sphere)o;
		return this.radius == s.radius && this.center.equals(s.center);
	}

	public float volume () {
		return PI_4_3 * this.radius * this.radius * this.radius;
	}

	public float surfaceArea () {
		return 4 * MathUtils.PI * this.radius * this.radius;
	}
}

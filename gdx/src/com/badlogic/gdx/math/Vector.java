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

package com.badlogic.gdx.math;

/** Encapsulates a general vector. Allows chaining operations by returning a reference to itself in all modification methods. See
 * {@link Vector2} and {@link Vector3} for specific implementations.
 * @author Xoppa */
public interface Vector<T extends Vector<T>> {
	/** @return a copy of this vector */
	T cpy ();

	/** @return The euclidian length */
	float len ();

	/** @return The squared euclidian length */
	float len2 ();
	
	/** Limits this vector's length to given value
	 * @return This vector for chaining */
	T limit (float limit);

	/** Clamps this vector's length to given value
	 * @param min Min length
	 * @param max Max length
	 * @return This vector for chaining */
	T clamp (float min, float max);
	
	/** Sets this vector from the given vector
	 * @param v The vector
	 * @return This vector for chaining */
	T set (T v);

	/** Substracts the given vector from this vector.
	 * @param v The vector
	 * @return This vector for chaining */
	T sub (T v);

	/** Normalizes this vector. Does nothing if it is zero.
	 * @return This vector for chaining */
	T nor ();

	/** Adds the given vector to this vector
	 * @param v The vector
	 * @return This vector for chaining */
	T add (T v);

	/** @param v The other vector
	 * @return The dot product between this and the other vector */
	float dot (T v);

	/** Scales this vector by a scalar
	 * @param scalar The scalar
	 * @return This vector for chaining */
	T scl (float scalar);

	/** Scales this vector by another vector
	 * @return This vector for chaining */
	T scl (T v);

	/** @param v The other vector
	 * @return the distance between this and the other vector */
	float dst (T v);

	/** @param v The other vector
	 * @return the squared distance between this and the other vector */
	float dst2 (T v);

	/** Linearly interpolates between this vector and the target vector by alpha which is in the range [0,1]. The result is stored
	 * in this vector.
	 * @param target The target vector
	 * @param alpha The interpolation coefficient
	 * @return This vector for chaining. */
	T lerp (T target, float alpha);

	// TODO: T crs(T v);
}

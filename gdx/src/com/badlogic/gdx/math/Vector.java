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

	/** This method is faster than {@link Vector#len()}
	 * because it avoids calculating a square root. It is useful for comparisons,
	 * but not for getting accurate lengths, as the return value is the square of the actual length.
	 * @return The squared euclidian length */
	float len2 ();

	/** Limits this vector's length to given value
	 * @return This vector for chaining */
	T limit (float limit);

	/** Clamps this vector's length to given min and max values
	 * @param min Min length
	 * @param max Max length
	 * @return This vector for chaining */
	T clamp (float min, float max);

	/** Sets this vector from the given vector
	 * @param v the vector to set from
	 * @return This vector for chaining */
	T set (T v);

	/** Substracts the given vector from this vector.
	 * @param v vector to subtract
	 * @return This vector for chaining */
	T sub (T v);

	/** Normalizes this vector. Does nothing if it is zero.
	 * @return This vector for chaining */
	T nor ();

	/** Adds the given vector to this vector
	 * @param v vector to add
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

	/** This method is faster than {@link Vector#dst(Vector)}
	 * because it avoids calculating a square root. It is useful for comparisons,
	 * but not for getting accurate distances, as the return value is the square of the actual distance.
	 * @param v The other vector
	 * @return the squared distance between this and the other vector */
	float dst2 (T v);

	/** Linearly interpolates between this vector and the target vector by alpha which is in the range [0,1]. The result is stored
	 * in this vector.
	 * @param target The target vector
	 * @param alpha The interpolation coefficient
	 * @return This vector for chaining. */
	T lerp (T target, float alpha);

	/** Adds the values of a vector that are first scaled (multiplied) by a scalar value.
	 * The Vector passed as argument is not modified.
	 * @param v The vector whose values will be first scaled by the scalar, then added
	 * @param scalar The scalar by which to scale the addition values
	 * @return This vector for chaining */
	T mad (T v, float scalar);

	/** @return Whether this vector is a unit length vector */
	public boolean isUnit ();

	/** @return Whether this vector is a unit length vector within the given margin. */
	public boolean isUnit(final float margin);

	/** @return Whether this vector is a zero vector */
	public boolean isZero ();

	/** @return Whether the length of this vector is smaller than the given margin */
	public boolean isZero (final float margin);

	/** @return Whether this vector is collinear with the given vector.
	 * The vectors need to be normalized for this to work.
	 * True if the normalized dot product is 1.
	 * @param vector the vector to check
	 * @param epsilon a positive small number close to zero */
	public boolean isCollinear(T vector, float epsilon);
	
	/** @return Whether this vector is collinear with the given vector.
	 * The vectors need to be normalized for this to work.
	 * True if the normalized dot product is 1.
	 * @param vector the vector to check */
	public boolean isCollinear(T vector);
	
	/** @return Whether this vector is collinear with the given vector but has opposite direction.
	 * True if the normalized dot product is -1.
	 * The vectors need to be normalized for this to work.
	 * @param vector the vector to check
	 * @param epsilon a positive small number close to zero */
	public boolean isCollinearOpposite(T vector, float epsilon);
	
	/** @return Whether this vector is collinear with the given vector but has opposite direction.
	 * True if the normalized dot product is -1.
	 * The vectors need to be normalized for this to work.
	 * @param vector the vector to check */
	public boolean isCollinearOpposite(T vector);
	
	/** @return Whether this vector is perpendicular with the given vector.
	 * True if the dot product is 0.*/
	public boolean isPerpendicular(T vector);
	
	/** @return Whether this vector is perpendicular with the given vector.
	 * True if the dot product is 0.
	 * @param epsilon a positive small number close to zero */
	public boolean isPerpendicular(T vector, float epsilon);
	
	/** @return Whether this vector has similar direction compared to the given vector.
	 * True if the normalized dot product is > 0.*/
	public boolean hasSameDirection(T vector);

	/** @return Whether this vector has opposite direction compared to the given vector.
	 * True if the normalized dot product is < 0.*/
	public boolean hasOppositeDirection(T vector);
	
	// TODO: T crs(T v);
}

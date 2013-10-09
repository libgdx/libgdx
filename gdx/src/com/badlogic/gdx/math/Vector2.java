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

import java.io.Serializable;

import com.badlogic.gdx.utils.NumberUtils;

/** Encapsulates a 2D vector. Allows chaining methods by returning a reference to itself
 * @author badlogicgames@gmail.com */
public class Vector2 implements Serializable, Vector<Vector2> {
	private static final long serialVersionUID = 913902788239530931L;

	public final static Vector2 X = new Vector2(1, 0);
	public final static Vector2 Y = new Vector2(0, 1);
	public final static Vector2 Zero = new Vector2(0, 0);

	/** the x-component of this vector **/
	public float x;
	/** the y-component of this vector **/
	public float y;

	/** Constructs a new vector at (0,0) */
	public Vector2 () {
	}

	/** Constructs a vector with the given components
	 * @param x The x-component
	 * @param y The y-component */
	public Vector2 (float x, float y) {
		this.x = x;
		this.y = y;
	}

	/** Constructs a vector from the given vector
	 * @param v The vector */
	public Vector2 (Vector2 v) {
		set(v);
	}

	/** @return a copy of this vector */
	public Vector2 cpy () {
		return new Vector2(this);
	}

	/** @return The euclidian length */
	public float len () {
		return (float)Math.sqrt(x * x + y * y);
	}

	/** @return The squared euclidian length */
	public float len2 () {
		return x * x + y * y;
	}

	/** Sets this vector from the given vector
	 * @param v The vector
	 * @return This vector for chaining */
	public Vector2 set (Vector2 v) {
		x = v.x;
		y = v.y;
		return this;
	}

	/** Sets the components of this vector
	 * @param x The x-component
	 * @param y The y-component
	 * @return This vector for chaining */
	public Vector2 set (float x, float y) {
		this.x = x;
		this.y = y;
		return this;
	}

	/** Subtracts the given vector from this vector.
	 * @param v The vector
	 * @return This vector for chaining */
	public Vector2 sub (Vector2 v) {
		x -= v.x;
		y -= v.y;
		return this;
	}

	/** Normalizes this vector. Does nothing if it is zero.
	 * @return This vector for chaining */
	public Vector2 nor () {
		float len = len();
		if (len != 0) {
			x /= len;
			y /= len;
		}
		return this;
	}

	/** Adds the given vector to this vector
	 * @param v The vector
	 * @return This vector for chaining */
	public Vector2 add (Vector2 v) {
		x += v.x;
		y += v.y;
		return this;
	}

	/** Adds the given components to this vector
	 * @param x The x-component
	 * @param y The y-component
	 * @return This vector for chaining */
	public Vector2 add (float x, float y) {
		this.x += x;
		this.y += y;
		return this;
	}

	/** @param v The other vector
	 * @return The dot product between this and the other vector */
	public float dot (Vector2 v) {
		return x * v.x + y * v.y;
	}

	/** Multiplies this vector by a scalar
	 * @param scalar The scalar
	 * @return This vector for chaining */
	public Vector2 scl (float scalar) {
		x *= scalar;
		y *= scalar;
		return this;
	}
	
	/** @deprecated Use {@link #scl(float)} instead. */
	public Vector2 mul (float scalar) {
		return scl(scalar);
	}

	/** Multiplies this vector by a scalar
	 * @return This vector for chaining */
	public Vector2 scl (float x, float y) {
		this.x *= x;
		this.y *= y;
		return this;
	}
	
	/** @deprecated Use {@link #scl(float, float)} instead. */
	public Vector2 mul (float x, float y) {
		return scl(x,y);
	}
	
	/** Multiplies this vector by a vector
	 * @return This vector for chaining */
	public Vector2 scl (Vector2 v) {
		this.x *= v.x;
		this.y *= v.y;
		return this;
	}
	
	/** @deprecated Use {@link #scl(Vector2)} instead. */
	public Vector2 mul (Vector2 v) {
		return scl(v);
	}

	public Vector2 div (float value) {
		return this.scl(1 / value);
	}

	public Vector2 div (float vx, float vy) {
		return this.scl(1 / vx, 1 / vy);
	}

	public Vector2 div (Vector2 other) {
		return this.scl(1 / other.x, 1 / other.y);
	}

	/** @param v The other vector
	 * @return the distance between this and the other vector */
	public float dst (Vector2 v) {
		final float x_d = v.x - x;
		final float y_d = v.y - y;
		return (float)Math.sqrt(x_d * x_d + y_d * y_d);
	}

	/** @param x The x-component of the other vector
	 * @param y The y-component of the other vector
	 * @return the distance between this and the other vector */
	public float dst (float x, float y) {
		final float x_d = x - this.x;
		final float y_d = y - this.y;
		return (float)Math.sqrt(x_d * x_d + y_d * y_d);
	}

	/** @param v The other vector
	 * @return the squared distance between this and the other vector */
	public float dst2 (Vector2 v) {
		final float x_d = v.x - x;
		final float y_d = v.y - y;
		return x_d * x_d + y_d * y_d;
	}

	/** @param x The x-component of the other vector
	 * @param y The y-component of the other vector
	 * @return the squared distance between this and the other vector */
	public float dst2 (float x, float y) {
		final float x_d = x - this.x;
		final float y_d = y - this.y;
		return x_d * x_d + y_d * y_d;
	}
	
	/** Limits this vector's length to given value
	 * @param limit Max length
	 * @return This vector for chaining */
	public Vector2 limit (float limit) {
		if (len2() > limit * limit) {
			nor();
			scl(limit);
		}
		return this;
	}
	
	/** Clamps this vector's length to given value
	 * @param min Min length
	 * @param max Max length
	 * @return This vector for chaining */
	public Vector2 clamp (float min, float max) {
		final float l2 = len2();
		if (l2 == 0f)
			return this;
		if (l2 > max * max)
			return nor().scl(max);
		if (l2 < min * min)
			return nor().scl(min);
		return this;
	}

	public String toString () {
		return "[" + x + ":" + y + "]";
	}

	/** Substracts the other vector from this vector.
	 * @param x The x-component of the other vector
	 * @param y The y-component of the other vector
	 * @return This vector for chaining */
	public Vector2 sub (float x, float y) {
		this.x -= x;
		this.y -= y;
		return this;
	}

	/** Left-multiplies this vector by the given matrix
	 * @param mat the matrix
	 * @return this vector */
	public Vector2 mul (Matrix3 mat) {
		float x = this.x * mat.val[0] + this.y * mat.val[3] + mat.val[6];
		float y = this.x * mat.val[1] + this.y * mat.val[4] + mat.val[7];
		this.x = x;
		this.y = y;
		return this;
	}

	/** Calculates the 2D cross product between this and the given vector.
	 * @param v the other vector
	 * @return the cross product */
	public float crs (Vector2 v) {
		return this.x * v.y - this.y * v.x;
	}

	/** Calculates the 2D cross product between this and the given vector.
	 * @param x the x-coordinate of the other vector
	 * @param y the y-coordinate of the other vector
	 * @return the cross product */
	public float crs (float x, float y) {
		return this.x * y - this.y * x;
	}

	/** @return the angle in degrees of this vector (point) relative to the x-axis.
	 * Angles are towards the positive y-axis (typically counter-clockwise) and between 0 and 360. */
	public float angle () {
		float angle = (float)Math.atan2(y, x) * MathUtils.radiansToDegrees;
		if (angle < 0) angle += 360;
		return angle;
	}

	/** Sets the angle of the vector in degrees relative to the x-axis, towards the positive y-axis (typically counter-clockwise).
	 * @param degrees The angle to set. */
	public Vector2 setAngle (float degrees) {
		this.set(len(), 0f);
		this.rotate(degrees);

		return this;
	}

	/** Rotates the Vector2 by the given angle, counter-clockwise assuming the y-axis points up.
	 * @param degrees the angle in degrees */
	public Vector2 rotate (float degrees) {
		float rad = degrees * MathUtils.degreesToRadians;
		float cos = (float)Math.cos(rad);
		float sin = (float)Math.sin(rad);

		float newX = this.x * cos - this.y * sin;
		float newY = this.x * sin + this.y * cos;

		this.x = newX;
		this.y = newY;

		return this;
	}

	/** Linearly interpolates between this vector and the target vector by alpha which is in the range [0,1]. The result is stored
	 * in this vector.
	 * 
	 * @param target The target vector
	 * @param alpha The interpolation coefficient
	 * @return This vector for chaining. */
	public Vector2 lerp (Vector2 target, float alpha) {
		final float invAlpha = 1.0f - alpha;
		this.x = (x * invAlpha) + (target.x * alpha);
		this.y = (y * invAlpha) + (target.y * alpha);
		return this;
	}

	@Override
	public int hashCode () {
		final int prime = 31;
		int result = 1;
		result = prime * result + NumberUtils.floatToIntBits(x);
		result = prime * result + NumberUtils.floatToIntBits(y);
		return result;
	}

	@Override
	public boolean equals (Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Vector2 other = (Vector2)obj;
		if (NumberUtils.floatToIntBits(x) != NumberUtils.floatToIntBits(other.x)) return false;
		if (NumberUtils.floatToIntBits(y) != NumberUtils.floatToIntBits(other.y)) return false;
		return true;
	}

	/** Compares this vector with the other vector, using the supplied epsilon for fuzzy equality testing.
	 * @param obj
	 * @param epsilon
	 * @return whether the vectors are the same. */
	public boolean epsilonEquals (Vector2 obj, float epsilon) {
		if (obj == null) return false;
		if (Math.abs(obj.x - x) > epsilon) return false;
		if (Math.abs(obj.y - y) > epsilon) return false;
		return true;
	}
	
	/** Compares this vector with the other vector, using the supplied epsilon for fuzzy equality testing.
	 * @param x
	 * @param y
	 * @param epsilon
	 * @return whether the vectors are the same. */
	public boolean epsilonEquals (float x, float y, float epsilon) {
		if (Math.abs(x - this.x) > epsilon) return false;
		if (Math.abs(y - this.y) > epsilon) return false;
		return true;
	}
}

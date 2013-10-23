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

/** A simple quaternion class. See 
 * <a href="http://en.wikipedia.org/wiki/Quaternion">http://en.wikipedia.org/wiki/Quaternion</a>
 * for more information.
 * 
 * @author badlogicgames@gmail.com
 * @author vesuvio */
public class Quaternion implements Serializable {
	private static final long serialVersionUID = -7661875440774897168L;
	private static final float NORMALIZATION_TOLERANCE = 0.00001f;
	private static Quaternion tmp1 = new Quaternion(0, 0, 0, 0);
	private static Quaternion tmp2 = new Quaternion(0, 0, 0, 0);

	public float x;
	public float y;
	public float z;
	public float w;

	/** Constructor, sets the four components of the quaternion.
	 * @param x The x-component
	 * @param y The y-component
	 * @param z The z-component
	 * @param w The w-component */
	public Quaternion (float x, float y, float z, float w) {
		this.set(x, y, z, w);
	}

	public Quaternion () {
		idt();
	}

	/** Constructor, sets the quaternion components from the given quaternion.
	 * 
	 * @param quaternion The quaternion to copy. */
	public Quaternion (Quaternion quaternion) {
		this.set(quaternion);
	}

	/** Constructor, sets the quaternion from the given axis vector and the angle around that axis in degrees.
	 * 
	 * @param axis The axis
	 * @param angle The angle in degrees. */
	public Quaternion (Vector3 axis, float angle) {
		this.set(axis, angle);
	}

	/** Sets the components of the quaternion
	 * @param x The x-component
	 * @param y The y-component
	 * @param z The z-component
	 * @param w The w-component
	 * @return This quaternion for chaining */
	public Quaternion set (float x, float y, float z, float w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
		return this;
	}

	/** Sets the quaternion components from the given quaternion.
	 * @param quaternion The quaternion.
	 * @return This quaternion for chaining. */
	public Quaternion set (Quaternion quaternion) {
		return this.set(quaternion.x, quaternion.y, quaternion.z, quaternion.w);
	}

	/** Sets the quaternion components from the given axis and angle around that axis.
	 * 
	 * @param axis The axis
	 * @param angle The angle in degrees
	 * @return This quaternion for chaining. */
	public Quaternion set (Vector3 axis, float angle) {
		return setFromAxis(axis.x, axis.y, axis.z, angle);
	}

	/** @return a copy of this quaternion */
	public Quaternion cpy () {
		return new Quaternion(this);
	}

	/** @return the euclidian length of this quaternion */
	public float len () {
		return (float)Math.sqrt(x * x + y * y + z * z + w * w);
	}

	/** {@inheritDoc} */
	public String toString () {
		return "[" + x + "|" + y + "|" + z + "|" + w + "]";
	}

	/** Sets the quaternion to the given euler angles.
	 * @param yaw the yaw in degrees
	 * @param pitch the pitch in degress
	 * @param roll the roll in degess
	 * @return this quaternion */
	public Quaternion setEulerAngles (float yaw, float pitch, float roll) {
		yaw = (float)Math.toRadians(yaw);
		pitch = (float)Math.toRadians(pitch);
		roll = (float)Math.toRadians(roll);
		float num9 = roll * 0.5f;
		float num6 = (float)Math.sin(num9);
		float num5 = (float)Math.cos(num9);
		float num8 = pitch * 0.5f;
		float num4 = (float)Math.sin(num8);
		float num3 = (float)Math.cos(num8);
		float num7 = yaw * 0.5f;
		float num2 = (float)Math.sin(num7);
		float num = (float)Math.cos(num7);
		float f1 = num * num4;
		float f2 = num2 * num3;
		float f3 = num * num3;
		float f4 = num2 * num4;

		x = (f1 * num5) + (f2 * num6);
		y = (f2 * num5) - (f1 * num6);
		z = (f3 * num6) - (f4 * num5);
		w = (f3 * num5) + (f4 * num6);
		return this;
	}

	/** @return the length of this quaternion without square root */
	public float len2 () {
		return x * x + y * y + z * z + w * w;
	}

	/** Normalizes this quaternion to unit length
	 * @return the quaternion for chaining */
	public Quaternion nor () {
		float len = len2();
		if (len != 0.f && (Math.abs(len - 1.0f) > NORMALIZATION_TOLERANCE)) {
			len = (float)Math.sqrt(len);
			w /= len;
			x /= len;
			y /= len;
			z /= len;
		}
		return this;
	}

	/** Conjugate the quaternion.
	 * 
	 * @return This quaternion for chaining */
	public Quaternion conjugate () {
		x = -x;
		y = -y;
		z = -z;
		return this;
	}

	// TODO : this would better fit into the vector3 class
	/** Transforms the given vector using this quaternion
	 * 
	 * @param v Vector to transform */
	public Vector3 transform (Vector3 v) {
		tmp2.set(this);
		tmp2.conjugate();
		tmp2.mulLeft(tmp1.set(v.x, v.y, v.z, 0)).mulLeft(this);

		v.x = tmp2.x;
		v.y = tmp2.y;
		v.z = tmp2.z;
		return v;
	}

	/** Multiplies this quaternion with another one
	 * 
	 * @param q Quaternion to multiply with
	 * @return This quaternion for chaining */
	public Quaternion mul (Quaternion q) {
		final float newX = w * q.x + x * q.w + y * q.z - z * q.y;
		final float newY = w * q.y + y * q.w + z * q.x - x * q.z;
		final float newZ = w * q.z + z * q.w + x * q.y - y * q.x;
		final float newW = w * q.w - x * q.x - y * q.y - z * q.z;
		x = newX;
		y = newY;
		z = newZ;
		w = newW;
		return this;
	}

	/** Multiplies this quaternion with another one in the form of q * this
	 * 
	 * @param q Quaternion to multiply with
	 * @return This quaternion for chaining */
	public Quaternion mulLeft (Quaternion q) {
		final float newX = q.w * x + q.x * w + q.y * z - q.z * y;
		final float newY = q.w * y + q.y * w + q.z * x - q.x * z;
		final float newZ = q.w * z + q.z * w + q.x * y - q.y * x;
		final float newW = q.w * w - q.x * x - q.y * y - q.z * z;
		x = newX;
		y = newY;
		z = newZ;
		w = newW;
		return this;
	}

	// TODO : the matrix4 set(quaternion) doesnt set the last row+col of the matrix to 0,0,0,1 so... that's why there is this
// method
	/** Fills a 4x4 matrix with the rotation matrix represented by this quaternion.
	 * 
	 * @param matrix Matrix to fill */
	public void toMatrix (final float[] matrix) {
		final float xx = x * x;
		final float xy = x * y;
		final float xz = x * z;
		final float xw = x * w;
		final float yy = y * y;
		final float yz = y * z;
		final float yw = y * w;
		final float zz = z * z;
		final float zw = z * w;
		// Set matrix from quaternion
		matrix[Matrix4.M00] = 1 - 2 * (yy + zz);
		matrix[Matrix4.M01] = 2 * (xy - zw);
		matrix[Matrix4.M02] = 2 * (xz + yw);
		matrix[Matrix4.M03] = 0;
		matrix[Matrix4.M10] = 2 * (xy + zw);
		matrix[Matrix4.M11] = 1 - 2 * (xx + zz);
		matrix[Matrix4.M12] = 2 * (yz - xw);
		matrix[Matrix4.M13] = 0;
		matrix[Matrix4.M20] = 2 * (xz - yw);
		matrix[Matrix4.M21] = 2 * (yz + xw);
		matrix[Matrix4.M22] = 1 - 2 * (xx + yy);
		matrix[Matrix4.M23] = 0;
		matrix[Matrix4.M30] = 0;
		matrix[Matrix4.M31] = 0;
		matrix[Matrix4.M32] = 0;
		matrix[Matrix4.M33] = 1;
	}

	/** Sets the quaternion to an identity Quaternion
	 * @return this quaternion for chaining */
	public Quaternion idt () {
		return this.set(0, 0, 0, 1);
	}

	// todo : the setFromAxis(v3,float) method should replace the set(v3,float) method
	/** Sets the quaternion components from the given axis and angle around that axis.
	 * 
	 * @param axis The axis
	 * @param angle The angle in degrees
	 * @return This quaternion for chaining. */
	public Quaternion setFromAxis (final Vector3 axis, final float angle) {
		return setFromAxis(axis.x, axis.y, axis.z, angle);
	}

	/** Sets the quaternion components from the given axis and angle around that axis.
	 * @param x X direction of the axis
	 * @param y Y direction of the axis
	 * @param z Z direction of the axis
	 * @param angle The angle in degrees
	 * @return This quaternion for chaining. */
	public Quaternion setFromAxis (final float x, final float y, final float z, final float angle) {
		float d = Vector3.len(x, y, z);
		if (d == 0f)
			return idt();
		d = 1f /d;
		float l_ang = angle * MathUtils.degreesToRadians;
		float l_sin = (float)Math.sin(l_ang / 2);
		float l_cos = (float)Math.cos(l_ang / 2);
		return this.set(d * x * l_sin, d * y * l_sin, d * z * l_sin, l_cos).nor();
	}

// fromRotationMatrix(xAxis.x, yAxis.x, zAxis.x, xAxis.y, yAxis.y, zAxis.y,
// xAxis.z, yAxis.z, zAxis.z);

// final float m00, final float m01, final float m02, final float m10,
// final float m11, final float m12, final float m20, final float m21, final float m22

	public Quaternion setFromMatrix (Matrix4 matrix) {
		return setFromAxes(matrix.val[Matrix4.M00], matrix.val[Matrix4.M01], matrix.val[Matrix4.M02], matrix.val[Matrix4.M10],
			matrix.val[Matrix4.M11], matrix.val[Matrix4.M12], matrix.val[Matrix4.M20], matrix.val[Matrix4.M21],
			matrix.val[Matrix4.M22]);
	}

	/** <p>
	 * Sets the Quaternion from the given x-, y- and z-axis which have to be orthonormal.
	 * </p>
	 * 
	 * <p>
	 * Taken from Bones framework for JPCT, see http://www.aptalkarga.com/bones/ which in turn took it from Graphics Gem code at
	 * ftp://ftp.cis.upenn.edu/pub/graphics/shoemake/quatut.ps.Z.
	 * </p>
	 * 
	 * @param xx x-axis x-coordinate
	 * @param xy x-axis y-coordinate
	 * @param xz x-axis z-coordinate
	 * @param yx y-axis x-coordinate
	 * @param yy y-axis y-coordinate
	 * @param yz y-axis z-coordinate
	 * @param zx z-axis x-coordinate
	 * @param zy z-axis y-coordinate
	 * @param zz z-axis z-coordinate */
	public Quaternion setFromAxes (float xx, float xy, float xz, float yx, float yy, float yz, float zx, float zy, float zz) {
		// the trace is the sum of the diagonal elements; see
		// http://mathworld.wolfram.com/MatrixTrace.html
		final float m00 = xx, m01 = xy, m02 = xz;
		final float m10 = yx, m11 = yy, m12 = yz;
		final float m20 = zx, m21 = zy, m22 = zz;
		final float t = m00 + m11 + m22;

		// we protect the division by s by ensuring that s>=1
		double x, y, z, w;
		if (t >= 0) { // |w| >= .5
			double s = Math.sqrt(t + 1); // |s|>=1 ...
			w = 0.5 * s;
			s = 0.5 / s; // so this division isn't bad
			x = (m21 - m12) * s;
			y = (m02 - m20) * s;
			z = (m10 - m01) * s;
		} else if ((m00 > m11) && (m00 > m22)) {
			double s = Math.sqrt(1.0 + m00 - m11 - m22); // |s|>=1
			x = s * 0.5; // |x| >= .5
			s = 0.5 / s;
			y = (m10 + m01) * s;
			z = (m02 + m20) * s;
			w = (m21 - m12) * s;
		} else if (m11 > m22) {
			double s = Math.sqrt(1.0 + m11 - m00 - m22); // |s|>=1
			y = s * 0.5; // |y| >= .5
			s = 0.5 / s;
			x = (m10 + m01) * s;
			z = (m21 + m12) * s;
			w = (m02 - m20) * s;
		} else {
			double s = Math.sqrt(1.0 + m22 - m00 - m11); // |s|>=1
			z = s * 0.5; // |z| >= .5
			s = 0.5 / s;
			x = (m02 + m20) * s;
			y = (m21 + m12) * s;
			w = (m10 - m01) * s;
		}

		return set((float)x, (float)y, (float)z, (float)w);
	}
	
	/** Set this quaternion to the rotation between two vectors.
	 * @param v1 The base vector, which should be normalized.
	 * @param v2 The target vector, which should be normalized.
	 * @return This quaternion for chaining */
	public Quaternion setFromCross (final Vector3 v1, final Vector3 v2) {
		final float dot = MathUtils.clamp(v1.dot(v2), -1f, 1f);
		final float angle = (float)Math.acos(dot) * MathUtils.radiansToDegrees;
		return setFromAxis(v1.y * v2.z - v1.z * v2.y, v1.z * v2.x - v1.x * v2.z, v1.x * v2.y - v1.y * v2.x, angle);
	}
	
	/** Set this quaternion to the rotation between two vectors.
	 * @param x1 The base vectors x value, which should be normalized.
	 * @param y1 The base vectors y value, which should be normalized.
	 * @param z1 The base vectors z value, which should be normalized.
	 * @param x2 The target vector x value, which should be normalized.
	 * @param y2 The target vector y value, which should be normalized.
	 * @param z2 The target vector z value, which should be normalized.
	 * @return This quaternion for chaining */
	public Quaternion setFromCross (final float x1, final float y1, final float z1, final float x2, final float y2, final float z2) {
		final float dot = MathUtils.clamp(Vector3.dot(x1, y1, z1, x2, y2, z2), -1f, 1f);
		final float angle = (float)Math.acos(dot) * MathUtils.radiansToDegrees;
		return setFromAxis(y1 * z2 - z1 * y2, z1 * x2 - x1 * z2, x1 * y2 - y1 * x2, angle);
	}

	/** Spherical linear interpolation between this quaternion and the other quaternion, based on the alpha value in the range
	 * [0,1]. Taken from. Taken from Bones framework for JPCT, see http://www.aptalkarga.com/bones/
	 * @param end the end quaternion
	 * @param alpha alpha in the range [0,1]
	 * @return this quaternion for chaining */
	public Quaternion slerp (Quaternion end, float alpha) {
		final float dot = dot(end);
		float absDot = dot < 0.f ? -dot : dot;

		// Set the first and second scale for the interpolation
		float scale0 = 1 - alpha;
		float scale1 = alpha;

		// Check if the angle between the 2 quaternions was big enough to
		// warrant such calculations
		if ((1 - absDot) > 0.1) {// Get the angle between the 2 quaternions,
			// and then store the sin() of that angle
			final double angle = Math.acos(absDot);
			final double invSinTheta = 1f / Math.sin(angle);

			// Calculate the scale for q1 and q2, according to the angle and
			// it's sine value
			scale0 = (float)(Math.sin((1 - alpha) * angle) * invSinTheta);
			scale1 = (float)(Math.sin((alpha * angle)) * invSinTheta);
		}

		if (dot < 0.f)
			scale1 = -scale1;
		
		// Calculate the x, y, z and w values for the quaternion by using a
		// special form of linear interpolation for quaternions.
		x = (scale0 * x) + (scale1 * end.x);
		y = (scale0 * y) + (scale1 * end.y);
		z = (scale0 * z) + (scale1 * end.z);
		w = (scale0 * w) + (scale1 * end.w);

		// Return the interpolated quaternion
		return this;
	}

	public boolean equals (final Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Quaternion)) {
			return false;
		}
		final Quaternion comp = (Quaternion)o;
		return this.x == comp.x && this.y == comp.y && this.z == comp.z && this.w == comp.w;

	}

	/** Dot product between this and the other quaternion.
	 * @param other the other quaternion.
	 * @return this quaternion for chaining. */
	public float dot (Quaternion other) {
		return x * other.x + y * other.y + z * other.z + w * other.w;
	}

	/** Multiplies the components of this quaternion with the given scalar.
	 * @param scalar the scalar.
	 * @return this quaternion for chaining. */
	public Quaternion mul (float scalar) {
		this.x *= scalar;
		this.y *= scalar;
		this.z *= scalar;
		this.w *= scalar;
		return this;
	}
	
	/**
	 * Get the angle and the axis of rotation
	 * @param axis axis to get
	 * @return the angle
	 */
	public float getAxisAngle(Vector3 axis) {
		//source : http://www.euclideanspace.com/maths/geometry/rotations/conversions/quaternionToAngle/
      if (this.w > 1)
      	this.nor(); // if w>1 acos and sqrt will produce errors, this cant happen if quaternion is normalised
      float angle = (float) (2.0 * Math.acos(this.w));
      double s = Math.sqrt(1 - this.w * this.w); // assuming quaternion normalised then w is less than 1, so term always positive.
      if (s < NORMALIZATION_TOLERANCE) { // test to avoid divide by zero, s is always positive due to sqrt
              // if s close to zero then direction of axis not important
              axis.x = this.x; // if it is important that axis is normalised then replace with x=1; y=z=0;
              axis.y = this.y;
              axis.z = this.z;
      } else {
              axis.x = (float) (this.x / s); // normalise axis
              axis.y = (float) (this.y / s);
              axis.z = (float) (this.z / s);
      }

      return MathUtils.radiansToDegrees * angle;
	}
}

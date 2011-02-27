/*
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com), Nathan Sweet (admin@esotericsoftware.com), Vevusio (vevusio@gmx.at)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.badlogic.gdx.math;

import java.io.Serializable;

import com.badlogic.gdx.utils.MathUtils;

/**
 * A simple quaternion class. See http://en.wikipedia.org/wiki/Quaternion for more information.
 * 
 * @author badlogicgames@gmail.com
 * @author vesuvio
 * 
 */
public class Quaternion implements Serializable {
	private static final long serialVersionUID = -7661875440774897168L;
	private static final float NORMALIZATION_TOLERANCE = 0.00001f;			
	private static Quaternion tmp1 = new Quaternion(0,0,0,0);
	private static Quaternion tmp2 = new Quaternion(0,0,0,0);	
	
	public float x;
	public float y;
	public float z;
	public float w;

	/**
	 * Constructor, sets the four components of the quaternion.
	 * @param x The x-component
	 * @param y The y-component
	 * @param z The z-component
	 * @param w The w-component
	 */
	public Quaternion (float x, float y, float z, float w) {
		this.set(x, y, z, w);
	}

	Quaternion () {

	}

	/**
	 * Constructor, sets the quaternion components from the given quaternion.
	 * 
	 * @param quaternion The quaternion to copy.
	 */
	public Quaternion (Quaternion quaternion) {
		this.set(quaternion);
	}

	/**
	 * Constructor, sets the quaternion from the given axis vector and the angle around that axis in degrees.
	 * 
	 * @param axis The axis
	 * @param angle The angle in degrees.
	 */
	public Quaternion (Vector3 axis, float angle) {
		this.set(axis, angle);
	}

	/**
	 * Sets the components of the quaternion
	 * @param x The x-component
	 * @param y The y-component
	 * @param z The z-component
	 * @param w The w-component
	 * @return This quaternion for chaining
	 */
	public Quaternion set (float x, float y, float z, float w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
		return this;
	}

	/**
	 * Sets the quaternion components from the given quaternion.
	 * @param quaternion The quaternion.
	 * @return This quaternion for chaining.
	 */
	public Quaternion set (Quaternion quaternion) {
		return this.set(quaternion.x, quaternion.y, quaternion.z, quaternion.w);
	}

	/**
	 * Sets the quaternion components from the given axis and angle around that axis.
	 * 
	 * @param axis The axis
	 * @param angle The angle in degrees
	 * @return This quaternion for chaining.
	 */
	public Quaternion set (Vector3 axis, float angle) {
		float l_ang = (float)Math.toRadians(angle);
		float l_sin = (float)Math.sin(l_ang / 2);
		float l_cos = (float)Math.cos(l_ang / 2);
		return this.set(axis.x * l_sin, axis.y * l_sin, axis.z * l_sin, l_cos).nor();
	}

	/**
	 * @return a copy of this quaternion
	 */
	public Quaternion cpy () {
		return new Quaternion(this);
	}

	/**
	 * @return the euclidian length of this quaternion
	 */
	public float len () {
		return (float)Math.sqrt(x * x + y * y + z * z + w * w);
	}

	/**
	 * {@inheritDoc}
	 */
	public String toString () {
		return "[" + x + "|" + y + "|" + z + "|" + w + "]";
	}

	/**
	 * Sets the quaternion to the given euler angles.
	 * @param yaw the yaw in degrees
	 * @param pitch the pitch in degress
	 * @param roll the roll in degess
	 * @return this quaternion
	 */
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
		x = ((num * num4) * num5) + ((num2 * num3) * num6);
		y = ((num2 * num3) * num5) - ((num * num4) * num6);
		z = ((num * num3) * num6) - ((num2 * num4) * num5);
		w = ((num * num3) * num5) + ((num2 * num4) * num6);
		return this;
	}

	/**
	 * @return the length of this quaternion without square root
	 */
	public float len2() {
		return x * x + y * y + z * z + w * w;
	}

	/**
	 * Normalizes this quaternion to unit length
	 * @return the quaternion for chaining
	 */
	public Quaternion nor() {
		float len = len2();
		if(len != 0.f &&
		   (Math.abs(len - 1.0f) > NORMALIZATION_TOLERANCE)) {
			len = (float) Math.sqrt(len);
			w /= len;
			x /= len;
			y /= len;
			z /= len;
		}
		return this;
	}

	/**
	 * Conjugate the quaternion.
	 *
	 * @return This quaternion for chaining
	 */
	public Quaternion conjugate() {
		x = -x;
		y = -y;
		z = -z;
		return this;
	}

	//TODO : this would better fit into the vector3 class
	/**
	 * Transforms the given vector using this quaternion
	 *
	 * @param v Vector to transform
	 */
	public void transform(Vector3 v) {
		tmp2.set(this);
		tmp2.conjugate();
		tmp2.mulLeft(tmp1.set(v.x, v.y, v.z, 0)).mulLeft(this);

		v.x = tmp2.x;
		v.y = tmp2.y;
		v.z = tmp2.z;
	}

	/**
	 * Multiplies this quaternion with another one
	 *
	 * @param q Quaternion to multiply with
	 * @return This quaternion for chaining
	 */
	public Quaternion mul(Quaternion q) {
		float newX = w * q.x + x * q.w + y * q.z - z * q.y;
		float newY = w * q.y + y * q.w + z * q.x - x * q.z;
		float newZ = w * q.z + z * q.w + x * q.y - y * q.x;
		float newW = w * q.w - x * q.x - y * q.y - z * q.z;
		x = newX;
		y = newY;
		z = newZ;
		w = newW;
		return this;
	}

	/**
	 * Multiplies this quaternion with another one in the form of q * this
	 *
	 * @param q Quaternion to multiply with
	 * @return This quaternion for chaining
	 */
	public Quaternion mulLeft(Quaternion q) {
		float newX = q.w * x + q.x * w + q.y * z - q.z * y;
		float newY = q.w * y + q.y * w + q.z * x - q.x * z;
		float newZ = q.w * z + q.z * w + q.x * y - q.y * x;
		float newW = q.w * w - q.x * x - q.y * y - q.z * z;
		x = newX;
		y = newY;
		z = newZ;
		w = newW;
		return this;
	}

	//TODO : the matrix4 set(quaternion) doesnt set the last row+col of the matrix to 0,0,0,1 so... that's why there is this method
	/**
	 * Fills a 4x4 matrix with the rotation matrix represented by this quaternion.
	 *
	 * @param matrix Matrix to fill
	 */
	public void toMatrix(float[] matrix) {
		float xx = x * x;
		float xy = x * y;
		float xz = x * z;
		float xw = x * w;
		float yy = y * y;
		float yz = y * z;
		float yw = y * w;
		float zz = z * z;
		float zw = z * w;
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

	/**
	 * Returns the identity quaternion x,y,z = 0 and w=1
	 *
	 * @return Identity quaternion
	 */
	public static Quaternion idt() {
		return new Quaternion(0, 0, 0, 1);
	}

	//todo : the setFromAxis(v3,float) method should replace the set(v3,float) method
	/**
	 * Sets the quaternion components from the given axis and angle around that axis.
	 *
	 * @param axis The axis
	 * @param angle The angle in degrees
	 * @return This quaternion for chaining.
	 */
	public Quaternion setFromAxis(Vector3 axis, float angle) {
		return setFromAxis(axis.z, axis.y, axis.z, angle);
	}

	/**
	 * Sets the quaternion components from the given axis and angle around that axis.
	 *
	 * @param x X direction of the axis
	 * @param y Y direction of the axis
	 * @param z Z direction of the axis
	 * @param angle The angle in degrees
	 * @return This quaternion for chaining.
	 */
	public Quaternion setFromAxis(float x, float y, float z, float angle) {
		float l_ang = angle * MathUtils.degreesToRadians;
		float l_sin = MathUtils.sin(l_ang / 2);
		float l_cos = MathUtils.cos(l_ang / 2);
		return this.set(x * l_sin, y * l_sin, z * l_sin, l_cos).nor();
	}
}

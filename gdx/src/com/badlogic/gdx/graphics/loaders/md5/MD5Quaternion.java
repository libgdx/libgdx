/*
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com), Nathan Sweet (admin@esotericsoftware.com)
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
package com.badlogic.gdx.graphics.loaders.md5;

import com.badlogic.gdx.math.Vector3;

public class MD5Quaternion {
	public float x, y, z, w;

	public MD5Quaternion () {

	}

	public MD5Quaternion (float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
		computeW();
	}

	public MD5Quaternion (float x, float y, float z, float w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}

	public void set (MD5Quaternion q) {
		this.w = q.w;
		this.x = q.x;
		this.y = q.y;
		this.z = q.z;
	}

	public void computeW () {
		float t = 1.0f - x * x - y * y - z * z;

		if (t < 0.0f)
			w = 0;
		else
			w = -(float)Math.sqrt(t);
	}

	public void normalize () {
		float mag = (float)Math.sqrt(x * x + y * y + z * z + w * w);

		if (mag > 0) {
			float inv = 1.0f / mag;

			x *= inv;
			y *= inv;
			z *= inv;
			w *= inv;
		}
	}

	public void multiply (MD5Quaternion q) {
		float tw = w * q.w - x * q.x - y * q.y - z * q.z;
		float tx = x * q.w + w * q.x + y * q.z - z * q.y;
		float ty = y * q.w + w * q.y + z * q.x - x * q.z;
		float tz = z * q.w + w * q.z + x * q.y - y * q.x;

		w = tw;
		x = tx;
		y = ty;
		z = tz;
	}

	public void multiply (Vector3 v) {
		float tw = -x * v.x - y * v.y - z * v.z;
		float tx = w * v.x + y * v.z - z * v.y;
		float ty = w * v.y + z * v.x - x * v.z;
		float tz = w * v.z + x * v.y - y * v.x;

		w = tw;
		x = tx;
		y = ty;
		z = tz;
	}

	private static final MD5Quaternion tmp = new MD5Quaternion();
	private static final MD5Quaternion inv = new MD5Quaternion();

	public void rotate (Vector3 vec) {
		inv.x = -x;
		inv.y = -y;
		inv.z = -z;
		inv.w = w;

// inv.normalize();
		tmp.set(this);
		tmp.multiply(vec);
		tmp.multiply(inv);

		vec.x = tmp.x;
		vec.y = tmp.y;
		vec.z = tmp.z;
	}

	public float dot (MD5Quaternion q) {
		return x * q.x + y * q.y + z * q.z + w * q.w;
	}

	public void slerp (MD5Quaternion q, float t) {
		if (t <= 0) return;

		if (t >= 1) {
			set(q);
			return;
		}

		float cosOmega = dot(q);

		float q1w = q.w;
		float q1x = q.x;
		float q1y = q.y;
		float q1z = q.z;

		if (cosOmega < 0) {
			q1w = -q1w;
			q1x = -q1x;
			q1y = -q1y;
			q1z = -q1z;
			cosOmega = -cosOmega;
		}

		assert cosOmega < 1.1f;

		float k0, k1;

		if (cosOmega > 0.9999f) {
			k0 = 1.0f - t;
			k1 = t;
		} else {
			float sinOmega = (float)Math.sqrt(1.0f - (cosOmega * cosOmega));
			float omega = (float)Math.atan2(sinOmega, cosOmega);
			float oneOverSinOmega = 1.0f / sinOmega;
			k0 = (float)Math.sin((1.0f - t) * omega) * oneOverSinOmega;
			k1 = (float)Math.sin(t * omega) * oneOverSinOmega;
		}

		w = k0 * w + k1 * q1w;
		x = k0 * x + k1 * q1x;
		y = k0 * y + k1 * q1y;
		z = k0 * z + k1 * q1z;
	}

	public String toString () {
		return String.format("%.4f", x) + ", " + String.format("%.4f", y) + ", " + String.format("%.4f", z) + ", "
			+ String.format("%.4f", w);
	}
}

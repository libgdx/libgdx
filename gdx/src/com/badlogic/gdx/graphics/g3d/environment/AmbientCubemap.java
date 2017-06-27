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

package com.badlogic.gdx.graphics.g3d.environment;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class AmbientCubemap {
	private static final int NUM_VALUES = 6 * 3;

	private final static float clamp (final float v) {
		return v < 0f ? 0f : (v > 1f ? 1f : v);
	}

	public final float data[];

	public AmbientCubemap () {
		data = new float[NUM_VALUES];
	}

	public AmbientCubemap (final float copyFrom[]) {
		if (copyFrom.length != (NUM_VALUES)) throw new GdxRuntimeException("Incorrect array size");
		data = new float[copyFrom.length];
		System.arraycopy(copyFrom, 0, data, 0, data.length);
	}

	public AmbientCubemap (final AmbientCubemap copyFrom) {
		this(copyFrom.data);
	}

	public AmbientCubemap set (final float values[]) {
		for (int i = 0; i < data.length; i++)
			data[i] = values[i];
		return this;
	}

	public AmbientCubemap set (final AmbientCubemap other) {
		return set(other.data);
	}

	public AmbientCubemap set (final Color color) {
		return set(color.r, color.g, color.b);
	}

	public AmbientCubemap set (float r, float g, float b) {
		for (int idx = 0; idx < NUM_VALUES;) {
			data[idx] = r;
			data[idx+1] = g;
			data[idx+2] = b;
			idx += 3;
		}
		return this;
	}

	public Color getColor (final Color out, int side) {
		side *= 3;
		return out.set(data[side], data[side + 1], data[side + 2], 1f);
	}

	public AmbientCubemap clear () {
		for (int i = 0; i < data.length; i++)
			data[i] = 0f;
		return this;
	}

	public AmbientCubemap clamp () {
		for (int i = 0; i < data.length; i++)
			data[i] = clamp(data[i]);
		return this;
	}

	public AmbientCubemap add (float r, float g, float b) {
		for (int idx = 0; idx < data.length;) {
			data[idx++] += r;
			data[idx++] += g;
			data[idx++] += b;
		}
		return this;
	}

	public AmbientCubemap add (final Color color) {
		return add(color.r, color.g, color.b);
	}

	public AmbientCubemap add (final float r, final float g, final float b, final float x, final float y, final float z) {
		final float x2 = x * x, y2 = y * y, z2 = z * z;
		float d = x2 + y2 + z2;
		if (d == 0f) return this;
		d = 1f / d * (d + 1f);
		final float rd = r * d, gd = g * d, bd = b * d;
		int idx = x > 0 ? 0 : 3;
		data[idx] += x2 * rd;
		data[idx + 1] += x2 * gd;
		data[idx + 2] += x2 * bd;
		idx = y > 0 ? 6 : 9;
		data[idx] += y2 * rd;
		data[idx + 1] += y2 * gd;
		data[idx + 2] += y2 * bd;
		idx = z > 0 ? 12 : 15;
		data[idx] += z2 * rd;
		data[idx + 1] += z2 * gd;
		data[idx + 2] += z2 * bd;
		return this;
	}

	public AmbientCubemap add (final Color color, final Vector3 direction) {
		return add(color.r, color.g, color.b, direction.x, direction.y, direction.z);
	}

	public AmbientCubemap add (final float r, final float g, final float b, final Vector3 direction) {
		return add(r, g, b, direction.x, direction.y, direction.z);
	}

	public AmbientCubemap add (final Color color, final float x, final float y, final float z) {
		return add(color.r, color.g, color.b, x, y, z);
	}

	public AmbientCubemap add (final Color color, final Vector3 point, final Vector3 target) {
		return add(color.r, color.g, color.b, target.x - point.x, target.y - point.y, target.z - point.z);
	}

	public AmbientCubemap add (final Color color, final Vector3 point, final Vector3 target, final float intensity) {
		final float t = intensity / (1f + target.dst(point));
		return add(color.r * t, color.g * t, color.b * t, target.x - point.x, target.y - point.y, target.z - point.z);
	}

	@Override
	public String toString () {
		String result = "";
		for (int i = 0; i < data.length; i += 3) {
			result += Float.toString(data[i]) + ", " + Float.toString(data[i + 1]) + ", " + Float.toString(data[i + 2]) + "\n";
		}
		return result;
	}
}

package com.badlogic.gdx.graphics.g3d.environment;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class SphericalHarmonics {
	// <kalle_h> last term is no x*x * y*y but x*x - y*y
	private final static float coeff[] = {0.282095f, 0.488603f, 0.488603f, 0.488603f, 1.092548f, 1.092548f, 1.092548f, 0.315392f, 0.546274f};
	private final static float clamp(final float v) {
		return v < 0f ? 0f : (v > 1f ? 1f : v);
	}
	
	public final float data[];
	
	public SphericalHarmonics() {
		data = new float[9*3];
	}
	
	public SphericalHarmonics(final float copyFrom[]) {
		if (copyFrom.length != (9*3))
			throw new GdxRuntimeException("Incorrect array size");
		data = copyFrom.clone();
	}
	
	public SphericalHarmonics set(final float values[]) {
		for (int i = 0; i < data.length; i++)
			data[i] = values[i];
		return this;
	}
	
	public SphericalHarmonics set(final AmbientCubemap other) {
		return set(other.data);
	}
	
	public SphericalHarmonics set(final Color color) {
		return set(color.r, color.g, color.b);
	}
	
	public SphericalHarmonics set(float r, float g, float b) {
		for (int idx = 0; idx < data.length;) {
			data[idx++] = r;
			data[idx++] = g;
			data[idx++] = b;
		}
		return this;
	}
}


package com.badlogic.gdx.math.noise;

import com.badlogic.gdx.math.Vector3;

public interface Noise {
	float getNoise1D (float x);

	float getNoise2D (float x, float y);

	float getNoise2D (Vector3 vec2);

	float getNoise3D (float x, float y, float z);

	float getNoise3D (Vector3 vec3);
}

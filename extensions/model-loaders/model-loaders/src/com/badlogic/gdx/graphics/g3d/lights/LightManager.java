package com.badlogic.gdx.graphics.g3d.lights;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public class LightManager {

	// enum Fallof {
	// linear, quadratic
	// };

	final public Array<PointLight> pointLights = new Array<PointLight>(false,
			16, PointLight.class);
	final private float[] positions;
	final private float[] colors;
	final private float[] intensities;

	public final int maxLightsPerModel;

	final Color ambientLight = new Color();

	public LightManager() {
		this(4);
	}

	public LightManager(int maxLightsPerModel) {
		this.maxLightsPerModel = maxLightsPerModel;

		colors = new float[3 * maxLightsPerModel];
		positions = new float[3 * maxLightsPerModel];
		intensities = new float[maxLightsPerModel];
	}

	public void addLigth(PointLight light) {
		pointLights.add(light);
	}

	public void clear() {
		pointLights.clear();
	}

	// TODO make it better if it slow
	// NAIVE but simple implementation of light choosing algorithm
	// currently calculate lights based on transformed center position of model
	// TODO one idea would be first cull lights that can't affect the scene with
	// frustum check.
	// TODO another idea would be first cut lights that are further from model
	// than x that would make sorted faster
	public void calculateLights(float x, float y, float z) {
		final int maxSize = pointLights.size;

		PointLight lights[] = pointLights.items;
		// solve what are closest lights
		if (maxSize > maxLightsPerModel) {

			for (int i = 0; i < maxSize; i++) {
				lights[i].distance = lights[i].position.dst2(x, y, z);
			}
			pointLights.sort();

		}

		// fill the light arrays
		final int size = maxLightsPerModel > maxSize ? maxSize
				: maxLightsPerModel;
		for (int i = 0; i < size; i++) {
			PointLight l = lights[i];
			final Vector3 pos = l.position;
			positions[3 * i + 0] = pos.x;
			positions[3 * i + 1] = pos.y;
			positions[3 * i + 2] = pos.z;

			final Color col = l.color;
			colors[3 * i + 0] = col.r;
			colors[3 * i + 1] = col.g;
			colors[3 * i + 2] = col.b;

			intensities[i] = l.range;
		}

		// TODO might not be needed
		for (int i = size; i < maxLightsPerModel; i++) {
			intensities[i] = 0;
		}
	}

	/** Apply lights GLES1.0, call calculateLights before aplying */
	public void applyLights() {

	}

	/** Apply lights GLES2.0, call calculateLights before aplying */
	public void applyLights(ShaderProgram shader) {
		// TODO should shader be begin first?
		shader.setUniform3fv("lightsPos", positions, 0, maxLightsPerModel * 3);
		shader.setUniform3fv("lightsCol", colors, 0, maxLightsPerModel * 3);
		shader.setUniform1fv("lightsInt", intensities, 0, maxLightsPerModel);
	}
}

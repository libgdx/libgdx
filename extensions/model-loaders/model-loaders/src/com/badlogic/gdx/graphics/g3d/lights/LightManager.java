
package com.badlogic.gdx.graphics.g3d.lights;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.StillModelInstance;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public class LightManager {

	public enum LightQuality {
		VERTEX, FRAGMENT
	};

	public LightQuality quality;

	final public Array<PointLight> pointLights = new Array<PointLight>(false, 16);
	final private float[] positions;
	final private float[] colors;
	final private float[] intensities;

	public final int maxLightsPerModel;

	final public Color ambientLight = new Color();

	/** Only one for optimizing - at least at now */
	public DirectionalLight dirLight;

	public LightManager () {
		this(4, LightQuality.VERTEX);
	}

	public LightManager (int maxLightsPerModel, LightQuality lightQuality) {
		quality = lightQuality;
		this.maxLightsPerModel = maxLightsPerModel;

		colors = new float[3 * maxLightsPerModel];
		positions = new float[3 * maxLightsPerModel];
		intensities = new float[maxLightsPerModel];
	}

	public void addLigth (PointLight light) {
		pointLights.add(light);
	}

	public void clear () {
		pointLights.clear();
	}

	public void calculateAndApplyLightsToModel (Vector3 center, ShaderProgram shader) {
		this.calculateLights(center.x, center.y, center.z);
		this.applyLights(shader);
	}

	// TODO make it better if it slow
	// NAIVE but simple implementation of light choosing algorithm
	// currently calculate lights based on transformed center position of model
	// TODO one idea would be first cull lights that can't affect the scene with
	// frustum check.
	// TODO another idea would be first cut lights that are further from model
	// than x that would make sorted faster
	public void calculateLights (float x, float y, float z) {
		final int maxSize = pointLights.size;
		// solve what are lights that influence most
		if (maxSize > maxLightsPerModel) {

			for (int i = 0; i < maxSize; i++) {
				final PointLight light = pointLights.get(i);
				light.priority = light.intensity / light.position.dst(x, y, z);
				// if just linear fallof
			}
			pointLights.sort();
		}

		// fill the light arrays
		final int size = maxLightsPerModel > maxSize ? maxSize : maxLightsPerModel;
		for (int i = 0; i < size; i++) {
			final PointLight light = pointLights.get(i);
			final Vector3 pos = light.position;
			positions[3 * i + 0] = pos.x;
			positions[3 * i + 1] = pos.y;
			positions[3 * i + 2] = pos.z;

			final Color col = light.color;
			colors[3 * i + 0] = col.r;
			colors[3 * i + 1] = col.g;
			colors[3 * i + 2] = col.b;

			intensities[i] = light.intensity;
		}

		// TODO might not be needed
		for (int i = size; i < maxLightsPerModel; i++) {
			intensities[i] = 0;
		}
	}

	/** Apply lights GLES1.0, call calculateLights before aplying */
	public void applyLights () {

	}

	/** Apply lights GLES2.0, call calculateLights before aplying */
	public void applyLights (ShaderProgram shader) {
		shader.setUniform3fv("lightsPos", positions, 0, maxLightsPerModel * 3);
		shader.setUniform3fv("lightsCol", colors, 0, maxLightsPerModel * 3);
		shader.setUniform1fv("lightsInt", intensities, 0, maxLightsPerModel);
	}

	public void applyGlobalLights () {
		// TODO fix me
	}

	public void applyGlobalLights (ShaderProgram shader) {
		shader.setUniformf("ambient", ambientLight.r, ambientLight.g, ambientLight.b);
		if (dirLight != null) {
			final Vector3 v = dirLight.direction;
			final Color c = dirLight.color;
			shader.setUniformf("dirLightDir", v.x, v.y, v.z);
			shader.setUniformf("dirLightCol", c.r, c.g, c.b);
		}
	}
}

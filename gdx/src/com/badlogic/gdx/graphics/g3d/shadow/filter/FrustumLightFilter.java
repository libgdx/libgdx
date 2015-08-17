package com.badlogic.gdx.graphics.g3d.shadow.filter;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Scene;
import com.badlogic.gdx.graphics.g3d.environment.BaseLight;
import com.badlogic.gdx.math.Frustum;

public class FrustumLightFilter implements LightFilter {

	protected Scene scene;

	public FrustumLightFilter(Scene scene) {
		this.scene = scene;
	}

	@Override
	public boolean filter (int n, BaseLight light, Camera camera) {
		Frustum f1 = scene.getCamera().frustum;
		Frustum f2 = camera.frustum;

		boolean result = false;
		for(int i = 0; i < f2.planePoints.length; i++) {
			if(f1.pointInFrustum(f2.planePoints[i])) {
				result = true;
			}
		}

		return result;
	}
}

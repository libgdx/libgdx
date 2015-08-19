package com.badlogic.gdx.graphics.g3d.shadow.filter;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.environment.BaseLight;

public class NoLightFilter implements LightFilter {

	@Override
	public boolean filter (int n, BaseLight light, Camera camera) {
		return true;
	}
}

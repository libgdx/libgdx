package com.badlogic.gdx.graphics.g3d.shadow.nearfar;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.environment.BaseLight;
import com.badlogic.gdx.math.Vector2;

/**
 * Nearfar Analyzer compute the near and far plane
 * of a camera
 * @author realitix
 */
public interface NearFarAnalyzer {
	/**
	 * Analyze near and far plane for the camera
	 * @param light Current light
	 * @param camera Camera associated with light
	 * @return Vector2
	 */
	public Vector2 analyze(BaseLight light, Camera camera);
}

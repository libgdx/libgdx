package com.badlogic.gdx.graphics.g3d.shadow.directional;

import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.math.Vector3;

/**
 * Directional Analyzer compute the properties of the camera
 * needed by a directional light
 * @author realitix
 */
public interface DirectionalAnalyzer {
	/**
	 * Compute the good orthographicCamera dimension based on the frustum.
	 * Be careful, direction must be normalized.
	 * @param frustum Frustum of the main camera
	 * @param direction Direction of the directional light
	 */
	public DirectionalResult analyze(Frustum frustum, Vector3 direction);
}

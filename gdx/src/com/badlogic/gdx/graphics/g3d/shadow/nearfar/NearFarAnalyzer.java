package com.badlogic.gdx.graphics.g3d.shadow.nearfar;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;

/**
 * Nearfar Analyzer compute the near and far plane
 * of a camera
 * @author realitix
 */
public interface NearFarAnalyzer {
	public Vector2 analyze(Camera camera);
}

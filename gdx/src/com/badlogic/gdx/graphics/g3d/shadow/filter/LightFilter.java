package com.badlogic.gdx.graphics.g3d.shadow.filter;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.environment.BaseLight;

/**
 * LightFilter allow to filter light to render
 * 
 * @author realitix
 *
 */
public interface LightFilter {
	/**
	 * Return true if the light has to be used for rendering
	 * @param n Pass number
	 * @param light Current light
	 * @param camera Camera of the light
	 * @return boolean
	 */
	public boolean filter(int n, BaseLight light, Camera camera);
}

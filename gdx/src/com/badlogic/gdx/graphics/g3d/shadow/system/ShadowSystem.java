package com.badlogic.gdx.graphics.g3d.shadow.system;

import java.util.Set;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Cubemap.CubemapSide;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.graphics.g3d.environment.SpotLight;
import com.badlogic.gdx.graphics.g3d.utils.ShaderProvider;

/**
 * Shadow system provides all needed to render shadows
 * @author realitix
 */
public interface ShadowSystem {

	/**
	 * Return the number of pass
	 * @return int
	 */
	public int getPassQuantity();

	/**
	 * Return the shaderProvider of the pass n
	 * @return ShaderProvider
	 */
	public ShaderProvider getPassShaderProvider(int n);

	/**
	 * Return the shaderProvider used for the rendering of the models
	 * @return ShaderProvider
	 */
	public ShaderProvider getShaderProvider();

	/**
	 * Add spot light in shadow system
	 * @param spot SpotLight to add in the ShadowSystem
	 */
	public void addLight(SpotLight spot);

	/**
	 * Add directional light in shadow system
	 * @param dir DirectionalLight to add in the ShadowSystem
	 */
	public void addLight(DirectionalLight dir);

	/**
	 * Add point light in shadow system
	 * @param point PointLight to add in the ShadowSystem
	 */
	public void addLight(PointLight point);

	/**
	 * Add point light in shadow system
	 * @param point PointLight to add in the ShadowSystem
	 * @param sides Set of side
	 */
	public void addLight(PointLight point, Set<CubemapSide> sides);

	/**
	 * Remove light from the shadowSystem
	 * @param spot SpotLight to remove in the ShadowSystem
	 */
	public void removeLight(SpotLight spot);

	/**
	 * Remove light from the shadowSystem
	 * @param dir DirectionalLight to remove in the ShadowSystem
	 */
	public void removeLight(DirectionalLight dir);

	/**
	 * Remove light from the shadowSystem
	 * @param point PointLight to remove in the ShadowSystem
	 */
	public void removeLight(PointLight point);

	/**
	 *
	 * @param spot SpotLight to check
	 * @return true if light analyzed
	 */
	public boolean hasLight(SpotLight spot);

	/**
	 *
	 * @param dir DirectionalLight to check
	 * @return true if light analyzed
	 */
	public boolean hasLight(DirectionalLight dir);

	/**
	 *
	 * @param point PointLight to check
	 * @return true if light analyzed
	 */
	public boolean hasLight(PointLight point);

	/**
	 * Update the shadowSystem
	 */
	public void update();

	/**
	 * Start the pass n rendering
	 */
	public void begin(int n);

	/**
	 * Switch light
	 * @return Current camera
	 */
	public Camera next();

	/**
	 * End the pass n rendering
	 */
	public void end(int n);
}

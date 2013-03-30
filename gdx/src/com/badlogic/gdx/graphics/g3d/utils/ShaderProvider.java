package com.badlogic.gdx.graphics.g3d.utils;

import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.materials.Material;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

/**
 * Returns {@link Shader} instances for a {@link Renderable} on request. Also responsible
 * for disposing of any created {@link ShaderProgram} instances on a call to {@link #dispose()}. 
 * @author badlogic
 *
 */
public interface ShaderProvider {
	/**
	 * Returns a {@link Shader} for the given {@link Renderable}. The RenderInstance may
	 * already contain a Shader, in which case the provider may decide to return that.
	 * @param renderable the Renderable
	 * @return the Shader to be used for the RenderInstance
	 */
	Shader getShader(Renderable renderable);
	
	/**
	 * Disposes all resources created by the provider
	 */
	public void dispose();
}

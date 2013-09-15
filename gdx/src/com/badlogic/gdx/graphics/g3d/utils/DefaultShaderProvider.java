package com.badlogic.gdx.graphics.g3d.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.shaders.GLES10Shader;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class DefaultShaderProvider extends BaseShaderProvider {
	/** The overall default number of directional lights to use (can be overridden using the {@link #numDirectionalLights} value) */
	public static int defaultNumDirectionalLights = 2;
	/** The overall default number of point lights to use (can be overridden using the {@link #numPointLights} value) */
	public static int defaultNumPointLights = 5;
	/** The overall default number of spot lights to use (can be overridden using the {@link #numSpotLights} value) */
	public static int defaultNumSpotLights = 3;
	/** The overall default number of bones to use (can be overridden using the {@link #numBones} value) */
	public static int defaultNumBones = 12;
	
	/** The uber vertex shader to use. */
	public String vertexShader;
	/** The uber fragment shader to use. */
	public String fragmentShader;
	/** The number of directional lights to use (negative to use {@link #defaultNumDirectionalLights}) */
	public int numDirectionalLights = -1;
	/** The number of point lights to use (negative to use {@link #defaultNumPointLights}) */
	public int numPointLights = -1;
	/** The number of spot lights to use (negative to use {@link #defaultNumSpotLights}) */
	public int numSpotLights = -1;
	/** The number of bones to use (negative to use {@link #defaultNumBones}) */
	public int numBones = -1;
	
	public DefaultShaderProvider(final String vertexShader, final String fragmentShader) {
		this.vertexShader = vertexShader;
		this.fragmentShader = fragmentShader;
	}
	
	public DefaultShaderProvider(final FileHandle vertexShader, final FileHandle fragmentShader) {
		this(vertexShader.readString(), fragmentShader.readString());
	}
	
	public DefaultShaderProvider() {
		this(DefaultShader.getDefaultVertexShader(), DefaultShader.getDefaultFragmentShader());
	}
	
	@Override
	protected Shader createShader(final Renderable renderable) {
		Gdx.app.log("DefaultShaderProvider", "Creating new shader");
		if (Gdx.graphics.isGL20Available()) {
            return new DefaultShader(
            	vertexShader, 
            	fragmentShader, 
            	renderable, 
            	renderable.lights != null,
            	renderable.lights != null && renderable.lights.environmentCubemap != null, 
            	renderable.lights != null && renderable.lights.shadowMap != null, 
            	renderable.lights != null && renderable.lights.fog != null, 
            	renderable.lights == null ? 0 : (numDirectionalLights < 0 ? defaultNumDirectionalLights : numDirectionalLights),
            	renderable.lights == null ? 0 : (numPointLights < 0 ? defaultNumPointLights : numPointLights),
            	renderable.lights == null ? 0 : (numSpotLights < 0 ? defaultNumSpotLights : numSpotLights),
            	renderable.bones == null ? 0 : (numBones < 0 ? defaultNumBones : numBones));
		}
		return new GLES10Shader();
	}
}

package com.badlogic.gdx.graphics.g3d.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.shaders.DepthShader;
import com.badlogic.gdx.graphics.g3d.shaders.GLES10Shader;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class DepthShaderProvider extends BaseShaderProvider {
	public String vertexShader;
	public String fragmentShader;
	public static boolean depthBufferOnly = false;
	
	public DepthShaderProvider(final String vertexShader, final String fragmentShader) {
		this.vertexShader = vertexShader;
		this.fragmentShader = fragmentShader;
		if (!Gdx.graphics.isGL20Available())
			throw new GdxRuntimeException("DepthShaderProvider requires OpenGL ES 2.0");
	}
	
	public DepthShaderProvider(final FileHandle vertexShader, final FileHandle fragmentShader) {
		this(vertexShader.readString(), fragmentShader.readString());
	}
	
	public DepthShaderProvider() {
		this(DepthShader.getDefaultVertexShader(), DepthShader.getDefaultFragmentShader());
	}
	
	@Override
	protected Shader createShader(final Renderable renderable) {
		Gdx.app.log("DepthShaderProvider", "Creating new shader");
		return new DepthShader(vertexShader, fragmentShader, renderable, renderable.bones == null ? 0 : 12, depthBufferOnly);
	}
}
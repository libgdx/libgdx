package com.badlogic.gdx.graphics.g3d.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.shaders.GLES10Shader;

public class DefaultShaderProvider extends BaseShaderProvider {
	public String vertexShader;
	public String fragmentShader;
	
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
            return new DefaultShader(vertexShader, fragmentShader, renderable.material, renderable.mesh.getVertexAttributes(), renderable.lights != null, renderable.lights != null && renderable.lights.fog != null, 2, 5, 3, renderable.bones == null ? 0 : 12);
        }
		return new GLES10Shader();
	}
}

package com.badlogic.gdx.graphics.g3d.shaders.subshaders;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public interface SubShader {
	public void init(Renderable renderable);
	public String[] getVertexShaderVars();
	public String[] getVertexShaderCode();
	public String[] getFragmentShaderVars();
	public String[] getFragmentShaderCode();
	public void apply(ShaderProgram program, RenderContext context, Camera camera, Renderable renderable);
}

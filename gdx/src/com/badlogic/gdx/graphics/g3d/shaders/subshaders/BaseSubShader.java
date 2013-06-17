package com.badlogic.gdx.graphics.g3d.shaders.subshaders;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Array;

public abstract class BaseSubShader implements SubShader {
	protected Array<String> vertexVars = new Array<String>(new String[0]);
	protected Array<String> vertexCode = new Array<String>(new String[0]);
	protected Array<String> fragmentVars = new Array<String>(new String[0]);
	protected Array<String> fragmentCode = new Array<String>(new String[0]);
	
	@Override
	public String[] getVertexShaderVars () {
		return vertexVars.toArray();
	}

	@Override
	public String[] getVertexShaderCode () {
		return vertexCode.toArray();
	}

	@Override
	public String[] getFragmentShaderVars () {
		return fragmentVars.toArray();
	}

	@Override
	public String[] getFragmentShaderCode () {
		return fragmentCode.toArray();
	}
}

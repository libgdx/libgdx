package com.badlogic.gdx.graphics.g3d.shaders.graph;

import com.badlogic.gdx.utils.Array;

public class UniformNode extends ShaderNode {
	public UniformNode(String name, String type) {
		this.name = name;
		this.defines = new Array<ShaderDefine>();
		this.requires = new Array<String>();
		this.inputs = new Array<ShaderInput>();
		this.outputs = new Array<ShaderOutput>();
	}
}

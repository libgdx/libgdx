package com.badlogic.gdx.graphics.g3d.shaders.graph;

public class ShaderConnection {
	private final ShaderNode outputNode;
	private final ShaderOutput output;
	private final ShaderNode inputNode;
	private final ShaderInput input;
	
	ShaderConnection(ShaderNode outputNode, ShaderOutput output, ShaderNode inputNode, ShaderInput input) {
		this.outputNode = outputNode;
		this.output = output;
		this.inputNode = inputNode;
		this.input = input;
	}

	public ShaderNode getOutputNode () {
		return outputNode;
	}

	public ShaderOutput getOutput () {
		return output;
	}

	public ShaderNode getInputNode () {
		return inputNode;
	}

	public ShaderInput getInput () {
		return input;
	}
}

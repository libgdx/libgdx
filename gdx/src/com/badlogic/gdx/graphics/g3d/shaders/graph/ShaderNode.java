package com.badlogic.gdx.graphics.g3d.shaders.graph;

import com.badlogic.gdx.graphics.g3d.shaders.graph.ShaderInput.ShaderInputQualifier;
import com.badlogic.gdx.utils.Array;

public class ShaderNode {
	public static enum ShaderNodeType {
		Vertex,
		Fragment
	}
	
	protected String name;
	protected ShaderNodeType type;
	protected Array<ShaderInput> inputs;
	protected Array<ShaderOutput> outputs;
	protected String code;
	protected Array<String> requires;
	protected Array<ShaderDefine> defines;
	
	ShaderNode() {
	}
	
	ShaderNode(String name, ShaderNodeType type, Array<String> requires, Array<ShaderDefine> defines, Array<ShaderInput> inputs, Array<ShaderOutput> outputs, String code) {
		this.name = name;
		this.type = type;
		this.requires = requires;
		this.defines = defines;
		this.inputs = inputs;
		this.outputs = outputs;
		this.code = code;
	}

	public String getName () {
		return name;
	}

	public ShaderNodeType getType () {
		return type;
	}

	public Array<ShaderInput> getInputs () {
		return inputs;
	}
	
	public ShaderInput getInput(String name) {
		for(ShaderInput input: inputs) {
			if(input.getName().equals(name)) return input;
		}
		return null;
	}

	public Array<ShaderOutput> getOutputs () {
		return outputs;
	}
	
	public ShaderOutput getOutput(String name) {
		for(ShaderOutput output: outputs) {
			if(output.getName().equals(name)) return output;
		}
		return null;
	}

	public String getCode () {
		return code;
	}
	
	public ShaderNode copy() {
		Array<ShaderInput> copiedInputs = new Array<ShaderInput>();
		for(ShaderInput input: inputs) {
			copiedInputs.add(input.copy());
		}
		Array<ShaderOutput> copiedOutputs = new Array<ShaderOutput>();
		for(ShaderOutput output: outputs) {
			copiedOutputs.add(output.copy());
		}
		return new ShaderNode(name, type, new Array<String>(requires), new Array<ShaderDefine>(defines), copiedInputs, copiedOutputs, code);
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("name ");
		builder.append(name);
		builder.append(";\n");
		
		builder.append("type ");
		builder.append(type);
		builder.append(";\n");
		
		for(String require: requires) {
			builder.append("requires ");
			builder.append(require);
			builder.append(";\n");
		}
		
		for(ShaderDefine define: defines) {
			builder.append("define ");
			builder.append(define.getName());
			builder.append("=");
			builder.append(define.getValue());
			builder.append(";\n");
		}
		
		builder.append("inputs {\n");
		for(ShaderInput input: inputs) {
			builder.append("   ");
			if(input.getQualifier() != ShaderInputQualifier.Local) {
				builder.append(input.getQualifier().toString().toLowerCase());
				builder.append(" ");
			}
			builder.append(input.getType());
			builder.append(" ");
			builder.append(input.getName());
			builder.append(";\n");
		}
		builder.append("}\n");
		
		builder.append("outputs {\n");
		for(ShaderOutput output: outputs) {
			builder.append("   ");
			if(output.isVarying()) {
				builder.append("varying ");
			}
			builder.append(output.getType());
			builder.append(" ");
			builder.append(output.getName());
			builder.append(";\n");
		}
		builder.append("}\n");
		
		builder.append("code {\n");
		String[] lines = code.split("\n");
		for(String line: lines) {
			builder.append("   " + line);
		}
		builder.append("\n}\n");
		
		return builder.toString();
	}
}
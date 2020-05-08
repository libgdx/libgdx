package com.badlogic.gdx.graphics.glutils;

import com.badlogic.gdx.Gdx;

/**
 * A shader program part is a section of GLSL code running at a programmable pipeline stage.
 */
public class ShaderPart {
	/** stage in the pipeline */
	final ShaderStage stage;
	/** original shader source code */
	final String source;
	/** shader handle */
	int handle;
	/** final shader code (prepended code + original source code) */
	String finalCode;
	/**
	 * @param stage shader stage
	 * @param source shader source code
	 */
	public ShaderPart (ShaderStage stage, String source) {
		this.stage = stage;
		this.source = source;
	}
}
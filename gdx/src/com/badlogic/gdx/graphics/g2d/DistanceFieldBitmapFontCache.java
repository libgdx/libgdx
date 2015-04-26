/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.graphics.g2d;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;


/** 
 * Caches and renders distance field font with custom shader. 
 * Inspired by https://github.com/libgdx/libgdx/wiki/Distance-field-fonts
 * @author Florian Falkner
 */
public class DistanceFieldBitmapFontCache extends BitmapFontCache {
	private static ShaderProgram shader;
	
	/*
	 * Needs to be done in a static block, otherwise doesn't work
	 */
	static {
		shader = createDistanceFieldShader();
	}
	
	public DistanceFieldBitmapFontCache (BitmapFont font, boolean integer) {
		super(font, integer);
	}

	public DistanceFieldBitmapFontCache (BitmapFont font) {
		super(font);
	}
	
	@Override
	public void draw (Batch spriteBatch) {
		spriteBatch.setShader(shader);
		shader.setUniformf("u_smoothing", getFont().getDistanceFieldSmoothing() * getFont().getScaleX());
		
		super.draw(spriteBatch);
		spriteBatch.setShader(null);
	}
	
	@Override
	public void draw (Batch spriteBatch, int start, int end) {
		spriteBatch.setShader(shader);
		shader.setUniformf("u_smoothing", getFont().getDistanceFieldSmoothing() * getFont().getScaleX());
		
		super.draw(spriteBatch, start, end);
		spriteBatch.setShader(null);
	}
	
	/** Returns a new instance of the distance field shader, see https://github.com/libgdx/libgdx/wiki/Distance-field-fonts. */
	static public ShaderProgram createDistanceFieldShader () {
		String vertexShader = "attribute vec4 " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
			+ "attribute vec4 " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" //
			+ "attribute vec2 " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" //
			+ "uniform mat4 u_projTrans;\n" //
			+ "varying vec4 v_color;\n" //
			+ "varying vec2 v_texCoords;\n" //
			+ "\n" //
			+ "void main()\n" //
			+ "{\n" //
			+ "   v_color = " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" //
			+ "   v_color.a = v_color.a * (255.0/254.0);\n" //
			+ "   v_texCoords = " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" //
			+ "   gl_Position =  u_projTrans * " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
			+ "}\n";
		
		String fragmentShader = "#ifdef GL_ES\n"
			+ "	precision mediump float;\n"
			+ "	precision mediump int;\n"
			+ "#endif\n"
			+ "\n"
			+ "uniform sampler2D u_texture;\n"
			+ "uniform float u_smoothing;\n"
			+ "varying vec4 v_color;\n" //
			+ "varying vec2 v_texCoords;\n" //
			+ "\n"
			+ "void main() {\n"
			+ "	float smoothing = 0.25 / u_smoothing;\n"
			+ "	float distance = texture2D(u_texture, v_texCoords).a;\n"
			+ "	float alpha = smoothstep(0.5 - smoothing, 0.5 + smoothing, distance);\n"
			+ "	gl_FragColor = vec4(v_color.rgb, alpha * v_color.a);\n"
			+ "}\n";
		
		ShaderProgram shader = new ShaderProgram(vertexShader, fragmentShader);
		if (shader.isCompiled() == false) throw new IllegalArgumentException("Error compiling shader: " + shader.getLog());
		return shader;
	}
}

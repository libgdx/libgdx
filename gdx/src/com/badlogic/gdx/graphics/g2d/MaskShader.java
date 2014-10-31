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

/** A shader that uses a separate grayscale texture as alpha channel. The mask texture must be bound to texture unit 1 before rendering.
 * @author Valentin Milea */
public class MaskShader extends ShaderProgram {

	// @off
	static public final String vertexShader =
		  "attribute vec4 " + ShaderProgram.POSITION_ATTRIBUTE + ";\n"
		+ "attribute vec4 " + ShaderProgram.COLOR_ATTRIBUTE + ";\n"
		+ "attribute vec2 " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n"
		+ "uniform mat4 u_projTrans;\n"
		+ "varying vec4 v_color;\n"
		+ "varying vec2 v_texCoords;\n"
		+ "\n"
		+ "void main()\n"
		+ "{\n"
		+ "   v_color = " + ShaderProgram.COLOR_ATTRIBUTE + ";\n"
		+ "   v_color.a = v_color.a * (255.0/254.0);\n"
		+ "   v_texCoords = " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n"
		+ "   gl_Position =  u_projTrans * " + ShaderProgram.POSITION_ATTRIBUTE + ";\n"
		+ "}\n";

	static public final String fragmentShader =
		  "#ifdef GL_ES\n"
		+ "#define LOWP lowp\n"
		+ "precision mediump float;\n"
		+ "#else\n"
		+ "#define LOWP \n"
		+ "#endif\n"
		+ "varying LOWP vec4 v_color;\n"
		+ "varying vec2 v_texCoords;\n"
		+ "uniform sampler2D u_texture;\n"
		+ "uniform sampler2D u_textureMask;\n"
		+ "void main()\n"
		+ "{\n"
		+ "   vec4 color = texture2D(u_texture, v_texCoords);\n"
		+ "   color.a = texture2D(u_textureMask, v_texCoords).r;\n"
		+ "   gl_FragColor = v_color * color;\n"
		+ "}";
	// @on

	public MaskShader () {
		super(vertexShader, fragmentShader);
	}

	@Override
	public void begin () {
		super.begin();
		setUniformi("u_textureMask", 1);
	}
}

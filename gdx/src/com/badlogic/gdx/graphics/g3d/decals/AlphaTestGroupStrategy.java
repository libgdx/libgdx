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

package com.badlogic.gdx.graphics.g3d.decals;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Pool;

/**
 * <p>Single Group strategy (all Decals same) using Z-buffer to render using screen door transparency to avoid having
 * to presort the decals. Materials are still sorted.</p>
 * <p>Avoids artifacts when the Decals are sufficiently proximate
 * and orientated such that sorting fails, and also from transparent decals overlapping
 * in plane (z-collisions) or by orientation.
 * </p>
 */
public class AlphaTestGroupStrategy implements GroupStrategy, Disposable {
	 private static final int GROUP_ALPHA_TEST = 0;

	 final Pool<Array<Decal>> arrayPool = new Pool<Array<Decal>>(16) {
		  @Override protected Array<Decal> newObject () {
				return new Array();
		  }
	 };
	 final Array<Array<Decal>> usedArrays = new Array<Array<Decal>>();
	 final ObjectMap<DecalMaterial, Array<Decal>> materialGroups = new ObjectMap<DecalMaterial, Array<Decal>>();

	 Camera camera;
	 ShaderProgram shader;

	 public AlphaTestGroupStrategy (final Camera camera) {
		  this.camera = camera;
		  Gdx.app.log("SHADER", "AlphaTestGroupStrategy");
		  createDefaultShader();
	 }

	 public Camera getCamera () {
		  return camera;
	 }

	 public void setCamera (Camera camera) {
		  this.camera = camera;
	 }

	 @Override public int decideGroup (Decal decal) {
		  return GROUP_ALPHA_TEST;
	 }

	 @Override public void beforeGroup (int group, Array<Decal> contents) {
		  for (int i = 0, n = contents.size; i < n; i++) {
				Decal        decal         = contents.get(i);
				Array<Decal> materialGroup = materialGroups.get(decal.getMaterial());
				if (materialGroup == null) {
					 materialGroup = arrayPool.obtain();
					 materialGroup.clear();
					 usedArrays.add(materialGroup);
					 materialGroups.put(decal.getMaterial(), materialGroup);
				}
				materialGroup.add(decal);
		  }

		  contents.clear();
		  for (Array<Decal> materialGroup : materialGroups.values()) {
				contents.addAll(materialGroup);
		  }

		  materialGroups.clear();
		  arrayPool.freeAll(usedArrays);
		  usedArrays.clear();
	 }

	 @Override public void afterGroup (int group) {
	 }

	 @Override public void beforeGroups () {

		  Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
		  shader.bind();
		  shader.setUniformMatrix("u_projectionViewMatrix", camera.combined);
		  shader.setUniformi("u_texture", 0);
	 }

	 @Override public void afterGroups () {
		  Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
	 }

	 private void createDefaultShader () {
		  String vertexShader = "attribute vec4 " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
			  + "attribute vec4 " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" //
			  + "attribute vec2 " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" //
			  + "uniform mat4 u_projectionViewMatrix;\n" //
			  + "varying vec4 v_color;\n" //
			  + "varying vec2 v_texCoords;\n" //
			  + "\n" //
			  + "void main()\n" //
			  + "{\n" //
			  + "   v_color = " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" //
			  + "   v_texCoords = " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" //
			  + "   gl_Position =  u_projectionViewMatrix * " + ShaderProgram.POSITION_ATTRIBUTE + ";" + "\n" //
			  + "}\n";

		  //Bayer
		  String ffragmentShader = "#ifdef GL_ES\n" //
			  + "precision mediump float;\n" //
			  + "precision mediump int;\n" //
			  + "#endif \n" //
			  + "varying vec4 v_color;\n" //
			  + "varying vec2 v_texCoords;\n" //
			  + "uniform sampler2D u_texture;\n" //
			  + "float[] bayerMatrix ={1.0f / 17.0f,  9.0f / 17.0f,  3.0f / 17.0f, 11.0f / 17.0f," //
			  + "13.0f / 17.0f,  5.0f / 17.0f, 15.0f / 17.0f,  7.0f / 17.0f,"//
			  + "4.0f / 17.0f, 12.0f / 17.0f,  2.0f / 17.0f, 10.0f / 17.0f, " //
			  + "16.0f / 17.0f,  8.0f / 17.0f, 14.0f /17.0f,  6.0f / 17.0f };\n" //
			  + "\n" //
			  + "void main()\n"//
			  + "{\n" //
			  + "int xPos = int(gl_FragCoord.x) % 4; \n" //
			  + "int yPos = int(gl_FragCoord.y) % 4; \n" //
			  + "float stipple = bayerMatrix[ xPos + (yPos*4) ]; \n" //
			  + "vec4 tex2D =  texture2D(u_texture, v_texCoords); \n" //
			  + "vec4 tinted = v_color * tex2D; \n" //
			  + "if( tinted.a <= stipple ) { discard; }\n" //
			  + "else\n" //
			  + "  { gl_FragColor = tinted; }\n" //
			  + "}";

		  //Default
//        String fragmentShader = "#ifdef GL_ES\n" //
//                                + "precision mediump float;\n" //
//                                + "#endif\n" //
//                                + "varying vec4 v_color;\n" //
//                                + "varying vec2 v_texCoords;\n" //
//                                + "uniform sampler2D u_texture;\n" //
//                                + "void main()\n"//
//                                + "{\n" //
//                                + "  gl_FragColor = v_color * texture2D(u_texture, v_texCoords);\n" //
//                                + "}";

//
		  //Tettinger interleaved
		  String jhfragmentShader = "#ifdef GL_ES\n" //
			  + "#define LOWP lowp\n" //
			  + "precision mediump float;\n" //
			  + "#else\n" //
			  + "#define LOWP \n" //
			  + "#endif\n" //
			  + "varying vec2 v_texCoords;\n" //
			  + "varying LOWP vec4 v_color;\n" //
			  + "uniform sampler2D u_texture;\n" //
			  + "void main()\n" //
			  + "{\n" //
			  + "float stipple = fract(52.9829189 * fract(0.06711056 * gl_FragCoord.x + 0.00583715 * gl_FragCoord.y));\n \n" //
			  + "vec4 tex2D =  texture2D(u_texture, v_texCoords); \n" //
			  + "vec4 tinted = v_color*tex2D; \n" //
			  + "if( dot(vec3(0.25), tinted.rgb) >= stipple ) { discard; }\n" //
			  + "gl_FragColor = tinted;\n" //
			  + "}";

		  final int MAX_BAYER_VALUE = 4;
		  String fragmentShader = "#ifdef GL_ES\n" //
			  + "precision mediump float;\n" //
			  + "#endif\n" //
			  + "\n" //
			  + "varying vec4 v_color;\n" //
			  + "varying vec2 v_texCoords;\n" //
			  + "uniform sampler2D u_texture;\n" //
			  + "\n" //
			  + "float GetBayer()\n" //
			  + "{\n" //
			  + "float finalBayer   = 0.0;\n" //
			  + "float finalDivisor = 0.0;\n" //
			  + "float layerMult\t   = 1.0;\n" //
			  + "\n" //
			  + "for(int bayerLevel = 2; bayerLevel >= 1; bayerLevel--)\n" //
			  + "{\n" //
			  + "float bayerSize \t= exp2( float(bayerLevel)) * 0.5;\n" //
			  + "vec2 bayercoord \t= mod( floor(gl_FragCoord.xy / bayerSize) , 2.0);\n" //
			  + "layerMult \t\t   *= 4.0;\n" //
			  + "\n" //
			  + "float line0202 = bayercoord.x * 2.0;\n" //
			  + "\n" //
			  + "finalBayer += mix(line0202,3.0 - line0202,bayercoord.y) / 3.0 * layerMult;\n" //
			  + "finalDivisor += layerMult;\n" //
			  + "}\n" //
			  + "return finalBayer / finalDivisor;\n" //
			  + "}\n" //
			  + "\n" //
			  + "void main()\n" //
			  + "{\n" //
			  + "float stipple =GetBayer(); \n" //
			  + "vec4 tex2D =  texture2D(u_texture, v_texCoords);\n" //
			  + "vec4 tinted = v_color * tex2D; \n" //
			  + "if( tinted.a <= stipple ) { discard; }\n" //
			  + "else\n" //
			  + "{ gl_FragColor = tinted; }\n" //
			  + "}"; //

		  //Tetting Bayer shader
		  String xfragmentShader = "#ifdef GL_ES\n" //
			  + "precision mediump float;\n" //
			  + "#endif \n" //
			  + "varying vec4 v_color;\n" //
			  + "varying vec2 v_texCoords;\n" //
			  + "uniform sampler2D u_texture;\n" //
			  + "\n" //
			  + "#define MAX_LEVEL 2\n" //
			  + "float GetBayer() {\n" //
			  + "  float finalBayer = 0.0;\n" //
			  + "  for(int i = 1 - MAX_LEVEL; i<= 0; i++) {\n" //
			  + "    float bayerSize = exp2(float(i));   // negative exponent makes an inverse\n" //
			  + "    vec2 c = mod(floor(gl_FragCoord.xy * bayerSize), 2.0);\n" //
			  + "    float bayer = 2. * c.x - 4. * c.x * c.y + 3. * c.y;  // borrows factors of the summed geometric series soon to"
			  //
			  + " divide it\n"  //
			  + "    finalBayer += exp2(float(2 * (i + MAX_LEVEL))) * bayer; // could use a left shift\n" //
			  + "  }\n" //
			  + "  float finalDivisor = exp2(float(2 * MAX_LEVEL + 2)) - 4.; // geometric series sum, can be calculated at compile "
			  //
			  + "time\n"  //
			  + "  return finalBayer/ finalDivisor;\n" //
			  + "}" //
			  + "\n" //
			  + "void main()\n"//
			  + "{\n" //
			  + "float stipple =GetBayer(); \n" //
			  + "vec4 tex2D =  texture2D(u_texture, v_texCoords); \n" //
			  + "vec4 tinted = v_color * tex2D; \n" //
			  + "if( tinted.a <= stipple ) { discard; }\n" //
			  + "else\n" //
			  + "  { gl_FragColor = tinted; }\n" //
			  + "}";

		  shader = new ShaderProgram(vertexShader, fragmentShader);
		  if (shader.isCompiled() == false) {
				throw new IllegalArgumentException("couldn't compile shader: " + shader.getLog());
		  }
	 }

	 @Override public ShaderProgram getGroupShader (int group) {
		  return shader;
	 }

	 @Override public void dispose () {
		  if (shader != null)
				shader.dispose();
	 }
}



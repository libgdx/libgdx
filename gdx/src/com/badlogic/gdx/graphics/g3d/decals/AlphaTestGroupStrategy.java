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
 * <p>No artifacts including when
 * 1) the Decals cross intersect each other such that depth sorts are impossible
 * 2) transparent/translucent overlap in-plane (z-collision)
 * </p>
 */
public class AlphaTestGroupStrategy implements GroupStrategy, Disposable {

	 /**
	  * Specifies the size of the dithering filter used to discard pixels
	  * of translucent textures.
	  * <p>
	  * {@link #DISABLED} Transparency only. Translucency disabled.
	  * {@link #FAST} 1x1
	  * {@link #QUICK} 2x2
	  * {@link #MODERATE} 3x3
	  * {@link #SLOW} 4x4
	  */
	 public enum BayerFilter {
		  DISABLED(0), FAST(1), QUICK(2), MODERATE(3), SLOW(4);

		  private final int size;

		  BayerFilter (final int size) {
				this.size = size;
		  }
	 }

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

	 /**
	  * Renders translucency with screen door effect/stippling.
	  *
	  * @param camera
	  */
	 public AlphaTestGroupStrategy (final Camera camera) {
		  this(camera, BayerFilter.MODERATE);
	 }

	 /**
	  * Renders translucency with screen door effect/stippling.
	  *
	  * @param camera
	  * @param bayerFilter Specifies the size of the dithering filter used to discard pixels
	  *                    of translucent textures.
	  */
	 public AlphaTestGroupStrategy (final Camera camera, final BayerFilter bayerFilter) {
		  this.camera = camera;
		  createDefaultShader(bayerFilter);
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

	 /**
	  * Creates default shader.
	  *
	  * @param bayerFilter 2^maxBAYERVALUE is the width and height of the Bayer filter.
	  *                    As 16x16 holds all possible 255 alpha values, 4 is the functional
	  *                    maximum size.
	  */
	 private void createDefaultShader (final BayerFilter bayerFilter) {
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

		  String dfragmentShader = "#ifdef GL_ES\n" //
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
			  + "for(int bayerLevel = " + bayerFilter.size + "; bayerLevel >= 1; bayerLevel--)\n" //
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
		  String fragmentShader = "#ifdef GL_ES\n" //
			  + "precision mediump float;\n" //
			  + "#endif \n" //
			  + "varying vec4 v_color;\n" //
			  + "varying vec2 v_texCoords;\n" //
			  + "uniform sampler2D u_texture;\n" //
			  + "\n" //
			  + "#define MAX_LEVEL 4\n" //
			  + "float GetBayer() {\n" //
			  + "  float finalBayer = 0.0;\n" //
			  + "  for(int i = 1 - "+bayerFilter.size +"; i<= 0; i++) {\n" //
			  + "    float bayerSize = exp2(float(i));   // negative exponent makes an inverse\n" //
			  + "    vec2 c = mod(floor(gl_FragCoord.xy * bayerSize), 2.0);\n" //
			  + "    float bayer = 2. * c.x - 4. * c.x * c.y + 3. * c.y;  // borrows factors of the summed geometric series soon to"
			  + "                                                            divide it\n"  //
			  + "    finalBayer += exp2(float(2 * (i + MAX_LEVEL))) * bayer; // could use a left shift\n" //
			  + "  }\n" //
			  + "  float finalDivisor = exp2(float(2 * MAX_LEVEL + 2)) - 4.; // geometric series sum, can be calculated at compile "
			  + "                                                               time\n"  //
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

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

package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.FloatFrameBuffer;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.tests.utils.GdxTest;

public class FloatTextureTest extends GdxTest {
	FrameBuffer fb;
	FloatFrameBuffer ffb;
	ShaderProgram fbshader, shader;
	Texture texture;
	Mesh quad, screenQuad;
	OrthographicCamera screenCamera;

	@Override
	public void create () {
		fb = new FrameBuffer(Format.RGBA8888, 200, 100, false);
		ffb = new FloatFrameBuffer(200, 100, false);

		// @off
		String vertexShader = 
			  "attribute vec4 a_position; "
			+ "varying vec2 v_position; "
			+ "void main(){ "
			+ "    v_position = a_position.xy; "
			+ "    gl_Position = vec4(a_position.x, a_position.y, 0.0, 1.0); "
			+ "}";

		String fragmentShader = 
			  "#ifdef GL_ES\n"
		   + "precision mediump float;\n"
		   + "#endif\n" + "uniform vec3 u_color;"
			+ "uniform vec2 u_viewport; "
		   + "void main(void){ "
			+ "    vec2 uv = gl_FragCoord.xy/u_viewport; "
		   + "    float res = mix(0.0, 0.0001, uv.x); " // <--- // regular (non-float) texture loses precision here, res == 0 for every fragment
			+ "    gl_FragColor = vec4(u_color, res); "
		   + "}";

		fbshader = new ShaderProgram(vertexShader, fragmentShader);

		vertexShader = 
			  "attribute vec4 a_position; "
		   + "attribute vec4 a_color; "
		   + "attribute vec2 a_texCoords; "
			+ "uniform mat4 u_worldView; "
		   + "varying vec4 v_color; "
			+ "varying vec2 v_texCoords; "
		   + "void main() "
			+ "{ "
		   + "    v_color = a_color; "
			+ "    v_texCoords = a_texCoords; "
			+ "    gl_Position =  u_worldView * a_position; "
			+ "}";

		fragmentShader =
			  "#ifdef GL_ES\n"
		   + "precision mediump float;\n"
			+ "#endif\n"
		   + "varying vec2 v_texCoords; "
			+ "uniform sampler2D u_fbtex, u_ffbtex; "
			+ "vec4 getValue(vec4 col) {"
			+ "    if (col.a > 0.00005)"
			+ "        return vec4(col.rgb, 1.0);"
			+ "    else"
			+ "        return vec4(0.0, 0.0, 0.0, 1.0);"
			+ "}"
			+ "void main() "
			+ "{ "
			+ "    if (v_texCoords.y < 0.45)"
			+ "        gl_FragColor = getValue(texture2D(u_fbtex, v_texCoords)); "
			+ "    else if (v_texCoords.y > 0.55)"
			+ "        gl_FragColor = getValue(texture2D(u_ffbtex, v_texCoords)); "
			+ "}";
		// @on
		
		shader = new ShaderProgram(vertexShader, fragmentShader);
		createQuad();

		screenCamera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		createScreenQuad();
	}

	public void render () {
		Gdx.gl20.glViewport(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);

		fb.begin();
		fbshader.begin();
		fbshader.setUniformf("u_viewport", fb.getWidth(), fb.getHeight());
		fbshader.setUniformf("u_color", 0.0f, 1.0f, 0.0f);
		quad.render(fbshader, GL20.GL_TRIANGLES);
		fbshader.end();
		fb.end();

		ffb.begin();
		fbshader.begin();
		fbshader.setUniformf("u_viewport", ffb.getWidth(), ffb.getHeight());
		fbshader.setUniformf("u_color", 1.0f, 0.0f, 0.0f);
		quad.render(fbshader, GL20.GL_TRIANGLES);
		fbshader.end();
		ffb.end();

		shader.begin();
		fb.getColorBufferTexture().bind(0);
		ffb.getColorBufferTexture().bind(1);
		shader.setUniformMatrix("u_worldView", screenCamera.combined);
		shader.setUniformi("u_fbtex", 0);
		shader.setUniformi("u_ffbtex", 1);
		screenQuad.render(shader, GL20.GL_TRIANGLES);
		shader.end();
	}

	private void createQuad () {
		if (quad != null) return;
		quad = new Mesh(true, 4, 6, new VertexAttribute(Usage.Position, 3, "a_position"), new VertexAttribute(Usage.ColorUnpacked, 4,
			"a_color"), new VertexAttribute(Usage.TextureCoordinates, 2, "a_texCoords"));

		quad.setVertices(new float[] {-1, -1, 0, 1, 1, 1, 1, 0, 1, 1, -1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 0, -1, 1, 0,
			1, 1, 1, 1, 0, 0});
		quad.setIndices(new short[] {0, 1, 2, 2, 3, 0});
	}

	private void createScreenQuad () {
		if (screenQuad != null) return;
		screenQuad = new Mesh(true, 4, 6, new VertexAttribute(Usage.Position, 3, "a_position"), new VertexAttribute(Usage.ColorUnpacked, 4,
			"a_color"), new VertexAttribute(Usage.TextureCoordinates, 2, "a_texCoords"));

		Vector3 vec0 = new Vector3(0, 0, 0);
		screenCamera.unproject(vec0);
		Vector3 vec1 = new Vector3(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 0);
		screenCamera.unproject(vec1);
		screenQuad.setVertices(new float[] {vec0.x, vec0.y, 0, 1, 1, 1, 1, 0, 1, vec1.x, vec0.y, 0, 1, 1, 1, 1, 1, 1, vec1.x,
			vec1.y, 0, 1, 1, 1, 1, 1, 0, vec0.x, vec1.y, 0, 1, 1, 1, 1, 0, 0});
		screenQuad.setIndices(new short[] {0, 1, 2, 2, 3, 0});
	}
}

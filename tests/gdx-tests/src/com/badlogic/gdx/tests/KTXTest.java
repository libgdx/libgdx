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
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.CubemapAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader.Config;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.KTXTextureData;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.tests.utils.GdxTest;

/** Simple test and example for the KTX/ZKTX file format
 * @author Vincent Bousquet */
public class KTXTest extends GdxTest {

	// 3D texture cubemap example
	private PerspectiveCamera perspectiveCamera;
	private CameraInputController inputController;
	private ModelBatch modelBatch;
	private Model model;
	private ModelInstance instance;
	private Environment environment;
	private Cubemap cubemap;

	// 2D texture alpha ETC1 example
	private OrthographicCamera orthoCamera;
	private Texture image;
	private SpriteBatch batch;
	private ShaderProgram etc1aShader;

	// animation
	private float time;

	@Override
	public void create () {

		// Cubemap test

		String cubemapVS = "" //
			+ "attribute vec3 a_position;\n"//
			+ "uniform mat4 u_projViewTrans;\n"//
			+ "uniform mat4 u_worldTrans;\n"//
			+ "\n"//
			+ "varying vec3 v_cubeMapUV;\n"//
			+ "\n"//
			+ "void main() {\n"//
			+ "   vec4 g_position = vec4(a_position, 1.0);\n"//
			+ "   g_position = u_worldTrans * g_position;\n"//
			+ "   v_cubeMapUV = normalize(g_position.xyz);\n"//
			+ "   gl_Position = u_projViewTrans * g_position;\n"//
			+ "}";
		String cubemapFS = ""//
			+ "#ifdef GL_ES\n"//
			+ "precision mediump float;\n"//
			+ "#endif\n"//
			+ "uniform samplerCube u_environmentCubemap;\n"//
			+ "varying vec3 v_cubeMapUV;\n"//
			+ "void main() {\n" //
			+ "	gl_FragColor = vec4(textureCube(u_environmentCubemap, v_cubeMapUV).rgb, 1.0);\n" //
			+ "}\n";
		modelBatch = new ModelBatch(new DefaultShaderProvider(new Config(cubemapVS, cubemapFS)));

		cubemap = new Cubemap(new KTXTextureData(Gdx.files.internal("data/cubemap.zktx"), true));
		cubemap.setFilter(TextureFilter.MipMapLinearLinear, TextureFilter.Linear);

		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.1f, 0.1f, 0.1f, 1.f));
		environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -0.5f, -1.0f, -0.8f));
		environment.set(new CubemapAttribute(CubemapAttribute.EnvironmentMap, cubemap));

		perspectiveCamera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		perspectiveCamera.position.set(10f, 10f, 10f);
		perspectiveCamera.lookAt(0, 0, 0);
		perspectiveCamera.near = 0.1f;
		perspectiveCamera.far = 300f;
		perspectiveCamera.update();

		ModelBuilder modelBuilder = new ModelBuilder();
		model = modelBuilder.createBox(5f, 5f, 5f, new Material(ColorAttribute.createDiffuse(Color.GREEN)), Usage.Position
			| Usage.Normal);
		instance = new ModelInstance(model);

		Gdx.input.setInputProcessor(new InputMultiplexer(this, inputController = new CameraInputController(perspectiveCamera)));

		// 2D texture test
		String etc1aVS = "" //
			+ "uniform mat4 u_projTrans;\n"//
			+ "\n"//
			+ "attribute vec4 a_position;\n"//
			+ "attribute vec2 a_texCoord0;\n"//
			+ "attribute vec4 a_color;\n"//
			+ "\n"//
			+ "varying vec4 v_color;\n"//
			+ "varying vec2 v_texCoord;\n"//
			+ "\n"//
			+ "void main() {\n"//
			+ "   gl_Position = u_projTrans * a_position;\n"//
			+ "   v_texCoord = a_texCoord0;\n"//
			+ "   v_color = a_color;\n"//
			+ "}\n";//
		String etc1aFS = ""//
			+ "#ifdef GL_ES\n"//
			+ "precision mediump float;\n"//
			+ "#endif\n"//
			+ "uniform sampler2D u_texture;\n"//
			+ "\n"//
			+ "varying vec4 v_color;\n"//
			+ "varying vec2 v_texCoord;\n"//
			+ "\n"//
			+ "void main() {\n"//
			+ "   vec3 col = texture2D(u_texture, v_texCoord.st).rgb;\n"//
			+ "   float alpha = texture2D(u_texture, v_texCoord.st + vec2(0.0, 0.5)).r;\n"//
			+ "   gl_FragColor = vec4(col, alpha) * v_color;\n"//
			+ "}\n";//
		etc1aShader = new ShaderProgram(etc1aVS, etc1aFS);
		orthoCamera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		image = new Texture("data/egg.zktx");
		batch = new SpriteBatch(100, etc1aShader);

	}

	@Override
	public void render () {
		time += Gdx.graphics.getDeltaTime();
		inputController.update();
		int gw = Gdx.graphics.getWidth(), gh = Gdx.graphics.getHeight();
		int pw = gw > gh ? gw / 2 : gw, ph = gw > gh ? gh : gh / 2;

		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		// cubemap
		Gdx.gl.glViewport(gw - pw, gh - ph, pw, ph);
		perspectiveCamera.viewportWidth = pw;
		perspectiveCamera.viewportHeight = ph;
		perspectiveCamera.update();
		modelBatch.begin(perspectiveCamera);
		modelBatch.render(instance, environment);
		modelBatch.end();

		// 2D texture with alpha & ETC1
		Gdx.gl.glViewport(0, 0, pw, ph);
		orthoCamera.viewportWidth = pw;
		orthoCamera.viewportHeight = ph;
		orthoCamera.update();
		batch.setProjectionMatrix(orthoCamera.combined);
		batch.begin();
		float s = 0.1f + 0.5f * (1 + MathUtils.sinDeg(time * 90.0f));
		float w = s * image.getWidth(), h = s * image.getHeight() / 2, x = -w / 2, y = -h / 2;
		batch.setShader(null);
		batch.disableBlending();
		batch.draw(image, -pw / 2, -ph / 2, pw, ph, 0, 1, 1, 0);
		batch.setShader(etc1aShader);
		batch.enableBlending();
		batch.draw(image, x, y, w, h, 0, 0.5f, 1, 0);
		batch.end();
	}

	@Override
	public void dispose () {
		modelBatch.dispose();
		model.dispose();
		cubemap.dispose();
		image.dispose();
		batch.dispose();
		etc1aShader.dispose();
	}

	public boolean needsGL20 () {
		return true;
	}

	public void resume () {
	}

	public void resize (int width, int height) {
	}

	public void pause () {
	}

}

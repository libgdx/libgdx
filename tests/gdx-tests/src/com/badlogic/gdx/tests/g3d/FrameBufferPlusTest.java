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

package com.badlogic.gdx.tests.g3d;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.glutils.FrameBufferPlus;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Array;

public class FrameBufferPlusTest extends GdxTest implements ApplicationListener {
	public PerspectiveCamera cam;
	public CameraInputController camController;
	public ModelBatch modelBatch;
	public AssetManager assets;
	public Array<ModelInstance> instances = new Array<ModelInstance>();
	public Environment lights;
	public boolean loading;

	public Array<ModelInstance> blocks = new Array<ModelInstance>();
	public Array<ModelInstance> invaders = new Array<ModelInstance>();
	public ModelInstance ship;
	public ModelInstance space;

	public FrameBufferPlus frameBuffer;
	public Mesh fullscreenQuad;
	public ShaderProgram flatTextureShader;

	public static Mesh createFullScreenQuad()
	{
		float[] verts = new float[20];
		int i = 0;

		verts[i++] = -1; // x1
		verts[i++] = -1; // y1
		verts[i++] = 0;
		verts[i++] = 0f; // u1
		verts[i++] = 0f; // v1

		verts[i++] = 1f; // x2
		verts[i++] = -1; // y2
		verts[i++] = 0;
		verts[i++] = 1f; // u2
		verts[i++] = 0f; // v2

		verts[i++] = 1f; // x3
		verts[i++] = 1f; // y2
		verts[i++] = 0;
		verts[i++] = 1f; // u3
		verts[i++] = 1f; // v3

		verts[i++] = -1; // x4
		verts[i++] = 1f; // y4
		verts[i++] = 0;
		verts[i++] = 0f; // u4
		verts[i++] = 1f; // v4

		Mesh mesh = new Mesh( true, 4, 0,  // static mesh with 4 vertices and no indices
				new VertexAttribute( VertexAttributes.Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE ),
				new VertexAttribute( VertexAttributes.Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE+"0" ) );

		mesh.setVertices( verts );
		return mesh;
	}

	@Override
	public void create () {
		frameBuffer = new FrameBufferPlus.Builder(Gdx.graphics.getWidth(), Gdx.graphics.getHeight())
				.addColorTexture(GL20.GL_RGBA)
				.addDepthTexture(GL30.GL_DEPTH24_STENCIL8)
				.build();

		modelBatch = new ModelBatch();
		lights = new Environment();
		lights = new Environment();
		lights.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1.f));
		lights.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

		cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.position.set(0f, 7f, 10f);
		cam.lookAt(0, 0, 0);
		cam.near = 1f;
		cam.far = 300f;
		cam.update();

		camController = new CameraInputController(cam);
		Gdx.input.setInputProcessor(camController);

		fullscreenQuad = createFullScreenQuad();
		flatTextureShader = new ShaderProgram(Gdx.files.internal("data/shaders/fullscreen-quad.vert.glsl"), Gdx.files.internal("data/shaders/fullscreen-quad.frag.glsl"));

		assets = new AssetManager();
		assets.load("data/g3d/invaders.g3dj", Model.class);
		loading = true;
	}

	private void doneLoading () {
		Model model = assets.get("data/g3d/invaders.g3dj", Model.class);
		for (int i = 0; i < model.nodes.size; i++) {
			String id = model.nodes.get(i).id;
			ModelInstance instance = new ModelInstance(model, id);
			Node node = instance.getNode(id);

			instance.transform.set(node.globalTransform);
			node.translation.set(0, 0, 0);
			node.scale.set(1, 1, 1);
			node.rotation.idt();
			instance.calculateTransforms();

			if (id.equals("space")) {
				space = instance;
				continue;
			}

			instances.add(instance);

			if (id.equals("ship"))
				ship = instance;
			else if (id.startsWith("block"))
				blocks.add(instance);
			else if (id.startsWith("invader")) invaders.add(instance);
		}

		loading = false;
	}

	@Override
	public void render () {
		if (loading && assets.update()) doneLoading();
		camController.update();

		// render the scene onto the frame buffer
		frameBuffer.begin();

			Gdx.gl.glViewport(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

			modelBatch.begin(cam);
			for (ModelInstance instance : instances)
				modelBatch.render(instance, lights);
			if (space != null) modelBatch.render(space);
			modelBatch.end();

		frameBuffer.end();

		// render the scene onto the backbuffer
		flatTextureShader.begin();

			frameBuffer.getColorTexture(0).bind(0);
			flatTextureShader.setUniformi("u_texture", 0);
			fullscreenQuad.render(flatTextureShader, GL20.GL_TRIANGLE_FAN);

		flatTextureShader.end();

		// render the depth in a tiny window
		Gdx.gl.glViewport(10, 10, 10 + Gdx.graphics.getWidth() / 3, 10 + Gdx.graphics.getHeight() / 3);
		flatTextureShader.begin();

			frameBuffer.getDepthTexture().bind(0);
			flatTextureShader.setUniformi("u_texture", 0);
			fullscreenQuad.render(flatTextureShader, GL20.GL_TRIANGLE_FAN);

		flatTextureShader.end();
	}

	@Override
	public void dispose () {
		modelBatch.dispose();
		instances.clear();
		assets.dispose();
	}
}

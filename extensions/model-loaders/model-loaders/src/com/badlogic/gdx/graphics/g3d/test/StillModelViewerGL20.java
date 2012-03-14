package com.badlogic.gdx.graphics.g3d.test;

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

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.jogl.JoglApplication;
import com.badlogic.gdx.graphics.FPSLogger;

import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL11;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.StillModelInstance;
import com.badlogic.gdx.graphics.g3d.StillModelNode;
import com.badlogic.gdx.graphics.g3d.experimental.ShaderLoader;
import com.badlogic.gdx.graphics.g3d.lights.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.lights.LightManager;
import com.badlogic.gdx.graphics.g3d.lights.PointLight;
import com.badlogic.gdx.graphics.g3d.loaders.ModelLoaderRegistry;
import com.badlogic.gdx.graphics.g3d.loaders.g3d.G3dLoader;
import com.badlogic.gdx.graphics.g3d.loaders.g3d.chunks.G3dExporter;
import com.badlogic.gdx.graphics.g3d.materials.Material;
import com.badlogic.gdx.graphics.g3d.model.still.StillModel;
import com.badlogic.gdx.graphics.g3d.model.still.StillSubMesh;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer10;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

public class StillModelViewerGL20 implements ApplicationListener {

	PerspectiveCamera cam;
	StillModel model;
	Texture[] textures = null;
	boolean hasNormals = false;
	BoundingBox bounds = new BoundingBox();
	float angle = 0;
	String fileName;
	String[] textureFileNames;
	FPSLogger fps = new FPSLogger();
	SpriteBatch batch;
	BitmapFont font;
	private ImmediateModeRenderer20 renderer;
	private ShaderProgram shader;
	private LightManager lightManager;
	private PrototypeRendererGL20 protoRenderer;
	private StillModelNode instance;
	private StillModelNode instance2;

	public StillModelViewerGL20(String fileName, String... textureFileNames) {
		this.fileName = fileName;
		this.textureFileNames = textureFileNames;
	}

	@Override
	public void create() {
		long start = System.nanoTime();
		model = ModelLoaderRegistry
				.loadStillModel(Gdx.files.internal(fileName));
		Gdx.app.log("StillModelViewer", "loading took: "
				+ (System.nanoTime() - start) / 1000000000.0f);

		if (textureFileNames.length != 0) {
			textures = new Texture[textureFileNames.length];
			for (int i = 0; i < textureFileNames.length; i++) {
				textures[i] = new Texture(
						Gdx.files.internal(textureFileNames[i]), i > 0 ? false
								: true);
			}
		}

		model.getBoundingBox(bounds);
		float len = bounds.getDimensions().len();
		System.out.println("bounds: " + bounds);

		cam = new PerspectiveCamera(60, Gdx.graphics.getWidth(),
				Gdx.graphics.getHeight());
		cam.position.set(bounds.getCenter().cpy()
				.add(len / 2, len / 2, len / 2));
		cam.lookAt(bounds.getCenter().x, bounds.getCenter().y,
				bounds.getCenter().z);
		cam.near = 0.1f;
		cam.far = 512;

		renderer = new ImmediateModeRenderer20(false, true, 16);
		batch = new SpriteBatch();
		font = new BitmapFont();

		shader = ShaderLoader.createShader("light", "light");

		lightManager = new LightManager(4);
		lightManager.ambientLight.set(0.1f, 0.1f, 0.1f, 0);
		lightManager.dirLight = new DirectionalLight();
		lightManager.dirLight.color.set(0.048f, 0.04f, 0.09f, 0);
		lightManager.dirLight.direction.set(-.1f, -1, 0.03f).nor();

		for (int i = 0; i < 8; i++) {
			PointLight l = new PointLight();
			l.position.set(MathUtils.random(6) - 3, MathUtils.random(9) - 3,
					-MathUtils.random(6) + 3);
			l.color.r = MathUtils.random();
			l.color.b = MathUtils.random();
			l.color.g = MathUtils.random();
			l.intensity = 64;
			lightManager.addLigth(l);
		}
		lightManager.ambientLight.set(1, 0, 0, 0);

		protoRenderer = new PrototypeRendererGL20();
		protoRenderer.setShader(shader);
		protoRenderer.setLightManager(lightManager);

		instance = new StillModelNode();
		instance.getTransform().translate(-len / 3, 4, 2);
		instance2 = new StillModelNode();
		instance2.getTransform().translate(len / 4, -2, 2);

		instance.radius = bounds.getDimensions().len() / 2;
		instance2.radius = instance.radius;

		for (StillSubMesh mesh : model.subMeshes) {
			System.out.println(mesh.material.name);
		}
	}

	@Override
	public void resume() {

	}

	@Override
	public void render() {
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		Gdx.gl.glEnable(GL10.GL_DEPTH_TEST);
		Gdx.gl.glEnable(GL10.GL_CULL_FACE);

		instance.getTransform().rotate(0, 1, -0.1f,
				35 * Gdx.graphics.getDeltaTime());
		instance2.getTransform().rotate(0, 1, 0.1f,
				-15 * Gdx.graphics.getDeltaTime());

		cam.update();

		shader.begin();
		shader.setUniformMatrix("u_projectionViewMatrix", cam.combined);

		shader.setUniformi("u_texture0", 0);
		shader.end();

		textures[0].bind(0);

		// shader.setUniformi("u_texture1", 1);
		// textures[1].bind(1);

		protoRenderer.begin();
		protoRenderer.draw(model, instance);
		protoRenderer.draw(model, instance2);
		protoRenderer.end();

		batch.begin();
		font.draw(batch, "fps: " + Gdx.graphics.getFramesPerSecond(), 20, 30);
		batch.end();

		fps.log();
	}

	@Override
	public void resize(int width, int height) {

	}

	@Override
	public void pause() {

	}

	@Override
	public void dispose() {
	}

	public static void main(String[] argv) {
		// if(argv.length != 1 && argv.length != 2) {
		// System.out.println("StillModelViewer <filename> ?<texture-filename>");
		// System.exit(-1);
		// }
		// new JoglApplication(new StillModelViewer(argv[0],
		// argv.length==2?argv[1]:null), "StillModel Viewer", 800, 480, false);
		// new JoglApplication(new
		// StillModelViewer("data/qbob/world_blobbie_brushes.g3dt",
		// "data/qbob/world_blobbie_blocks.png"),
		// "StillModel Viewer", 800, 480, false);
		new JoglApplication(new StillModelViewerGL20(
				"data/models/basicscene.obj", "data/multipleuvs_1.png",
				"data/multipleuvs_2.png"), "StillModel Viewer gles2.0", 800,
				480, true);
		// new JoglApplication(new StillModelViewer("data/head.obj"),
		// "StillModel Viewer", 800, 480, false);
	}
}
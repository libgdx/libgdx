
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

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL11;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.StillModelInstance;
import com.badlogic.gdx.graphics.g3d.StillModelNode;
import com.badlogic.gdx.graphics.g3d.experimental.ShaderFactory;
import com.badlogic.gdx.graphics.g3d.experimental.ShaderLoader;
import com.badlogic.gdx.graphics.g3d.lights.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.lights.LightManager;
import com.badlogic.gdx.graphics.g3d.lights.LightManager.LightQuality;
import com.badlogic.gdx.graphics.g3d.lights.PointLight;
import com.badlogic.gdx.graphics.g3d.loaders.ModelLoaderRegistry;
import com.badlogic.gdx.graphics.g3d.loaders.g3d.G3dLoader;
import com.badlogic.gdx.graphics.g3d.loaders.g3d.chunks.G3dExporter;
import com.badlogic.gdx.graphics.g3d.materials.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.materials.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.materials.Material;
import com.badlogic.gdx.graphics.g3d.materials.MaterialAttribute;
import com.badlogic.gdx.graphics.g3d.materials.TextureAttribute;
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
	String fileName;
	String[] textureFileNames;
	FPSLogger fps = new FPSLogger();
	SpriteBatch batch;
	BitmapFont font;
	private LightManager lightManager;
	private PrototypeRendererGL20 protoRenderer;
	private StillModelNode instance;
	private StillModelNode instance2;
	private ShaderProgram shader2;
	private ShaderProgram shader1;

	public StillModelViewerGL20 (String fileName, String... textureFileNames) {
		this.fileName = fileName;
		this.textureFileNames = textureFileNames;
	}

	@Override
	public void create () {
		long start = System.nanoTime();
		model = ModelLoaderRegistry.loadStillModel(Gdx.files.internal(fileName));
		Gdx.app.log("StillModelViewer", "loading took: " + (System.nanoTime() - start) / 1000000000.0f);

		if (textureFileNames.length != 0) {
			textures = new Texture[textureFileNames.length];
			for (int i = 0; i < textureFileNames.length; i++) {
				textures[i] = new Texture(Gdx.files.internal(textureFileNames[i]), i > 0 ? false : true);
			}
		}

		model.getBoundingBox(bounds);
		float len = bounds.getDimensions().len();
		System.out.println("bounds: " + bounds);

		cam = new PerspectiveCamera(60, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.position.set(bounds.getCenter().cpy().add(len / 2, len / 2, len / 2));
		cam.lookAt(bounds.getCenter().x, bounds.getCenter().y, bounds.getCenter().z);
		cam.near = 0.1f;
		cam.far = 64;

		batch = new SpriteBatch();
		font = new BitmapFont();

		// shader1 = ShaderLoader.createShader("light", "light");
		// shader2 = ShaderLoader.createShader("vertexpath", "vertexpath");

		lightManager = new LightManager(4, LightQuality.VERTEX);
		lightManager.dirLight = new DirectionalLight();
		lightManager.dirLight.color.set(0.09f, 0.07f, 0.09f, 0);
		lightManager.dirLight.direction.set(-.4f, -1, 0.03f).nor();

		for (int i = 0; i < 4; i++) {
			PointLight l = new PointLight();
			l.position.set(-MathUtils.random(8) + 4, MathUtils.random(3), -MathUtils.random(6) + 3);
			
			l.color.r = MathUtils.random();
			l.color.b = MathUtils.random();
			l.color.g = MathUtils.random();
			l.intensity = 3;
			lightManager.addLigth(l);
		}
		lightManager.ambientLight.set(.03f, 0.05f, 0.06f, 0);

		protoRenderer = new PrototypeRendererGL20(lightManager);
		protoRenderer.cam = cam;

		MaterialAttribute c1 = new ColorAttribute(new Color(0.5f, 0.51f, 0.51f, 1.0f), ColorAttribute.specular);
		MaterialAttribute c2 = new ColorAttribute(new Color(0.95f, 0.95f, 0.95f, 1.0f), ColorAttribute.diffuse);
		MaterialAttribute t0 = new TextureAttribute(textures[0], 0, TextureAttribute.diffuseTexture);		
		Material material = new Material("basic", c1, c2, t0);

		model.setMaterial(material);
		instance = new StillModelNode();
		instance.getTransform().translate(-len / 2, -1, 2);
		instance.radius = bounds.getDimensions().len() / 2;

		instance2 = new StillModelNode();
		instance2.getTransform().translate(len / 2, -1, -7);

		instance2.radius = instance.radius;

	}

	@Override
	public void resume () {

	}

	@Override
	public void render () {

		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		Gdx.gl.glEnable(GL10.GL_DEPTH_TEST);
		Gdx.gl.glEnable(GL10.GL_CULL_FACE);

		instance.getTransform().rotate(0, 1, -0.1f, 35 * Gdx.graphics.getDeltaTime());
		instance2.getTransform().rotate(0, 1, 0.1f, -15 * Gdx.graphics.getDeltaTime());

		cam.update();

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
	public void resize (int width, int height) {

	}

	@Override
	public void pause () {

	}

	@Override
	public void dispose () {
	}

	public static void main (String[] argv) {
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
		new JoglApplication(new StillModelViewerGL20("data/models/basicscene.obj", "data/multipleuvs_1.png",
			"data/multipleuvs_2.png"), "StillModel Viewer gles2.0", 800, 480, true);
		// new JoglApplication(new StillModelViewer("data/head.obj"),
		// "StillModel Viewer", 800, 480, false);
	}
}

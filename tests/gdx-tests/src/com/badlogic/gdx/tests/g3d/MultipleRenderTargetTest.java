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

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Attributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelCache;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.BaseShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.DefaultRenderableSorter;
import com.badlogic.gdx.graphics.g3d.utils.DefaultTextureBinder;
import com.badlogic.gdx.graphics.g3d.utils.FirstPersonCameraController;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.g3d.utils.RenderableSorter;
import com.badlogic.gdx.graphics.g3d.utils.ShaderProvider;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.GLFrameBuffer;
import com.badlogic.gdx.graphics.glutils.GLOnlyTextureData;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.StringBuilder;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

/** MRT test compliant with GLES 3.0, with per pixel lighting and normal and specular mapping.
 * Thanks to http://www.blendswap.com/blends/view/73922 for the cannon model, licensed under CC-BY-SA
 *
 /** @author Tomski */
public class MultipleRenderTargetTest extends GdxTest {

	RenderContext renderContext;
	FrameBuffer frameBuffer;

	PerspectiveCamera camera;
	FirstPersonCameraController cameraController;

	ShaderProgram mrtSceneShader;

	SpriteBatch batch;
	Mesh quad;

	ShaderProvider shaderProvider;
	RenderableSorter renderableSorter;

	ModelCache modelCache;
	ModelInstance floorInstance;
	ModelInstance cannon;
	Array<Light> lights = new Array<Light>();
	Array<Renderable> renderables = new Array<Renderable>();
	RenderablePool renerablePool = new RenderablePool();

	static int DIFFUSE_ATTACHMENT = 0;
	static int NORMAL_ATTACHMENT = 1;
	static int POSITION_ATTACHMENT = 2;
	static int DEPTH_ATTACHMENT = 3;


	final int NUM_LIGHTS = 10;

	@Override
	public void create () {
		//use default prepend shader code for batch, some gpu drivers are less forgiving
		batch = new SpriteBatch();

		ShaderProgram.pedantic = false;//depth texture not currently sampled

		modelCache = new ModelCache();

		ShaderProgram.prependVertexCode = Gdx.app.getType().equals(Application.ApplicationType.Desktop) ? "#version 140\n #extension GL_ARB_explicit_attrib_location : enable\n" : "#version 300 es\n";
		ShaderProgram.prependFragmentCode = Gdx.app.getType().equals(Application.ApplicationType.Desktop) ? "#version 140\n #extension GL_ARB_explicit_attrib_location : enable\n" : "#version 300 es\n";

		renderContext = new RenderContext(new DefaultTextureBinder(DefaultTextureBinder.ROUNDROBIN));
		shaderProvider = new BaseShaderProvider() {
			@Override
			protected Shader createShader (Renderable renderable) {
				return new MRTShader(renderable);
			}
		};
		renderableSorter = new DefaultRenderableSorter() {
			@Override
			public int compare (Renderable o1, Renderable o2) {
				return o1.shader.compareTo(o2.shader);
			}

		};

		mrtSceneShader = new ShaderProgram(Gdx.files.internal("data/g3d/shaders/mrtscene.vert"),
			Gdx.files.internal("data/g3d/shaders/mrtscene.frag"));
		if (!mrtSceneShader.isCompiled()) {
			System.out.println(mrtSceneShader.getLog());
		}

		quad = createFullScreenQuad();

		camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera.near = 1f;
		camera.far = 100f;
		camera.position.set(3, 5, 10);
		camera.lookAt(0, 2, 0);
		camera.up.set(0, 1, 0);
		camera.update();
		cameraController = new FirstPersonCameraController(camera);
		cameraController.setVelocity(50);
		Gdx.input.setInputProcessor(cameraController);

		GLFrameBuffer.FrameBufferBuilder frameBufferBuilder = new GLFrameBuffer.FrameBufferBuilder(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		frameBufferBuilder.addColorTextureAttachment(GL30.GL_RGBA8, GL30.GL_RGBA, GL30.GL_UNSIGNED_BYTE);
		frameBufferBuilder.addColorTextureAttachment(GL30.GL_RGB8, GL30.GL_RGB, GL30.GL_UNSIGNED_BYTE);
		frameBufferBuilder.addColorTextureAttachment(GL30.GL_RGB8, GL30.GL_RGB, GL30.GL_UNSIGNED_BYTE);
		frameBufferBuilder.addDepthTextureAttachment(GL30.GL_DEPTH_COMPONENT, GL30.GL_UNSIGNED_SHORT);

		frameBuffer = frameBufferBuilder.build();

		AssetManager assetManager = new AssetManager();
		assetManager.load("data/g3d/materials/cannon.g3db", Model.class);
		assetManager.finishLoading();

		Model scene = assetManager.get("data/g3d/materials/cannon.g3db");

		cannon = new ModelInstance(scene, "Cannon_LP");
		cannon.transform.setToTranslationAndScaling(0, 0, 0, 0.001f, 0.001f, 0.001f);

		ModelBuilder modelBuilder = new ModelBuilder();

		for (int i = 0; i < NUM_LIGHTS; i++) {
			modelBuilder.begin();

			Light light = new Light();
			light.color.set(MathUtils.random(1f), MathUtils.random(1f), MathUtils.random(1f));
			light.position.set(MathUtils.random(-10f, 10f), MathUtils.random(10f, 15f), MathUtils.random(-10f, 10f));
			light.vy = MathUtils.random(10f, 20f);
			light.vx = MathUtils.random(-10f, 10f);
			light.vz = MathUtils.random(-10f, 10f);

			MeshPartBuilder meshPartBuilder = modelBuilder.part("light", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.ColorPacked | VertexAttributes.Usage.Normal, new Material());
			meshPartBuilder.setColor(light.color.x, light.color.y, light.color.z, 1f);
			meshPartBuilder.sphere(0.2f, 0.2f, 0.2f, 10, 10);

			light.lightInstance = new ModelInstance(modelBuilder.end());
			lights.add(light);
		}

		modelBuilder.begin();
		MeshPartBuilder meshPartBuilder = modelBuilder.part("floor", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.ColorPacked | VertexAttributes.Usage.Normal, new Material());
		meshPartBuilder.setColor(0.2f, 0.2f, 0.2f, 1f);
		meshPartBuilder.box(0, -0.1f, 0f, 20f, 0.1f, 20f);
		floorInstance = new ModelInstance(modelBuilder.end());

		Gdx.input.setInputProcessor(new InputMultiplexer(this, cameraController));
	}

	@Override
	public boolean keyDown (int keycode) {
		if (keycode == Input.Keys.SPACE) {
			for (Light light : lights) {
				light.vy = MathUtils.random(10f, 20f);
				light.vx = MathUtils.random(-10f, 10f);
				light.vz = MathUtils.random(-10f, 10f);
			}
		}
		return super.keyDown(keycode);
	}

	float track;

	@Override
	public void render () {
		track += Gdx.graphics.getDeltaTime();

		Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		cameraController.update(Gdx.graphics.getDeltaTime());

		renderContext.begin();

		frameBuffer.begin();
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		renerablePool.flush();
		renderables.clear();

		modelCache.begin(camera);
		modelCache.add(cannon);
		modelCache.add(floorInstance);
		for (Light light : lights) {
			light.update(Gdx.graphics.getDeltaTime());
			modelCache.add(light.lightInstance);
		}
		modelCache.end();
		modelCache.getRenderables(renderables, renerablePool);

		for (Renderable renderable : renderables) {
			renderable.shader = shaderProvider.getShader(renderable);
		}

		renderableSorter.sort(camera, renderables);
		Shader currentShader = null;
		for (int i = 0; i < renderables.size; i++) {
			final Renderable renderable = renderables.get(i);
			if (currentShader != renderable.shader) {
				if (currentShader != null) currentShader.end();
				currentShader = renderable.shader;
				currentShader.begin(camera, renderContext);
			}
			currentShader.render(renderable);
		}
		if (currentShader != null) currentShader.end();

		frameBuffer.end();

		mrtSceneShader.bind();
		mrtSceneShader.setUniformi("u_diffuseTexture",
			renderContext.textureBinder.bind(frameBuffer.getTextureAttachments().get(DIFFUSE_ATTACHMENT)));
		mrtSceneShader.setUniformi("u_normalTexture",
			renderContext.textureBinder.bind(frameBuffer.getTextureAttachments().get(NORMAL_ATTACHMENT)));
		mrtSceneShader.setUniformi("u_positionTexture",
			renderContext.textureBinder.bind(frameBuffer.getTextureAttachments().get(POSITION_ATTACHMENT)));
		mrtSceneShader.setUniformi("u_depthTexture", renderContext.textureBinder.bind(frameBuffer.getTextureAttachments().get(DEPTH_ATTACHMENT)));
		for (int i = 0; i < lights.size; i++) {
			Light light = lights.get(i);
			mrtSceneShader.setUniformf("lights[" + i + "].lightPosition", light.position);
			mrtSceneShader.setUniformf("lights[" + i + "].lightColor", light.color);
		}
		mrtSceneShader.setUniformf("u_viewPos", camera.position);
		mrtSceneShader.setUniformMatrix("u_inverseProjectionMatrix", camera.invProjectionView);
		quad.render(mrtSceneShader, GL30.GL_TRIANGLE_FAN);
		renderContext.end();


		batch.disableBlending();
		batch.begin();
		batch.draw(frameBuffer.getTextureAttachments().get(DIFFUSE_ATTACHMENT), 0, 0, Gdx.graphics.getWidth() / 4f,
			Gdx.graphics.getHeight() / 4f, 0f, 0f, 1f, 1f);
		batch.draw(frameBuffer.getTextureAttachments().get(NORMAL_ATTACHMENT), Gdx.graphics.getWidth() / 4f, 0,
			Gdx.graphics.getWidth() / 4f, Gdx.graphics.getHeight() / 4f, 0f, 0f, 1f, 1f);
		batch.draw(frameBuffer.getTextureAttachments().get(POSITION_ATTACHMENT), 2 * Gdx.graphics.getWidth() / 4f, 0,
			Gdx.graphics.getWidth() / 4f, Gdx.graphics.getHeight() / 4f, 0f, 0f, 1f, 1f);
		batch.draw(frameBuffer.getTextureAttachments().get(DEPTH_ATTACHMENT), 3 * Gdx.graphics.getWidth() / 4f, 0,
			Gdx.graphics.getWidth() / 4f, Gdx.graphics.getHeight() / 4f, 0f, 0f, 1f, 1f);
		batch.end();
	}

	@Override
	public void dispose () {
		frameBuffer.dispose();
		batch.dispose();
		cannon.model.dispose();
		floorInstance.model.dispose();
		for (Light light : lights) {
			light.lightInstance.model.dispose();
		}
		mrtSceneShader.dispose();
		quad.dispose();
	}

	public Mesh createFullScreenQuad () {

		float[] verts = new float[20];
		int i = 0;

		verts[i++] = -1;
		verts[i++] = -1;
		verts[i++] = 0;
		verts[i++] = 0f;
		verts[i++] = 0f;

		verts[i++] = 1f;
		verts[i++] = -1;
		verts[i++] = 0;
		verts[i++] = 1f;
		verts[i++] = 0f;

		verts[i++] = 1f;
		verts[i++] = 1f;
		verts[i++] = 0;
		verts[i++] = 1f;
		verts[i++] = 1f;

		verts[i++] = -1;
		verts[i++] = 1f;
		verts[i++] = 0;
		verts[i++] = 0f;
		verts[i++] = 1f;

		Mesh mesh = new Mesh(true, 4, 0,
			new VertexAttribute(VertexAttributes.Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE),
			new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + "0"));

		mesh.setVertices(verts);
		return mesh;
	}

	static class Light {
		Vector3 position = new Vector3();
		Vector3 color = new Vector3();
		ModelInstance lightInstance;

		float vy;
		float vx;
		float vz;

		public void update (float deltaTime) {
			vy += -30f * deltaTime;

			position.y += vy * deltaTime;
			position.x += vx * deltaTime;
			position.z += vz * deltaTime;

			if (position.y < 0.1f) {
				vy *= -0.70f;
				position.y = 0.1f;
			}
			if (position.x < -5) {
				vx = -vx;
				position.x = -5;
			}
			if (position.x > 5) {
				vx = -vx;
				position.x = 5;
			}
			if (position.z < -5) {
				vz = -vz;
				position.z = -5;
			}
			if (position.z > 5) {
				vz = -vz;
				position.z = 5;
			}

			lightInstance.transform.setToTranslation(position);
		}
	}

	static class MRTShader implements Shader {

		ShaderProgram shaderProgram;
		long attributes;

		RenderContext context;

		Matrix3 matrix3 = new Matrix3();
		static Attributes tmpAttributes = new Attributes();

		public MRTShader (Renderable renderable) {
			String prefix = "";
			if (renderable.material.has(TextureAttribute.Normal)) {
				prefix += "#define texturedFlag\n";
			}

			String vert = Gdx.files.internal("data/g3d/shaders/mrt.vert").readString();
			String frag = Gdx.files.internal("data/g3d/shaders/mrt.frag").readString();
			shaderProgram = new ShaderProgram(prefix + vert, prefix + frag);
			if (!shaderProgram.isCompiled()) {
				throw new GdxRuntimeException(shaderProgram.getLog());
			}
			renderable.material.set(tmpAttributes);
			attributes = tmpAttributes.getMask();
		}

		@Override
		public void init () {
		}

		@Override
		public int compareTo (Shader other) {
			//quick and dirty shader sort
			if (((MRTShader) other).attributes == attributes) return 0;
			if ((((MRTShader) other).attributes & TextureAttribute.Normal) == 1) return -1;
			return 1;

		}

		@Override
		public boolean canRender (Renderable instance) {
			return attributes == instance.material.getMask();
		}

		@Override
		public void begin (Camera camera, RenderContext context) {
			this.context = context;
			shaderProgram.bind();
			shaderProgram.setUniformMatrix("u_projViewTrans", camera.combined);
			context.setDepthTest(GL20.GL_LEQUAL);
			context.setCullFace(GL20.GL_BACK);
		}

		@Override
		public void render (Renderable renderable) {
			Material material = renderable.material;

			TextureAttribute diffuseTexture = (TextureAttribute)material.get(TextureAttribute.Diffuse);
			TextureAttribute normalTexture = (TextureAttribute)material.get(TextureAttribute.Normal);
			TextureAttribute specTexture = (TextureAttribute)material.get(TextureAttribute.Specular);

			if (diffuseTexture != null) {
				shaderProgram.setUniformi("u_diffuseTexture", context.textureBinder.bind(diffuseTexture.textureDescription.texture));
			}
			if (normalTexture != null) {
				shaderProgram.setUniformi("u_normalTexture", context.textureBinder.bind(normalTexture.textureDescription.texture));
			}
			if (specTexture != null) {
				shaderProgram.setUniformi("u_specularTexture", context.textureBinder.bind(specTexture.textureDescription.texture));
			}

			shaderProgram.setUniformMatrix("u_worldTrans", renderable.worldTransform);
			shaderProgram.setUniformMatrix("u_normalMatrix", matrix3.set(renderable.worldTransform).inv().transpose());

			renderable.meshPart.render(shaderProgram);
		}

		@Override
		public void end () {

		}

		@Override
		public void dispose () {
			shaderProgram.dispose();
		}
	}

	protected static class RenderablePool extends Pool<Renderable> {
		protected Array<Renderable> obtained = new Array<Renderable>();

		@Override
		protected Renderable newObject () {
			return new Renderable();
		}

		@Override
		public Renderable obtain () {
			Renderable renderable = super.obtain();
			renderable.environment = null;
			renderable.material = null;
			renderable.meshPart.set("", null, 0, 0, 0);
			renderable.shader = null;
			obtained.add(renderable);
			return renderable;
		}

		public void flush () {
			super.freeAll(obtained);
			obtained.clear();
		}
	}

}

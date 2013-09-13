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

package com.badlogic.gdx.tests.bullet;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.lights.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.lights.DirectionalShadowLight;
import com.badlogic.gdx.graphics.g3d.lights.Lights;
import com.badlogic.gdx.graphics.g3d.lights.PointLight;
import com.badlogic.gdx.graphics.g3d.loader.ObjLoader;
import com.badlogic.gdx.graphics.g3d.materials.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.materials.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.materials.Material;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.DepthShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.physics.bullet.btIDebugDraw;
import com.badlogic.gdx.physics.bullet.btIDebugDraw.DebugDrawModes;
import com.badlogic.gdx.physics.bullet.btRigidBody;
import com.badlogic.gdx.physics.bullet.btTransform;
import com.badlogic.gdx.physics.bullet.gdxBullet;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.SharedLibraryLoader;

/** @author xoppa */
public class BaseBulletTest extends BulletTest {
	// Set this to the path of the lib to use it on desktop instead of default lib. 
	private final static String customDesktopLib = null;//"D:\\Data\\code\\android\\libs\\libgdx\\extensions\\gdx-bullet\\jni\\vs\\gdxBullet\\x64\\Debug\\gdxBullet.dll";
	
	private static boolean initialized = false;
	public static void init() {
		if (initialized) return;
		// Need to initialize bullet before using it.
		if (Gdx.app.getType() == ApplicationType.Desktop && customDesktopLib != null)
			System.load(customDesktopLib);
		else
			Bullet.init();
		Gdx.app.log("Bullet", "Version = "+gdxBullet.btGetVersion());
		initialized = true;
	}
	
	public Lights lights;
	public DirectionalShadowLight shadowLight;
	public ModelBatch shadowBatch;

	public BulletWorld world;
	public ObjLoader objLoader = new ObjLoader();
	public ModelBuilder modelBuilder = new ModelBuilder();
	public ModelBatch modelBatch;
	public Array<Disposable> disposables = new Array<Disposable>();
	private int debugMode = DebugDrawModes.DBG_NoDebug;

	public BulletWorld createWorld() {
		return new BulletWorld();
	}
	
	@Override
	public void create () {
		init();
		lights = new Lights(0.3f, 0.3f, 0.3f).add(
			(shadowLight = new DirectionalShadowLight(1024, 1024, 30f, 30f, 1f, 100f)).set(0.8f, 0.8f, 0.8f, -0.5f, -1f, 0.7f)
		);
		lights.shadowMap = shadowLight;
		shadowBatch = new ModelBatch(new DepthShaderProvider());
		
		modelBatch = new ModelBatch();
		
		world = createWorld();
		world.performanceCounter = performanceCounter;

		final float width = Gdx.graphics.getWidth();
		final float height = Gdx.graphics.getHeight();
		if (width > height)
			camera = new PerspectiveCamera(67f, 3f * width / height, 3f);
		else
			camera = new PerspectiveCamera(67f, 3f, 3f * height / width);
		camera.position.set(10f, 10f, 10f);
		camera.lookAt(0, 0, 0);
		camera.update();
		
		// Create some simple models
		final Model groundModel = modelBuilder.createRect(20f, 0f, -20f, -20f, 0f, -20f, -20f, 0f, 20f, 20f, 0f, 20f, 0, 1, 0, 
			new Material(ColorAttribute.createDiffuse(Color.WHITE), ColorAttribute.createSpecular(Color.WHITE), FloatAttribute.createShininess(16f)),
			Usage.Position | Usage.Normal);
		disposables.add(groundModel);
		final Model boxModel = modelBuilder.createBox(1f, 1f, 1f,
			new Material(ColorAttribute.createDiffuse(Color.WHITE), ColorAttribute.createSpecular(Color.WHITE), FloatAttribute.createShininess(64f)), 
			Usage.Position | Usage.Normal);
		disposables.add(boxModel);

		// Add the constructors
		world.addConstructor("ground", new BulletConstructor(groundModel, 0f)); // mass = 0: static body
		world.addConstructor("box", new BulletConstructor(boxModel, 1f)); // mass = 1kg: dynamic body
		world.addConstructor("staticbox", new BulletConstructor(boxModel, 0f)); // mass = 0: static body
	}
	
	@Override
	public void dispose () {
		world.dispose();
		world = null;
		
		for (Disposable disposable : disposables)
			disposable.dispose();
		disposables.clear();
		
		modelBatch.dispose();
		modelBatch = null;
		
		shadowBatch.dispose();
		shadowBatch = null;
		
		shadowLight.dispose();
		shadowLight = null;
		
		super.dispose();
	}
	
	@Override
	public void render () {
		render(true);
	}
		
	public void render(boolean update) {
		fpsCounter.put(Gdx.graphics.getFramesPerSecond());
		
		if (update)
			update();
		
		beginRender(true);

		renderWorld();

		Gdx.gl.glDisable(GL10.GL_DEPTH_TEST);
		if (debugMode != DebugDrawModes.DBG_NoDebug)
			world.setDebugMode(debugMode, camera.combined);
		Gdx.gl.glEnable(GL10.GL_DEPTH_TEST);
		
		performance.setLength(0);
		performance.append("FPS: ").append(fpsCounter.value).append(", Bullet: ")
			.append((int)(performanceCounter.load.value*100f)).append("%");
	}
	
	protected void beginRender(boolean lighting) {
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl.glClearColor(0,0,0,0);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		camera.update();
	}
	
	protected void renderWorld() {
		shadowLight.begin(Vector3.Zero, camera.direction);
		shadowBatch.begin(shadowLight.getCamera());
		world.render(shadowBatch, null);
		shadowBatch.end();
		shadowLight.end();
		
		modelBatch.begin(camera);
		world.render(modelBatch, lights);
		modelBatch.end();
	}
	
	public void update() {
		world.update();
	}
	
	public BulletEntity shoot(final float x, final float y) {
		return shoot(x,y,30f);
	}
	
	public BulletEntity shoot(final float x, final float y, final float impulse) {
		return shoot("box", x, y, impulse);
	}
	
	public BulletEntity shoot(final String what, final float x, final float y, final float impulse) {
		// Shoot a box
		Ray ray = camera.getPickRay(x, y);
		BulletEntity entity = world.add(what, ray.origin.x, ray.origin.y, ray.origin.z);
		entity.setColor(0.5f + 0.5f * (float)Math.random(), 0.5f + 0.5f * (float)Math.random(), 0.5f + 0.5f * (float)Math.random(), 1f);
		((btRigidBody)entity.body).applyCentralImpulse(ray.direction.scl(impulse));
		return entity;
	}
	
	public void setDebugMode(final int mode) {
		world.setDebugMode(debugMode = mode, camera.combined);
	}
	
	public void toggleDebugMode() {
		if (world.getDebugMode() == DebugDrawModes.DBG_NoDebug)
			setDebugMode(DebugDrawModes.DBG_DrawWireframe);
		else if (world.renderMeshes)
			world.renderMeshes = false;
		else {
			world.renderMeshes = true;
			setDebugMode(DebugDrawModes.DBG_NoDebug);
		}
	}
	
	@Override
	public boolean longPress (float x, float y) {
		toggleDebugMode();
		return true;
	}
	
	@Override
	public boolean keyUp (int keycode) {
		if (keycode == Keys.ENTER) {
			toggleDebugMode();
			return true;
		}
		return super.keyUp(keycode);
	}
}

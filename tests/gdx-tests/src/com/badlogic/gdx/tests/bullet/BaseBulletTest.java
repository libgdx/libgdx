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
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.model.data.ModelData;
import com.badlogic.gdx.graphics.g3d.model.data.ModelMaterial;
import com.badlogic.gdx.graphics.g3d.model.data.ModelMesh;
import com.badlogic.gdx.graphics.g3d.model.data.ModelMeshPart;
import com.badlogic.gdx.graphics.g3d.model.data.ModelMeshPartMaterial;
import com.badlogic.gdx.graphics.g3d.model.data.ModelNode;
import com.badlogic.gdx.graphics.g3d.old.loaders.wavefront.ObjLoader;
import com.badlogic.gdx.graphics.g3d.old.materials.Material;
import com.badlogic.gdx.graphics.g3d.test.Light;
import com.badlogic.gdx.graphics.g3d.test.TestShader;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.physics.bullet.btIDebugDraw;
import com.badlogic.gdx.physics.bullet.btIDebugDraw.DebugDrawModes;
import com.badlogic.gdx.physics.bullet.btRigidBody;
import com.badlogic.gdx.physics.bullet.btTransform;
import com.badlogic.gdx.utils.SharedLibraryLoader;

/** @author xoppa */
public class BaseBulletTest extends BulletTest {
	// Set this to the path of the lib to use it on desktop instead of default lib. 
	private final static String customDesktopLib = null; //"D:\\Data\\code\\android\\libs\\libgdx\\extensions\\gdx-bullet\\jni\\vs\\gdxBullet\\x64\\Debug\\gdxBullet.dll";
	
	private static boolean initialized = false;
	private static void init() {
		if (initialized) return;
		// Need to initialize bullet before using it.
		if (Gdx.app.getType() == ApplicationType.Desktop && customDesktopLib != null)
			System.load(customDesktopLib);
		else
			Bullet.init();

		initialized = true;
	}
	
	public Light[] lights = new Light[] {
		//new Light(Color.WHITE, Vector3.tmp.set(-10f, 10f, -10f), 150f),
		//new Light(Color.BLUE, Vector3.tmp.set(10f, 5f, 0f), 10f),
		//new Light(Color.GREEN, Vector3.tmp.set(0f, 10f, 5f), 5f)
	};

	public PerspectiveCamera camera;
	public BulletWorld world;
	public ObjLoader objLoader = new ObjLoader();
	public ModelBatch modelBatch;
			
	public BulletWorld createWorld() {
		return new BulletWorld();
	}
	
	public static Model createSimpleModel(final VertexAttribute[] attributes, final float[] vertices, final short[] indices) {
		final ModelMesh mesh = new ModelMesh();
		mesh.attributes = attributes;
		mesh.id = "mesh1";
		mesh.vertices = vertices;
		ModelMeshPart mp = new ModelMeshPart();
		mp.id = "part1";
		mp.indices = indices;
		mp.primitiveType = GL10.GL_TRIANGLES;
		mesh.parts = new ModelMeshPart[] { mp };
		ModelNode node = new ModelNode();
		node.id = "node1";
		node.meshId = "mesh1";
		node.translation = new Vector3();
		node.rotation = new Quaternion();
		node.scale = new Vector3(1,1,1);
		ModelMeshPartMaterial pm = new ModelMeshPartMaterial();
		pm.meshPartId = "part1";
		pm.materialId = "mat1";
		node.meshPartMaterials = new ModelMeshPartMaterial[] { pm };
		ModelMaterial mat = new ModelMaterial();
		mat.id = "mat1";
		mat.diffuse = new Color(Color.WHITE);
		final ModelData data = new ModelData();
		data.meshes.add(mesh);
		data.nodes.add(node);
		data.materials.add(mat);
		return new Model(data);
	}
	
	@Override
	public void create () {
		init();
		modelBatch = new ModelBatch();
		TestShader.ignoreUnimplemented = true;
		
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
		
		// Create some simple meshes
		final Model groundModel = createSimpleModel(new VertexAttribute[] { new VertexAttribute(Usage.Position, 3, "a_position") },
			new float[] {20f, 0f, 20f, 20f, 0f, -20f, -20f, 0f, 20f, -20f, 0f, -20f},
			new short[] {0, 1, 2, 1, 2, 3}); 

		final Model boxModel = createSimpleModel(new VertexAttribute[] { new VertexAttribute(Usage.Position, 3, "a_position") },
			new float[] {0.5f, 0.5f, 0.5f, 0.5f, 0.5f, -0.5f, -0.5f, 0.5f, 0.5f, -0.5f, 0.5f, -0.5f,
						0.5f, -0.5f, 0.5f, 0.5f, -0.5f, -0.5f, -0.5f, -0.5f, 0.5f, -0.5f, -0.5f, -0.5f},
			new short[] {0, 1, 2, 1, 2, 3, // top
						4, 5, 6, 5, 6, 7, // bottom
						0, 2, 4, 4, 6, 2, // front
						1, 3, 5, 5, 7, 3, // back
						2, 3, 6, 6, 7, 3, // left
						0, 1, 4, 4, 5, 1 // right
				});

		// Add the constructors
		world.addConstructor("ground", new BulletConstructor(groundModel, 0f)); // mass = 0: static body
		world.addConstructor("box", new BulletConstructor(boxModel, 1f)); // mass = 1kg: dynamic body
		world.addConstructor("staticbox", new BulletConstructor(boxModel, 0f)); // mass = 0: static body
	}
	
	@Override
	public void dispose () {
		world.dispose();
		world = null;
		
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
		
		performance.setLength(0);
		performance.append("FPS: ").append(fpsCounter.value).append(", Bullet: ")
			.append((int)(performanceCounter.load.value*100f)).append("%");
	}
	
	protected void beginRender(boolean lighting) {
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		camera.update();
		/* GL10 gl = Gdx.gl10;
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		gl.glEnable(GL10.GL_DEPTH_TEST);
		gl.glDepthFunc(GL10.GL_LEQUAL);
		gl.glEnable(GL10.GL_COLOR_MATERIAL);
		if (lighting) {
			gl.glEnable(GL10.GL_LIGHTING);
			gl.glEnable(GL10.GL_LIGHT0);
			gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_AMBIENT, lightAmbient, 0);
			gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, lightPosition, 0);
			gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_DIFFUSE, lightDiffuse, 0);
		} else
			gl.glDisable(GL10.GL_LIGHTING);
		camera.apply(Gdx.gl10); */
	}
	
	protected void renderWorld() {
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
		entity.getColor().set(0.5f + 0.5f * (float)Math.random(), 0.5f + 0.5f * (float)Math.random(), 0.5f + 0.5f * (float)Math.random(), 1f);
		((btRigidBody)entity.body).applyCentralImpulse(ray.direction.mul(impulse));
		return entity;
	}
	
	public void setDebugMode(final int mode) {
		world.setDebugMode(mode, camera.combined);
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

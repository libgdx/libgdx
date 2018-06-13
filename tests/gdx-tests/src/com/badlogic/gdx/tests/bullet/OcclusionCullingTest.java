/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.tests.bullet;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalShadowLight;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.math.RandomXS128;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.collision.btCollisionDispatcher;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btDbvtBroadphase;
import com.badlogic.gdx.physics.bullet.collision.btDefaultCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btSequentialImpulseConstraintSolver;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.StringBuilder;

/** @author jsjolund */
public class OcclusionCullingTest extends BaseBulletTest {

	/** Types of culling to use in the test application */
	private enum CullingPolicy {
		/** Occlusion culling, renders only entities which are visible from the viewpoint of a camera. Objects which are hidden
		 * behind other objects (occluded) do not need to be rendered. */
		OCCLUSION,
		/** No culling, renders all objects. */
		NONE,
		/** Simple culling which loops through all entities in the world, and checks if the radius of the object bounding box is
		 * inside the camera frustum. Hidden surfaces are not taken into account. Bullet is not used. */
		SIMPLE,
		/** Same as {@link CullingPolicy#SIMPLE}, except instead of checking each object in the world, the process is accelerated by
		 * the Bullet broadphase bounding volume tree. */
		KDOP;

		private static CullingPolicy[] val = values();

		public CullingPolicy next () {
			return val[(this.ordinal() + 1) % val.length];
		}
	}

	/** Collision objects with this collision flag can occlude other objects */
	public final static short CF_OCCLUDER_OBJECT = 512;
	/** Amount of occludee entities to spawn at program start */
	private final static int STARTING_OCCLUDEE_AMOUNT = 300;
	/** Number of occludee entities to spawn at key press */
	private final static int KEY_SPAWN_OCCLUDEE_AMOUNT = 100;
	/** Occlusion depth buffer image size */
	private final static int[] OCL_BUFFER_EXTENTS = new int[] {128, 256, 512, 32, 64};

	// Animated frustum camera settings
	private final static float FRUSTUM_CAMERA_FAR = 50f;
	private final static float FRUSTUM_CAMERA_FOV = 60f;
	private final static float FRUSTUM_ANG_SPEED = 360f / 15f;
	private final static float FRUSTUM_LIN_SPEED = -6f;
	private final static float FRUSTUM_MOVE_RADIUS = 12;

	// Occludee models and textures used in test
	private final static String DEFAULT_TEX_PATH = "data/g3d/checkboard.png";
	private final static String[] OCCLUDEE_PATHS_DYNAMIC = new String[] {"data/car.obj", "data/wheel.obj", "data/cube.obj",
			"data/g3d/ship.obj", "data/g3d/shapes/sphere.g3dj", "data/g3d/shapes/torus.g3dj",};
	private final static String[] OCCLUDEE_PATHS_STATIC = new String[OCCLUDEE_PATHS_DYNAMIC.length];
	private final static float OCCLUDEE_MAX_EXTENT = 1.5f;
	private final static Vector3 OCCLUDER_DIM = new Vector3(1f, 6f, 20f);
	private final static Vector3 GROUND_DIM = new Vector3(120, 1, 120);

	private final static int USE_FRUSTUM_CAM = 1;
	private final static int PAUSE_FRUSTUM_CAM = 2;
	private final static int SHOW_DEBUG_IMAGE = 4;
	private final Vector3 frustumCamPos = new Vector3(0, 4, FRUSTUM_MOVE_RADIUS);
	private float frustumCamAngleY;

	private PerspectiveCamera frustumCam;
	private ModelInstance frustumInstance;
	private PerspectiveCamera overviewCam;

	final Array<BulletEntity> visibleEntities = new Array<BulletEntity>();

	// Program state variables
	private CullingPolicy cullingPolicy = CullingPolicy.OCCLUSION;
	private int bufferExtentIndex = 0;
	private int state = 0;

	// For occlusion culling
	private OcclusionBuffer oclBuffer;
	private OcclusionCuller occlusionCuller;
	private btDbvtBroadphase broadphase;

	private final RandomXS128 rng = new RandomXS128(0);

	// For drawing occlusion buffer debug image
	private ShapeRenderer shapeRenderer;
	private SpriteBatch spriteBatch;

	private GLProfiler glProfiler;

	/** Adds an occluder entity of specified type
	 *
	 * @param type Type name
	 * @param rotationY Rotation on Y axis in degrees
	 * @param position The world position
	 * @return The added entity */
	private BulletEntity addOccluder (String type, float rotationY, Vector3 position) {
		BulletEntity e = world.add(type, 0, 0, 0);
		e.body.setWorldTransform(e.transform.setToRotation(Vector3.Y, rotationY).setTranslation(position));
		e.body.setCollisionFlags(e.body.getCollisionFlags() | CF_OCCLUDER_OBJECT);
		e.setColor(Color.RED);
		return e;
	}

	/** Adds an occludee entity of random type at a random place on the ground.
	 *
	 * @param dynamic If true, entity body will be dynamic (mass > 0)
	 * @return The added entity */
	private BulletEntity addRandomOccludee (boolean dynamic) {
		// Add occludee to world
		BulletEntity entity = world.add(getRandomOccludeeType(dynamic), 0, 0, 0);
		entity.setColor(Color.WHITE);
		// Random rotation
		float rotationY = rng.nextFloat() * 360f;
		// Random ground position
		Vector3 position = tmpV1;
		int maxDstX = (int)(GROUND_DIM.x * 0.49f);
		position.x = rng.nextInt(maxDstX) * ((rng.nextBoolean()) ? 1 : -1);
		position.z = rng.nextInt(maxDstX) * ((rng.nextBoolean()) ? 1 : -1);
		position.y = entity.boundingBox.getDimensions(tmpV2).y * 0.5f;
		entity.modelInstance.transform.setToRotation(Vector3.Y, rotationY).setTranslation(position);
		entity.body.setWorldTransform(entity.modelInstance.transform);
		return entity;
	}

	@Override
	public void create () {
		Gdx.input.setOnscreenKeyboardVisible(true);
		super.create();

		glProfiler = new GLProfiler(Gdx.graphics);
		glProfiler.enable();

		StringBuilder sb = new StringBuilder();
		sb.append("Swipe for next test\n");
		sb.append("Long press to toggle debug mode\n");
		sb.append("Ctrl+drag to rotate\n");
		sb.append("Scroll to zoom\n");
		sb.append("Tap to spawn dynamic entity, press\n");
		sb.append("'0' to spawn ").append(KEY_SPAWN_OCCLUDEE_AMOUNT).append(" static entities\n");
		sb.append("'1' to set normal/disabled/occlusion-culling\n");
		sb.append("'2' to change camera\n");
		sb.append("'3' to toggle camera movement\n");
		sb.append("'4' to cycle occlusion buffer sizes\n");
		sb.append("'5' to toggle occlusion buffer image\n");
		sb.append("'6' to toggle shadows\n");
		instructions = sb.toString();

		AssetManager assets = new AssetManager();
		disposables.add(assets);
		for (String modelName : OCCLUDEE_PATHS_DYNAMIC)
			assets.load(modelName, Model.class);
		assets.load(DEFAULT_TEX_PATH, Texture.class);

		Camera shadowCamera = ((DirectionalShadowLight)light).getCamera();
		shadowCamera.viewportWidth = shadowCamera.viewportHeight = 120;

		// User controlled camera
		overviewCam = camera;
		overviewCam.position.set(overviewCam.direction).nor().scl(-100);
		overviewCam.lookAt(Vector3.Zero);
		overviewCam.far = camera.far *= 2;
		overviewCam.update(true);

		// Animated frustum camera model
		frustumCam = new PerspectiveCamera(FRUSTUM_CAMERA_FOV, camera.viewportWidth, camera.viewportHeight);
		frustumCam.far = FRUSTUM_CAMERA_FAR;
		frustumCam.update(true);
		final Model frustumModel = FrustumCullingTest.createFrustumModel(frustumCam.frustum.planePoints);
		frustumModel.materials.first().set(new ColorAttribute(ColorAttribute.AmbientLight, Color.WHITE));
		disposables.add(frustumModel);
		frustumInstance = new ModelInstance(frustumModel);

		spriteBatch = new SpriteBatch();
		disposables.add(spriteBatch);

		shapeRenderer = new ShapeRenderer();
		disposables.add(shapeRenderer);

		oclBuffer = new OcclusionBuffer(OCL_BUFFER_EXTENTS[0], OCL_BUFFER_EXTENTS[0]);
		disposables.add(oclBuffer);

		occlusionCuller = new OcclusionCuller() {

			@Override
			public boolean isOccluder (btCollisionObject object) {
				return (object.getCollisionFlags() & CF_OCCLUDER_OBJECT) != 0;
			}

			@Override
			public void onObjectVisible (btCollisionObject object) {
				visibleEntities.add(world.entities.get(object.getUserValue()));
			}

		};
		disposables.add(occlusionCuller);

		// Add occluder walls
		final Model occluderModel = modelBuilder.createBox(OCCLUDER_DIM.x, OCCLUDER_DIM.y, OCCLUDER_DIM.z,
				new Material(ColorAttribute.createDiffuse(Color.WHITE)),
				VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
		disposables.add(occluderModel);
		world.addConstructor("wall", new BulletConstructor(occluderModel, 0, new btBoxShape(tmpV1.set(OCCLUDER_DIM).scl(0.5f))));
		float y = OCCLUDER_DIM.y * 0.5f;
		addOccluder("wall", 0, tmpV1.set(20, y, 0));
		addOccluder("wall", -60, tmpV1.set(10, y, 20));
		addOccluder("wall", 60, tmpV1.set(10, y, -20));
		addOccluder("wall", 0, tmpV1.set(-20, y, 0));
		addOccluder("wall", 60, tmpV1.set(-10, y, 20));
		addOccluder("wall", -60, tmpV1.set(-10, y, -20));

		// Add ground
		final Model groundModel = modelBuilder.createBox(GROUND_DIM.x, GROUND_DIM.y, GROUND_DIM.z,
				new Material(ColorAttribute.createDiffuse(Color.WHITE)),
				VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
		btCollisionShape groundShape = new btBoxShape(tmpV1.set(GROUND_DIM).scl(0.5f));
		world.addConstructor("big_ground", new BulletConstructor(groundModel, 0, groundShape));
		BulletEntity e = world.add("big_ground", 0, -GROUND_DIM.y * 0.5f, 0f);
		e.body.setFriction(1f);
		e.setColor(Color.FOREST);

		// Occludee entity constructors. Scale models uniformly and set a default diffuse texture.
		BoundingBox bb = new BoundingBox();
		assets.finishLoadingAsset(DEFAULT_TEX_PATH);
		TextureAttribute defaultTexture = new TextureAttribute(TextureAttribute.Diffuse,
				assets.get(DEFAULT_TEX_PATH, Texture.class));
		for (int i = 0; i < OCCLUDEE_PATHS_DYNAMIC.length; i++) {
			String modelPath = OCCLUDEE_PATHS_DYNAMIC[i];
			OCCLUDEE_PATHS_STATIC[i] = "static" + modelPath;
			assets.finishLoadingAsset(modelPath);
			Model model = assets.get(modelPath, Model.class);
			if (!model.materials.first().has(TextureAttribute.Diffuse)) model.materials.first().set(defaultTexture);
			Vector3 dim = model.calculateBoundingBox(bb).getDimensions(tmpV1);
			float scaleFactor = OCCLUDEE_MAX_EXTENT / Math.max(dim.x, Math.max(dim.y, dim.z));
			for (Node node : model.nodes)
				node.scale.scl(scaleFactor);
			btCollisionShape shape = new btBoxShape(dim.scl(scaleFactor * 0.5f));
			world.addConstructor(modelPath, new BulletConstructor(model, 1, shape));
			world.addConstructor(OCCLUDEE_PATHS_STATIC[i], new BulletConstructor(model, 0, shape));
		}
		// Add occludees
		for (int i = 0; i < STARTING_OCCLUDEE_AMOUNT; i++)
			addRandomOccludee(false);
	}

	@Override
	public BulletWorld createWorld () {
		btDefaultCollisionConfiguration collisionConfig = new btDefaultCollisionConfiguration();
		btCollisionDispatcher dispatcher = new btCollisionDispatcher(collisionConfig);
		btSequentialImpulseConstraintSolver solver = new btSequentialImpulseConstraintSolver();
		broadphase = new btDbvtBroadphase();
		btDiscreteDynamicsWorld collisionWorld = new btDiscreteDynamicsWorld(dispatcher, broadphase, solver, collisionConfig);
		return new BulletWorld(collisionConfig, dispatcher, broadphase, solver, collisionWorld);
	}

	@Override
	public void dispose () {
		Gdx.input.setOnscreenKeyboardVisible(false);
		glProfiler.disable();
		visibleEntities.clear();
		rng.setSeed(0);
		state = 0;
		bufferExtentIndex = 0;
		cullingPolicy = CullingPolicy.OCCLUSION;
		super.dispose();
	}

	/** Checks if entity is inside camera frustum.
	 *
	 * @param entity An entity
	 * @return True if entity is inside camera frustum */
	private boolean entityInFrustum (BulletEntity entity) {
		entity.modelInstance.transform.getTranslation(tmpV1);
		return frustumCam.frustum.sphereInFrustum(tmpV1.add(entity.boundingBox.getCenter(tmpV2)), entity.boundingBoxRadius);
	}

	/** Get the type name of a random occludee entity.
	 *
	 * @param dynamic If true, the name of a dynamic entity will be returned (mass > 0)
	 * @return Name of a random entity type */
	private String getRandomOccludeeType (boolean dynamic) {
		int i = rng.nextInt(OCCLUDEE_PATHS_STATIC.length);
		return (dynamic) ? OCCLUDEE_PATHS_DYNAMIC[i] : OCCLUDEE_PATHS_STATIC[i];
	}

	@Override
	public boolean keyTyped (char character) {
		oclBuffer.clear();
		switch (character) {
			case '0':
				for (int i = 0; i < KEY_SPAWN_OCCLUDEE_AMOUNT; i++)
					addRandomOccludee(false);
				break;
			case '1':
				cullingPolicy = cullingPolicy.next();
				break;
			case '2':
				state ^= USE_FRUSTUM_CAM;
				camera = ((state & USE_FRUSTUM_CAM) == USE_FRUSTUM_CAM) ? frustumCam : overviewCam;
				break;
			case '3':
				state ^= PAUSE_FRUSTUM_CAM;
				break;
			case '4':
				oclBuffer.dispose();
				bufferExtentIndex = (bufferExtentIndex + 1) % OCL_BUFFER_EXTENTS.length;
				int extent = OCL_BUFFER_EXTENTS[bufferExtentIndex];
				oclBuffer = new OcclusionBuffer(extent, extent);
				break;
			case '5':
				state ^= SHOW_DEBUG_IMAGE;
				break;
			case '6':
				shadows = !shadows;
				// Clear the old shadows
				visibleEntities.clear();
				renderShadows();
				break;
		}
		return true;
	}

	@Override
	public void render () {
		super.render();
		if ((state & SHOW_DEBUG_IMAGE) == SHOW_DEBUG_IMAGE) renderOclDebugImage();
		performance.append(", Culling: ").append(cullingPolicy.name());
		performance.append(", Visible: ").append(visibleEntities.size).append("/").append(world.entities.size);
		performance.append(", Buffer: ").append(OCL_BUFFER_EXTENTS[bufferExtentIndex]).append("px ");
		performance.append(", GL Draw calls: ").append(glProfiler.getDrawCalls());
		glProfiler.reset();
	}

	private void renderOclDebugImage () {
		TextureRegion oclDebugTexture = oclBuffer.drawDebugTexture();
		spriteBatch.begin();
		spriteBatch.draw(oclDebugTexture, 0, 0);
		spriteBatch.end();
		shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
		shapeRenderer.setColor(Color.DARK_GRAY);
		shapeRenderer.rect(0, 0, oclDebugTexture.getRegionWidth(), oclDebugTexture.getRegionHeight());
		shapeRenderer.end();
	}

	private void renderShadows () {
		((DirectionalShadowLight)light).begin(Vector3.Zero, camera.direction);
		shadowBatch.begin(((DirectionalShadowLight)light).getCamera());
		world.render(shadowBatch, null, visibleEntities);
		shadowBatch.end();
		((DirectionalShadowLight)light).end();
	}

	@Override
	protected void renderWorld () {
		visibleEntities.clear();

		if (world.performanceCounter != null) world.performanceCounter.start();
		if (cullingPolicy == CullingPolicy.NONE) {
			visibleEntities.addAll(world.entities);
		} else if (cullingPolicy == CullingPolicy.SIMPLE) {
			for (BulletEntity entity : world.entities)
				if (entityInFrustum(entity)) visibleEntities.add(entity);
		} else if (cullingPolicy == CullingPolicy.OCCLUSION) {
			oclBuffer.clear();
			occlusionCuller.performOcclusionCulling(broadphase, oclBuffer, frustumCam);
		} else if (cullingPolicy == CullingPolicy.KDOP) {
			occlusionCuller.performKDOPCulling(broadphase, frustumCam);
		}
		if (world.performanceCounter != null) world.performanceCounter.stop();

		if (shadows) renderShadows();
		modelBatch.begin(camera);
		world.render(modelBatch, environment, visibleEntities);
		if ((state & USE_FRUSTUM_CAM) != USE_FRUSTUM_CAM) modelBatch.render(frustumInstance);
		modelBatch.end();
	}

	@Override
	public boolean tap (float x, float y, int count, int button) {
		BulletEntity entity = shoot(getRandomOccludeeType(true), x, y, 30f);
		entity.setColor(Color.WHITE);
		return true;
	}

	@Override
	public void update () {
		super.update();
		// Transform the frustum camera
		if ((state & PAUSE_FRUSTUM_CAM) == PAUSE_FRUSTUM_CAM) return;
		final float dt = Gdx.graphics.getDeltaTime();
		frustumInstance.transform.idt().rotate(Vector3.Y, frustumCamAngleY = (frustumCamAngleY + dt * FRUSTUM_ANG_SPEED) % 360);
		frustumCam.direction.set(0, 0, -1);
		frustumCam.up.set(Vector3.Y);
		frustumCam.position.set(Vector3.Zero);
		frustumCam.rotate(frustumInstance.transform);
		float frustumCamPosY = frustumCamPos.y;
		frustumCamPos.add(tmpV1.set(Vector3.Y).crs(tmpV2.set(frustumCamPos).nor()).scl(dt * FRUSTUM_LIN_SPEED)).nor()
				.scl(FRUSTUM_MOVE_RADIUS);
		frustumCamPos.y = frustumCamPosY;
		frustumCam.position.set(frustumCamPos);
		frustumInstance.transform.setTranslation(frustumCamPos);
		frustumCam.update();
	}

}

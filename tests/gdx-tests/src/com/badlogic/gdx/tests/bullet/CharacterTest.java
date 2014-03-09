package com.badlogic.gdx.tests.bullet;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btAxisSweep3;
import com.badlogic.gdx.physics.bullet.collision.btBroadphaseProxy;
import com.badlogic.gdx.physics.bullet.collision.btCapsuleShape;
import com.badlogic.gdx.physics.bullet.collision.btCollisionDispatcher;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btConvexShape;
import com.badlogic.gdx.physics.bullet.collision.btDbvtBroadphase;
import com.badlogic.gdx.physics.bullet.collision.btDefaultCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.collision.btGhostPairCallback;
import com.badlogic.gdx.physics.bullet.collision.btPairCachingGhostObject;
import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btKinematicCharacterController;
import com.badlogic.gdx.physics.bullet.dynamics.btSequentialImpulseConstraintSolver;

public class CharacterTest extends BaseBulletTest {
	final int BOXCOUNT_X = 5;
	final int BOXCOUNT_Y = 5;
	final int BOXCOUNT_Z = 1;

	final float BOXOFFSET_X = -2.5f;
	final float BOXOFFSET_Y = 0.5f;
	final float BOXOFFSET_Z = 0f;
	
	BulletEntity ground;
	BulletEntity character;
	
	btGhostPairCallback ghostPairCallback;
	btPairCachingGhostObject ghostObject;
	btConvexShape ghostShape;
	btKinematicCharacterController characterController;
	Matrix4 characterTransform;
	Vector3 characterDirection = new Vector3();
	Vector3 walkDirection = new Vector3();
	
	@Override
	public BulletWorld createWorld () {
		// We create the world using an axis sweep broadphase for this test
		btDefaultCollisionConfiguration collisionConfiguration = new btDefaultCollisionConfiguration();
		btCollisionDispatcher dispatcher = new btCollisionDispatcher(collisionConfiguration);
		btAxisSweep3 sweep = new btAxisSweep3(new Vector3(-1000, -1000, -1000), new Vector3(1000, 1000, 1000));
		btSequentialImpulseConstraintSolver solver = new btSequentialImpulseConstraintSolver();
		btDiscreteDynamicsWorld collisionWorld = new btDiscreteDynamicsWorld(dispatcher, sweep, solver, collisionConfiguration);
		ghostPairCallback = new btGhostPairCallback();
		sweep.getOverlappingPairCache().setInternalGhostPairCallback(ghostPairCallback);
		return new BulletWorld(collisionConfiguration, dispatcher, sweep, solver, collisionWorld);
	}
	
	@Override
	public void create () {
		super.create();
		instructions = "Tap to shoot\nArrow keys to move\nR to reset\nLong press to toggle debug mode\nSwipe for next test";
		
		// Create a visual representation of the character (note that we don't use the physics part of BulletEntity, we'll do that manually)
		final Texture texture = new Texture(Gdx.files.internal("data/badlogic.jpg"));
		disposables.add(texture);
		final Material material = new Material(TextureAttribute.createDiffuse(texture), ColorAttribute.createSpecular(1,1,1,1), FloatAttribute.createShininess(8f));
		final long attributes = Usage.Position | Usage.Normal | Usage.TextureCoordinates;
		final Model capsule = modelBuilder.createCapsule(2f, 6f, 16, material, attributes);
		disposables.add(capsule);
		world.addConstructor("capsule", new BulletConstructor(capsule, null));
		character = world.add("capsule", 5f, 3f, 5f);
		characterTransform = character.transform; // Set by reference
		
		// Create the physics representation of the character
		ghostObject = new btPairCachingGhostObject();
		ghostObject.setWorldTransform(characterTransform);
		ghostShape = new btCapsuleShape(2f, 2f);
		ghostObject.setCollisionShape(ghostShape);
		ghostObject.setCollisionFlags(btCollisionObject.CollisionFlags.CF_CHARACTER_OBJECT);
		characterController = new btKinematicCharacterController(ghostObject, ghostShape, .35f);
		
		// And add it to the physics world
		world.collisionWorld.addCollisionObject(ghostObject, 
			(short)btBroadphaseProxy.CollisionFilterGroups.CharacterFilter,
			(short)(btBroadphaseProxy.CollisionFilterGroups.StaticFilter | btBroadphaseProxy.CollisionFilterGroups.DefaultFilter));
		((btDiscreteDynamicsWorld)(world.collisionWorld)).addAction(characterController);
		
		// Add the ground
		(ground = world.add("ground", 0f, 0f, 0f))
			.setColor(0.25f + 0.5f * (float)Math.random(), 0.25f + 0.5f * (float)Math.random(), 0.25f + 0.5f * (float)Math.random(), 1f);
		// Create some boxes to play with
		for (int x = 0; x < BOXCOUNT_X; x++) {
			for (int y = 0; y < BOXCOUNT_Y; y++) {
				for (int z = 0; z < BOXCOUNT_Z; z++) {
					world.add("box", BOXOFFSET_X + x, BOXOFFSET_Y + y, BOXOFFSET_Z + z)
						.setColor(0.5f + 0.5f * (float)Math.random(), 0.5f + 0.5f * (float)Math.random(), 0.5f + 0.5f * (float)Math.random(), 1f);
				}
			}
		}
	}
	
	@Override
	public void update () {
		// If the left or right key is pressed, rotate the character and update its physics update accordingly.
		if (Gdx.input.isKeyPressed(Keys.LEFT)) {
			characterTransform.rotate(0, 1, 0, 5f);
			ghostObject.setWorldTransform(characterTransform);
		}
		if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
			characterTransform.rotate(0, 1, 0, -5f);
			ghostObject.setWorldTransform(characterTransform);
		}
		// Fetch which direction the character is facing now
		characterDirection.set(-1,0,0).rot(characterTransform).nor();
		// Set the walking direction accordingly (either forward or backward)
		walkDirection.set(0,0,0);
		if (Gdx.input.isKeyPressed(Keys.UP))
			walkDirection.add(characterDirection);
		if (Gdx.input.isKeyPressed(Keys.DOWN))
			walkDirection.add(-characterDirection.x, -characterDirection.y, -characterDirection.z);
		walkDirection.scl(4f * Gdx.graphics.getDeltaTime());
		// And update the character controller
		characterController.setWalkDirection(walkDirection);
		// Now we can update the world as normally
		super.update();
		// And fetch the new transformation of the character (this will make the model be rendered correctly)
		ghostObject.getWorldTransform(characterTransform);
	}
	
	@Override
	protected void renderWorld () {
		// TODO Auto-generated method stub
		super.renderWorld();
	}
	
	@Override
	public boolean tap (float x, float y, int count, int button) {
		shoot(x, y);
		return true;
	}
	
	@Override
	public void dispose () {
		((btDiscreteDynamicsWorld)(world.collisionWorld)).removeAction(characterController);
		world.collisionWorld.removeCollisionObject(ghostObject);
		super.dispose();
		characterController.dispose();
		ghostObject.dispose();
		ghostShape.dispose();
		ghostPairCallback.dispose();
		ground = null;
	}
}


package com.badlogic.gdx.tests.bullet;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.bullet.collision.CustomCollisionDispatcher;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.collision.btCapsuleShape;
import com.badlogic.gdx.physics.bullet.collision.btCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.collision.btCollisionDispatcher;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btConeShape;
import com.badlogic.gdx.physics.bullet.collision.btCylinderShape;
import com.badlogic.gdx.physics.bullet.collision.btDbvtBroadphase;
import com.badlogic.gdx.physics.bullet.collision.btDefaultCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.collision.btSphereShape;
import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btSequentialImpulseConstraintSolver;

public class CollisionDispatcherTest extends BaseBulletTest {
	public static class MyCollisionDispatcher extends CustomCollisionDispatcher {
		public MyCollisionDispatcher (btCollisionConfiguration collisionConfiguration) {
			super(collisionConfiguration);
		}

		@Override
		public boolean needsCollision (btCollisionObject body0, btCollisionObject body1) {
			if (body0.getUserValue() % 2 == 0 || body1.getUserValue() % 2 == 0) return super.needsCollision(body0, body1);
			return false;
		}

		@Override
		public boolean needsResponse (btCollisionObject body0, btCollisionObject body1) {
			if (body0.getUserValue() % 2 == 0 || body1.getUserValue() % 2 == 0) return super.needsCollision(body0, body1);
			return false;
		}
	}

	@Override
	public BulletWorld createWorld () {
		btDefaultCollisionConfiguration collisionConfiguration = new btDefaultCollisionConfiguration();
		MyCollisionDispatcher dispatcher = new MyCollisionDispatcher(collisionConfiguration);
		btDbvtBroadphase broadphase = new btDbvtBroadphase();
		btSequentialImpulseConstraintSolver solver = new btSequentialImpulseConstraintSolver();
		btDiscreteDynamicsWorld collisionWorld = new btDiscreteDynamicsWorld(dispatcher, broadphase, solver, collisionConfiguration);
		return new BulletWorld(collisionConfiguration, dispatcher, broadphase, solver, collisionWorld);
	}

	@Override
	public void create () {
		super.create();

		// Create the entities
		world.add("ground", 0f, 0f, 0f).setColor(0.25f + 0.5f * (float)Math.random(), 0.25f + 0.5f * (float)Math.random(),
			0.25f + 0.5f * (float)Math.random(), 1f);
		for (float x = -5f; x <= 5f; x += 2f) {
			for (float y = 5f; y <= 15f; y += 2f) {
				world.add("box", x + 0.1f * MathUtils.random(), y + 0.1f * MathUtils.random(), 0.1f * MathUtils.random()).body
					.setUserValue((int)((x + 5f) / 2f + .5f));
			}
		}
	}

	@Override
	public boolean tap (float x, float y, int count, int button) {
		shoot(x, y);
		return true;
	}
}

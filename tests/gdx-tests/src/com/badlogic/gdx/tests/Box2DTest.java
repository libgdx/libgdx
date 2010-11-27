/*
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com), Nathan Sweet (admin@esotericsoftware.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.badlogic.gdx.tests;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.BitmapFont;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.ImmediateModeRenderer;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.WorldManifold;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;
import com.badlogic.gdx.tests.utils.GdxTest;

public class Box2DTest extends GdxTest implements InputProcessor {
	/** the camera **/
	private OrthographicCamera camera;

	/** the immediate mode renderer to output our debug drawings **/
	private ImmediateModeRenderer renderer;

	/** a spritebatch and a font for text rendering **/
	private SpriteBatch batch;
	private BitmapFont font;

	/** our box2D world **/
	private World world;

	/** our boxes **/
	private ArrayList<Body> boxes = new ArrayList<Body>();

	/** our ground box **/
	Body groundBody;

	/** our mouse joint **/
	private MouseJoint mouseJoint = null;

	/** a hit body **/
	Body hitBody = null;

	@Override public void create () {
		// setup the camera. In Box2D we operate on a
		// meter scale, pixels won't do it. So we use
		// an orthographic camera with a viewport of
		// 48 meters in width and 32 meters in height.
		// We also position the camera so that it
		// looks at (0,16) (that's where the middle of the
		// screen will be located).
		camera = new OrthographicCamera();
		camera.setViewport(48, 32);
		camera.getPosition().set(0, 16, 0);

		// next we setup the immediate mode renderer
		renderer = new ImmediateModeRenderer();

		// next we create a SpriteBatch and a font
		batch = new SpriteBatch();
		font = new BitmapFont();
		font.setColor(Color.RED);

		// next we create out physics world.
		createPhysicsWorld();
		
		// register ourselfs as an InputProcessor
		Gdx.input.setInputProcessor(this);
	}

	private void createPhysicsWorld () {
		// we instantiate a new World with a proper gravity vector
		// and tell it to sleep when possible.
		world = new World(new Vector2(0, -10), true);

		// next we create a static ground platform. This platform
		// is not moveable and will not react to any influences from
		// outside. It will however influence other bodies. First we
		// create a PolygonShape that holds the form of the platform.
		// it will be 100 meters wide and 2 meters high, centered
		// around the origin
		PolygonShape groundPoly = new PolygonShape();
		groundPoly.setAsBox(50, 1);

		// next we create the body for the ground platform. It's
		// simply a static body.
		BodyDef groundBodyDef = new BodyDef();
		groundBodyDef.type = BodyType.StaticBody;
		groundBody = world.createBody(groundBodyDef);

		// finally we add a fixture to the body using the polygon
		// defined above. Note that we have to dispose PolygonShapes
		// and CircleShapes once they are no longer used. This is the
		// only time you have to care explicitely for memomry managment.
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = groundPoly;
		fixtureDef.filter.groupIndex = 0;
		groundBody.createFixture(fixtureDef);
		groundPoly.dispose();

		createBoxes();
	}

	private void createBoxes () {
		// next we create 50 boxes at random locations above the ground
		// body. First we create a nice polygon representing a box 2 meters
		// wide and high.
		PolygonShape boxPoly = new PolygonShape();
		boxPoly.setAsBox(1, 1);

		// next we create the 50 box bodies using the PolygonShape we just
		// defined. This process is similar to the one we used for the ground
		// body. Note that we reuse the polygon for each body fixture.
		for (int i = 0; i < 20; i++) {
			// Create the BodyDef, set a random position above the
			// ground and create a new body
			BodyDef boxBodyDef = new BodyDef();
			boxBodyDef.type = BodyType.DynamicBody;
			boxBodyDef.position.x = -24 + (float)(Math.random() * 48);
			boxBodyDef.position.y = 10 + (float)(Math.random() * 100);
			Body boxBody = world.createBody(boxBodyDef);

			// add the boxPoly shape as a fixture
			FixtureDef fixtureDef = new FixtureDef();
			fixtureDef.shape = boxPoly;
			boxBody.createFixture(fixtureDef);

			// add the box to our list of boxes
			boxes.add(boxBody);
		}

		// we are done, all that's left is disposing the boxPoly
		boxPoly.dispose();
	}

	@Override public void render () {
		// first we update the world. For simplicity
		// we use the delta time provided by the Graphics
		// instance. Normally you'll want to fix the time
		// step.
		long start = System.nanoTime();
		world.step(Gdx.graphics.getDeltaTime(), 3, 3);
		float updateTime = (System.nanoTime() - start) / 1000000000.0f;

		// next we clear the color buffer and set the camera
		// matrices
		GL10 gl = Gdx.graphics.getGL10();
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		camera.setMatrices();

		// next we render the ground body
		renderBox(gl, groundBody, 50, 1);

		// next we render each box via the ImmediateModeRenderer
		// we instantiated previously
		for (int i = 0; i < boxes.size(); i++) {
			Body box = boxes.get(i);
			renderBox(gl, box, 1, 1);
		}

		// finally we render all contact points
		gl.glPointSize(4);
		renderer.begin(GL10.GL_POINTS);
		for (int i = 0; i < world.getContactCount(); i++) {
			Contact contact = world.getContactList().get(i);
			// we only render the contact if it actually touches
			if (contact.isTouching()) {
				// get the world manifold from which we get the
				// contact points. A manifold can have 0, 1 or 2
				// contact points.
				WorldManifold manifold = contact.GetWorldManifold();
				int numContactPoints = manifold.getNumberOfContactPoints();
				for (int j = 0; j < numContactPoints; j++) {
					Vector2 point = manifold.getPoints()[j];
					renderer.color(0, 1, 0, 1);
					renderer.vertex(point.x, point.y, 0);
				}
			}
		}
		renderer.end();
		gl.glPointSize(1);

		// finally we render the time it took to update the world
		batch.begin();
		font.draw( batch, "fps: " + Gdx.graphics.getFramesPerSecond() + " update time: " + updateTime, 0,
				20);		
		batch.end();	
	}

	private void renderBox (GL10 gl, Body body, float halfWidth, float halfHeight) {
		// push the current matrix and
		// get the bodies center and angle in world coordinates
		gl.glPushMatrix();
		Vector2 pos = body.getWorldCenter();
		float angle = body.getAngle();

		// set the translation and rotation matrix
		gl.glTranslatef(pos.x, pos.y, 0);
		gl.glRotatef((float)Math.toDegrees(angle), 0, 0, 1);

		// render the box
		renderer.begin(GL10.GL_LINE_STRIP);
		renderer.color(1, 1, 1, 1);
		renderer.vertex(-halfWidth, -halfHeight, 0);
		renderer.color(1, 1, 1, 1);
		renderer.vertex(-halfWidth, halfHeight, 0);
		renderer.color(1, 1, 1, 1);
		renderer.vertex(halfWidth, halfHeight, 0);
		renderer.color(1, 1, 1, 1);
		renderer.vertex(halfWidth, -halfHeight, 0);
		renderer.color(1, 1, 1, 1);
		renderer.vertex(-halfWidth, -halfHeight, 0);
		renderer.end();

		// pop the matrix
		gl.glPopMatrix();
	}

	/** we instantiate this vector and the callback here so we don't irritate the GC **/
	Vector2 testPoint = new Vector2();
	QueryCallback callback = new QueryCallback() {
		@Override public boolean reportFixture (Fixture fixture) {
			// if the hit fixture's body is the ground body
			// we ignore it
			if (fixture.getBody() == groundBody) return true;

			// if the hit point is inside the fixture of the body
			// we report it
			if (fixture.testPoint(testPoint)) {
				hitBody = fixture.getBody();
				return false;
			} else
				return true;
		}
	};

	@Override public boolean touchDown (int x, int y, int pointer) {
		// translate the mouse coordinates to world coordinates
		camera.getScreenToWorld(x, y, testPoint);
		// ask the world which bodies are within the given
		// bounding box around the mouse pointer
		hitBody = null;
		world.QueryAABB(callback, testPoint.x - 0.1f, testPoint.y - 0.1f, testPoint.x + 0.1f, testPoint.y + 0.1f);

		// if we hit something we create a new mouse joint
		// and attach it to the hit body.
		if (hitBody != null) {
			MouseJointDef def = new MouseJointDef();
			def.bodyA = groundBody;
			def.bodyB = hitBody;
			def.collideConnected = true;
			def.target.set(testPoint);
			def.maxForce = 1000.0f * hitBody.getMass();

			mouseJoint = (MouseJoint)world.createJoint(def);
			hitBody.setAwake(true);
		} else {
			for (Body box : boxes)
				world.destroyBody(box);
			boxes.clear();
			createBoxes();
		}

		return false;
	}

	/** another temporary vector **/
	Vector2 target = new Vector2();

	@Override public boolean touchDragged (int x, int y, int pointer) {
		// if a mouse joint exists we simply update
		// the target of the joint based on the new
		// mouse coordinates
		if (mouseJoint != null) {
			camera.getScreenToWorld(x, y, target);
			mouseJoint.setTarget(target);
		}
		return false;
	}

	@Override public boolean touchUp (int x, int y, int pointer) {
		// if a mouse joint exists we simply destroy it
		if (mouseJoint != null) {
			world.destroyJoint(mouseJoint);
			mouseJoint = null;
		}
		return false;
	}

	@Override public void dispose () {
		world.dispose();
	}
	
	@Override public boolean needsGL20 () {
		return false;
	}
	
	// ---------------------------------------------------------------
	// STUBS FOR UNIMPLEMENTED INTERFACE METHODS, NOTHING TO SEE HERE
	// MOVE ALONG
	// ---------------------------------------------------------------

	@Override public boolean keyDown (int keycode) {
		return false;
	}

	@Override public boolean keyTyped (char character) {
		return false;
	}

	@Override public boolean keyUp (int keycode) {
		return false;
	}
}

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

package com.badlogic.gdx.tests;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer10;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.ChainShape;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.WorldManifold;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.TimeUtils;

public class Box2DTest extends GdxTest implements InputProcessor {
	/** the camera **/
	private com.badlogic.gdx.graphics.OrthographicCamera camera;

	/** the immediate mode renderer to output our debug drawings **/
	private ImmediateModeRenderer10 renderer;

	/** box2d debug renderer **/
	private Box2DDebugRenderer debugRenderer;

	/** a spritebatch and a font for text rendering and a Texture to draw our boxes **/
	private SpriteBatch batch;
	private BitmapFont font;
	private TextureRegion textureRegion;

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

	@Override
	public void create () {
		// setup the camera. In Box2D we operate on a
		// meter scale, pixels won't do it. So we use
		// an orthographic camera with a viewport of
		// 48 meters in width and 32 meters in height.
		// We also position the camera so that it
		// looks at (0,16) (that's where the middle of the
		// screen will be located).
		camera = new OrthographicCamera(48, 32);
		camera.position.set(0, 16, 0);

		// next we setup the immediate mode renderer
		renderer = new ImmediateModeRenderer10();

		// next we create the box2d debug renderer
		debugRenderer = new Box2DDebugRenderer();

		// next we create a SpriteBatch and a font
		batch = new SpriteBatch();
		font = new BitmapFont();
		font.setColor(Color.RED);
		textureRegion = new TextureRegion(new Texture(Gdx.files.internal("data/badlogicsmall.jpg")));

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

		// We also create a simple ChainShape we put above our
		// ground polygon for extra funkyness.
		ChainShape chainShape = new ChainShape();
		chainShape.createLoop(new Vector2[] {
			new Vector2(-10, 10),
			new Vector2(-10, 5),
			new Vector2(10, 5),
			new Vector2(10, 11),
		});
		BodyDef chainBodyDef = new BodyDef();
		chainBodyDef.type = BodyType.StaticBody;
		Body chainBody = world.createBody(chainBodyDef);
		chainBody.createFixture(chainShape, 0);
		chainShape.dispose();
		
		createBoxes();

		// You can savely ignore the rest of this method :)
		world.setContactListener(new ContactListener() {
			@Override
			public void beginContact (Contact contact) {
// System.out.println("begin contact");
			}

			@Override
			public void endContact (Contact contact) {
// System.out.println("end contact");
			}

			@Override
			public void preSolve (Contact contact, Manifold oldManifold) {
// Manifold.ManifoldType type = oldManifold.getType();
// Vector2 localPoint = oldManifold.getLocalPoint();
// Vector2 localNormal = oldManifold.getLocalNormal();
// int pointCount = oldManifold.getPointCount();
// ManifoldPoint[] points = oldManifold.getPoints();
// System.out.println("pre solve, " + type +
// ", point: " + localPoint +
// ", local normal: " + localNormal +
// ", #points: " + pointCount +
// ", [" + points[0] + ", " + points[1] + "]");
			}

			@Override
			public void postSolve (Contact contact, ContactImpulse impulse) {
// float[] ni = impulse.getNormalImpulses();
// float[] ti = impulse.getTangentImpulses();
// System.out.println("post solve, normal impulses: " + ni[0] + ", " + ni[1] + ", tangent impulses: " + ti[0] + ", " + ti[1]);
			}
		});
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

			boxBody.createFixture(boxPoly, 1);

			// add the box to our list of boxes
			boxes.add(boxBody);
		}

		// we are done, all that's left is disposing the boxPoly
		boxPoly.dispose();
	}

	@Override
	public void render () {
		// first we update the world. For simplicity
		// we use the delta time provided by the Graphics
		// instance. Normally you'll want to fix the time
		// step.
		long start = TimeUtils.nanoTime();
		world.step(Gdx.graphics.getDeltaTime(), 8, 3);
		float updateTime = (TimeUtils.nanoTime() - start) / 1000000000.0f;

		// next we clear the color buffer and set the camera
		// matrices
		GL10 gl = Gdx.graphics.getGL10();
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		camera.update();
		camera.apply(gl);

		// next we render the ground body
		renderBox(gl, groundBody, 50, 1);

		// next we render each box via the SpriteBatch.
		// for this we have to set the projection matrix of the
		// spritebatch to the camera's combined matrix. This will
		// make the spritebatch work in world coordinates
		batch.getProjectionMatrix().set(camera.combined);
		batch.begin();
		for (int i = 0; i < boxes.size(); i++) {
			Body box = boxes.get(i);
			Vector2 position = box.getPosition(); // that's the box's center position
			float angle = MathUtils.radiansToDegrees * box.getAngle(); // the rotation angle around the center
			batch.draw(textureRegion, position.x - 1, position.y - 1, // the bottom left corner of the box, unrotated
				1f, 1f, // the rotation center relative to the bottom left corner of the box
				2, 2, // the width and height of the box
				1, 1, // the scale on the x- and y-axis
				angle); // the rotation angle
		}
		batch.end();

		// next we use the debug renderer. Note that we
		// simply apply the camera again and then call
		// the renderer. the camera.apply() call is actually
		// not needed as the opengl matrices are already set
		// by the spritebatch which in turn uses the camera matrices :)
		camera.apply(Gdx.gl10);
		debugRenderer.render(world, camera.combined);

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
				WorldManifold manifold = contact.getWorldManifold();
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
		// for this we have to set the projection matrix again, so
		// we work in pixel coordinates
		batch.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		batch.begin();
		font.draw(batch, "fps: " + Gdx.graphics.getFramesPerSecond() + " update time: " + updateTime, 0, 20);
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
	Vector3 testPoint = new Vector3();
	QueryCallback callback = new QueryCallback() {
		@Override
		public boolean reportFixture (Fixture fixture) {
			// if the hit fixture's body is the ground body
			// we ignore it
			if (fixture.getBody() == groundBody) return true;

			// if the hit point is inside the fixture of the body
			// we report it
			if (fixture.testPoint(testPoint.x, testPoint.y)) {
				hitBody = fixture.getBody();
				return false;
			} else
				return true;
		}
	};

	@Override
	public boolean touchDown (int x, int y, int pointer, int newParam) {
		// translate the mouse coordinates to world coordinates
		testPoint.set(x, y, 0);
		camera.unproject(testPoint);

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
			def.target.set(testPoint.x, testPoint.y);
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

	@Override
	public boolean touchDragged (int x, int y, int pointer) {
		// if a mouse joint exists we simply update
		// the target of the joint based on the new
		// mouse coordinates
		if (mouseJoint != null) {
			camera.unproject(testPoint.set(x, y, 0));
			mouseJoint.setTarget(target.set(testPoint.x, testPoint.y));
		}
		return false;
	}

	@Override
	public boolean touchUp (int x, int y, int pointer, int button) {
		// if a mouse joint exists we simply destroy it
		if (mouseJoint != null) {
			world.destroyJoint(mouseJoint);
			mouseJoint = null;
		}
		return false;
	}

	@Override
	public void dispose () {
		world.dispose();
	}

	@Override
	public boolean needsGL20 () {
		return false;
	}

	// ---------------------------------------------------------------
	// STUBS FOR UNIMPLEMENTED INTERFACE METHODS, NOTHING TO SEE HERE
	// MOVE ALONG
	// ---------------------------------------------------------------

	@Override
	public boolean keyDown (int keycode) {
		return false;
	}

	@Override
	public boolean keyTyped (char character) {
		return false;
	}

	@Override
	public boolean keyUp (int keycode) {
		return false;
	}

	@Override
	public boolean touchMoved (int x, int y) {
		return false;
	}

	@Override
	public boolean scrolled (int amount) {
		return false;
	}
}

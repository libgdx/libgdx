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
package com.badlogic.gdx.physics.box2d;

import java.util.Iterator;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.JointDef.JointType;
import com.badlogic.gdx.physics.box2d.Shape.Type;
import com.badlogic.gdx.physics.box2d.joints.PulleyJoint;

public class Box2DDebugRenderer {

	/** the immediate mode renderer to output our debug drawings **/
	protected ImmediateModeRenderer renderer;

	/** a spritebatch and a font for text rendering **/
	public SpriteBatch batch;
// public Font font;

	/** vertices for polygon rendering **/
	private static Vector2[] vertices = new Vector2[100];

	public Box2DDebugRenderer () {
		// next we setup the immediate mode renderer
		renderer = new ImmediateModeRenderer();

		// next we create a SpriteBatch and a font
		batch = new SpriteBatch();
// font = Gdx.graphics.newFont("Arial", 12, FontStyle.Plain);

		// initialize vertices array
		for (int i = 0; i < vertices.length; i++)
			vertices[i] = new Vector2();
	}

	/**
	 * This assumes that the projection matrix has already been set.
	 */
	public void render (World world) {
		renderBodies(world);
	}

	private final Color SHAPE_NOT_ACTIVE = new Color(0.5f, 0.5f, 0.3f, 1);
	private final Color SHAPE_STATIC = new Color(0.5f, 0.9f, 0.5f, 1);
	private final Color SHAPE_KINEMATIC = new Color(0.5f, 0.5f, 0.9f, 1);
	private final Color SHAPE_NOT_AWAKE = new Color(0.6f, 0.6f, 0.6f, 1);
	private final Color SHAPE_AWAKE = new Color(0.9f, 0.7f, 0.7f, 1);
	private final Color JOINT_COLOR = new Color(0.5f, 0.8f, 0.8f, 1);

	private void renderBodies (World world) {
		for (Iterator<Body> iter = world.getBodies(); iter.hasNext();) {
			Body body = iter.next();
			Transform transform = body.getTransform();
			int len = body.getFixtureList().size();
			List<Fixture> fixtures = body.getFixtureList();
			for (int i = 0; i < len; i++) {
				Fixture fixture = fixtures.get(i);
				if (body.isActive() == false)
					drawShape(fixture, transform, SHAPE_NOT_ACTIVE);
				else if (body.getType() == BodyType.StaticBody)
					drawShape(fixture, transform, SHAPE_STATIC);
				else if (body.getType() == BodyType.KinematicBody)
					drawShape(fixture, transform, SHAPE_KINEMATIC);
				else if (body.isAwake() == false)
					drawShape(fixture, transform, SHAPE_NOT_AWAKE);
				else
					drawShape(fixture, transform, SHAPE_AWAKE);
			}
		}

		for (Iterator<Joint> iter = world.getJoints(); iter.hasNext();) {
			Joint joint = iter.next();
			drawJoint(joint);
		}

		int len = world.getContactList().size();
		for (int i = 0; i < len; i++)
			drawContact(world.getContactList().get(i));
	}

	private static Vector2 t = new Vector2();
	private static Vector2 axis = new Vector2();

	private void drawShape (Fixture fixture, Transform transform, Color color) {
		if (fixture.getType() == Type.Circle) {
			CircleShape circle = (CircleShape)fixture.getShape();
			t.set(circle.getPosition());
			transform.mul(t);
			drawSolidCircle(t, circle.getRadius(), axis.set(transform.vals[Transform.COL1_X], transform.vals[Transform.COL1_Y]),
				color);
		} else {
			PolygonShape poly = (PolygonShape)fixture.getShape();
			int vertexCount = poly.getVertexCount();
			for (int i = 0; i < vertexCount; i++) {
				poly.getVertex(i, vertices[i]);
				transform.mul(vertices[i]);
			}
			drawSolidPolygon(vertices, vertexCount, color);
		}
	}

	private final Vector2 v = new Vector2();

	private void drawSolidCircle (Vector2 center, float radius, Vector2 axis, Color color) {
		renderer.begin(GL10.GL_LINE_LOOP);
		float angle = 0;
		float angleInc = 2 * (float)Math.PI / 20;
		for (int i = 0; i < 20; i++, angle += angleInc) {
			v.set((float)Math.cos(angle) * radius + center.x, (float)Math.sin(angle) * radius + center.y);
			renderer.color(color.r, color.g, color.b, color.a);
			renderer.vertex(v.x, v.y, 0);
		}
		renderer.end();

		renderer.begin(GL10.GL_LINES);
		renderer.color(color.r, color.g, color.b, color.a);
		renderer.vertex(center.x, center.y, 0);
		renderer.color(color.r, color.g, color.b, color.a);
		renderer.vertex(center.x + axis.x * radius, center.y + axis.y * radius, 0);
		renderer.end();
	}

	private void drawSolidPolygon (Vector2[] vertices, int vertexCount, Color color) {
		renderer.begin(GL10.GL_LINE_LOOP);
		for (int i = 0; i < vertexCount; i++) {
			Vector2 v = vertices[i];
			renderer.color(color.r, color.g, color.b, color.a);
			renderer.vertex(v.x, v.y, 0);
		}
		renderer.end();
	}

	private void drawJoint (Joint joint) {
		Body bodyA = joint.getBodyA();
		Body bodyB = joint.getBodyB();
		Transform xf1 = bodyA.getTransform();
		Transform xf2 = bodyB.getTransform();

		Vector2 x1 = xf1.getPosition();
		Vector2 x2 = xf2.getPosition();
		Vector2 p1 = joint.getAnchorA();
		Vector2 p2 = joint.getAnchorB();

		if (joint.getType() == JointType.DistanceJoint) {
			drawSegment(p1, p2, JOINT_COLOR);
		} else if (joint.getType() == JointType.PulleyJoint) {
			PulleyJoint pulley = (PulleyJoint)joint;
			Vector2 s1 = pulley.getGroundAnchorA();
			Vector2 s2 = pulley.getGroundAnchorB();
			drawSegment(s1, p1, JOINT_COLOR);
			drawSegment(s2, p2, JOINT_COLOR);
			drawSegment(s1, s2, JOINT_COLOR);
		} else if (joint.getType() == JointType.MouseJoint) {
		} else {
			drawSegment(x1, p1, JOINT_COLOR);
			drawSegment(p1, p2, JOINT_COLOR);
			drawSegment(x2, p2, JOINT_COLOR);
		}
	}

	private void drawSegment (Vector2 x1, Vector2 x2, Color color) {
		renderer.begin(GL10.GL_LINES);
		renderer.color(color.r, color.g, color.b, color.a);
		renderer.vertex(x1.x, x1.y, 0);
		renderer.color(color.r, color.g, color.b, color.a);
		renderer.vertex(x2.x, x2.y, 0);
		renderer.end();
	}

	private void drawContact (Contact contact) {

	}

	public void dispose () {
		batch.dispose();
// font.dispose();
	}
}

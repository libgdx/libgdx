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

package com.badlogic.gdx.tests.box2d;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

public class ConveyorBelt extends Box2DTest implements ContactListener {
	Fixture m_platform;

	@Override
	protected void createWorld (World world) {
		world.setContactListener(this);

		// Ground
		{
			BodyDef bodyDef = new BodyDef();
			groundBody = world.createBody(bodyDef);

			EdgeShape shape = new EdgeShape();
			shape.set(new Vector2(-20.0f, 0.0f), new Vector2(20.0f, 0.0f));
			groundBody.createFixture(shape, 0.0f);
		}

		// Platform
		{
			BodyDef bd = new BodyDef();
			bd.position.set(-5.0f, 5.0f);
			Body body = world.createBody(bd);

			PolygonShape shape = new PolygonShape();
			shape.setAsBox(10.0f, 0.5f);

			FixtureDef fd = new FixtureDef();
			fd.shape = shape;
			fd.friction = 0.8f;
			m_platform = body.createFixture(fd);
		}

		// Boxes
		for (int i = 0; i < 5; ++i) {
			BodyDef bd = new BodyDef();
			bd.type = BodyType.DynamicBody;
			bd.position.set(-10.0f + 2.0f * i, 7.0f);
			Body body = world.createBody(bd);

			PolygonShape shape = new PolygonShape();
			shape.setAsBox(0.5f, 0.5f);
			body.createFixture(shape, 20.0f);
		}
	}

	public void preSolve (Contact contact, Manifold oldManifold) {
		Fixture fixtureA = contact.getFixtureA();
		Fixture fixtureB = contact.getFixtureB();

		if (fixtureA == m_platform || fixtureB == m_platform) {
			contact.setTangentSpeed(5.0f);
		}
	}

	@Override
	public void beginContact (Contact contact) {

	}

	@Override
	public void endContact (Contact contact) {
	}

	@Override
	public void postSolve (Contact contact, ContactImpulse impulse) {
	}
}

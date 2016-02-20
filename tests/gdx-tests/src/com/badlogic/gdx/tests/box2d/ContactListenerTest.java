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

package com.badlogic.gdx.tests.box2d;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

public class ContactListenerTest extends Box2DTest implements ContactListener {

	@Override
	protected void createWorld (World world) {
		world.setContactListener(this);
		{
			BodyDef bd = new BodyDef();
			Body ground = world.createBody(bd);

			EdgeShape shape = new EdgeShape();
			shape.set(new Vector2(-20, 0), new Vector2(20, 0));
			ground.createFixture(shape, 0);
			shape.dispose();
		}

		{
			BodyDef bd = new BodyDef();
			Body ground = world.createBody(bd);

			EdgeShape shape = new EdgeShape();
			shape.setRadius(0);
			shape.set(new Vector2(-8, 1), new Vector2(-6, 1));
			ground.createFixture(shape, 0);
			shape.set(new Vector2(-6, 1), new Vector2(-4, 1));
			ground.createFixture(shape, 0);
			shape.set(new Vector2(-4, 1), new Vector2(-2, 1));
			ground.createFixture(shape, 0);
			shape.dispose();
		}

		{
			BodyDef bd = new BodyDef();
			Body ground = world.createBody(bd);

			PolygonShape shape = new PolygonShape();
			shape.setAsBox(1, 1, new Vector2(4, 3), 0);
			ground.createFixture(shape, 0);
			shape.setAsBox(1, 1, new Vector2(6, 3), 0);
			ground.createFixture(shape, 0);
			shape.setAsBox(1, 1, new Vector2(8, 3), 0);
			ground.createFixture(shape, 0);
			shape.dispose();
		}

		{
			BodyDef bd = new BodyDef();
			Body ground = world.createBody(bd);

			EdgeShape shape = new EdgeShape();
			float d = 2 * 2 * 0.005f;
			shape.setRadius(0);
			shape.set(new Vector2(-1 + d, 3), new Vector2(1 - d, 3));
			ground.createFixture(shape, 0);
			shape.set(new Vector2(1, 3 + d), new Vector2(1, 5 - d));
			ground.createFixture(shape, 0);
			shape.set(new Vector2(1 - d, 5), new Vector2(-1 + d, 5));
			ground.createFixture(shape, 0);
			shape.set(new Vector2(-1, 5 - d), new Vector2(-1, 3 + d));
			ground.createFixture(shape, 0);
			shape.dispose();
		}

		{
			BodyDef bd = new BodyDef();
			bd.position.set(-3, 20);
			bd.type = BodyType.DynamicBody;
			bd.fixedRotation = true;
			bd.allowSleep = false;

			Body body = world.createBody(bd);

			PolygonShape shape = new PolygonShape();
			shape.setAsBox(0.5f, 0.5f);

			FixtureDef fd = new FixtureDef();
			fd.shape = shape;
			fd.density = 20.0f;
			body.createFixture(fd);
			shape.dispose();
		}

		{
			BodyDef bd = new BodyDef();
			bd.position.set(-5, 25);
			bd.type = BodyType.DynamicBody;
			bd.fixedRotation = true;
			bd.allowSleep = false;

			Body body = world.createBody(bd);

			float angle = 0;
			float delta = (float)Math.PI / 3;
			Vector2[] vertices = new Vector2[6];
			for (int i = 0; i < 6; i++) {
				vertices[i] = new Vector2(0.5f * (float)Math.cos(angle), 0.5f * (float)Math.sin(angle));
				angle += delta;
			}

			PolygonShape shape = new PolygonShape();
			shape.set(vertices);

			FixtureDef fd = new FixtureDef();
			fd.shape = shape;
			fd.density = 20.0f;
			body.createFixture(fd);
			shape.dispose();
		}

		{
			BodyDef bd = new BodyDef();
			bd.position.set(3, 30);
			bd.type = BodyType.DynamicBody;
			bd.fixedRotation = true;
			bd.allowSleep = false;

			Body body = world.createBody(bd);

			CircleShape shape = new CircleShape();
			shape.setRadius(0.5f);

			FixtureDef fd = new FixtureDef();
			fd.shape = shape;
			fd.density = 20.0f;
			body.createFixture(fd);
			shape.dispose();
		}
	}

	@Override
	public void beginContact (Contact contact) {
		System.out.println(String.format("beginContact() addr=%d", getContactAddr(contact)));
		System.out.println(String.format("beginContact() addrA=%d, addrB=%d", 
			getFixtureAddrA(contact), 
			getFixtureAddrB(contact)));
		System.out.println(String.format("beginContact() fixA=%s, fixB=%s", 
			contact.getFixtureA(), 
			contact.getFixtureB()));
		
		final Body toRemove = contact.getFixtureA().getBody().getType() == BodyType.DynamicBody ?
			contact.getFixtureA().getBody() :
			contact.getFixtureB().getBody();
			Gdx.app.postRunnable(new Runnable() {
				@Override
				public void run () {
					world.destroyBody(toRemove);
				}
			});
	}

	@Override
	public void endContact (Contact contact) {
		System.out.println(String.format("  endContact() addr=%d", getContactAddr(contact)));
		System.out.println(String.format("  endContact() addrA=%d, addrB=%d", 
			getFixtureAddrA(contact), 
			getFixtureAddrB(contact)));
		System.out.println(String.format("  endContact() fixA=%s, fixB=%s", 
			contact.getFixtureA(), 
			contact.getFixtureB()));
		
		final Fixture fixtureA = contact.getFixtureA();
		final Fixture fixtureB = contact.getFixtureB();
		if(fixtureA == null || fixtureB == null) {
			throw new RuntimeException("No fixture found.");
		}
	}

	@Override
	public void preSolve (Contact contact, Manifold oldManifold) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void postSolve (Contact contact, ContactImpulse impulse) {
		// TODO Auto-generated method stub
		
	}
	
	public long getFixtureAddrA(Contact contact) {
		try {
			long addr = getContactAddr(contact);
			
			Method getFixtureA = contact.getClass().getDeclaredMethod("jniGetFixtureA", long.class);
			getFixtureA.setAccessible(true);
			return (Long) getFixtureA.invoke(contact, addr);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}
		
	public long getFixtureAddrB(Contact contact) {
		try {
			long addr =getContactAddr(contact);
			
			Method getFixtureB = contact.getClass().getDeclaredMethod("jniGetFixtureB", long.class);
			getFixtureB.setAccessible(true);
			return (Long) getFixtureB.invoke(contact, addr);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	public long getContactAddr(Contact contact) {
		try {
			Field addrField = contact.getClass().getDeclaredField("addr");
			addrField.setAccessible(true);
			return addrField.getLong(contact);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}
}
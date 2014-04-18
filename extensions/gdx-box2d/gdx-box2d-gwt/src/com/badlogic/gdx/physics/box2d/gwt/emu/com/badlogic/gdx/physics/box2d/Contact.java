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

import com.badlogic.gdx.math.Vector2;

/** The class manages contact between two shapes. A contact exists for each overlapping AABB in the broad-phase (except if
 * filtered). Therefore a contact object may exist that has no contact points.
 * @author mzechner */
public class Contact {
	final World world;
	org.jbox2d.dynamics.contacts.Contact contact;
	final WorldManifold worldManifold = new WorldManifold();
	final org.jbox2d.collision.WorldManifold worldManifold2 = new org.jbox2d.collision.WorldManifold();

	Contact (World world) {
		this.world = world;
	}

	protected Contact (World world, org.jbox2d.dynamics.contacts.Contact contact) {
		this.world = world;
		this.contact = contact;
	}

	public WorldManifold getWorldManifold () {
		contact.getWorldManifold(worldManifold2);
		int numContactPoints = contact.getManifold().pointCount;

		worldManifold.numContactPoints = numContactPoints;
		worldManifold.normal.set(worldManifold2.normal.x, worldManifold2.normal.y);

		for (int i = 0; i < worldManifold.points.length; i++) {
			worldManifold.points[i] = new Vector2(worldManifold2.points[i].x, worldManifold2.points[i].y);
		}
		return worldManifold;
	}

	public boolean isTouching () {
		return contact.isTouching();
	}

	/** Enable/disable this contact. This can be used inside the pre-solve contact listener. The contact is only disabled for the
	 * current time step (or sub-step in continuous collisions). */
	public void setEnabled (boolean flag) {
		contact.setEnabled(flag);
	}

	/** Has this contact been disabled? */
	public boolean isEnabled () {
		return contact.isEnabled();
	}

	/** Get the first fixture in this contact. */
	public Fixture getFixtureA () {
		return world.fixtures.get(contact.m_fixtureA);
	}

	/** Get the second fixture in this contact. */
	public Fixture getFixtureB () {
		return world.fixtures.get(contact.m_fixtureB);
	}

	/** Get the child primitive index for fixture A. */
	public int getChildIndexA () {
		return contact.getChildIndexA();
	}

	/** Get the child primitive index for fixture B. */
	public int getChildIndexB () {
		return contact.getChildIndexB();
	}

	/** Override the default friction mixture. You can call this in b2ContactListener::PreSolve. This value persists until set or
	 * reset. */
	public void setFriction (float friction) {
		contact.setFriction(friction);
	}

	/** Get the friction. */
	public float getFriction () {
		return contact.getFriction();
	}

	/** Reset the friction mixture to the default value. */
	public void resetFriction () {
		contact.resetFriction();
	}

	/** Override the default restitution mixture. You can call this in b2ContactListener::PreSolve. The value persists until you set
	 * or reset. */
	public void setRestitution (float restitution) {
		contact.setRestitution(restitution);
	}

	/** Get the restitution. */
	public float getRestitution () {
		return contact.getRestitution();
	}

	/** Reset the restitution to the default value. */
	public void ResetRestitution () {
		contact.resetRestitution();
	}

	public float getTangentSpeed () {
		return contact.getTangentSpeed();
	}

	public void setTangentSpeed (float speed) {
		contact.setTangentSpeed(speed);
	}
}

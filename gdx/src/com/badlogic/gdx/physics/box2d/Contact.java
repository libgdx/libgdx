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
	/** the address **/
	protected long addr;

	/** the world **/
	protected World world;

	/** the world manifold **/
	protected final WorldManifold worldManifold = new WorldManifold();

	protected Contact (World world, long addr) {
		this.addr = addr;
		this.world = world;
	}

	/** Get the world manifold. */
	private final float[] tmp = new float[6];

	public WorldManifold getWorldManifold () {
		int numContactPoints = jniGetWorldManifold(addr, tmp);

		worldManifold.numContactPoints = numContactPoints;
		worldManifold.normal.set(tmp[0], tmp[1]);
		for (int i = 0; i < numContactPoints; i++) {
			Vector2 point = worldManifold.points[i];
			point.x = tmp[2 + i * 2];
			point.y = tmp[2 + i * 2 + 1];
		}

		return worldManifold;
	}

	private native int jniGetWorldManifold (long addr, float[] manifold);

	public boolean isTouching () {
		return jniIsTouching(addr);
	}

	private native boolean jniIsTouching (long addr);

	/** Enable/disable this contact. This can be used inside the pre-solve contact listener. The contact is only disabled for the
	 * current time step (or sub-step in continuous collisions). */
	public void setEnabled (boolean flag) {
		jniSetEnabled(addr, flag);
	}

	private native void jniSetEnabled (long addr, boolean flag);

	/** Has this contact been disabled? */
	public boolean isEnabled () {
		return jniIsEnabled(addr);
	}

	private native boolean jniIsEnabled (long addr);

	/** Get the first fixture in this contact. */
	public Fixture getFixtureA () {
		return world.fixtures.get(jniGetFixtureA(addr));
	}

	private native long jniGetFixtureA (long addr);

	/** Get the second fixture in this contact. */
	public Fixture getFixtureB () {
		return world.fixtures.get(jniGetFixtureB(addr));
	}

	private native long jniGetFixtureB (long addr);
	
	/**
	 *  Get the child primitive index for fixture A.
	 */
	public int getChildIndexA() {
		return jniGetChildIndexA(addr);
	}

	private native int jniGetChildIndexA (long addr);

	/**
	 *  Get the child primitive index for fixture B.
	 */
	public int getChildIndexB() {
		return jniGetChildIndexB(addr);
	}

	private native int jniGetChildIndexB (long addr);

	/**
	 *  Override the default friction mixture. You can call this in b2ContactListener::PreSolve.
	 * This value persists until set or reset.
	 */
	public void setFriction(float friction) {
		jniSetFriction(addr, friction);
	}

	private native void jniSetFriction(long addr, float friction);
	
	/**
	 *  Get the friction.
	 */
	public float getFriction() {
		return jniGetFriction(addr);
	}
	
	private native float jniGetFriction(long addr);

	/**
	 *  Reset the friction mixture to the default value.
	 */
	public void resetFriction() {
		jniResetFriction(addr);
	}
	
	private native void jniResetFriction(long addr);

	/**
	 *  Override the default restitution mixture. You can call this in b2ContactListener::PreSolve.
	 * The value persists until you set or reset. */
	public void setRestitution(float restitution) {
		jniSetRestitution(addr, restitution);
	}
	
	private native void jniSetRestitution(long addr, float restitution);

	/**
	 *  Get the restitution.
	 */
	public float getRestitution() {
		return jniGetRestitution(addr);
	}
	
	private native float jniGetRestitution(long addr);

	/**
	 * Reset the restitution to the default value.
	 */
	public void ResetRestitution() {
		jniResetRestitution(addr);
	}
	
	private native void jniResetRestitution(long addr);
}

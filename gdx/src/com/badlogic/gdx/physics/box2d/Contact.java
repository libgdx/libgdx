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
	// @off
	/*JNI
#include <Box2D/Box2D.h>
	 */
	
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

	private native int jniGetWorldManifold (long addr, float[] tmp); /*
		b2Contact* contact = (b2Contact*)addr;
		b2WorldManifold manifold;
		contact->GetWorldManifold(&manifold);
		int numPoints = contact->GetManifold()->pointCount;
	
		tmp[0] = manifold.normal.x;
		tmp[1] = manifold.normal.y;
	
		for( int i = 0; i < numPoints; i++ )
		{
			tmp[2 + i*2] = manifold.points[i].x;
			tmp[2 + i*2+1] = manifold.points[i].y;
		}
	
		return numPoints;
	*/

	public boolean isTouching () {
		return jniIsTouching(addr);
	}

	private native boolean jniIsTouching (long addr); /*
		b2Contact* contact = (b2Contact*)addr;
		return contact->IsTouching();
	*/

	/** Enable/disable this contact. This can be used inside the pre-solve contact listener. The contact is only disabled for the
	 * current time step (or sub-step in continuous collisions). */
	public void setEnabled (boolean flag) {
		jniSetEnabled(addr, flag);
	}

	private native void jniSetEnabled (long addr, boolean flag); /*
		b2Contact* contact = (b2Contact*)addr;
		contact->SetEnabled(flag);
	*/

	/** Has this contact been disabled? */
	public boolean isEnabled () {
		return jniIsEnabled(addr);
	}

	private native boolean jniIsEnabled (long addr); /*
		b2Contact* contact = (b2Contact*)addr;
		return contact->IsEnabled();
	*/

	/** Get the first fixture in this contact. */
	public Fixture getFixtureA () {
		return world.fixtures.get(jniGetFixtureA(addr));
	}

	private native long jniGetFixtureA (long addr); /*
		b2Contact* contact = (b2Contact*)addr;
		return (jlong)contact->GetFixtureA();
	*/

	/** Get the second fixture in this contact. */
	public Fixture getFixtureB () {
		return world.fixtures.get(jniGetFixtureB(addr));
	}

	private native long jniGetFixtureB (long addr); /*
		b2Contact* contact = (b2Contact*)addr;
		return (jlong)contact->GetFixtureB();
	*/

	/** Get the child primitive index for fixture A. */
	public int getChildIndexA () {
		return jniGetChildIndexA(addr);
	}

	private native int jniGetChildIndexA (long addr); /*
		b2Contact* contact = (b2Contact*)addr;
		return contact->GetChildIndexA();
	*/

	/** Get the child primitive index for fixture B. */
	public int getChildIndexB () {
		return jniGetChildIndexB(addr);
	}

	private native int jniGetChildIndexB (long addr); /*
		b2Contact* contact = (b2Contact*)addr;
		return contact->GetChildIndexB();
	*/

	/** Override the default friction mixture. You can call this in b2ContactListener::PreSolve. This value persists until set or
	 * reset. */
	public void setFriction (float friction) {
		jniSetFriction(addr, friction);
	}

	private native void jniSetFriction (long addr, float friction); /*
		b2Contact* contact = (b2Contact*)addr;
		contact->SetFriction(friction);
	*/

	/** Get the friction. */
	public float getFriction () {
		return jniGetFriction(addr);
	}

	private native float jniGetFriction (long addr); /*
		b2Contact* contact = (b2Contact*)addr;
		return contact->GetFriction();
	*/

	/** Reset the friction mixture to the default value. */
	public void resetFriction () {
		jniResetFriction(addr);
	}

	private native void jniResetFriction (long addr); /*
	  	b2Contact* contact = (b2Contact*)addr;
		contact->ResetFriction();
	*/

	/** Override the default restitution mixture. You can call this in b2ContactListener::PreSolve. The value persists until you set
	 * or reset. */
	public void setRestitution (float restitution) {
		jniSetRestitution(addr, restitution);
	}

	private native void jniSetRestitution (long addr, float restitution); /*
	  	b2Contact* contact = (b2Contact*)addr;
		contact->SetRestitution(restitution);
	*/

	/** Get the restitution. */
	public float getRestitution () {
		return jniGetRestitution(addr);
	}

	private native float jniGetRestitution (long addr); /*
	  	b2Contact* contact = (b2Contact*)addr;
		return contact->GetRestitution();
	*/

	/** Reset the restitution to the default value. */
	public void ResetRestitution () {
		jniResetRestitution(addr);
	}

	private native void jniResetRestitution (long addr); /*
	  	b2Contact* contact = (b2Contact*)addr;
		contact->ResetRestitution();
	*/
}

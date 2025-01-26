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
#include <box2d/box2d.h>
	 */ // @on

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

	private final float[] tmp = new float[8];

	/** Get the world manifold. */
	public WorldManifold getWorldManifold () {
		int numContactPoints = jniGetWorldManifold(addr, tmp);

		worldManifold.numContactPoints = numContactPoints;
		worldManifold.normal.set(tmp[0], tmp[1]);
		for (int i = 0; i < numContactPoints; i++) {
			Vector2 point = worldManifold.points[i];
			point.x = tmp[2 + i * 2];
			point.y = tmp[2 + i * 2 + 1];
		}
		worldManifold.separations[0] = tmp[6];
		worldManifold.separations[1] = tmp[7];

		return worldManifold;
	}

	private native int jniGetWorldManifold (long addr, float[] tmp); /*
		// @off
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

		tmp[6] = manifold.separations[0];
		tmp[7] = manifold.separations[1];

		return numPoints;
	*/ // @on

	public boolean isTouching () {
		return jniIsTouching(addr);
	}

	private native boolean jniIsTouching (long addr); /*
		// @off
		b2Contact* contact = (b2Contact*)addr;
		return contact->IsTouching();
	*/ // @on

	/** Enable/disable this contact. This can be used inside the pre-solve contact listener. The contact is only disabled for the
	 * current time step (or sub-step in continuous collisions). */
	public void setEnabled (boolean flag) {
		jniSetEnabled(addr, flag);
	}

	private native void jniSetEnabled (long addr, boolean flag); /*
		// @off
		b2Contact* contact = (b2Contact*)addr;
		contact->SetEnabled(flag);
	*/ // @on

	/** Has this contact been disabled? */
	public boolean isEnabled () {
		return jniIsEnabled(addr);
	}

	private native boolean jniIsEnabled (long addr); /*
		// @off
		b2Contact* contact = (b2Contact*)addr;
		return contact->IsEnabled();
	*/ // @on

	/** Get the first fixture in this contact. */
	public Fixture getFixtureA () {
		return world.fixtures.get(jniGetFixtureA(addr));
	}

	private native long jniGetFixtureA (long addr); /*
		// @off
		b2Contact* contact = (b2Contact*)addr;
		return (jlong)contact->GetFixtureA();
	*/ // @on

	/** Get the second fixture in this contact. */
	public Fixture getFixtureB () {
		return world.fixtures.get(jniGetFixtureB(addr));
	}

	private native long jniGetFixtureB (long addr); /*
		// @off
		b2Contact* contact = (b2Contact*)addr;
		return (jlong)contact->GetFixtureB();
	*/ // @on

	/** Get the child primitive index for fixture A. */
	public int getChildIndexA () {
		return jniGetChildIndexA(addr);
	}

	private native int jniGetChildIndexA (long addr); /*
		// @off
		b2Contact* contact = (b2Contact*)addr;
		return contact->GetChildIndexA();
	*/ // @on

	/** Get the child primitive index for fixture B. */
	public int getChildIndexB () {
		return jniGetChildIndexB(addr);
	}

	private native int jniGetChildIndexB (long addr); /*
		// @off
		b2Contact* contact = (b2Contact*)addr;
		return contact->GetChildIndexB();
	*/ // @on

	/** Override the default friction mixture. You can call this in b2ContactListener::PreSolve. This value persists until set or
	 * reset. */
	public void setFriction (float friction) {
		jniSetFriction(addr, friction);
	}

	private native void jniSetFriction (long addr, float friction); /*
		// @off
		b2Contact* contact = (b2Contact*)addr;
		contact->SetFriction(friction);
	*/ // @on

	/** Get the friction. */
	public float getFriction () {
		return jniGetFriction(addr);
	}

	private native float jniGetFriction (long addr); /*
		// @off
		b2Contact* contact = (b2Contact*)addr;
		return contact->GetFriction();
	*/ // @on

	/** Reset the friction mixture to the default value. */
	public void resetFriction () {
		jniResetFriction(addr);
	}

	private native void jniResetFriction (long addr); /*
		// @off
	  	b2Contact* contact = (b2Contact*)addr;
		contact->ResetFriction();
	*/ // @on

	/** Override the default restitution mixture. You can call this in b2ContactListener::PreSolve. The value persists until you
	 * set or reset. */
	public void setRestitution (float restitution) {
		jniSetRestitution(addr, restitution);
	}

	private native void jniSetRestitution (long addr, float restitution); /*
		// @off
	  	b2Contact* contact = (b2Contact*)addr;
		contact->SetRestitution(restitution);
	*/ // @on

	/** Get the restitution. */
	public float getRestitution () {
		return jniGetRestitution(addr);
	}

	private native float jniGetRestitution (long addr); /*
		// @off
	  	b2Contact* contact = (b2Contact*)addr;
		return contact->GetRestitution();
	*/ // @on

	/** Reset the restitution to the default value. */
	public void ResetRestitution () {
		jniResetRestitution(addr);
	}

	private native void jniResetRestitution (long addr); /*
		// @off
	  	b2Contact* contact = (b2Contact*)addr;
		contact->ResetRestitution();
	*/ // @on

	/** Override the default restitution mixture. You can call this in b2ContactListener::PreSolve. The value persists until you
	 * set or reset. */
	public void setRestitutionThreshold (float restitutionThreshold) {
		jniSetRestitutionThreshold(addr, restitutionThreshold);
	}

	private native void jniSetRestitutionThreshold (long addr, float restitutionThreshold); /*
		// @off
	  	b2Contact* contact = (b2Contact*)addr;
		contact->SetRestitutionThreshold(restitutionThreshold);
	*/ // @on

	/** Get the restitution threshold. */
	public float getRestitutionThreshold () {
		return jniGetRestitutionThreshold(addr);
	}

	private native float jniGetRestitutionThreshold (long addr); /*
		// @off
	  	b2Contact* contact = (b2Contact*)addr;
		return contact->GetRestitutionThreshold();
	*/ // @on

	/** Reset the restitution threshold to the default value. */
	public void ResetRestitutionThreshold () {
		jniResetRestitutionThreshold(addr);
	}

	private native void jniResetRestitutionThreshold (long addr); /*
		// @off
	  	b2Contact* contact = (b2Contact*)addr;
		contact->ResetRestitutionThreshold();
	*/ // @on

	/** Get the tangent speed. */
	public float getTangentSpeed () {
		return jniGetTangentSpeed(addr);
	}

	private native float jniGetTangentSpeed (long addr); /*
		// @off
	  	b2Contact* contact = (b2Contact*)addr;
		return contact->GetTangentSpeed();
	*/ // @on

	/** Set the tangent speed. */
	public void setTangentSpeed (float speed) {
		jniSetTangentSpeed(addr, speed);
	}

	private native void jniSetTangentSpeed (long addr, float speed); /*
		// @off
	  	b2Contact* contact = (b2Contact*)addr;
		contact->SetTangentSpeed(speed);
	*/ // @on
}

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

import org.jbox2d.common.Settings;
import org.jbox2d.common.Vec2;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;

/** The world class manages all physics entities, dynamic simulation, and asynchronous queries. The world also contains efficient
 * memory management facilities.
 * @author mzechner */
public final class World implements Disposable {
	org.jbox2d.dynamics.World world;
	Vec2 tmp = new Vec2();
	Vector2 tmp2 = new Vector2();
	
	/** Construct a world object.
	 * @param gravity the world gravity vector.
	 * @param doSleep improve performance by not simulating inactive bodies. */
	public World (Vector2 gravity, boolean doSleep) {
		world = new org.jbox2d.dynamics.World(tmp.set(gravity.x, gravity.y));
		world.setAllowSleep(doSleep);
	}

	/** Register a destruction listener. The listener is owned by you and must remain in scope. */
	public void setDestructionListener (DestructionListener listener) {
		// FIXME
	}

	/** Register a contact filter to provide specific control over collision. Otherwise the default filter is used
	 * (b2_defaultFilter). The listener is owned by you and must remain in scope. */
	public void setContactFilter (ContactFilter filter) {
		// FIXME
	}
	

	/** Register a contact event listener. The listener is owned by you and must remain in scope. */
	public void setContactListener (ContactListener listener) {
		// FIXME
	}

	/** Create a rigid body given a definition. No reference to the definition is retained.
	 * @warning This function is locked during callbacks. */
	public Body createBody (BodyDef def) {
		// FIXME
		return null;
	}

	/** Destroy a rigid body given a definition. No reference to the definition is retained. This function is locked during
	 * callbacks.
	 * @warning This automatically deletes all associated shapes and joints.
	 * @warning This function is locked during callbacks. */
	public void destroyBody (Body body) {
		// FIXME
	}

	/** Create a joint to constrain bodies together. No reference to the definition is retained. This may cause the connected bodies
	 * to cease colliding.
	 * @warning This function is locked during callbacks. */
	public Joint createJoint (JointDef def) {
		// FIXME
		return null;
	}

	/** Destroy a joint. This may cause the connected bodies to begin colliding.
	 * @warning This function is locked during callbacks. */
	public void destroyJoint (Joint joint) {
		// FIXME
	}

	/** Take a time step. This performs collision detection, integration, and constraint solution.
	 * @param timeStep the amount of time to simulate, this should not vary.
	 * @param velocityIterations for the velocity constraint solver.
	 * @param positionIterations for the position constraint solver. */
	public void step (float timeStep, int velocityIterations, int positionIterations) {
		world.step(timeStep, velocityIterations, positionIterations);
	}

	/** Manually clear the force buffer on all bodies. By default, forces are cleared automatically after each call to Step. The
	 * default behavior is modified by calling SetAutoClearForces. The purpose of this function is to support sub-stepping.
	 * Sub-stepping is often used to maintain a fixed sized time step under a variable frame-rate. When you perform sub-stepping
	 * you will disable auto clearing of forces and instead call ClearForces after all sub-steps are complete in one pass of your
	 * game loop. {@link #setAutoClearForces(boolean)} */
	public void clearForces () {
		world.clearForces();
	}

	/** Enable/disable warm starting. For testing. */
	public void setWarmStarting (boolean flag) {
		world.setWarmStarting(flag);
	}

	/** Enable/disable continuous physics. For testing. */
	public void setContinuousPhysics (boolean flag) {
		world.setContinuousPhysics(flag);
	}

	/** Get the number of broad-phase proxies. */
	public int getProxyCount () {
		return world.getProxyCount();
	}

	/** Get the number of bodies. */
	public int getBodyCount () {
		return world.getBodyCount();
	}

	/** Get the number of joints. */
	public int getJointCount () {
		return world.getJointCount();
	}

	/** Get the number of contacts (each may have 0 or more contact points). */
	public int getContactCount () {
		return world.getContactCount();
	}

	/** Change the global gravity vector. */
	public void setGravity (Vector2 gravity) {
		world.setGravity(tmp.set(gravity.x, gravity.y));
	}

	public Vector2 getGravity () {
		Vec2 gravity = world.getGravity();
		return tmp2.set(gravity.x, gravity.y);
	}
	
	/** Is the world locked (in the middle of a time step). */
	public boolean isLocked () {
		return world.isLocked();
	}

	/** Set flag to control automatic clearing of forces after each time step. */
	public void setAutoClearForces (boolean flag) {
		world.setAutoClearForces(flag);
	}

	/** Get the flag that controls automatic clearing of forces after each time step. */
	public boolean getAutoClearForces () {
		return world.getAutoClearForces();
	}

	/** Query the world for all fixtures that potentially overlap the provided AABB.
	 * @param callback a user implemented callback class.
	 * @param lowerX the x coordinate of the lower left corner
	 * @param lowerY the y coordinate of the lower left corner
	 * @param upperX the x coordinate of the upper right corner
	 * @param upperY the y coordinate of the upper right corner */
	public void QueryAABB (QueryCallback callback, float lowerX, float lowerY, float upperX, float upperY) {
		// FIXME
	}

	/** Returns the list of {@link Contact} instances produced by the last call to {@link #step(float, int, int)}. Note that the
	 * returned list will have O(1) access times when using indexing. contacts are created and destroyed in the middle of a time
	 * step. Use {@link ContactListener} to avoid missing contacts
	 * @return the contact list */
	public List<Contact> getContactList () {
		// FIXME
		return null;
	}

	/** @return all bodies currently in the simulation */
	public Iterator<Body> getBodies () {
		// FIXME
		return null;
	}

	/** @return all joints currently in the simulation */
	public Iterator<Joint> getJoints () {
		// FIXME
		return null;
	}

	public void dispose () {
	}


	/** Sets the box2d velocity threshold globally, for all World instances.
	 * @param threshold the threshold, default 1.0f */
	public static void setVelocityThreshold (float threshold) {
		Settings.velocityThreshold = threshold;
	}

	/** @return the global box2d velocity threshold. */
	public static float getVelocityThreshold () {
		return Settings.velocityThreshold;
	}
	
	/** Ray-cast the world for all fixtures in the path of the ray. The ray-cast ignores shapes that contain the starting point.
	 * @param callback a user implemented callback class.
	 * @param point1 the ray starting point
	 * @param point2 the ray ending point */
	public void rayCast (RayCastCallback callback, Vector2 point1, Vector2 point2) {
		// FIXME
	}
}

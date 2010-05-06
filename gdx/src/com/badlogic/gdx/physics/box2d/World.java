package com.badlogic.gdx.physics.box2d;

import com.badlogic.gdx.math.Vector2;

/**
 * The world class manages all physics entities, dynamic simulation,
 * and asynchronous queries. The world also contains efficient memory
 * management facilities.
 * @author mzechner
 */
public class World 
{
	/** the address of the world instance **/
	private final long addr;
	
	/**
	 * Construct a world object.
	 * @param gravity the world gravity vector.
	 * @param doSleep improve performance by not simulating inactive bodies.
	 */
	public World( Vector2 gravity, boolean doSleep )
	{
		addr = newWorld( gravity.x, gravity.y, doSleep );
	}
	
	private native long newWorld( float gravityX, float gravityY, boolean doSleep );
	
	/**
	 *  Register a destruction listener. The listener is owned by you and must
	 * remain in scope.
	 */
	public void setDestructionListener(DestructionListener listener)
	{
		
	}

	/**
	 *  Register a contact filter to provide specific control over collision.
	 * Otherwise the default filter is used (b2_defaultFilter). The listener is
	 * owned by you and must remain in scope.
	 */ 
	public void setContactFilter(ContactFilter filter)
	{
		
	}

	/**
	 *  Register a contact event listener. The listener is owned by you and must
	 * remain in scope.
	 */
	void setContactListener(ContactListener listener)
	{
		
	}

	/**
	 *  Create a rigid body given a definition. No reference to the definition
	 * is retained.
	 * @warning This function is locked during callbacks.
	 */
	public Body createBody(BodyDef def)
	{
		return new Body( jniCreateBody( addr, 
										def.type.getValue(),
										def.position.x, def.position.y,
										def.angle, 
										def.linearVelocity.x, def.linearVelocity.y,
										def.angularVelocity,
										def.linearDamping,
										def.angularDamping, 
										def.allowSleep,
										def.awake, 
										def.fixedRotation,
										def.bullet,
										def.active,
										def.inertiaScale) );
	}
	
	private native long jniCreateBody( long addr, 
									   int type, 
									   float positionX, float positionY,
									   float angle,
									   float linearVelocityX, float linearVelocityY,
									   float angularVelocity,
									   float linearDamping,
									   float angularDamping,
									   boolean allowSleep,
									   boolean awake,
									   boolean fixedRotation,
									   boolean bullet,
									   boolean active,
									   float intertiaScale ); 

	/**
	 * Destroy a rigid body given a definition. No reference to the definition
	 * is retained. This function is locked during callbacks.
	 * @warning This automatically deletes all associated shapes and joints.
	 * @warning This function is locked during callbacks.
	 */
	public void destroyBody(Body body)
	{
		jniDestroyBody( body.addr );
	}

	private native void jniDestroyBody( long addr );
	
	/** 
	 * Create a joint to constrain bodies together. No reference to the definition
	 * is retained. This may cause the connected bodies to cease colliding.
	 * @warning This function is locked during callbacks.
	 */
	public Joint createJoint(JointDef def)
	{
		long jointAddr = jniCreateJoint( addr, def.type.getValue(), def.bodyA.addr, def.bodyB.addr, def.collideConnected);
		
		return null;
	}
	
	private native long jniCreateJoint( long addr, int type, long bodyA, long bodyB, boolean collideConnected );
	
	

	/**
	 * Destroy a joint. This may cause the connected bodies to begin colliding.
	 * @warning This function is locked during callbacks.
	 */
	public void destroyJoint(Joint joint)
	{
		jniDestroyJoint( joint.addr );
	}
	
	private native void jniDestroyJoint( long addr );

	/**
	 * Take a time step. This performs collision detection, integration,
	 * and constraint solution.
	 * @param timeStep the amount of time to simulate, this should not vary.
	 * @param velocityIterations for the velocity constraint solver.
	 * @param positionIterations for the position constraint solver.
	 */
	public void step(	float timeStep,
						int velocityIterations,
						int positionIterations)
	{
		jniStep( addr, timeStep, velocityIterations, positionIterations );
	}
	
	private native void jniStep( long addr, float timeStep, int velocityIterations, int positionIterations );

	/**
	 * Call this after you are done with time steps to clear the forces. You normally	 
	 * call this after each call to Step, unless you are performing sub-steps. By default,
	 * forces will be automatically cleared, so you don't need to call this function.
	 * @see SetAutoClearForces
	 */
	public void clearForces()
	{
		jniClearForces(addr);
	}

	private native void jniClearForces(long addr);
	
	/**
	 * Enable/disable warm starting. For testing.
	 */
	public void setWarmStarting(boolean flag)
	{
		jniSetWarmStarting(addr, flag);
	}

	private native void jniSetWarmStarting( long addr, boolean flag );
	
	/**
	 * Enable/disable continuous physics. For testing.
	 */
	public void setContinuousPhysics(boolean flag)
	{
		jniSetContiousPhysics(addr, flag);
	}
	
	private native void jniSetContiousPhysics( long addr, boolean flag );

	/**
	 * Get the number of broad-phase proxies.
	 */
	public int getProxyCount()
	{
		return jniGetProxyCount(addr);
	}
	
	private native int jniGetProxyCount( long addr );

	/**
	 * Get the number of bodies.
	 */
	public int getBodyCount()
	{
		return jniGetBodyCount(addr);
	}
	
	private native int jniGetBodyCount( long addr );

	/**
	 * Get the number of joints.
	 */
	public int getJointCount()
	{
		return jniGetJointcount(addr);
	}
	
	private native int jniGetJointcount( long addr );
	
	/**
	 * Get the number of contacts (each may have 0 or more contact points).
	 */
	public int getContactCount()
	{
		return jniGetContactCount( addr );
	}

	private native int jniGetContactCount( long addr );
	
	/**
	 * Change the global gravity vector.
	 */
	public void setGravity(Vector2 gravity)
	{
		jniSetGravity( addr, gravity.x, gravity.y );
	}
	
	private native void jniSetGravity( long addr, float gravityX, float gravityY );
	
	/**
	 * Get the global gravity vector.
	 */
	final float[] tmpGravity = new float[2];
	final Vector2 gravity = new Vector2( );
	public Vector2 getGravity()
	{
		jniGetGravity( addr, tmpGravity );
		gravity.x = tmpGravity[0]; gravity.y = tmpGravity[1];
		return gravity;
	}

	private native void jniGetGravity( long addr, float[] gravity ); 
	
	/**
	 * Is the world locked (in the middle of a time step).
	 */	
	public boolean isLocked()
	{
		return jniIsLocked( addr );
	}

	private native boolean jniIsLocked( long addr );
	
	/**
	 *  Set flag to control automatic clearing of forces after each time step.
	 */
	public void setAutoClearForces(boolean flag)
	{
		jniSetAutoClearForces(addr, flag);
	}

	private native void jniSetAutoClearForces( long addr, boolean flag );
	
	/**
	 *  Get the flag that controls automatic clearing of forces after each time step.
	 */
	public boolean getAutoClearForces()
	{
		return jniGetAutoClearForces( addr );
	}
	
	private native boolean jniGetAutoClearForces( long addr );
	
//	/// Query the world for all fixtures that potentially overlap the
//	/// provided AABB.
//	/// @param callback a user implemented callback class.
//	/// @param aabb the query box.
//	void QueryAABB(b2QueryCallback* callback, const b2AABB& aabb) const;
//
//	/// Ray-cast the world for all fixtures in the path of the ray. Your callback
//	/// controls whether you get the closest point, any point, or n-points.
//	/// The ray-cast ignores shapes that contain the starting point.
//	/// @param callback a user implemented callback class.
//	/// @param point1 the ray starting point
//	/// @param point2 the ray ending point
//	void RayCast(b2RayCastCallback* callback, const b2Vec2& point1, const b2Vec2& point2) const;
//
//	/// Get the world body list. With the returned body, use b2Body::GetNext to get
//	/// the next body in the world list. A NULL body indicates the end of the list.
//	/// @return the head of the world body list.
//	b2Body* GetBodyList();
//
//	/// Get the world joint list. With the returned joint, use b2Joint::GetNext to get
//	/// the next joint in the world list. A NULL joint indicates the end of the list.
//	/// @return the head of the world joint list.
//	b2Joint* GetJointList();
//
//	/// Get the world contact list. With the returned contact, use b2Contact::GetNext to get
//	/// the next contact in the world list. A NULL contact indicates the end of the list.
//	/// @return the head of the world contact list.
//	/// @warning contacts are 
//	b2Contact* GetContactList();
}

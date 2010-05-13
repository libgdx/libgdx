package com.badlogic.gdx.physics.box2d;

import java.util.ArrayList;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

/**
 * A rigid body. These are created via World.CreateBody.
 * @author mzechner
 *
 */
public class Body 
{
	/** the address of the body **/
	protected final long addr;
	
	/** temporary float array **/
	private final float[] tmp = new float[4];
	
	/** World **/
	private final World world;
	
	/** Fixtures of this body **/
	private ArrayList<Fixture> fixtures = new ArrayList<Fixture>(2);
	
	/** Joints of this body **/
	protected ArrayList<JointEdge> joints = new ArrayList<JointEdge>(2);	
	
	/** user data **/
	private Object userData;
	
	/**
	 * Constructs a new body with the given address
	 * @param world the world
	 * @param addr the address
	 */
	protected Body( World world, long addr )
	{
		this.world = world;
		this.addr = addr;
	}
	
	/**
	 *  Creates a fixture and attach it to this body. Use this function if you need
	 *  to set some fixture parameters, like friction. Otherwise you can create the
	 *  fixture directly from a shape.
	 *  If the density is non-zero, this function automatically updates the mass of the body.
	 *  Contacts are not created until the next time step.
	 *  @param def the fixture definition.
	 *  @warning This function is locked during callbacks.
	 */	 
	public Fixture createFixture(FixtureDef def)
	{				
		Fixture fixture = new Fixture( world, this, jniCreateFixture(addr, def.shape.addr, def.friction, def.restitution, def.density, def.isSensor, def.filter.categoryBits, def.filter.maskBits, def.filter.groupIndex) );
		this.world.fixtures.put( fixture.addr, fixture );
		this.fixtures.add( fixture );
		return fixture;
	}
	
	private native long jniCreateFixture( long addr, long shapeAddr, float friction, float restitution, float density, boolean isSensor, short filterCategoryBits, short filterMaskBits, short filterGroupIndex );		
	
	/**
	 * Creates a fixture from a shape and attach it to this body.
	 * This is a convenience function. Use b2FixtureDef if you need to set parameters
	 * like friction, restitution, user data, or filtering.
	 * If the density is non-zero, this function automatically updates the mass of the body.
	 * @param shape the shape to be cloned.
	 * @param density the shape density (set to zero for static bodies).
	 * @warning This function is locked during callbacks. 
	 */
	public Fixture createFixture(Shape shape, float density)
	{
		Fixture fixture = new Fixture( world, this, jniCreateFixture(addr, shape.addr, density));
		this.world.fixtures.put( fixture.addr, fixture );
		this.fixtures.add( fixture );
		return fixture;
	}
	
	private native long jniCreateFixture( long addr, long shapeAddr, float density );
	
	/**
	 * Destroy a fixture. This removes the fixture from the broad-phase and
	 * destroys all contacts associated with this fixture. This will
	 * automatically adjust the mass of the body if the body is dynamic and the
	 * fixture has positive density.
	 * All fixtures attached to a body are implicitly destroyed when the body is destroyed.
	 * @param fixture the fixture to be removed.
	 * @warning This function is locked during callbacks. 
	 */
	public void destroyFixture(Fixture fixture)
	{
		jniDestroyFixture( addr, fixture.addr );
		this.world.fixtures.remove(fixture);
		this.fixtures.remove(fixture);
	}
	
	private native void jniDestroyFixture( long addr, long fixtureAddr );

	/** 
	 * Set the position of the body's origin and rotation.	 
	 * This breaks any contacts and wakes the other bodies.
	 * Manipulating a body's transform may cause non-physical behavior.
	 * @param position the world position of the body's local origin.
	 * @param angle the world rotation in radians.
	 */
	public void setTransform(Vector2 position, float angle)
	{
		jniSetTransform( addr, position.x, position.y, angle);
	}	
	
	private native void jniSetTransform( long addr, float positionX, float positionY, float angle );
	
	/** 
	 * Get the body transform for the body's origin. FIXME
	 */	
	private final Transform transform = new Transform( );
	public Transform getTransform()
	{
		return transform;
	}

	/**
	 * Get the world body origin position.
	 * @return the world position of the body's origin.
	 */	
	private final Vector2 position = new Vector2( );
	public Vector2 getPosition()
	{
		jniGetPosition( addr, tmp );
		position.x = tmp[0]; position.y = tmp[1];
		return position;
	}

	private native void jniGetPosition( long addr, float[] position );
	
	/**
	 * Get the angle in radians.
	 * @return the current world rotation angle in radians.
	 */
	public float getAngle()
	{
		return jniGetAngle( addr );
	}

	private native float jniGetAngle( long addr );
	
	/**
	 *  Get the world position of the center of mass. 
	 */	
	private final Vector2 worldCenter = new Vector2( );
	public Vector2 getWorldCenter()
	{
		jniGetWorldCenter( addr, tmp );
		worldCenter.x = tmp[0]; worldCenter.y = tmp[1];
		return worldCenter;
	}

	private native void jniGetWorldCenter( long addr, float[] worldCenter );
	
	/**
	 * Get the local position of the center of mass. 
	 */	
	private final Vector2 localCenter = new Vector2( );
	public Vector2 getLocalCenter()
	{
		jniGetLocalCenter( addr, tmp );
		localCenter.x = tmp[0]; localCenter.y = tmp[1];
		return localCenter;
	}
	
	private native void jniGetLocalCenter( long addr, float[] localCenter );

	/**
	 * Set the linear velocity of the center of mass.	 
	 */
	public void setLinearVelocity(Vector2 v)
	{
		jniSetLinearVelocity( addr, v.x, v.y );
	}
	
	private native void jniSetLinearVelocity( long addr, float x, float y );

	/**
	 * Get the linear velocity of the center of mass.
	 */	
	private final Vector2 linearVelocity = new Vector2( );
	public Vector2 getLinearVelocity()
	{
		jniGetLinearVelocity( addr, tmp );
		linearVelocity.x = tmp[0]; linearVelocity.y = tmp[1];
		return linearVelocity;
	}

	private native void jniGetLinearVelocity( long addr, float[] tmpLinearVelocity );
	
	/**
	 * Set the angular velocity.
	 */
	public void setAngularVelocity(float omega)
	{
		jniSetAngularVelocity(addr, omega);
	}
	
	private native void jniSetAngularVelocity( long addr, float omega );

	/**
	 * Get the angular velocity.
	 */
	public float getAngularVelocity()
	{
		return jniGetAngularVelocity( addr );
	}
	
	private native float jniGetAngularVelocity( long addr );

	/**
	 * Apply a force at a world point. If the force is not
 	 * applied at the center of mass, it will generate a torque and
	 * affect the angular velocity. This wakes up the body.
	 * @param force the world force vector, usually in Newtons (N).
	 * @param point the world position of the point of application.
	 */
	public void applyForce(Vector2 force, Vector2 point)
	{
		jniApplyForce( addr, force.x, force.y, point.x, point.y );
	}
	
	private native void jniApplyForce( long addr, float forceX, float forceY, float pointX, float pointY );

	/**
	 *  Apply a torque. This affects the angular velocity
	 * without affecting the linear velocity of the center of mass.
	 * This wakes up the body.
	 * @param torque about the z-axis (out of the screen), usually in N-m.
	 */
	public void applyTorque(float torque)
	{
		jniApplyTorque( addr, torque );
	}
	
	private native void jniApplyTorque( long addr, float torque );

	/**
	 *  Apply an impulse at a point. This immediately modifies the velocity.
	 * It also modifies the angular velocity if the point of application
	 * is not at the center of mass. This wakes up the body.
	 * @param impulse the world impulse vector, usually in N-seconds or kg-m/s.
	 * @param point the world position of the point of application.
	 */
	public void applyLinearImpulse(Vector2 impulse, Vector2 point)
	{
		jniApplyLinearImpulse( addr, impulse.x, impulse.y, point.x, point.y );
	}
	
	private native void jniApplyLinearImpulse( long addr, float impulseX, float impulseY, float pointX, float pointY );

	/**
	 * Apply an angular impulse.
	 * @param impulse the angular impulse in units of kg*m*m/s
	 */
	public void applyAngularImpulse(float impulse)
	{
		jniApplyAngularImpulse( addr, impulse );
	}
	
	private native void jniApplyAngularImpulse( long addr, float impulse );

	/**
	 * Get the total mass of the body.
	 * @return the mass, usually in kilograms (kg).
	 */
	public float getMass()
	{
		return jniGetMass( addr );
	}
	
	private native float jniGetMass( long addr );

	/**
	 * Get the rotational inertia of the body about the local origin.
	 * @return the rotational inertia, usually in kg-m^2.
	 */
	public float getInertia()
	{
		return jniGetInertia( addr );
	}
	
	private native float jniGetInertia( long addr );

	/**
	 * Get the mass data of the body.
	 * @return a struct containing the mass, inertia and center of the body.
	 */		
	private final MassData massData = new MassData( );
	public MassData getMassData()
	{
		jniGetMassData(addr, tmp);
		massData.mass = tmp[0];
		massData.center.x = tmp[1];
		massData.center.y = tmp[2];
		massData.I = tmp[3];
		return null;
	}
	
	private native void jniGetMassData( long addr, float[] massData );

	/**
	 *  Set the mass properties to override the mass properties of the fixtures.
	 * Note that this changes the center of mass position.
	 * Note that creating or destroying fixtures can also alter the mass.
	 * This function has no effect if the body isn't dynamic.
	 * @param massData the mass properties.
	 */
	public void setMassData(MassData data)
	{
		jniSetMassData( addr, data.mass, data.center.x, data.center.y, data.I );
	}
	
	private native void jniSetMassData( long addr, float mass, float centerX, float centerY, float I );

	/**
	 *  This resets the mass properties to the sum of the mass properties of the fixtures.
	 * This normally does not need to be called unless you called SetMassData to override
	 * the mass and you later want to reset the mass.
	 */
	public void resetMassData()
	{
		jniResetMassData( addr );
	}
	
	private native void jniResetMassData( long addr );

	/**
	 *  Get the world coordinates of a point given the local coordinates.
	 * @param localPoint a point on the body measured relative the the body's origin.
	 * @return the same point expressed in world coordinates.
	 */	
	private final Vector2 localPoint = new Vector2( );
	public Vector2 getWorldPoint(Vector2 localPoint)
	{
		jniGetWorldPoint( addr, localPoint.x, localPoint.y, tmp );
		this.localPoint.x = tmp[0]; this.localPoint.y = tmp[1];
		return this.localPoint;
	}

	private native void jniGetWorldPoint( long addr, float localPointX, float localPointY, float[] worldPoint );
	
	/**
	 *  Get the world coordinates of a vector given the local coordinates.
	 * @param localVector a vector fixed in the body.
	 * @return the same vector expressed in world coordinates.
	 */	
	private final Vector2 worldVector = new Vector2( );
	public Vector2 getWorldVector(Vector2 localVector)
	{
		jniGetWorldVector( addr, localVector.x, localVector.y, tmp );
		worldVector.x = tmp[0]; worldVector.y = tmp[1];
		return worldVector;
	}
	
	private native void jniGetWorldVector( long addr, float localVectorX, float localVectorY, float[] worldVector );

	/**
	 *  Gets a local point relative to the body's origin given a world point.
	 * @param a point in world coordinates.
	 * @return the corresponding local point relative to the body's origin.
	 */	
	public final Vector2 localPoint2 = new Vector2( );
	public Vector2 getLocalPoint(Vector2 worldPoint)
	{
		jniGetLocalPoint( addr, worldPoint.x, worldPoint.y, tmp );
		localPoint2.x = tmp[0]; localPoint2.y = tmp[1];
		return localPoint2;
	}

	private native void jniGetLocalPoint( long addr, float worldPointX, float worldPointY, float[] localPoint );
	
	/**
	 *  Gets a local vector given a world vector.
	 * @param a vector in world coordinates.
	 * @return the corresponding local vector.
	 */	
	public final Vector2 localVector = new Vector2( );
	public Vector2 getLocalVector(Vector2 worldVector)
	{
		jniGetLocalVector( addr, worldVector.x, worldVector.y, tmp );
		localVector.x = tmp[0]; localVector.y = tmp[1];
		return localVector;
	}
	
	private native void jniGetLocalVector( long addr, float worldVectorX, float worldVectorY, float[] worldVector );

	/**
	 *  Get the world linear velocity of a world point attached to this body.
	 * @param a point in world coordinates.
	 * @return the world velocity of a point.
	 */	
	public final Vector2 linVelWorld = new Vector2( );
	public Vector2 getLinearVelocityFromWorldPoint(Vector2 worldPoint)
	{
		jniGetLinearVelocityFromWorldPoint( addr, worldPoint.x, worldPoint.y, tmp );
		linVelWorld.x = tmp[0]; linVelWorld.y = tmp[1];
		return linVelWorld;
	}
	
	private native void jniGetLinearVelocityFromWorldPoint( long addr, float worldPointX, float worldPointY, float[] linVelWorld );

	/**
	 *  Get the world velocity of a local point.
	 * @param a point in local coordinates.
	 * @return the world velocity of a point.
	 */	
	public final Vector2 linVelLoc = new Vector2( );
	
	public Vector2 getLinearVelocityFromLocalPoint(Vector2 localPoint)
	{
		jniGetLinearVelocityFromLocalPoint( addr, localPoint.x, localPoint.y, tmp );
		linVelLoc.x = tmp[0]; linVelLoc.y = tmp[1];
		return linVelLoc;
	}

	private native void jniGetLinearVelocityFromLocalPoint( long addr, float localPointX, float localPointY, float[] linVelLoc );
	
	/**
	 *  Get the linear damping of the body.
	 */
	public float getLinearDamping()
	{
		return jniGetLinearDamping( addr );
	}
	
	private native float jniGetLinearDamping( long add );

	/**
	 *  Set the linear damping of the body.
	 */
	public void setLinearDamping(float linearDamping)
	{
		jniSetLinearDamping( addr, linearDamping );
	}

	private native void jniSetLinearDamping( long addr, float linearDamping );
	
	/**
	 * Get the angular damping of the body.
	 */
	public float getAngularDamping()
	{
		return jniGetAngularDamping( addr );
	}
	
	private native float jniGetAngularDamping( long addr );

	/**
	 *  Set the angular damping of the body.
	 */
	public void setAngularDamping(float angularDamping)
	{
		jniSetAngularDamping( addr, angularDamping );
	}
	
	private native void jniSetAngularDamping( long addr, float angularDamping );

	/**
	 *  Set the type of this body. This may alter the mass and velocity.
	 */
	public void setType(BodyType type)
	{
		jniSetType( addr, type.getValue() );
	}
	
	private native void jniSetType( long addr, int type );

	/**
	 * Get the type of this body.
	 */
	public BodyType getType()
	{
		int type = jniGetType( addr );
		if( type == 0 )
			return BodyType.StaticBody;
		if( type == 1 )
			return BodyType.KinematicBody;
		if( type == 2 )
			return BodyType.DynamicBody;
		return BodyType.StaticBody;
	}
	
	private native int jniGetType( long addr );

	/**
	 * Should this body be treated like a bullet for continuous collision detection?
	 */
	public void setBullet(boolean flag)
	{
		jniSetBullet( addr, flag );		
	}
	
	private native void jniSetBullet( long addr, boolean flag );

	/**
	 *  Is this body treated like a bullet for continuous collision detection?
	 */
	public boolean isBullet()
	{
		return jniIsBullet( addr );
	}
	
	private native boolean jniIsBullet( long addr );

	/**
	 *  You can disable sleeping on this body. If you disable sleeping, the
	 */
	public void setSleepingAllowed(boolean flag)
	{
		jniSetSleepingAllowed( addr, flag );
	}
	
	private native void jniSetSleepingAllowed( long addr, boolean flag );

	/**
	 *  Is this body allowed to sleep
	 */
	public boolean isSleepingAllowed()
	{
		return jniIsSleepingAllowed( addr );
	}
	
	private native boolean jniIsSleepingAllowed( long addr );

	/**
	 *  Set the sleep state of the body. A sleeping body has very
	 * low CPU cost.
	 * @param flag set to true to put body to sleep, false to wake it.
	 */
	public void setAwake(boolean flag)
	{
		jniSetAwake( addr, flag );		
	}
	
	private native void jniSetAwake( long addr, boolean flag );

	/**
	 *  Get the sleeping state of this body.
	 * @return true if the body is sleeping.
	 */
	public boolean isAwake()
	{
		return jniIsAwake( addr );
	}
	
	private native boolean jniIsAwake( long addr );

	/**
	 *  Set the active state of the body. An inactive body is not
	 * simulated and cannot be collided with or woken up.
	 * If you pass a flag of true, all fixtures will be added to the
	 * broad-phase.
	 * If you pass a flag of false, all fixtures will be removed from
	 * the broad-phase and all contacts will be destroyed.
	 * Fixtures and joints are otherwise unaffected. You may continue
	 * to create/destroy fixtures and joints on inactive bodies.
	 * Fixtures on an inactive body are implicitly inactive and will
	 * not participate in collisions, ray-casts, or queries.
	 * Joints connected to an inactive body are implicitly inactive.
	 * An inactive body is still owned by a b2World object and remains
	 * in the body list.
	 */
	public void setActive(boolean flag)
	{
		jniSetActive( addr, flag );
	}
	
	private native void jniSetActive( long addr, boolean flag );

	/**
	 *  Get the active state of the body.
	 */
	public boolean isActive()
	{
		return jniIsActive( addr );
	}
	
	private native boolean jniIsActive( long addr );

	/**
	 *  Set this body to have fixed rotation. This causes the mass
	 * to be reset.
	 */
	public void setFixedRotation(boolean flag)
	{
		jniSetFixedRotation( addr, flag );
	}
	
	private native void jniSetFixedRotation( long addr, boolean flag );

	/** 
	 * Does this body have fixed rotation?
	 */
	public boolean isFixedRotation()
	{
		return jniIsFixedRotation( addr );
	}
	
	private native boolean jniIsFixedRotation( long addr );
	
	/**
	 * Get the list of all fixtures attached to this body.
	 * Do not modify the list!
	 */
	public ArrayList<Fixture> getFixtureList()
	{
		return fixtures;
	}

	/**
	 *  Get the list of all joints attached to this body.
	 *  Do not modify the list!
	 */
	public ArrayList<JointEdge> getJointList()
	{
		return joints;
	}	

	/**
	 *  Get the list of all contacts attached to this body.	 
	 * @warning this list changes during the time step and you may
	 * miss some collisions if you don't use b2ContactListener.
	 * Do not modify the returned list!
	 */
//	ArrayList<ContactEdge> getContactList()
//	{
//		return contacts;
//	}
	
	/**
	 *  Get the parent world of this body.
	 */
	public World getWorld()
	{
		return world;
	}
	
	/**
	 * Get the user data
	 */
	public Object getUserData( )
	{
		return userData;
	}
	
	/**
	 * Set the user data
	 */
	public void setUserData( Object userData )
	{
		this.userData = userData;
	}
}

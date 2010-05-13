package com.badlogic.gdx.physics.box2d;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Shape.Type;

public class Fixture 
{			
	/** world **/
	private final World world;
	
	/** body **/
	private final Body body;
	
	/** the address of the fixture **/
	protected final long addr;
	
	/**
	 * Constructs a new fixture
	 * @param addr the address of the fixture
	 */
	protected Fixture( World world, Body body, long addr )
	{
		this.world = world;
		this.body = body;
		this.addr = addr;
	}
	
	/**
	 * Get the type of the child shape. You can use this to down cast to the concrete shape.
	 * @return the shape type.
	 */
	public Type getType()
	{
		int type = jniGetType( addr );
		if( type == 0 )
			return Type.Circle;
		else
			return Type.Polygon;		
	}
	
	private native int jniGetType( long addr );

//	/// Get the child shape. You can modify the child shape, however you should not change the
//	/// number of vertices because this will crash some collision caching mechanisms.
//	/// Manipulating the shape may lead to non-physical behavior.
//	b2Shape* GetShape();
//	const b2Shape* GetShape() const;

	/**
	 *  Set if this fixture is a sensor.
	 */
	public void setSensor(boolean sensor)
	{
		jniSetSensor( addr, sensor );
	}
	
	private native void jniSetSensor( long addr, boolean sensor );

	/**
	 * Is this fixture a sensor (non-solid)?
	 * @return the true if the shape is a sensor.
	 */	
	public boolean isSensor()
	{
		return jniIsSensor( addr );
	}
	
	private native boolean jniIsSensor( long addr );

	/**
	 * Set the contact filtering data. This will not update contacts until the next time
	 * step when either parent body is active and awake.
	 */
	public void setFilterData(Filter filter)
	{
		jniSetFilterData( addr, filter.categoryBits, filter.maskBits, filter.groupIndex );
	}
	
	private native void jniSetFilterData( long addr, short categoryBits, short maskBits, short groupIndex );

	/**
	 *  Get the contact filtering data.
	 */
	private final short[] tmp = new short[3];
	private final Filter filter = new Filter( );
	public Filter getFilterData()
	{
		jniGetFilterData( addr, tmp );
		filter.categoryBits = tmp[0];
		filter.maskBits = tmp[1];
		filter.groupIndex = tmp[2];
		return filter;
	}
	
	private native void jniGetFilterData( long addr, short[] filter );

	/**
	 *  Get the parent body of this fixture. This is NULL if the fixture is not attached.
	 */
	public Body getBody()
	{
		return body;
	}
	
	/** 
	 * Test a point for containment in this fixture.	 
	 * @param p a point in world coordinates.
	 */
	public boolean testPoint(Vector2 p)
	{
		return jniTestPoint( addr, p.x, p.y );
	}
	
	private native boolean jniTestPoint( long addr, float x, float y );
	
//	const b2Body* GetBody() const;
//
//	/// Get the next fixture in the parent body's fixture list.
//	/// @return the next shape.
//	b2Fixture* GetNext();
//	const b2Fixture* GetNext() const;
//
//	/// Get the user data that was assigned in the fixture definition. Use this to
//	/// store your application specific data.
//	void* GetUserData() const;
//
//	/// Set the user data. Use this to store your application specific data.
//	void SetUserData(void* data);
//
//	/// Cast a ray against this shape.
//	/// @param output the ray-cast results.
//	/// @param input the ray-cast input parameters.
//	bool RayCast(b2RayCastOutput* output, const b2RayCastInput& input) const;
//
//	/// Get the mass data for this fixture. The mass data is based on the density and
//	/// the shape. The rotational inertia is about the shape's origin. This operation
//	/// may be expensive.
//	void GetMassData(b2MassData* massData) const;

	/**
	 *  Set the density of this fixture. This will _not_ automatically adjust the mass
	 *  of the body. You must call b2Body::ResetMassData to update the body's mass.
	 */
	public void setDensity(float density)
	{
		jniSetDensity( addr, density );
	}
	
	private native void jniSetDensity( long addr, float density );

	/**
	 *  Get the density of this fixture.
	 */
	public float getDensity()
	{
		return jniGetDensity( addr );
	}
	
	private native float jniGetDensity( long addr );

	/**
	 *  Get the coefficient of friction.
	 */
	public float getFriction()
	{
		return jniGetFriction( addr );	
	}
	
	private native float jniGetFriction( long addr );

	/**
	 *  Set the coefficient of friction.
	 */
	public void setFriction(float friction)
	{
		jniSetFriction( addr, friction );
	}
	
	private native void jniSetFriction( long addr, float friction );

	/**
	 * Get the coefficient of restitution.
	 */
	public float getRestitution()
	{
		return jniGetRestitution( addr );
	}
	
	private native float jniGetRestitution( long addr );

	/** 
	 * Set the coefficient of restitution.
	 */
	public void setRestitution(float restitution)
	{
		jniSetRestitution( addr, restitution );
	}
	
	private native void jniSetRestitution( long addr, float restitution );

//	/// Get the fixture's AABB. This AABB may be enlarge and/or stale.
//	/// If you need a more accurate AABB, compute it using the shape and
//	/// the body transform.
//	const b2AABB& GetAABB() const;
}

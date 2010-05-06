package com.badlogic.gdx.physics.box2d;

public class JointDef 
{
	public enum JointType
	{
		RevoluteJoint(0),
		PrismaticJoint(1),
		DistanceJoint(2),
		PulleyJoint(3),
		MouseJoint(4),
		GearJoint(5),
		LineJoint(6),
		WeldJoint(7),
		FrictionJoint(8),
		Unknown(9);
		
		private int value;
		
		JointType( int value )
		{
			this.value = value;
		}
		
		public int getValue( )
		{
			return value;
		}
	}
	
	/** The joint type is set automatically for concrete joint types. **/
	public JointType type = JointType.Unknown;
	
	/** The first attached body. **/
	public Body bodyA = null;
	
	/** The second attached body **/
	public Body bodyB = null;
	
	/** Set this flag to true if the attached bodies should collide. **/
	public boolean collideConnected = false;
}

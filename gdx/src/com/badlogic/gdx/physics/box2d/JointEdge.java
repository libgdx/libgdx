package com.badlogic.gdx.physics.box2d;

/**
 * A joint edge is used to connect bodies and joints together
 * in a joint graph where each body is a node and each joint
 * is an edge. A joint edge belongs to a doubly linked list
 * maintained in each attached body. Each joint has two joint
 * nodes, one for each attached body. 
 */
public class JointEdge 
{
	public final Body other;
	public final Joint joint;
	
	protected JointEdge( Body other, Joint joint )
	{
		this.other = other;
		this.joint = joint;
	}
}

package com.badlogic.gdx.physics.box2d.joints;

import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.World;

/**
 *  A weld joint essentially glues two bodies together. A weld joint may
 * distort somewhat because the island constraint solver is approximate.
 */
public class WeldJoint extends Joint
{
	public WeldJoint(World world, long addr) 
	{
		super(world,addr);	
	}
}

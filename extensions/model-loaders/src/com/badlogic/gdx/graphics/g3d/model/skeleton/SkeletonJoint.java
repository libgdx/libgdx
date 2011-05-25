package com.badlogic.gdx.graphics.g3d.model.skeleton;

import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public class SkeletonJoint {
	public String name;
	
	public int index;
	public int parentIndex;
	public SkeletonJoint parent;
	public final Array<SkeletonJoint> children = new Array<SkeletonJoint>(1);
	
	public final Vector3 position = new Vector3();	
	public final Quaternion rotation = new Quaternion(new Vector3(0, 1, 0), 0);	
	public final Vector3 scale = new Vector3(1, 1, 1);		
}
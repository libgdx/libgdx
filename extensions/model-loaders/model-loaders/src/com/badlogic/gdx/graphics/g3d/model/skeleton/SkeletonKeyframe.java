package com.badlogic.gdx.graphics.g3d.model.skeleton;

import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

public class SkeletonKeyframe {
	public float timeStamp = 0;
	public int parentIndex = -1;
	public final Vector3 position = new Vector3();
	public final Vector3 scale = new Vector3(1, 1, 1);
	public final Quaternion rotation = new Quaternion(0, 0, 0, 1);	
	
	public String toString() {
		return "time: " + timeStamp + ", " +
				 "parent: " + parentIndex + ", " +
				 "position: " + position + ", " + 
				 "scale: " + scale + ", " + 
				 "rotation: " + rotation;		
	}
}

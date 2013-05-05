package com.badlogic.gdx.graphics.g3d.model.data;

import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

public class ModelNodeKeyframe {
	/** the timestamp of the keyframe in seconds **/
	public float keytime;
	/** the translation, in local space relative to the parent **/
	public Vector3 translation;
	/** the scale, in local space relative to the parent **/
	public Vector3 scale;
	/** the rotation, in local space relative to the parent **/
	public Quaternion rotation;
}

package com.badlogic.gdx.graphics.g3d.model.data;

import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

public class ModelBoneKeyframe {
	/** the timestamp of the keyframe in seconds **/
	public float keytime;
	/** the translation, in local space relative to the parent **/
	public final Vector3 translation = new Vector3();
	/** the scale, in local space relative to the parent **/
	public final Vector3 scale = new Vector3();
	/** the rotation, in local space relative to the parent **/
	public final Quaternion rotation = new Quaternion();
}

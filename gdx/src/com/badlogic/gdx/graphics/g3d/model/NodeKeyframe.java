package com.badlogic.gdx.graphics.g3d.model;

import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

/**
 * A BoneyKeyframe specifies the translation, rotation and scale of a frame within
 * a {@link NodeAnimation}.
 * @author badlogic
 *
 */
public class NodeKeyframe {
	/** the timestamp of this keyframe **/
	public float keytime;
	/** the translation, given in local space, relative to the parent **/
	public final Vector3 translation = new Vector3();
	/** the scale, given in local space relative to the parent **/
	public final Vector3 scale = new Vector3(1,1,1);
	/** the rotation, given in local space, relative to the parent **/
	public final Quaternion rotation = new Quaternion();
}

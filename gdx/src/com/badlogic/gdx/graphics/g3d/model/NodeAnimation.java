package com.badlogic.gdx.graphics.g3d.model;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.utils.Array;

/**
 * A NodeAnimation defines keyframes for a {@link Node} in a {@link Model}. The keyframes
 * are given as a translation vector, a rotation quaternion and a scale vector. Keyframes are 
 * interpolated linearly for now. Keytimes are given in seconds.
 * @author badlogic
 *
 */
public class NodeAnimation {
	/** the Node affected by this animation **/
	public Node node;
	/** the keyframes, sorted by time, ascending **/
	public Array<NodeKeyframe> keyframes = new Array<NodeKeyframe>();
}

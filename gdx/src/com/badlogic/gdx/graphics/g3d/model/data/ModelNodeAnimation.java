package com.badlogic.gdx.graphics.g3d.model.data;

import com.badlogic.gdx.utils.Array;

public class ModelNodeAnimation {
	/** the id of the node animated by this animation FIXME should be nodeId **/
	public String nodeId;
	/** the keyframes, defining the transformation of a node for a sepcific timestamp **/
	public final Array<ModelNodeKeyframe> keyframes = new Array<ModelNodeKeyframe>();
}

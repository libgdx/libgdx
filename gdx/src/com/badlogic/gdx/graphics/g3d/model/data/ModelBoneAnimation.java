package com.badlogic.gdx.graphics.g3d.model.data;

import com.badlogic.gdx.utils.Array;

public class ModelBoneAnimation {
	/** the id of the node animated by this animation FIXME should be nodeId **/
	public String boneId;
	/** the keyframes, defining the transformation of a node for a sepcific timestamp **/
	public Array<ModelBoneKeyframe> keyframes = new Array<ModelBoneKeyframe>();
}

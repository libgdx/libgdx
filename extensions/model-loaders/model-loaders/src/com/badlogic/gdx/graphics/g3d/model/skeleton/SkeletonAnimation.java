package com.badlogic.gdx.graphics.g3d.model.skeleton;

import com.badlogic.gdx.graphics.g3d.model.Animation;

public class SkeletonAnimation extends Animation {
	public final SkeletonKeyframe[][] perJointkeyFrames;
	
	public SkeletonAnimation(String name, float totalDuration, SkeletonKeyframe[][] perJointKeyFrames) {
		super(name, totalDuration);
		this.perJointkeyFrames = perJointKeyFrames;
	}	
}

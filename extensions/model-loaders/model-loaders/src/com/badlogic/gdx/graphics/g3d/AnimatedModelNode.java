
package com.badlogic.gdx.graphics.g3d;

public class AnimatedModelNode extends StillModelNode implements AnimatedModelInstance {
	public String animation;
	public float time;
	public boolean looping;

	@Override
	public String getAnimation () {
		return animation;
	}

	@Override
	public float getAnimationTime () {
		return time;
	}

	@Override
	public boolean isLooping () {
		return looping;
	}

}

package com.badlogic.gdx.graphics.g3d;

public interface AnimatedModelInstance extends StillModelInstance {
	public String getAnimation();
	public float getAnimationTime();
	public boolean isLooping();
}

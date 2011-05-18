package com.badlogic.gdx.graphics.g3d.model;

public interface AnimatedModel extends Model {
	public void setAnimation(String animation, float time);	
	public Animation getAnimation(String name);
	public Animation[] getAnimations();
}

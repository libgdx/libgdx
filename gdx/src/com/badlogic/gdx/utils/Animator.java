package com.badlogic.gdx.utils;

/**
 * A class make it easy to manage animation of sprite
 * @author Ngo Trong TRung
 */
public interface Animator {
	public void setFrameDuration(float frameDuration);
	
	public void start();
	
	public void start(float frameDuration);
	
	public void start(float frameDuration,int playMode);
	
	public void stop();
	
	public void pause();
	
	public void switchState();
	
	public void resetFrame();
	
	public void update(float delta);
	
	public boolean isRunning();
}

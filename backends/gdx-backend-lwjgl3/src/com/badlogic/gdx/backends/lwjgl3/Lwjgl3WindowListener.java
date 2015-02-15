package com.badlogic.gdx.backends.lwjgl3;

public interface Lwjgl3WindowListener 
{
	
	public void exit (Lwjgl3Application app);
	public void size (Lwjgl3Application app, int width, int height);
	public void refresh (Lwjgl3Application app);
	public void focus (Lwjgl3Application app, boolean focused);
	public void position (Lwjgl3Application app, int x, int y);
}

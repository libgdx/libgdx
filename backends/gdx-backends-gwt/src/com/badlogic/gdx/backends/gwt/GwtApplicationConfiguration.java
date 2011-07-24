package com.badlogic.gdx.backends.gwt;

public class GwtApplicationConfiguration {
	public int width;
	public int height;
	public boolean stencil = false;
	public boolean antialiasing = false;
	public int fps = 60;
	
	public GwtApplicationConfiguration(int width, int height) {
		this.width = width;
		this.height = height;
	}
}

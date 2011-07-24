package com.badlogic.gdx.backends.gwt;

import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TextArea;

public class GwtApplicationConfiguration {
	public int width;
	public int height;
	public boolean stencil = false;
	public boolean antialiasing = false;
	public int fps = 60;
	public Panel rootPanel;
	public TextArea log;
	
	public GwtApplicationConfiguration(int width, int height) {
		this.width = width;
		this.height = height;
	}
}

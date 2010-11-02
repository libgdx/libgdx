
package com.badlogic.gdx.backends.desktop;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Canvas;

import com.badlogic.gdx.ApplicationListener;

public class LwjglApplet extends Applet {
	private LwjglCanvas lwjglCanvas;

	public LwjglApplet (ApplicationListener listener, boolean useGL2) {
		lwjglCanvas = new LwjglCanvas(listener, useGL2);
		Canvas canvas = lwjglCanvas.getCanvas();
		setLayout(new BorderLayout());
		add(canvas);
		canvas.setFocusable(true);
		canvas.requestFocus();
		setVisible(true);
	}

	public void destroy () {
		remove(lwjglCanvas.getCanvas());
		super.destroy();
	}
}

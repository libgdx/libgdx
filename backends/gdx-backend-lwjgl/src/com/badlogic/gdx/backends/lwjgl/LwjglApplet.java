
package com.badlogic.gdx.backends.desktop;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Canvas;

import com.badlogic.gdx.ApplicationListener;

public class LwjglApplet extends Applet {
	final Canvas canvas;
	LwjglApplication app;

	public LwjglApplet (final ApplicationListener listener, final boolean useGL2) {
		canvas = new Canvas() {
			public final void addNotify () {
				super.addNotify();
				app = new LwjglApplication(listener, useGL2, canvas);
			}

			public final void removeNotify () {
				app.stop();
				super.removeNotify();
			}
		};
		setLayout(new BorderLayout());
		canvas.setIgnoreRepaint(true);
		add(canvas);
		canvas.setFocusable(true);
		canvas.requestFocus();
	}

	public void destroy () {
		remove(canvas);
		super.destroy();
	}
}


package com.badlogic.gdx.backends.lwjgl;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import com.badlogic.gdx.ApplicationListener;

/** Wraps an {@link LwjglCanvas} in a resizable {@link JFrame}. */
public class LwjglFrame extends JFrame {
	public LwjglFrame (ApplicationListener listener, String title, int width, int height, boolean useGL2) {
		super(title);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(width, height);
		setLocationRelativeTo(null);

		final LwjglCanvas lwjglCanvas = new LwjglCanvas(listener, useGL2);
		getContentPane().add(lwjglCanvas.getCanvas());

		addWindowListener(new WindowAdapter() {
			public void windowClosing (WindowEvent e) {
				lwjglCanvas.stop();
			}
		});
	}
}

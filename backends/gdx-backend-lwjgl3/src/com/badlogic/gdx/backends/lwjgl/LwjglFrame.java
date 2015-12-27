/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.backends.lwjgl;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Point;

import javax.swing.JFrame;

import com.badlogic.gdx.ApplicationListener;

/** Wraps an {@link LwjglCanvas} in a resizable {@link JFrame}. */
public class LwjglFrame extends JFrame {
	LwjglCanvas lwjglCanvas;
	private Thread shutdownHook;

	public LwjglFrame (ApplicationListener listener, String title, int width, int height) {
		super(title);
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = title;
		config.width = width;
		config.height = height;
		construct(listener, config);
	}

	public LwjglFrame (ApplicationListener listener, LwjglApplicationConfiguration config) {
		super(config.title);
		construct(listener, config);
	}

	private void construct (ApplicationListener listener, LwjglApplicationConfiguration config) {
		lwjglCanvas = new LwjglCanvas(listener, config) {
			protected void stopped () {
				LwjglFrame.this.dispose();
			}

			protected void setTitle (String title) {
				LwjglFrame.this.setTitle(title);
			}

			protected void setDisplayMode (int width, int height) {
				LwjglFrame.this.getContentPane().setPreferredSize(new Dimension(width, height));
				LwjglFrame.this.getContentPane().invalidate();
				LwjglFrame.this.pack();
				LwjglFrame.this.setLocationRelativeTo(null);
				updateSize(width, height);
			}

			protected void resize (int width, int height) {
				updateSize(width, height);
			}

			protected void start () {
				LwjglFrame.this.start();
			}

			protected void exception (Throwable t) {
				LwjglFrame.this.exception(t);
			}

			protected int getFrameRate () {
				int frameRate = LwjglFrame.this.getFrameRate();
				return frameRate == 0 ? super.getFrameRate() : frameRate;
			}
		};

		setHaltOnShutdown(true);

		setDefaultCloseOperation(EXIT_ON_CLOSE);
		getContentPane().setPreferredSize(new Dimension(config.width, config.height));

		initialize();
		pack();
		Point location = getLocation();
		if (location.x == 0 && location.y == 0) setLocationRelativeTo(null);
		lwjglCanvas.getCanvas().setSize(getSize());

		// Finish with invokeLater so any LwjglFrame super constructor has a chance to initialize.
		EventQueue.invokeLater(new Runnable() {
			public void run () {
				addCanvas();
				setVisible(true);
				lwjglCanvas.getCanvas().requestFocus();
			}
		});
	}

	/** When true, <code>Runtime.getRuntime().halt(0);</code> is used when the JVM shuts down. This prevents Swing shutdown hooks
	 * from causing a deadlock and keeping the JVM alive indefinitely. Default is true. */
	public void setHaltOnShutdown (boolean halt) {
		if (halt) {
			if (shutdownHook != null) return;
			shutdownHook = new Thread() {
				public void run () {
					Runtime.getRuntime().halt(0); // Because fuck you, deadlock causing Swing shutdown hooks.
				}
			};
			Runtime.getRuntime().addShutdownHook(shutdownHook);
		} else if (shutdownHook != null) {
			Runtime.getRuntime().removeShutdownHook(shutdownHook);
			shutdownHook = null;
		}
	}

	protected int getFrameRate () {
		return 0;
	}

	protected void exception (Throwable ex) {
		ex.printStackTrace();
		lwjglCanvas.stop();
	}

	/** Called before the JFrame is made displayable. */
	protected void initialize () {
	}

	/** Adds the canvas to the content pane. This triggers addNotify and starts the canvas' game loop. */
	protected void addCanvas () {
		getContentPane().add(lwjglCanvas.getCanvas());
	}

	/** Called after {@link ApplicationListener} create and resize, but before the game loop iteration. */
	protected void start () {
	}

	/** Called when the canvas size changes. */
	public void updateSize (int width, int height) {
	}

	public LwjglCanvas getLwjglCanvas () {
		return lwjglCanvas;
	}
}

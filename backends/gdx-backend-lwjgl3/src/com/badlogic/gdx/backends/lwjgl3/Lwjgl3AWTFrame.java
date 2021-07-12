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

package com.badlogic.gdx.backends.lwjgl3;

import com.badlogic.gdx.ApplicationListener;

import javax.swing.JFrame;
import java.awt.Dimension;

/** Wraps an {@link Lwjgl3AWTCanvas} in a resizable {@link JFrame}. */
public class Lwjgl3AWTFrame extends JFrame {
	final Lwjgl3AWTCanvas lwjglAWTCanvas;
	private Thread shutdownHook;

	public Lwjgl3AWTFrame (ApplicationListener listener, String title, int width, int height) {
		super(title);

		lwjglAWTCanvas = new Lwjgl3AWTCanvas(listener) {
			protected void stopped () {
				Lwjgl3AWTFrame.this.dispose();
			}

			protected void setTitle (String title) {
				Lwjgl3AWTFrame.this.setTitle(title);
			}

			protected void setDisplayMode (int width, int height) {
				Lwjgl3AWTFrame.this.getContentPane().setPreferredSize(new Dimension(width, height));
				Lwjgl3AWTFrame.this.getContentPane().invalidate();
				Lwjgl3AWTFrame.this.pack();
				Lwjgl3AWTFrame.this.setLocationRelativeTo(null);
				updateSize(width, height);
			}

			protected void resize (int width, int height) {
				updateSize(width, height);
			}

			protected void start () {
				Lwjgl3AWTFrame.this.start();
			}
		};
		getContentPane().add(lwjglAWTCanvas.getCanvas());

		setHaltOnShutdown(true);

		setDefaultCloseOperation(EXIT_ON_CLOSE);
		getContentPane().setPreferredSize(new Dimension(width, height));
		initialize();
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
		lwjglAWTCanvas.getCanvas().requestFocus();
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

	/** Called before the JFrame is shown. */
	protected void initialize () {
	}

	/** Called after {@link ApplicationListener} create and resize, but before the game loop iteration. */
	protected void start () {
	}

	/** Called when the canvas size changes. */
	public void updateSize (int width, int height) {
	}

	public Lwjgl3AWTCanvas getLwjglAWTCanvas () {
		return lwjglAWTCanvas;
	}
}

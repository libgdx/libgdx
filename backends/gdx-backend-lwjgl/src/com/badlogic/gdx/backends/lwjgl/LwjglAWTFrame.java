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

import com.badlogic.gdx.ApplicationListener;

import java.awt.Dimension;

import javax.swing.JFrame;

/** Wraps an {@link LwjglAWTCanvas} in a resizable {@link JFrame}. */
public class LwjglAWTFrame extends JFrame {
	final LwjglAWTCanvas lwjglAWTCanvas;

	public LwjglAWTFrame (ApplicationListener listener, String title, int width, int height) {
		super(title);

		lwjglAWTCanvas = new LwjglAWTCanvas(listener) {
			protected void stopped () {
				LwjglAWTFrame.this.dispose();
			}

			protected void setTitle (String title) {
				LwjglAWTFrame.this.setTitle(title);
			}

			protected void setDisplayMode (int width, int height) {
				LwjglAWTFrame.this.getContentPane().setPreferredSize(new Dimension(width, height));
				LwjglAWTFrame.this.getContentPane().invalidate();
				LwjglAWTFrame.this.pack();
				LwjglAWTFrame.this.setLocationRelativeTo(null);
				updateSize(width, height);
			}

			protected void resize (int width, int height) {
				updateSize(width, height);
			}

			protected void start () {
				LwjglAWTFrame.this.start();
			}
		};
		getContentPane().add(lwjglAWTCanvas.getCanvas());

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run () {
				Runtime.getRuntime().halt(0); // Because fuck you, deadlock causing Swing shutdown hooks.
			}
		});

		setDefaultCloseOperation(EXIT_ON_CLOSE);
		getContentPane().setPreferredSize(new Dimension(width, height));
		initialize();
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
		lwjglAWTCanvas.getCanvas().requestFocus();
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

	public LwjglAWTCanvas getLwjglAWTCanvas () {
		return lwjglAWTCanvas;
	}
}

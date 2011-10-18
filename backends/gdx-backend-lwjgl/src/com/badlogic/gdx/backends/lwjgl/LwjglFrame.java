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

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import com.badlogic.gdx.ApplicationListener;

/** Wraps an {@link LwjglCanvas} in a resizable {@link JFrame}. */
public class LwjglFrame extends JFrame {
	final LwjglCanvas lwjglCanvas;

	public LwjglFrame (ApplicationListener listener, String title, int width, int height, boolean useGL2) {
		super(title);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(width, height);
		setLocationRelativeTo(null);

		lwjglCanvas = new LwjglCanvas(listener, useGL2) {
			protected void stopped () {
				LwjglFrame.this.dispose();
			}
		};
		getContentPane().add(lwjglCanvas.getCanvas());

		addWindowListener(new WindowAdapter() {
			public void windowClosing (WindowEvent e) {
				lwjglCanvas.stop();
			}
		});

		setVisible(true);
	}

	public LwjglCanvas getLwjglCanvas () {
		return lwjglCanvas;
	}
}

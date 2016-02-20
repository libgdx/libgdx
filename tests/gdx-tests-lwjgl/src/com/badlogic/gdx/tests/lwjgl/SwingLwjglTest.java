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

package com.badlogic.gdx.tests.lwjgl;

import java.awt.BorderLayout;
import java.awt.Container;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglAWTCanvas;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.tests.AnimationTest;
import com.badlogic.gdx.tests.MusicTest;
import com.badlogic.gdx.tests.UITest;

/** Demonstrates how to use LwjglAWTCanvas to have multiple GL widgets in a Swing application.
 * @author mzechner */
public class SwingLwjglTest extends JFrame {
	LwjglAWTCanvas canvas1;
	LwjglAWTCanvas canvas2;
	LwjglAWTCanvas canvas3;

	public SwingLwjglTest () {
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		Container container = getContentPane();
		canvas1 = new LwjglAWTCanvas(new MusicTest());
		canvas2 = new LwjglAWTCanvas(new UITest(), canvas1);
		canvas3 = new LwjglAWTCanvas(new WindowCreator(), canvas1);

		canvas1.getCanvas().setSize(200, 480);
		canvas2.getCanvas().setSize(200, 480);
		canvas3.getCanvas().setSize(200, 480);

		container.add(canvas1.getCanvas(), BorderLayout.LINE_START);
		container.add(canvas2.getCanvas(), BorderLayout.CENTER);
		container.add(canvas3.getCanvas(), BorderLayout.LINE_END);

		pack();
		setVisible(true);
		setSize(800, 480);
	}

	class WindowCreator extends ApplicationAdapter {
		SpriteBatch batch;
		BitmapFont font;

		@Override
		public void create () {
			batch = new SpriteBatch();
			font = new BitmapFont();
		}

		@Override
		public void dispose () {
			font.dispose();
			batch.dispose();
		}

		@Override
		public void render () {
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
			batch.begin();
			font.draw(batch, "Click to create a new window", 10, 100);
			batch.end();

			if (Gdx.input.justTouched()) {
				createWindow();
			}
		}

		private void createWindow () {
			JFrame window = new JFrame();
			LwjglAWTCanvas canvas = new LwjglAWTCanvas(new UITest(), canvas1);
			window.getContentPane().add(canvas.getCanvas(), BorderLayout.CENTER);
			window.pack();
			window.setVisible(true);
			window.setSize(200, 200);
		}
	}

	public static void main (String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run () {
				new SwingLwjglTest();
			}
		});
	}

	@Override
	public void dispose() {
		canvas3.stop();
		canvas2.stop();
		canvas1.stop();

		super.dispose();
	}
}

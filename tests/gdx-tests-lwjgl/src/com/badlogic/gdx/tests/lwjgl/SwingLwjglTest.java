package com.badlogic.gdx.tests.lwjgl;

import java.awt.BorderLayout;
import java.awt.Container;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglAWTCanvas;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.tests.MusicTest;
import com.badlogic.gdx.tests.ObjTest;
import com.badlogic.gdx.tests.UITest;

/**
 * Demonstrates how to use LwjglAWTCanvas to have multiple GL widgets in a
 * Swing application.
 * @author mzechner
 *
 */
public class SwingLwjglTest extends JFrame {
	LwjglAWTCanvas canvas1;
	
	public SwingLwjglTest() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		Container container = getContentPane();
		canvas1 = new LwjglAWTCanvas(new MusicTest(), false);
		LwjglAWTCanvas canvas2 = new LwjglAWTCanvas(new UITest(), false, canvas1);
		LwjglAWTCanvas canvas3 = new LwjglAWTCanvas(new WindowCreator(), false, canvas1);
		
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
	
	private class WindowCreator extends ApplicationAdapter {
		SpriteBatch batch;
		BitmapFont font;

		@Override
		public void create () {
			batch = new SpriteBatch();
			font = new BitmapFont();
		}

		@Override
		public void render () {
			Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
			batch.begin();
			font.draw(batch, "Click to create a new window", 10, 100);
			batch.end();
			
			if(Gdx.input.justTouched()) {
				createWindow();
			}
		}
		
		private void createWindow() {
			JFrame window = new JFrame();
			LwjglAWTCanvas canvas = new LwjglAWTCanvas(new ObjTest(), false, canvas1);
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
}

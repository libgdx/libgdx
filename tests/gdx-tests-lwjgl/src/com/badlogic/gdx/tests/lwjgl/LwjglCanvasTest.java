package com.badlogic.gdx.tests.lwjgl;

import java.awt.BorderLayout;
import java.awt.Container;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglAWTCanvas;
import com.badlogic.gdx.backends.lwjgl.LwjglCanvas;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.tests.MusicTest;
import com.badlogic.gdx.tests.ObjTest;
import com.badlogic.gdx.tests.UITest;
import com.badlogic.gdx.tests.lwjgl.SwingLwjglTest.WindowCreator;

public class LwjglCanvasTest extends JFrame {
	LwjglCanvas canvas;

	public LwjglCanvasTest () {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		Container container = getContentPane();
		canvas = new LwjglCanvas(new UITest(), false);

		container.add(canvas.getCanvas(), BorderLayout.CENTER);

		pack();
		setVisible(true);
		setSize(800, 480);
	}

	public static void main (String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run () {
				new LwjglCanvasTest();
			}
		});
	}
}

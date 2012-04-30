package com.badlogic.gdx.tests.lwjgl;

import java.awt.BorderLayout;
import java.awt.Container;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.backends.lwjgl.LwjglAWTCanvas;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g3d.loaders.ModelLoaderOld;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.tests.ObjTest;
import com.badlogic.gdx.tests.SpriteBatchTest;
import com.badlogic.gdx.tests.UITest;

/**
 * Demonstrates how to use LwjglAWTCanvas to have multiple GL widgets in a
 * Swing application.
 * @author mzechner
 *
 */
public class SwingLwjglTest extends JFrame {
	public SwingLwjglTest() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		Container container = getContentPane();
		LwjglAWTCanvas canvas1 = new LwjglAWTCanvas(new ObjTest(), false);
		LwjglAWTCanvas canvas2 = new LwjglAWTCanvas(new UITest(), false, canvas1);
		LwjglAWTCanvas canvas3 = new LwjglAWTCanvas(new SpriteBatchTest(), false, canvas1);
		
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
	
	public static void main (String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run () {
				new SwingLwjglTest();
			}
		});
	}
}

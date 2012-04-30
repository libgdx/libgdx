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
		LwjglAWTCanvas canvas1 = new LwjglAWTCanvas(new GlListener(), false);
		LwjglAWTCanvas canvas2 = new LwjglAWTCanvas(new GlListener(), false, canvas1);
		LwjglAWTCanvas canvas3 = new LwjglAWTCanvas(new GlListener(), false, canvas1);
		
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
	
	private class GlListener extends ApplicationAdapter implements InputProcessor {
		PerspectiveCamera cam;
		Mesh mesh;
		Texture texture;
		float angleY = 0;
		float angleX = 0;
		float[] lightColor = {1, 1, 1, 0};
		float[] lightPosition = {2, 5, 10, 0};
		float touchStartX = 0;
		float touchStartY = 0;

		@Override
		public void create () {
			mesh = ModelLoaderOld.loadObj(Gdx.files.internal("data/cube.obj").read());
			Gdx.app.log("ObjTest", "obj bounds: " + mesh.calculateBoundingBox());
			texture = new Texture(Gdx.files.internal("data/badlogic.jpg"), true);
			texture.setFilter(TextureFilter.MipMap, TextureFilter.Linear);

			cam = new PerspectiveCamera(45, 4, 4);
			cam.position.set(3, 3, 3);
			cam.direction.set(-1, -1, -1);
			Gdx.input.setInputProcessor(this);
		}

		@Override
		public void render () {
			GL10 gl = Gdx.graphics.getGL10();

			gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
			gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
			gl.glEnable(GL10.GL_DEPTH_TEST);
			gl.glEnable(GL10.GL_LIGHTING);
			gl.glEnable(GL10.GL_COLOR_MATERIAL);
			gl.glEnable(GL10.GL_TEXTURE_2D);

			cam.update();
			gl.glMatrixMode(GL10.GL_PROJECTION);
			gl.glLoadIdentity();
	// Gdx.graphics.getGLU().gluPerspective(Gdx.gl10, 45, 1, 1, 100);
			gl.glLoadMatrixf(cam.projection.val, 0);
			gl.glMatrixMode(GL10.GL_MODELVIEW);
			gl.glLoadMatrixf(cam.view.val, 0);

			gl.glEnable(GL10.GL_LIGHT0);
			gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_DIFFUSE, lightColor, 0);
			gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, lightPosition, 0);

			gl.glRotatef(angleY, 0, 1, 0);
			gl.glRotatef(angleX, 1, 0, 0);
			texture.bind();
			mesh.render(GL10.GL_TRIANGLES);
		}

		@Override
		public boolean keyDown (int keycode) {
			return false;
		}

		@Override
		public boolean keyTyped (char character) {
			return false;
		}

		@Override
		public boolean keyUp (int keycode) {
			return false;
		}

		@Override
		public boolean touchDown (int x, int y, int pointer, int newParam) {
			touchStartX = x;
			touchStartY = y;
			return false;
		}

		@Override
		public boolean touchDragged (int x, int y, int pointer) {
			angleY += (x - touchStartX);
			angleX += (y - touchStartY);
			touchStartX = x;
			touchStartY = y;
			return false;
		}

		@Override
		public boolean touchUp (int x, int y, int pointer, int button) {
			return false;
		}

		@Override
		public boolean touchMoved (int x, int y) {
			return false;
		}

		@Override
		public boolean scrolled (int amount) {
			return false;
		}
	}
}

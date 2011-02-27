
package com.badlogic.gdx.backends.jogl;

import javax.media.opengl.glu.GLU;

import com.badlogic.gdx.graphics.GL10;

public class JoglGLU implements com.badlogic.gdx.graphics.GLU {
	GLU glu = new GLU();
	double modeld[] = new double[16];
	double projectd[] = new double[16];
	double wind[] = new double[3];

	@Override public void gluLookAt (GL10 gl, float eyeX, float eyeY, float eyeZ, float centerX, float centerY, float centerZ,
		float upX, float upY, float upZ) {
		glu.gluLookAt(eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ);
	}

	@Override public void gluOrtho2D (GL10 gl, float left, float right, float bottom, float top) {
		glu.gluOrtho2D(left, right, bottom, top);
	}

	@Override public void gluPerspective (GL10 gl, float fovy, float aspect, float zNear, float zFar) {
		glu.gluPerspective(fovy, aspect, zNear, zFar);
	}

	@Override public boolean gluProject (float objX, float objY, float objZ, float[] model, int modelOffset, float[] project,
		int projectOffset, int[] view, int viewOffset, float[] win, int winOffset) {
		for (int i = 0; i < 16; i++) {
			modeld[i] = model[modelOffset + i];
			projectd[i] = project[projectOffset + i];
		}
		boolean result = glu.gluProject(objX, objY, objZ, modeld, 0, projectd, 0, view, 0, wind, 0);
		win[winOffset] = (float)wind[0];
		win[winOffset + 1] = (float)wind[1];
		win[winOffset + 2] = (float)wind[2];
		return result;
	}

	@Override public boolean gluUnProject (float winX, float winY, float winZ, float[] model, int modelOffset, float[] project,
		int projectOffset, int[] view, int viewOffset, float[] obj, int objOffset) {
		for (int i = 0; i < 16; i++) {
			modeld[i] = model[modelOffset + i];
			projectd[i] = project[projectOffset + i];
		}
		boolean result = glu.gluUnProject(winX, winY, winZ, modeld, 0, projectd, 0, view, 0, wind, 0);
		obj[objOffset] = (float)wind[0];
		obj[objOffset + 1] = (float)wind[1];
		obj[objOffset + 2] = (float)wind[2];
		return result;
	}

}

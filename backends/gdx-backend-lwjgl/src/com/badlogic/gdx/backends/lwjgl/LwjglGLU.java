package com.badlogic.gdx.backends.lwjgl;

import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GLU;

public class LwjglGLU implements GLU {

	@Override public void gluLookAt (GL10 gl, float eyeX, float eyeY, float eyeZ, float centerX, float centerY, float centerZ,
		float upX, float upY, float upZ) {
		throw new UnsupportedOperationException();
	}

	@Override public void gluOrtho2D (GL10 gl, float left, float right, float bottom, float top) {
		throw new UnsupportedOperationException();
	}

	@Override public void gluPerspective (GL10 gl, float fovy, float aspect, float zNear, float zFar) {
		throw new UnsupportedOperationException();
	}

	@Override public boolean gluProject (float objX, float objY, float objZ, float[] model, int modelOffset, float[] project,
		int projectOffset, int[] view, int viewOffset, float[] win, int winOffset) {
		throw new UnsupportedOperationException();
//		return false;
	}

	@Override public boolean gluUnProject (float winX, float winY, float winZ, float[] model, int modelOffset, float[] project,
		int projectOffset, int[] view, int viewOffset, float[] obj, int objOffset) {
		throw new UnsupportedOperationException();
//		return false;
	}

}

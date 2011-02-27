package com.badlogic.gdx.backends.android;

import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GLU;

public class AndroidGLU implements GLU {	
	
	@Override public void gluLookAt (GL10 gl, float eyeX, float eyeY, float eyeZ, float centerX, float centerY, float centerZ,
		float upX, float upY, float upZ) {
		android.opengl.GLU.gluLookAt(((AndroidGL10)gl).gl, eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ);
	}

	@Override public void gluOrtho2D (GL10 gl, float left, float right, float bottom, float top) {
		android.opengl.GLU.gluOrtho2D(((AndroidGL10)gl).gl, left, right, bottom, top);
	}

	@Override public void gluPerspective (GL10 gl, float fovy, float aspect, float zNear, float zFar) {
		android.opengl.GLU.gluPerspective(((AndroidGL10)gl).gl, fovy, aspect, zNear, zFar);
	}

	@Override public boolean gluProject (float objX, float objY, float objZ, float[] model, int modelOffset, float[] project,
		int projectOffset, int[] view, int viewOffset, float[] win, int winOffset) {
		int result = android.opengl.GLU.gluProject(objX, objY, objZ, model, modelOffset, project, projectOffset, view, viewOffset, win, winOffset);
		return result == GL10.GL_TRUE;
	}

	@Override public boolean gluUnProject (float winX, float winY, float winZ, float[] model, int modelOffset, float[] project,
		int projectOffset, int[] view, int viewOffset, float[] obj, int objOffset) {
		int result = android.opengl.GLU.gluUnProject(winX, winY, winZ, model, modelOffset, project, projectOffset, view, viewOffset, obj, objOffset);
		return result == GL10.GL_TRUE;
	}

}

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

package com.badlogic.gdx.backends.angle;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GLU;
import com.badlogic.gdx.utils.BufferUtils;

public class AngleGLU implements GLU {

	FloatBuffer modelb;
	FloatBuffer projectb;
	IntBuffer viewb;
	FloatBuffer winb;

	public AngleGLU () {
		modelb = BufferUtils.newFloatBuffer(16);
		projectb = BufferUtils.newFloatBuffer(16);
		viewb = BufferUtils.newIntBuffer(4);
		winb = BufferUtils.newFloatBuffer(3);
	}

	@Override
	public void gluLookAt (GL10 gl, float eyeX, float eyeY, float eyeZ, float centerX, float centerY, float centerZ, float upX,
		float upY, float upZ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void gluOrtho2D (GL10 gl, float left, float right, float bottom, float top) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void gluPerspective (GL10 gl, float fovy, float aspect, float zNear, float zFar) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean gluProject (float objX, float objY, float objZ, float[] model, int modelOffset, float[] project,
		int projectOffset, int[] view, int viewOffset, float[] win, int winOffset) {
		modelb.clear();
		modelb.put(model, modelOffset, 16);
		projectb.clear();
		projectb.put(project, projectOffset, 16);
		viewb.clear();
		viewb.put(view, viewOffset, 4);
		winb.clear();

		boolean result = org.lwjgl.util.glu.GLU.gluProject(objX, objY, objZ, modelb, projectb, viewb, winb);
		win[winOffset] = winb.get(0);
		win[winOffset + 1] = winb.get(1);
		win[winOffset + 2] = winb.get(2);
		return result;
	}

	@Override
	public boolean gluUnProject (float winX, float winY, float winZ, float[] model, int modelOffset, float[] project,
		int projectOffset, int[] view, int viewOffset, float[] obj, int objOffset) {
		modelb.clear();
		modelb.put(model, modelOffset, 16);
		projectb.clear();
		projectb.put(project, projectOffset, 16);
		viewb.clear();
		viewb.put(view, viewOffset, 4);
		winb.clear();

		boolean result = org.lwjgl.util.glu.GLU.gluUnProject(winX, winY, winZ, modelb, projectb, viewb, winb);
		obj[objOffset] = winb.get(0);
		obj[objOffset + 1] = winb.get(1);
		obj[objOffset + 2] = winb.get(2);
		return result;
	}
}

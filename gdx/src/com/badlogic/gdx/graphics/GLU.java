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

package com.badlogic.gdx.graphics;

/** OpenGL utility class.
 * @author mzechner */
public interface GLU {
	public void gluLookAt (GL10 gl, float eyeX, float eyeY, float eyeZ, float centerX, float centerY, float centerZ, float upX,
		float upY, float upZ);

	public void gluOrtho2D (GL10 gl, float left, float right, float bottom, float top);

	public void gluPerspective (GL10 gl, float fovy, float aspect, float zNear, float zFar);

	public boolean gluProject (float objX, float objY, float objZ, float[] model, int modelOffset, float[] project,
		int projectOffset, int[] view, int viewOffset, float[] win, int winOffset);

	public boolean gluUnProject (float winX, float winY, float winZ, float[] model, int modelOffset, float[] project,
		int projectOffset, int[] view, int viewOffset, float[] obj, int objOffset);
}

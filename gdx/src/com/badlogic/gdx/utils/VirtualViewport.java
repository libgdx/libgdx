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

package com.badlogic.gdx.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.scenes.scene2d.Stage;

/** This is used to work with a fixed virtual viewport. It implements "letterboxing" which means that it will maintain the aspect
 * ratio of the virtual viewport while scaling it to fit the screen. It has methods to help with setting the correct OpenGL
 * viewport, as well as dealing with {@code Stage}s and {@code Camera}s viewports.
 * 
 * @author Daniel Holderbaum */
public class VirtualViewport {

	private int virtualWidth;

	private int virtualHeight;

	private int viewportWidth;

	private int viewportHeight;

	private int viewportX;

	private int viewportY;

	/** Initializes this virtual viewport.
	 * 
	 * @param virtualWidth The constant width of this viewport.
	 * @param virtualHeight The constant height of this viewport. */
	public VirtualViewport (int virtualWidth, int virtualHeight) {
		this.virtualWidth = virtualWidth;
		this.virtualHeight = virtualHeight;
		resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}

	/** Call this method on each {@code ApplicationListener.resize()} or {@code Screen.resize()}.
	 * 
	 * @param width The real screen width.
	 * @param height The real screen height. */
	public void resize (int width, int height) {
		Vector2 scaled = Scaling.fit.apply(virtualWidth, virtualHeight, width, height);
		viewportWidth = Math.round(scaled.x);
		viewportHeight = Math.round(scaled.y);
		// center the viewport in the middle of the screen
		viewportX = (width - viewportWidth) / 2;
		viewportY = (height - viewportHeight) / 2;
	}

	/** Call this method on each {@code ApplicationListener.resize()} or {@code Screen.resize()}. It centers the OpenGL viewport in
	 * the middle of the window and will add bars to the border of the window, if necessary. */
	public void setOpenGLViewport () {
		Gdx.gl.glViewport(viewportX, viewportY, viewportWidth, viewportHeight);
	}

	/** Call this method on each {@code ApplicationListener.resize()} or {@code Screen.resize()}, in case the stage should have the
	 * same viewport. */
	public void setStageViewport (Stage stage) {
		stage.setViewport(virtualWidth, virtualHeight, true, viewportX, viewportY, viewportWidth, viewportHeight);
	}

	public void unproject (Camera camera, Vector3 vec) {
		camera.unproject(vec, viewportX, viewportY, viewportWidth, viewportHeight);
	}

	public void project (Camera camera, Vector3 vec) {
		camera.project(vec, viewportX, viewportY, viewportWidth, viewportHeight);
	}

	public Ray getPickRay (Camera camera, float x, float y) {
		return camera.getPickRay(x, y, viewportX, viewportY, viewportWidth, viewportHeight);
	}

	public int getVirtualWidth () {
		return virtualWidth;
	}

	public int getVirtualHeight () {
		return virtualHeight;
	}

	public int getViewportWidth () {
		return viewportWidth;
	}

	public int getViewportHeight () {
		return viewportHeight;
	}

	public int getViewportX () {
		return viewportX;
	}

	public int getViewportY () {
		return viewportY;
	}

}

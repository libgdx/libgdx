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

package com.badlogic.gdx.utils.viewport;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;

/** It has methods to help with setting the correct OpenGL viewport, as well as dealing with {@code Stage}s and {@code Camera}s
 * viewports.
 * @author Daniel Holderbaum
 * @author Nathan Sweet */
public abstract class Viewport {
	/** The virtual size of this viewport, which is scaled to the viewport size. */
	protected float worldWidth, worldHeight;

	/** The viewport size. */
	protected int viewportWidth, viewportHeight;

	/** The viewport's offset from the bottom-left corner of the window. */
	protected int viewportX, viewportY;

	/** The managed camera. */
	protected Camera camera;

	private final Vector3 tmp = new Vector3();

	/** Updates the viewport's camera. Typically called from {@code ApplicationListener#resize(int, int)} or
	 * {@code Screen#resize(int, int)}.
	 * <p>
	 * The default implementation calls {@link GL20#glViewport(int, int, int, int)} and configures the camera viewport size and
	 * position. */
	public void update (int screenWidth, int screenHeight) {
		Gdx.gl.glViewport(viewportX, viewportY, viewportWidth, viewportHeight);
		camera.viewportWidth = worldWidth;
		camera.viewportHeight = worldHeight;
		camera.position.set(worldWidth / 2, worldHeight / 2, 0);
		camera.update();
	}

	/** @see Camera#unproject(Vector3) */
	public void unproject (Vector2 screenCoords) {
		tmp.set(screenCoords.x, screenCoords.y, 1);
		camera.unproject(tmp, viewportX, viewportY, viewportWidth, viewportHeight);
		screenCoords.set(tmp.x, tmp.y);
	}

	/** @see Camera#project(Vector3) */
	public void project (Vector2 worldCoords) {
		tmp.set(worldCoords.x, worldCoords.y, 1);
		camera.project(tmp, viewportX, viewportY, viewportWidth, viewportHeight);
		worldCoords.set(tmp.x, tmp.y);
	}

	/** @see Camera#unproject(Vector3) */
	public void unproject (Vector3 screenCoords) {
		camera.unproject(screenCoords, viewportX, viewportY, viewportWidth, viewportHeight);
	}

	/** @see Camera#project(Vector3) */
	public void project (Vector3 worldCoords) {
		camera.project(worldCoords, viewportX, viewportY, viewportWidth, viewportHeight);
	}

	/** @see Camera#getPickRay(float, float, float, float, float, float) */
	public Ray getPickRay (float screenX, float screenY) {
		return camera.getPickRay(screenX, screenY, viewportX, viewportY, viewportWidth, viewportHeight);
	}

	/** @see ScissorStack#calculateScissors(Camera, float, float, float, float, Matrix4, Rectangle, Rectangle) */
	public void calculateScissors (Matrix4 batchTransform, Rectangle area, Rectangle scissor) {
		ScissorStack.calculateScissors(camera, viewportX, viewportY, viewportWidth, viewportHeight, batchTransform, area, scissor);
	}

	/** Transforms a point to real screen coordinates (as oposed to OpenGL ES window coordinates), where the origin is in the top
	 * left and the the y-axis is pointing downwards. */
	public Vector2 toScreenCoordinates (Vector2 worldCoords, Matrix4 transformMatrix) {
		tmp.set(worldCoords.x, worldCoords.y, 0);
		tmp.mul(transformMatrix);
		camera.project(tmp);
		tmp.y = Gdx.graphics.getHeight() - tmp.y;
		worldCoords.x = tmp.x;
		worldCoords.y = tmp.y;
		return worldCoords;
	}

	public Camera getCamera () {
		return camera;
	}

	public void setCamera (Camera camera) {
		this.camera = camera;
	}

	public float getWorldWidth () {
		return worldWidth;
	}

	public void setWorldWidth (float worldWidth) {
		this.worldWidth = worldWidth;
	}

	public float getWorldHeight () {
		return worldHeight;
	}

	public void setWorldHeight (float worldHeight) {
		this.worldHeight = worldHeight;
	}

	public int getViewportX () {
		return viewportX;
	}

	public int getViewportY () {
		return viewportY;
	}

	public int getViewportWidth () {
		return viewportWidth;
	}

	public int getViewportHeight () {
		return viewportHeight;
	}
}

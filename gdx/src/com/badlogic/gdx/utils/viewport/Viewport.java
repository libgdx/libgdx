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
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;

/** It has methods to help with setting the correct OpenGL viewport, as well as dealing with {@code Stage}s and {@code Camera}s
 * viewports.
 * 
 * @author Daniel Holderbaum */
public abstract class Viewport {

	/** The virtual width of this viewport which is used and might get scaled to viewportWidth. */
	public float virtualWidth;

	/** The virtual height of this viewport which is used and might get scaled to viewportHeight. */
	public float virtualHeight;

	/** The viewport's width. This should only be accessed directly, but not changed. */
	public int viewportWidth;

	/** The viewport's height. This should only be accessed directly, but not changed. */
	public int viewportHeight;

	/** The viewport's offset on the X-axis from the bottom-left corner of the window. This should only be accessed directly, but
	 * not changed. */
	public int viewportX;

	/** The viewport's offset on the Y-axis from the bottom-left corner of the window. This should only be accessed directly, but
	 * not changed. */
	public int viewportY;

	private boolean disabled;

	/** The managed camera. */
	public Camera camera;

	/** Call this method on each {@code ApplicationListener.resize()} or {@code Screen.resize()}.
	 * 
	 * @param width The real screen width.
	 * @param height The real screen height. */
	public void update (int width, int height) {
		calculateViewport(width, height);

		if (!disabled) {
			Gdx.gl.glViewport(viewportX, viewportY, viewportWidth, viewportHeight);

			if (camera != null) {
				camera.viewportWidth = virtualWidth;
				camera.viewportHeight = virtualHeight;
				camera.update();
			}
		}
	}

	/** Disables this viewport and allows to render in the area of the black bars. The width of the bars is determined by
	 * {@code Viewport.getViewportX()}, the height by {@code Viewport.getViewportY()}. */
	public void disable () {
		disabled = true;
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		if (camera != null) {
			camera.viewportWidth = Gdx.graphics.getWidth();
			camera.viewportHeight = Gdx.graphics.getHeight();
			camera.update();
		}
	}

	/** Enables this viewport and makes it impossible to draw in the area of the black bars. The viewport is enabled by default. */
	public void enable () {
		disabled = false;
		update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}

	/** Calculates the viewport size and position.
	 * 
	 * @param width The real screen width.
	 * @param height The real screen height. */
	protected abstract void calculateViewport (int width, int height);

	/** Stages often have a Table as their root element. Those needs some special handling in many cases. That's why we have this
	 * helper to retrieve this Table.
	 * @param stage The stage with a possible Table.
	 * @return The Table or null in case there is none. */
	private Table getRootTable (Stage stage) {
		if (stage.getRoot().getChildren().size == 1 && stage.getRoot().getChildren().get(0) instanceof Table) {
			Table rootTable = (Table)stage.getRoot().getChildren().get(0);
			return rootTable;
		}
		return null;
	}

	/** This needs to be called after updating the viewport when a resize event occurs and a stage is being used.
	 * 
	 * @param stage The Stage whose camera is being managed. */
	public void updateStage (Stage stage) {
		stage.setViewport(virtualWidth, virtualHeight, viewportX, viewportY, viewportWidth, viewportHeight);

		Table root = getRootTable(stage);
		if (root != null) {
			root.setBounds(viewportX, viewportY, viewportWidth, viewportHeight);
			root.invalidate();
			stage.getCamera().position.set((viewportWidth / 2) + viewportX, (viewportHeight / 2) + viewportY, 0);
			stage.getCamera().update();
		} else {
			stage.getCamera().position.set(virtualWidth / 2, virtualHeight / 2, 0);
			stage.getCamera().update();
		}
	}

	private Vector3 tmp = new Vector3();

	/** See {@link Camera#unproject(Vector3)}. */
	public void unproject (Vector2 vec) {
		tmp.set(vec.x, vec.y, 1);
		camera.unproject(tmp, viewportX, viewportY, viewportWidth, viewportHeight);
		vec.set(tmp.x, tmp.y);
	}

	/** See {@link Camera#project(Vector3)}. */
	public void project (Vector2 vec) {
		tmp.set(vec.x, vec.y, 1);
		camera.project(tmp, viewportX, viewportY, viewportWidth, viewportHeight);
		vec.set(tmp.x, tmp.y);
	}

	/** See {@link Camera#unproject(Vector3)}. */
	public void unproject (Vector3 vec) {
		camera.unproject(vec, viewportX, viewportY, viewportWidth, viewportHeight);
	}

	/** See {@link Camera#project(Vector3)}. */
	public void project (Vector3 vec) {
		camera.project(vec, viewportX, viewportY, viewportWidth, viewportHeight);
	}

	/** See {@link Camera#getPickRay(float, float, float, float, float, float)}. */
	public Ray getPickRay (float x, float y) {
		return camera.getPickRay(x, y, viewportX, viewportY, viewportWidth, viewportHeight);
	}

	/** See {@link ScissorStack#calculateScissors(Camera, float, float, float, float, Matrix4, Rectangle, Rectangle)}. */
	public void calculateScissors (Matrix4 batchTransform, Rectangle area, Rectangle scissor) {
		ScissorStack.calculateScissors(camera, viewportX, viewportY, viewportWidth, viewportHeight, batchTransform, area, scissor);
	}

}

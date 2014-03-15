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
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ObjectSet;

/** It has methods to help with setting the correct OpenGL viewport, as well as dealing with {@code Stage}s and {@code Camera}s
 * viewports.
 * 
 * @author Daniel Holderbaum */
public abstract class Viewport {

	private boolean disabled;

	protected int virtualWidth;

	protected int virtualHeight;

	protected int viewportWidth;

	protected int viewportHeight;

	protected int viewportX;

	protected int viewportY;

	private ObjectSet<Camera> managedCameras = new ObjectSet<Camera>(2);

	private ObjectSet<Stage> managedStages = new ObjectSet<Stage>(2);

	/** Call this method on each {@code ApplicationListener.resize()} or {@code Screen.resize()}.
	 * 
	 * @param width The real screen width.
	 * @param height The real screen height. */
	public void update (int width, int height) {
		calculateViewport(width, height);

		if (!disabled) {
			Gdx.gl.glViewport(viewportX, viewportY, viewportWidth, viewportHeight);

			for (Camera camera : managedCameras) {
				camera.viewportWidth = virtualWidth;
				camera.viewportHeight = virtualHeight;
				camera.update();
			}

			for (Stage stage : managedStages) {
				update(stage);
			}
		}

	}

	/** Disables this viewport and allows to render in the area of the black bars. The width of the bars is determined by
	 * {@code Viewport.getViewportX()}, the height by {@code Viewport.getViewportY()}. */
	public void disable () {
		disabled = true;
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		for (Camera camera : managedCameras) {
			camera.viewportWidth = Gdx.graphics.getWidth();
			camera.viewportHeight = Gdx.graphics.getHeight();
			camera.update();
		}

		for (Stage stage : managedStages) {
			stage.setViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		}
	}

	/** Enables this viewport and makes it impossible to draw in the area of the black bars. The viewport is enabled by default. */
	public void enable () {
		disabled = false;
		update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}

	protected abstract void update (Stage stage);

	/** Make the given stage managed by this viewport. Every change on this viewport by resize will also result in a manipulation of
	 * the stage.
	 * @param stage The stage to manage. */
	public void manage (Stage stage) {
		managedStages.add(stage);
		update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}

	/** Make the given camera managed by this viewport. Every change on this viewport by resize will also result in a manipulation
	 * of the camera.
	 * @param stage The camera to manage. */
	public void manage (Camera camera) {
		managedCameras.add(camera);
		update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}

	/** Make the given stage managed by this viewport. The stage will not be changed anymore by this viewport.
	 * @param stage The stage to manage. */
	public void unmanage (Stage stage) {
		managedStages.remove(stage);
	}

	/** Make the given camera unmanaged by this viewport. The camera will not be changed anymore by this viewport.
	 * @param stage The camera to unmanage. */
	public void unmanage (Camera camera) {
		managedCameras.remove(camera);
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
	protected Table getRootTable (Stage stage) {
		if (stage.getRoot().getChildren().size == 1 && stage.getRoot().getChildren().get(0) instanceof Table) {
			Table rootTable = (Table)stage.getRoot().getChildren().get(0);
			return rootTable;
		}
		return null;
	}

	private Vector3 tmp = new Vector3();

	/** @link {@link Camera#unproject(Vector3)} */
	public void unproject (Camera camera, Vector2 vec) {
		tmp.set(vec.x, vec.y, 1);
		camera.unproject(tmp, viewportX, viewportY, viewportWidth, viewportHeight);
		vec.set(tmp.x, tmp.y);
	}

	/** @link {@link Camera#project(Vector3)} */
	public void project (Camera camera, Vector2 vec) {
		tmp.set(vec.x, vec.y, 1);
		camera.project(tmp, viewportX, viewportY, viewportWidth, viewportHeight);
		vec.set(tmp.x, tmp.y);
	}

	/** @link {@link Camera#unproject(Vector3)} */
	public void unproject (Camera camera, Vector3 vec) {
		camera.unproject(vec, viewportX, viewportY, viewportWidth, viewportHeight);
	}

	/** @link {@link Camera#project(Vector3)} */
	public void project (Camera camera, Vector3 vec) {
		camera.project(vec, viewportX, viewportY, viewportWidth, viewportHeight);
	}

	/** @link {@link Camera#getPickRay(float, float, float, float, float, float)} */
	public Ray getPickRay (Camera camera, float x, float y) {
		return camera.getPickRay(x, y, viewportX, viewportY, viewportWidth, viewportHeight);
	}

	/** Returns the width of this viewport. */
	public int getViewportWidth () {
		return viewportWidth;
	}

	/** Returns the height of this viewport. */
	public int getViewportHeight () {
		return viewportHeight;
	}

	/** Returns the offset on the X-axis of this viewport. */
	public int getViewportX () {
		return viewportX;
	}

	/** Returns the offset on the Y-axis of this viewport. */
	public int getViewportY () {
		return viewportY;
	}

}

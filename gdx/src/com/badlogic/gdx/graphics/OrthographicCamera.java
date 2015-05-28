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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

/** A camera with orthographic projection.
 * 
 * @author mzechner */
public class OrthographicCamera extends Camera {
	/** the zoom of the camera **/
	public float zoom = 1;

	public OrthographicCamera () {
		this.near = 0;
	}

	/** Constructs a new OrthographicCamera, using the given viewport width and height. For pixel perfect 2D rendering just supply
	 * the screen size, for other unit scales (e.g. meters for box2d) proceed accordingly. The camera will show the region
	 * [-viewportWidth/2, -(viewportHeight/2-1)] - [(viewportWidth/2-1), viewportHeight/2]
	 * @param viewportWidth the viewport width
	 * @param viewportHeight the viewport height */
	public OrthographicCamera (float viewportWidth, float viewportHeight) {
		this.viewportWidth = viewportWidth;
		this.viewportHeight = viewportHeight;
		this.near = 0;
		update();
	}

	private final Vector3 tmp = new Vector3();

	@Override
	public void update () {
		update(true);
	}

	@Override
	public void update (boolean updateFrustum) {
		projection.setToOrtho(zoom * -viewportWidth / 2, zoom * (viewportWidth / 2), zoom * -(viewportHeight / 2), zoom
			* viewportHeight / 2, near, far);
		view.setToLookAt(position, tmp.set(position).add(direction), up);
		combined.set(projection);
		Matrix4.mul(combined.val, view.val);

		if (updateFrustum) {
			invProjectionView.set(combined);
			Matrix4.inv(invProjectionView.val);
			frustum.update(invProjectionView);
		}
	}

	/** Sets this camera to an orthographic projection using a viewport fitting the screen resolution, centered at
	 * (Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2), with the y-axis pointing up or down.
	 * @param yDown whether y should be pointing down */
	public void setToOrtho (boolean yDown) {
		setToOrtho(yDown, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}

	/** Sets this camera to an orthographic projection, centered at (viewportWidth/2, viewportHeight/2), with the y-axis pointing up
	 * or down.
	 * @param yDown whether y should be pointing down.
	 * @param viewportWidth
	 * @param viewportHeight */
	public void setToOrtho (boolean yDown, float viewportWidth, float viewportHeight) {
		if (yDown) {
			up.set(0, -1, 0);
			direction.set(0, 0, 1);
		} else {
			up.set(0, 1, 0);
			direction.set(0, 0, -1);
		}
		position.set(zoom * viewportWidth / 2.0f, zoom * viewportHeight / 2.0f, 0);
		this.viewportWidth = viewportWidth;
		this.viewportHeight = viewportHeight;
		update();
	}

	/** Rotates the camera by the given angle around the direction vector. The direction and up vector will not be orthogonalized.
	 * @param angle */
	public void rotate (float angle) {
		rotate(direction, angle);
	}

	/** Moves the camera by the given amount on each axis.
	 * @param x the displacement on the x-axis
	 * @param y the displacement on the y-axis */
	public void translate (float x, float y) {
		translate(x, y, 0);
	}

	/** Moves the camera by the given vector.
	 * @param vec the displacement vector */
	public void translate (Vector2 vec) {
		translate(vec.x, vec.y, 0);
	}

	/** Function to translate a point given in screen coordinates to world space. It's the same as GLU gluUnProject, but does not
	* rely on OpenGL. The x- and y-coordinate of vec are assumed to be in screen coordinates (origin is the top left corner, y
	* pointing down, x pointing to the right) as reported by the touch methods in {@link Input}. A z-coordinate of 0 will return a
	* point on the near plane, a z-coordinate of 1 will return a point on the far plane. This method allows you to specify the
	* viewport position and dimensions in the coordinate system expected by {@link GL20#glViewport(int, int, int, int)}, with the
	* origin in the bottom left corner of the screen.
	* @param screenCoords the point in screen coordinates (origin top left)
	* @param viewportX the coordinate of the bottom left corner of the viewport in glViewport coordinates.
	* @param viewportY the coordinate of the bottom left corner of the viewport in glViewport coordinates.
	* @param viewportWidth the width of the viewport in pixels
	* @param viewportHeight the height of the viewport in pixels */
	@Override
	public Vector3 unproject (Vector3 screenCoords, float viewportX, float viewportY, float viewportWidth, float viewportHeight) {
		float x = screenCoords.x, y = screenCoords.y;
		x = x - viewportX;
		y = viewportHeight - y - 1;
		y = y - viewportY;
		screenCoords.x = (2 * x) / viewportWidth - 1;
		screenCoords.y = (2 * y) / viewportHeight - 1;
		screenCoords.z = 2 * screenCoords.z - 1;
		screenCoords.prj(invProjectionView);
		return screenCoords;
	}

	/** Function to translate a point given in screen coordinates to world space. It's the same as GLU gluUnProject but does not
	* rely on OpenGL. The viewport is assumed to span the whole screen and is fetched from {@link Graphics#getWidth()} and
	* {@link Graphics#getHeight()}. The x- and y-coordinate of vec are assumed to be in screen coordinates (origin is the top left
	* corner, y pointing down, x pointing to the right) as reported by the touch methods in {@link Input}. A z-coordinate of 0
	* will return a point on the near plane, a z-coordinate of 1 will return a point on the far plane.
	* @param screenCoords the point in screen coordinates */
	@Override
	public Vector3 unproject (Vector3 screenCoords) {
		unproject(screenCoords, 0, 0, viewportWidth, viewportHeight);
		return screenCoords;
	}
}

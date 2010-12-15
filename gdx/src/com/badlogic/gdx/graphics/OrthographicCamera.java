/*
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com), Nathan Sweet (admin@esotericsoftware.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.badlogic.gdx.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;

/**
 * An orthographic camera having a position and a scale value for zooming. Looks at one of the sides given in the {@link Side}
 * enums, default is front, looking along the z-Axis. Generally you will want to alter the camera's scale and position and then
 * set its matrices via a call to {@link OrthographicCamera#setMatrices()} to set the OpenGL perspective and modelview matrices.
 * You can get a picking ray via a call to {@link OrthographicCamera#getPickRay(int, int)} . For OpenGL ES 2.0 you can get the
 * matrices via {@link OrthographicCamera#getCombinedMatrix()} to set them as a uniform. For this you have to call
 * {@link OrthographicCamera#update()} before you retrieve the matrices.
 * 
 * @author badlogicgames@gmail.com
 * 
 */
public final class OrthographicCamera {
	public enum Side {
		FRONT, BACK, TOP, BOTTOM, LEFT, RIGHT
	}

	private Side side;
	private Vector3 position = new Vector3();
	private Vector3 direction = new Vector3(0, 0, -1);
	private Vector3 up = new Vector3(0, 0, -1);
	private Vector3 axis = new Vector3(0, 1, 0);
	private float near = -1000;
	private float far = 1000;
	private float scale = 1.0f;
	private float viewportWidth = 0;
	private float viewportHeight = 0;

	private final Matrix4 proj = new Matrix4();
	private final Matrix4 model = new Matrix4();
	private final Matrix4 combined = new Matrix4();
	private final Matrix4 rotationMatrix = new Matrix4();

	/**
	 * Constructor, sets side to {@link Side#FRONT}
	 */
	public OrthographicCamera () {
		setSide(Side.FRONT);
	}

	/**
	 * @return Which side the camera looks at
	 */
	public Side getSide () {
		return side;
	}

	/**
	 * Sets the side the camera looks at.
	 * 
	 * @param side The side.
	 */
	public void setSide (Side side) {
		this.side = side;
		calculateRotationMatrix();
	}

	/**
	 * @return The near plane.
	 */
	public float getNear () {
		return near;
	}

	/**
	 * Sets the near plane.
	 * 
	 * @param near The near plane
	 */
	public void setNear (float near) {
		this.near = near;
	}

	/**
	 * @return The far plane.
	 */
	public float getFar () {
		return far;
	}

	/**
	 * Sets the far plane
	 * 
	 * @param far the far plane
	 */
	public void setFar (float far) {
		this.far = far;
	}

	/**
	 * @return The scale
	 */
	public float getScale () {
		return scale;
	}

	/**
	 * @param scale Sets the scale
	 */
	public void setScale (float scale) {
		this.scale = scale;
	}

	/**
	 * @return The position
	 */
	public Vector3 getPosition () {
		return position;
	}

	/**
	 * Sets the viewport
	 * 
	 * @param width The viewport width in pixels
	 * @param height The viewport height in pixels
	 */
	public void setViewport (float width, float height) {
		this.viewportWidth = width;
		this.viewportHeight = height;
	}

	Vector3 tmp = new Vector3();

	/**
	 * Updates the frustum as well as the matrices of the camera based on the near and far plane, the position, the side and the
	 * scale.
	 */
	public void update () {
		proj.setToOrtho2D(0, 0, (viewportWidth * scale), (viewportHeight * scale), near, far);
		model.setToTranslation(tmp.set((-position.x + (viewportWidth / 2) * scale), (-position.y + (viewportHeight / 2) * scale),
			(-position.z)));
		combined.set(proj);
		combined.mul(model);
		combined.mul(rotationMatrix);
	}

	/**
	 * Sets the projection matrix that also incorporates the camera's scale and position and loads an identity to the model view
	 * matrix of OpenGL ES 1.x. The current matrices get overwritten. The matrix mode will be left in the model view state after a
	 * call to this.
	 */
	public void setMatrices () {
		update();
		GL10 gl = Gdx.gl10;
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadMatrixf(getCombinedMatrix().val, 0);
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();
	}

	private Matrix4 calculateRotationMatrix () {
		float rotation = 0;
		if (side == Side.FRONT) {
			direction.set(0, 0, -1);
			up.set(0, 1, 0);
		} else if (side == Side.BACK) {
			axis.set(0, 1, 0);
			rotation = 180;
			direction.set(0, 0, 1);
			up.set(0, 1, 0);
		} else if (side == Side.TOP) {
			axis.set(1, 0, 0);
			rotation = 90;
			direction.set(0, -1, 0);
			up.set(0, 0, -1);
		} else if (side == Side.BOTTOM) {
			axis.set(1, 0, 0);
			rotation = -90;
			direction.set(0, 1, 0);
			up.set(0, 0, -1);
		} else if (side == Side.LEFT) {
			axis.set(0, 1, 0);
			rotation = 90;
			direction.set(-1, 0, 0);
			up.set(0, 1, 0);
		} else if (side == Side.RIGHT) {
			axis.set(0, 1, 0);
			rotation = -90;
			direction.set(1, 0, 0);
			up.set(0, 1, 0);
		}

		rotationMatrix.setToRotation(axis, rotation);
		return rotationMatrix;
	}

	/**
	 * Calculates the world coordinates of the given screen coordinates and stores the result in world
	 * 
	 * @param screenX the x-coordinate of the screen position
	 * @param screenY the y-coordinate of the screen position
	 * @param world the vector to store the result in
	 */
	public void getScreenToWorld (float screenX, float screenY, Vector2 world) {
		screenX = screenX / Gdx.graphics.getWidth() * viewportWidth;
		screenY = screenY / Gdx.graphics.getHeight() * viewportHeight;

		world.set((screenX * scale) - (viewportWidth * scale) / 2 + position.x, ((viewportHeight - screenY) * scale)
			- (viewportHeight * scale) / 2 + position.y);
	}

	/**
	 * Calculates the screen coordinates of the given world coordinates
	 * 
	 * @param worldX world x-coordinate
	 * @param worldY world y-coordinate
	 * @param screen the screen coordinates get stored here
	 */
	public void getWorldToScreen (float worldX, float worldY, Vector2 screen) {
		screen.x = (int)((worldX + (viewportWidth * scale) / 2 - position.x) / scale);
		screen.y = (int)(-(-worldY + (viewportHeight * scale) / 2 + position.y - viewportHeight * scale) / scale);
	}

	/**
	 * Returns the given screen x-coordinates as a world x-coordinate
	 * 
	 * @param screenX The screen x-coordinate
	 * @return The world x-coordinate
	 */
	public float getScreenToWorldX (float screenX) {
		screenX = screenX / Gdx.graphics.getWidth() * viewportWidth;
		return (screenX * scale) - (viewportWidth * scale) / 2 + position.x;
	}

	/**
	 * Returns the given world x-coordinate as a screen x-coordinate
	 * 
	 * @param worldX The world x-coordinate
	 * @return The screen x-coordinate
	 */
	public int getWorldToScreenX (float worldX) {
		return (int)((worldX + (viewportWidth * scale) / 2 - position.x) / scale);
	}

	/**
	 * Returns the given screen y-coordinates as a world y-coordinate
	 * 
	 * @param screenY The screen y-coordinate
	 * @return The world y-coordinate
	 */
	public float getScreenToWorldY (float screenY) {
		screenY = screenY / Gdx.graphics.getHeight() * viewportHeight;
		return ((viewportHeight - screenY - 1) * scale) - (viewportHeight * scale) / 2 + position.y;
	}

	/**
	 * Returns the given world y-coordinate as a screen x-coordinate
	 * 
	 * @param worldY The world y-coordinate
	 * @return The screen y-coordinate
	 */
	public int getWorldToScreenY (float worldY) {
		return (int)(-(-worldY + (viewportHeight * scale) / 2 + position.y - viewportHeight * scale) / scale);
	}

	Ray ray = new Ray(new Vector3(), new Vector3());
	Vector3 tmp2 = new Vector3();

	/**
	 * Returns a ray in world space form the given screen coordinates. This can be used for picking. The returned Ray is an
	 * internal member of this class to reduce memory allocations. Do not reuse it outside of this class.
	 * 
	 * @param screenX The screen x-coordinate
	 * @param screenY The screen y-coordinate
	 * @return The picking ray.
	 */
	public Ray getPickRay (int screenX, int screenY) {
		float x = getScreenToWorldX(screenX);
		float y = getScreenToWorldY(screenY);

		if (side == Side.TOP) {
			return ray.set(x, 1000 / 2, -y, 0, -1, 0);
		} else if (side == Side.BOTTOM) {
			return ray.set(x, 1000 / 2, y, 0, 1, 0);
		} else
			return ray.set(tmp2.set(x, y, 10000 / 2).mul(rotationMatrix), tmp.set(0, 0, -1).mul(rotationMatrix));
	}

	/**
	 * @return The combined matrix.
	 */
	public Matrix4 getCombinedMatrix () {
		return combined;
	}
}

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
	 * the screen size, for other unit scales (e.g. meters for box2d) proceed accordingly.
	 * 
	 * @param viewportWidth the viewport width
	 * @param viewportHeight the viewport height */
	public OrthographicCamera (float viewportWidth, float viewportHeight) {
		this.viewportWidth = viewportWidth;
		this.viewportHeight = viewportHeight;
		this.near = 0;
		update();
	}

	/** Constructs a new OrthographicCamera, using the given viewport width and height. This will create a camera useable for
	 * iso-metric views. The diamond angle is specifies the angle of a tile viewed isometrically.
	 * 
	 * @param viewportWidth the viewport width
	 * @param viewportHeight the viewport height
	 * @param diamondAngle the angle in degrees */
	public OrthographicCamera (float viewportWidth, float viewportHeight, float diamondAngle) {
		this.viewportWidth = viewportWidth;
		this.viewportHeight = viewportHeight;
		this.near = 0;
		findDirectionForIsoView(diamondAngle, 0.00000001f, 20);
		update();
	}

	public void findDirectionForIsoView (float targetAngle, float epsilon, int maxIterations) {
		float start = targetAngle - 5;
		float end = targetAngle + 5;
		float mid = targetAngle;

		int iterations = 0;
		float aMid = 0;
		while (Math.abs(targetAngle - aMid) > epsilon && iterations++ < maxIterations) {
			aMid = calculateAngle(mid);

			if (targetAngle < aMid) {
				end = mid;
			} else {
				start = mid;
			}
			mid = start + (end - start) / 2;
		}
		position.set(calculateDirection(mid));
		position.y = -position.y;
		lookAt(0, 0, 0);
		normalizeUp();
	}

	private float calculateAngle (float a) {
		Vector3 camPos = calculateDirection(a);
		position.set(camPos.mul(30));
		lookAt(0, 0, 0);
		normalizeUp();
		update();

		Vector3 orig = new Vector3(0, 0, 0);
		Vector3 vec = new Vector3(1, 0, 0);
		project(orig);
		project(vec);
		Vector2 d = new Vector2(vec.x - orig.x, -(vec.y - orig.y));
		return d.angle();
	}

	private Vector3 calculateDirection (float angle) {
		Matrix4 transform = new Matrix4();
		Vector3 dir = new Vector3(-1, 0, 1).nor();
		float rotAngle = (float)Math.toDegrees(Math.asin(Math.tan(Math.toRadians(angle))));
		transform.setToRotation(new Vector3(1, 0, 1).nor(), angle);
		dir.mul(transform).nor();
		return dir;
	}

	private final Vector3 tmp = new Vector3();

	@Override
	public void update () {
		projection.setToOrtho(zoom * -viewportWidth / 2, zoom * viewportWidth / 2, zoom * -viewportHeight / 2, zoom
			* viewportHeight / 2, Math.abs(near), Math.abs(far));
		view.setToLookAt(position, tmp.set(position).add(direction), up);
		combined.set(projection);
		Matrix4.mul(combined.val, view.val);
		invProjectionView.set(combined);
		Matrix4.inv(invProjectionView.val);
		frustum.update(invProjectionView);
	}

	@Override
	public void update (boolean updateFrustum) {
		projection.setToOrtho(zoom * -viewportWidth / 2, zoom * viewportWidth / 2, zoom * -viewportHeight / 2, zoom
			* viewportHeight / 2, Math.abs(near), Math.abs(far));
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
		}
		position.set(viewportWidth / 2.0f, viewportHeight / 2.0f, 0);
		this.viewportWidth = viewportWidth;
		this.viewportHeight = viewportHeight;
		update();
	}
}

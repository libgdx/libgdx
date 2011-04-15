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

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

/**
 * A camera with orthographic projection.
 * 
 * @author mzechner
 *
 */
public class OrthographicCamera extends Camera {
	/** the zoom of the camera **/
	public float zoom = 1;
	
	/**
	 * Constructs a new OrthographicCamera, using the given viewport
	 * width and height. For pixel perfect 2D rendering just supply
	 * the screen size, for other unit scales (e.g. meters for box2d)
	 * proceed accordingly. 
	 * 
	 * @param viewportWidth the viewport width
	 * @param viewportHeight the viewport height
	 */
	public OrthographicCamera(float viewportWidth, float viewportHeight) {
		this.viewportWidth = viewportWidth;
		this.viewportHeight = viewportHeight;		
		this.near = 0;
	}
	
	private final Vector3 tmp = new Vector3();
	@Override	
	public void update() {
		projection.setToOrtho(zoom * -viewportWidth / 2, zoom * viewportWidth / 2, zoom * -viewportHeight / 2, zoom * viewportHeight / 2, Math.abs(near), Math.abs(far));
		view.setToLookAt(position, tmp.set(position).add(direction), up);	
		combined.set(projection);
		Matrix4.mul(combined.val, view.val);
		invProjectionView.set(combined);	
		Matrix4.inv(invProjectionView.val);
		frustum.update(combined);
	}
}

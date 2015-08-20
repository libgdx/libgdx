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

package com.badlogic.gdx.graphics.g3d.shadow.directional;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;

/** Contains the result of the directional analyzer
 * @author result */
public class DirectionalResult {
	public Vector3 direction = new Vector3();
	public Vector3 position = new Vector3();
	public Vector3 up = new Vector3();
	public float near;
	public float far;
	public float viewportWidth;
	public float viewportHeight;

	public void set (Camera cam) {
		cam.direction.set(direction);
		cam.position.set(position);
		cam.up.set(up);
		cam.near = near;
		cam.far = far;
		cam.viewportWidth = viewportWidth;
		cam.viewportHeight = viewportHeight;
	}
}

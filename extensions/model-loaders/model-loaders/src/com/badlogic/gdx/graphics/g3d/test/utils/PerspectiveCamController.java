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
package com.badlogic.gdx.graphics.g3d.test.utils;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;

public class PerspectiveCamController extends InputAdapter {
	final PerspectiveCamera camera;
	final Vector3 curr = new Vector3();
	final Vector3 last = new Vector3(-1, -1, -1);
	final Vector3 delta = new Vector3();
	final Plane plane = new Plane(new Vector3(0, 0, 1), 0);

	public PerspectiveCamController (PerspectiveCamera camera) {
		this.camera = camera;
	}

	@Override
	public boolean touchDragged (int x, int y, int pointer) {
		Ray ray = camera.getPickRay(x, y);
		Intersector.intersectRayPlane(ray, plane, curr);

		if (!(last.x == -1 && last.y == -1 && last.z == -1)) {
			ray = camera.getPickRay(last.x, last.y);
			Intersector.intersectRayPlane(ray, plane, delta);
			delta.sub(curr);
			camera.position.add(delta.x, delta.y, delta.z);
		}
		last.set(x, y, 0);
		return false;
	}

	@Override
	public boolean touchUp (int x, int y, int pointer, int button) {
		last.set(-1, -1, -1);
		return false;
	}
}
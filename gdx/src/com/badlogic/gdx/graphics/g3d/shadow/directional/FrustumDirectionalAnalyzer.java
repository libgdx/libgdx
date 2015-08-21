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
import com.badlogic.gdx.graphics.g3d.environment.BaseLight;
import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

/** @FIXME NOT WORKING, DO NOT USE
 * @author realitix
 * @see "http://gamedev.stackexchange.com/questions/81734/how-to-calculate-directional-light-frustum-from-camera-frustum" */
public class FrustumDirectionalAnalyzer implements DirectionalAnalyzer {
	protected Vector3 vz = new Vector3();
	protected Vector3 vx = new Vector3();
	protected Vector3 vy = new Vector3();

	protected Vector2 dimz = new Vector2();
	protected Vector2 dimx = new Vector2();
	protected Vector2 dimy = new Vector2();

	/** @FIXME NOT WORKING */
	@Override
	public Camera analyze (BaseLight light, Frustum frustum, Vector3 direction, Camera out) {
		vz.set(direction);
		vx.set(vz.y, vz.z, vz.x);
		vy.set(vz).crs(vx);

		dimx.set(9999999, -9999999);
		dimy.set(9999999, -9999999);
		dimz.set(9999999, -9999999);

		int i = 0;
		float d;

		for (i = 0; i < frustum.planePoints.length; i++) {
			// z
			d = frustum.planePoints[i].dot(vz);
			if (d < dimz.x) dimz.x = d;
			if (d > dimz.y) dimz.y = d;

			// x
			d = frustum.planePoints[i].dot(vx);
			if (d < dimx.x) dimx.x = d;
			if (d > dimx.y) dimx.y = d;

			// y
			d = frustum.planePoints[i].dot(vy);
			if (d < dimy.x) dimy.x = d;
			if (d > dimy.y) dimy.y = d;
		}

		return out;
	}
}

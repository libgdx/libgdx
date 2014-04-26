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

package com.badlogic.gdx.graphics.g3d.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.BSpline;
import com.badlogic.gdx.math.CatmullRomSpline;
import com.badlogic.gdx.math.Path;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.GdxRuntimeException;

/** High-level Movement controller for managing and playing camera movement paths
 * @author florianbaethge (evident) */
public class CameraMovementController {
	private Camera camera;

	private ShapeRenderer debugRenderer;

	/** paths for position and lookat */
	public CameraPath positionPath;
	public CameraPath lookAtPath;
	public final Vector3 up = new Vector3().set(Vector3.Y);

	/** tmp variables needed for debugrenderer */
	private final Vector3 debugTmp1 = new Vector3();
	private final Vector3 debugTmp2 = new Vector3();
	private final Vector3 debugTmp3 = new Vector3();

	public CameraMovementController (final Camera cam) {
		this.camera = cam;

		debugRenderer = new ShapeRenderer();
	}

	public void update (float delta) {
		if (positionPath != null) {
			positionPath.update(delta);
			camera.position.set(positionPath.getValue());
		}
		if (lookAtPath != null) {
			lookAtPath.update(delta);
			camera.up.set(up);
			camera.lookAt(lookAtPath.getValue());
			debugTmp3.set(lookAtPath.getValue());

		}
		camera.update();
	}

	public void debugDraw (Camera debugCamera) {

		debugRenderer.setProjectionMatrix(debugCamera.combined);
		drawCurve(positionPath, Color.RED, Color.MAGENTA);
		drawCurve(lookAtPath, Color.GREEN, Color.CYAN);

		debugRenderer.begin(ShapeType.Filled);
		// render control points
		debugRenderer.setColor(Color.ORANGE);
		debugRenderer.box(camera.position.x, camera.position.y, camera.position.z, 0.4f, 0.4f, 0.4f);
		if (debugTmp3 != null) {
			debugRenderer.box(debugTmp3.x, debugTmp3.y, debugTmp3.z, 0.2f, 0.2f, 0.2f);
		}
		debugRenderer.end();
		debugRenderer.begin(ShapeType.Line);
		debugRenderer.line(camera.position, debugTmp3);
		debugRenderer.line(camera.position, camera.position.cpy().add(camera.up));
		debugRenderer.end();
	}

	private void drawCurve (CameraPath path, Color color1, Color color2) {
		if (path == null) {
			return;
		}

		debugRenderer.begin(ShapeType.Line);
		// render control point lines
		debugRenderer.setColor(color1);
		debugTmp1.set(path.keys[0]);
		for (Vector3 key : path.keys) {
			debugRenderer.line(debugTmp1, key);
			debugTmp1.set(key);
		}

		// render curve
		debugRenderer.setColor(color2);

		for (float i = 0; i < 1.0f; i += 0.01f) {
			path.spline.valueAt(debugTmp1, i);
			path.spline.valueAt(debugTmp2, i + 0.01f);
			debugRenderer.line(debugTmp1, debugTmp2);
		}

		debugRenderer.end();

		debugRenderer.begin(ShapeType.Filled);
		// render control points
		debugRenderer.setColor(color1);
		for (Vector3 key : path.keys) {
			debugRenderer.box(key.x - 0.05f, key.y - 0.05f, key.z - 0.05f, 0.2f, 0.2f, 0.2f);
		}
		debugRenderer.end();
	}

}

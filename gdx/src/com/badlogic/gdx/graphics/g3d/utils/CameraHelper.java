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

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Pool;

/** Camera helper helps you to visualize your camera. Of course, you shouldn't use camera helper with your rendering camera.
 * <p>
 * How to use it:
 * </p>
 * <pre>
 * // Create helper
 * CameraHelper helper = new CameraHelper(camera);
 * // During the rendering
 * camera.update();
 * helper.update();
 * modelBatch.render(helper);
 * // After using it
 * helper.dispose();
 * </pre>
 * @author realitix */
public class CameraHelper implements RenderableProvider, Disposable {
	private Camera camera;
	private Mesh mesh;
	private Color frustumColor = new Color(1, 0.66f, 0, 1);
	private Color coneColor = new Color(1, 0, 0, 1);
	private Color upColor = new Color(0, 0.66f, 1, 1);
	private Color targetColor = new Color(1, 1, 1, 1);
	private Color crossColor = new Color(0.2f, 0.2f, 0.2f, 1);
	private float vertices[];
	private int id;
	private Vector3 tmp = new Vector3();
	private Vector3 tmp2 = new Vector3();
	private Renderable renderable = new Renderable();

	/** Create camera helper
	 * @param camera */
	public CameraHelper (Camera camera) {
		this.camera = camera;
		init();
		update();
	}

	private void init () {
		// Init indices
		short[] indices = new short[] {
			// Near
			0, 1, 1, 2, 2, 3, 3, 0,
			// Far
			4, 5, 5, 6, 6, 7, 7, 4,
			// Sides
			0, 4, 1, 5, 2, 6, 3, 7,
			// Cone
			0, 8, 1, 8, 2, 8, 3, 8,
			// Cross near
			9, 10, 11, 12,
			// Cross far
			13, 14, 15, 16,
			// Target (position -> near -> far)
			8, 17, 17, 18,
			// Up triangle
			19, 2, 2, 3, 3, 19};

		// Init mesh
		int maxVertices = 20;
		mesh = new Mesh(false, maxVertices, indices.length, new VertexAttribute(Usage.Position, 3, "a_position"),
			new VertexAttribute(Usage.ColorPacked, 4, "a_color"));
		mesh.setIndices(indices, 0, indices.length);

		// Init vertices
		vertices = new float[maxVertices * (mesh.getVertexSize() / 4)];

		// Init renderable
		renderable.meshPart.mesh = mesh;
		renderable.meshPart.offset = 0;
		renderable.meshPart.size = mesh.getNumIndices();
		renderable.meshPart.primitiveType = GL20.GL_LINES;
		renderable.material = new Material();
	}

	/** Update cameraHelper mesh. You should call this method if you update your camera. */
	public void update () {
		id = 0;
		Vector3[] planePoints = camera.frustum.planePoints;

		// 0 - 7 = Frustum points
		for (int i = 0; i < planePoints.length; i++) {
			vertice(planePoints[i], frustumColor);
		}

		// 8 = Camera position
		vertice(camera.position, coneColor);

		// 9 - 12 = Cross near
		vertice(middlePoint(planePoints[1], planePoints[0]), crossColor);
		vertice(middlePoint(planePoints[3], planePoints[2]), crossColor);
		vertice(middlePoint(planePoints[2], planePoints[1]), crossColor);
		vertice(middlePoint(planePoints[3], planePoints[0]), crossColor);

		// 13 - 16 = Cross far
		vertice(middlePoint(planePoints[5], planePoints[4]), crossColor);
		vertice(middlePoint(planePoints[7], planePoints[6]), crossColor);
		vertice(middlePoint(planePoints[6], planePoints[5]), crossColor);
		vertice(middlePoint(planePoints[7], planePoints[4]), crossColor);

		// 17 - 18 = Target point
		vertice(centerPoint(planePoints[0], planePoints[1], planePoints[2]), crossColor);
		vertice(centerPoint(planePoints[4], planePoints[5], planePoints[6]), targetColor);

		// 19 = Up vertice
		float halfNearSize = tmp.set(planePoints[1]).sub(planePoints[0]).scl(0.5f).len();
		Vector3 centerNear = centerPoint(planePoints[0], planePoints[1], planePoints[2]);
		tmp.set(camera.up).scl(halfNearSize * 2);
		vertice(centerNear.add(tmp), upColor);

		// Set vertices
		mesh.setVertices(vertices, 0, id);

		// Update meshPart center, halfExtends and radius
		renderable.meshPart.update();
	}

	/** Return the middle point of the segment
	 * @param point0 First segment's point
	 * @param point1 Second segment's point
	 * @return the middle point */
	private Vector3 middlePoint (Vector3 point0, Vector3 point1) {
		tmp.set(point1).sub(point0).scl(0.5f);
		return tmp2.set(point0).add(tmp);
	}

	/** Return the center point of the rectangle
	 * @param point0
	 * @param point1
	 * @param point2
	 * @return the center point */
	private Vector3 centerPoint (Vector3 point0, Vector3 point1, Vector3 point2) {
		tmp.set(point1).sub(point0).scl(0.5f);
		tmp2.set(point0).add(tmp);
		tmp.set(point2).sub(point1).scl(0.5f);
		return tmp2.add(tmp);
	}

	private void vertice (Vector3 point, Color color) {
		vertices[id++] = point.x;
		vertices[id++] = point.y;
		vertices[id++] = point.z;
		vertices[id++] = color.toFloatBits();
	}

	@Override
	public void getRenderables (Array<Renderable> renderables, Pool<Renderable> pool) {
		renderables.add(renderable);
	}

	/** Set frustum color
	 * @param color */
	public void setFrustumColor (Color color) {
		frustumColor.set(color);
	}

	/** Set cone color. Starting from camera position to near plane.
	 * @param color */
	public void setConeColor (Color color) {
		coneColor.set(color);
	}

	/** Set up color.
	 * @param color */
	public void setUpColor (Color color) {
		upColor.set(color);
	}

	/** Set color for line starting from camera position to far plane.
	 * @param color */
	public void setTargetColor (Color color) {
		targetColor.set(color);
	}

	/** Set cross color
	 * @param color */
	public void setCrossColor (Color color) {
		crossColor.set(color);
	}

	@Override
	public void dispose () {
		mesh.dispose();
		camera = null;
	}
}

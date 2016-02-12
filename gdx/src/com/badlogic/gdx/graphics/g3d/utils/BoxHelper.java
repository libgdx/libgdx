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

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

/** Box helper allows to see a bounding box in 3d space.
 * <p>
 * How to use it:
 * </p>
 * 
 * <pre>
 * // Create helper
 * BoxHelper helper = new BoxHelper(box);
 * // During the rendering
 * box.set(min, max);
 * helper.update();
 * modelBatch.render(helper);
 * // After using it
 * helper.dispose();
 * </pre>
 * @author realitix */
public class BoxHelper implements Helper {
	private Mesh mesh;
	private float vertices[];
	private int id;
	private Vector3 tmp = new Vector3();
	private Renderable renderable = new Renderable();
	private BoundingBox box;

	/** Create box helper
	 * @param box BoundingBox to display */
	public BoxHelper (BoundingBox box) {
		this.box = box;
		init();
		update();
	}

	private void init () {
		// Init indices
		short[] indices = new short[] {
			// Face 1
			0, 1, 1, 2, 2, 3, 3, 0,
			// Face 2
			4, 5, 5, 6, 6, 7, 7, 4,
			// Close cube
			0, 4, 1, 5, 2, 6, 3, 7};

		// Init mesh
		int maxVertices = 8;
		mesh = new Mesh(false, maxVertices, indices.length, new VertexAttribute(Usage.Position, 3, "a_position"));
		mesh.setIndices(indices, 0, indices.length);

		// Init vertices
		vertices = new float[maxVertices * (mesh.getVertexSize() / 4)];

		// Init renderable
		renderable.meshPart.mesh = mesh;
		renderable.meshPart.offset = 0;
		renderable.meshPart.size = mesh.getNumIndices();
		renderable.meshPart.primitiveType = GL20.GL_LINES;
		renderable.material = new Material(ColorAttribute.createDiffuse(1, 0.66f, 0, 1));
	}

	/** Update box. */
	@Override
	public void update () {
		id = 0;

		box.getCorner000(tmp);
		vertice(tmp);

		box.getCorner001(tmp);
		vertice(tmp);

		box.getCorner011(tmp);
		vertice(tmp);

		box.getCorner010(tmp);
		vertice(tmp);

		box.getCorner100(tmp);
		vertice(tmp);

		box.getCorner101(tmp);
		vertice(tmp);

		box.getCorner111(tmp);
		vertice(tmp);

		box.getCorner110(tmp);
		vertice(tmp);

		// Set vertices
		mesh.setVertices(vertices, 0, id);

		// Update meshPart center, halfExtends and radius
		renderable.meshPart.update();
	}

	private void vertice (Vector3 point) {
		vertices[id++] = point.x;
		vertices[id++] = point.y;
		vertices[id++] = point.z;
	}

	@Override
	public void getRenderables (Array<Renderable> renderables, Pool<Renderable> pool) {
		renderables.add(renderable);
	}

	/** Set box color
	 * @param color */
	public void setBoxColor (Color color) {
		renderable.material.get(ColorAttribute.class, ColorAttribute.Diffuse).color.set(color);
	}

	@Override
	public void dispose () {
		mesh.dispose();
	}
}

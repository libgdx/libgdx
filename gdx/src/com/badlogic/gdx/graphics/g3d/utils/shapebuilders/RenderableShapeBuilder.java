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

package com.badlogic.gdx.graphics.g3d.utils.shapebuilders;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FlushablePool;

/** RenderableShapeBuilder builds various properties of a renderable.
 * @author realitix */
public class RenderableShapeBuilder extends BaseShapeBuilder {

	private static class RenderablePool extends FlushablePool<Renderable> {
		public RenderablePool () {
			super();
		}

		@Override
		protected Renderable newObject () {
			return new Renderable();
		}

		@Override
		public Renderable obtain () {
			Renderable renderable = super.obtain();
			renderable.environment = null;
			renderable.material = null;
			renderable.meshPart.set("", null, 0, 0, 0);
			renderable.shader = null;
			renderable.userData = null;
			return renderable;
		}
	}

	private static short[] indices;
	private static float[] vertices;
	private final static RenderablePool renderablesPool = new RenderablePool();
	private final static Array<Renderable> renderables = new Array<Renderable>();
	private static final int FLOAT_BYTES = 4;

	/** Builds normal, tangent and binormal of a RenderableProvider with default colors (normal blue, tangent red, binormal green).
	 * @param builder
	 * @param renderableProvider
	 * @param vectorSize Size of the normal vector */
	public static void buildNormals (MeshPartBuilder builder, RenderableProvider renderableProvider, float vectorSize) {
		buildNormals(builder, renderableProvider, vectorSize, tmpColor0.set(0, 0, 1, 1), tmpColor1.set(1, 0, 0, 1),
			tmpColor2.set(0, 1, 0, 1));
	}

	/** Builds normal, tangent and binormal of a RenderableProvider.
	 * @param builder
	 * @param renderableProvider
	 * @param vectorSize Size of the normal vector
	 * @param normalColor Normal vector's color
	 * @param tangentColor Tangent vector's color
	 * @param binormalColor Binormal vector's color */
	public static void buildNormals (MeshPartBuilder builder, RenderableProvider renderableProvider, float vectorSize,
		Color normalColor, Color tangentColor, Color binormalColor) {

		renderableProvider.getRenderables(renderables, renderablesPool);

		for (Renderable renderable : renderables) {
			buildNormals(builder, renderable, vectorSize, normalColor, tangentColor, binormalColor);
		}

		renderablesPool.flush();
		renderables.clear();
	}

	/** Builds normal, tangent and binormal of a Renderable.
	 * @param builder
	 * @param renderable
	 * @param vectorSize Size of the normal vector
	 * @param normalColor Normal vector's color
	 * @param tangentColor Tangent vector's color
	 * @param binormalColor Binormal vector's color */
	public static void buildNormals (MeshPartBuilder builder, Renderable renderable, float vectorSize, Color normalColor,
		Color tangentColor, Color binormalColor) {
		Mesh mesh = renderable.meshPart.mesh;

		// Position
		int positionOffset = -1;
		if (mesh.getVertexAttribute(Usage.Position) != null)
			positionOffset = mesh.getVertexAttribute(Usage.Position).offset / FLOAT_BYTES;

		// Normal
		int normalOffset = -1;
		if (mesh.getVertexAttribute(Usage.Normal) != null)
			normalOffset = mesh.getVertexAttribute(Usage.Normal).offset / FLOAT_BYTES;

		// Tangent
		int tangentOffset = -1;
		if (mesh.getVertexAttribute(Usage.Tangent) != null)
			tangentOffset = mesh.getVertexAttribute(Usage.Tangent).offset / FLOAT_BYTES;

		// Binormal
		int binormalOffset = -1;
		if (mesh.getVertexAttribute(Usage.BiNormal) != null)
			binormalOffset = mesh.getVertexAttribute(Usage.BiNormal).offset / FLOAT_BYTES;

		int attributesSize = mesh.getVertexSize() / FLOAT_BYTES;
		int verticesOffset = 0;
		int verticesQuantity = 0;

		if (mesh.getNumIndices() > 0) {
			// Get min vertice to max vertice in indices array
			ensureIndicesCapacity(mesh.getNumIndices());
			mesh.getIndices(renderable.meshPart.offset, renderable.meshPart.size, indices, 0);

			short minVertice = minVerticeInIndices();
			short maxVertice = maxVerticeInIndices();

			verticesOffset = minVertice;
			verticesQuantity = maxVertice - minVertice;
		} else {
			verticesOffset = renderable.meshPart.offset;
			verticesQuantity = renderable.meshPart.size;
		}

		ensureVerticesCapacity(verticesQuantity * attributesSize);
		mesh.getVertices(verticesOffset * attributesSize, verticesQuantity * attributesSize, vertices, 0);

		for (int i = verticesOffset; i < verticesQuantity; i++) {
			int id = i * attributesSize;

			// Vertex position
			tmpV0.set(vertices[id + positionOffset], vertices[id + positionOffset + 1], vertices[id + positionOffset + 2]);

			// Vertex normal, tangent, binormal
			if (normalOffset != -1) {
				tmpV1.set(vertices[id + normalOffset], vertices[id + normalOffset + 1], vertices[id + normalOffset + 2]);
				tmpV2.set(tmpV0).add(tmpV1.scl(vectorSize));
			}

			if (tangentOffset != -1) {
				tmpV3.set(vertices[id + tangentOffset], vertices[id + tangentOffset + 1], vertices[id + tangentOffset + 2]);
				tmpV4.set(tmpV0).add(tmpV3.scl(vectorSize));
			}

			if (binormalOffset != -1) {
				tmpV5.set(vertices[id + binormalOffset], vertices[id + binormalOffset + 1], vertices[id + binormalOffset + 2]);
				tmpV6.set(tmpV0).add(tmpV5.scl(vectorSize));
			}

			// World transform
			tmpV0.mul(renderable.worldTransform);
			tmpV2.mul(renderable.worldTransform);
			tmpV4.mul(renderable.worldTransform);
			tmpV6.mul(renderable.worldTransform);

			// Draws normal, tangent, binormal
			if (normalOffset != -1) {
				builder.setColor(normalColor);
				builder.line(tmpV0, tmpV2);
			}

			if (tangentOffset != -1) {
				builder.setColor(tangentColor);
				builder.line(tmpV0, tmpV4);
			}

			if (binormalOffset != -1) {
				builder.setColor(binormalColor);
				builder.line(tmpV0, tmpV6);
			}
		}
	}

	private static void ensureVerticesCapacity (int capacity) {
		if (vertices == null || vertices.length < capacity) vertices = new float[capacity];
	}

	private static void ensureIndicesCapacity (int capacity) {
		if (indices == null || indices.length < capacity) indices = new short[capacity];
	}

	private static short minVerticeInIndices () {
		short min = (short)32767;
		for (int i = 0; i < indices.length; i++)
			if (indices[i] < min) min = indices[i];
		return min;
	}

	private static short maxVerticeInIndices () {
		short max = (short)-32768;
		for (int i = 0; i < indices.length; i++)
			if (indices[i] > max) max = indices[i];
		return max;
	}
}

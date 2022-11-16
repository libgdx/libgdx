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

	private static final int FLOAT_BYTES = 4;

	/** Builds normal, tangent and binormal of a RenderableProvider with default colors (normal blue, tangent red, binormal green).
	 * @param builder
	 * @param renderableProvider
	 * @param vectorSize Size of the normal vector */
	public static void buildNormals (MeshPartBuilder builder, RenderableProvider renderableProvider, float vectorSize) {
		final BaseShapeData data = tlData.get();
		buildNormals(builder, renderableProvider, vectorSize, data.tmpColor0.set(0, 0, 1, 1), data.tmpColor1.set(1, 0, 0, 1),
			data.tmpColor2.set(0, 1, 0, 1));
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
		final RenderablePool renderablesPool = new RenderablePool();
		final Array<Renderable> renderables = new Array<Renderable>();

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
		final BaseShapeData data = tlData.get();

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
			final short[] indices = new short[mesh.getNumIndices()];
			mesh.getIndices(renderable.meshPart.offset, renderable.meshPart.size, indices, 0);

			short minVertice = minVerticeInIndices(indices);
			short maxVertice = maxVerticeInIndices(indices);

			verticesOffset = minVertice;
			verticesQuantity = maxVertice - minVertice;
		} else {
			verticesOffset = renderable.meshPart.offset;
			verticesQuantity = renderable.meshPart.size;
		}

		final float[] vertices = new float[verticesQuantity * attributesSize];
		mesh.getVertices(verticesOffset * attributesSize, verticesQuantity * attributesSize, vertices, 0);

		for (int i = verticesOffset; i < verticesQuantity; i++) {
			int id = i * attributesSize;

			// Vertex position
			data.tmpV0.set(vertices[id + positionOffset], vertices[id + positionOffset + 1], vertices[id + positionOffset + 2]);

			// Vertex normal, tangent, binormal
			if (normalOffset != -1) {
				data.tmpV1.set(vertices[id + normalOffset], vertices[id + normalOffset + 1], vertices[id + normalOffset + 2]);
				data.tmpV2.set(data.tmpV0).add(data.tmpV1.scl(vectorSize));
			}

			if (tangentOffset != -1) {
				data.tmpV3.set(vertices[id + tangentOffset], vertices[id + tangentOffset + 1], vertices[id + tangentOffset + 2]);
				data.tmpV4.set(data.tmpV0).add(data.tmpV3.scl(vectorSize));
			}

			if (binormalOffset != -1) {
				data.tmpV5.set(vertices[id + binormalOffset], vertices[id + binormalOffset + 1], vertices[id + binormalOffset + 2]);
				data.tmpV6.set(data.tmpV0).add(data.tmpV5.scl(vectorSize));
			}

			// World transform
			data.tmpV0.mul(renderable.worldTransform);
			data.tmpV2.mul(renderable.worldTransform);
			data.tmpV4.mul(renderable.worldTransform);
			data.tmpV6.mul(renderable.worldTransform);

			// Draws normal, tangent, binormal
			if (normalOffset != -1) {
				builder.setColor(normalColor);
				builder.line(data.tmpV0, data.tmpV2);
			}

			if (tangentOffset != -1) {
				builder.setColor(tangentColor);
				builder.line(data.tmpV0, data.tmpV4);
			}

			if (binormalOffset != -1) {
				builder.setColor(binormalColor);
				builder.line(data.tmpV0, data.tmpV6);
			}
		}
	}

	private static short minVerticeInIndices (short[] indices) {
		short min = (short)32767;
		for (int i = 0; i < indices.length; i++)
			if (indices[i] < min) min = indices[i];
		return min;
	}

	private static short maxVerticeInIndices (short[] indices) {
		short max = (short)-32768;
		for (int i = 0; i < indices.length; i++)
			if (indices[i] > max) max = indices[i];
		return max;
	}
}

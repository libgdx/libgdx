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
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder.VertexInfo;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

/** Helper class with static methods to build box shapes using {@link MeshPartBuilder}.
 * @author realitix, xoppa */
public class BoxShapeBuilder extends BaseShapeBuilder {

	/** Build a box with the shape of the specified {@link BoundingBox}.
	 * @param box */
	public static void build (MeshPartBuilder builder, BoundingBox box) {
		builder.box(box.getCorner000(obtainV3()), box.getCorner010(obtainV3()), box.getCorner100(obtainV3()), box.getCorner110(obtainV3()),
			box.getCorner001(obtainV3()), box.getCorner011(obtainV3()), box.getCorner101(obtainV3()), box.getCorner111(obtainV3()));
		freeAll();
	}

	/** Add a box. Requires GL_POINTS, GL_LINES or GL_TRIANGLES primitive type. */
	public static void build (MeshPartBuilder builder, VertexInfo corner000, VertexInfo corner010, VertexInfo corner100,
		VertexInfo corner110, VertexInfo corner001, VertexInfo corner011, VertexInfo corner101, VertexInfo corner111) {
		builder.ensureVertices(8);
		final short i000 = builder.vertex(corner000);
		final short i100 = builder.vertex(corner100);
		final short i110 = builder.vertex(corner110);
		final short i010 = builder.vertex(corner010);
		final short i001 = builder.vertex(corner001);
		final short i101 = builder.vertex(corner101);
		final short i111 = builder.vertex(corner111);
		final short i011 = builder.vertex(corner011);

		final int primitiveType = builder.getPrimitiveType();
		if (primitiveType == GL20.GL_LINES) {
			builder.ensureIndices(24);
			builder.rect(i000, i100, i110, i010);
			builder.rect(i101, i001, i011, i111);
			builder.index(i000, i001, i010, i011, i110, i111, i100, i101);
		} else if (primitiveType == GL20.GL_POINTS) {
			builder.ensureRectangleIndices(2);
			builder.rect(i000, i100, i110, i010);
			builder.rect(i101, i001, i011, i111);
		} else { // GL20.GL_TRIANGLES
			builder.ensureRectangleIndices(6);
			builder.rect(i000, i100, i110, i010);
			builder.rect(i101, i001, i011, i111);
			builder.rect(i000, i010, i011, i001);
			builder.rect(i101, i111, i110, i100);
			builder.rect(i101, i100, i000, i001);
			builder.rect(i110, i111, i011, i010);
		}
	}

	/** Add a box. Requires GL_POINTS, GL_LINES or GL_TRIANGLES primitive type. */
	public static void build (MeshPartBuilder builder, Vector3 corner000, Vector3 corner010, Vector3 corner100, Vector3 corner110,
		Vector3 corner001, Vector3 corner011, Vector3 corner101, Vector3 corner111) {
		if ((builder.getAttributes().getMask() & (Usage.Normal | Usage.BiNormal | Usage.Tangent | Usage.TextureCoordinates)) == 0) {
			build(builder, vertTmp1.set(corner000, null, null, null), vertTmp2.set(corner010, null, null, null),
				vertTmp3.set(corner100, null, null, null), vertTmp4.set(corner110, null, null, null),
				vertTmp5.set(corner001, null, null, null), vertTmp6.set(corner011, null, null, null),
				vertTmp7.set(corner101, null, null, null), vertTmp8.set(corner111, null, null, null));
		} else {
			builder.ensureVertices(24);
			builder.ensureRectangleIndices(6);
			Vector3 nor = tmpV1.set(corner000).lerp(corner110, 0.5f).sub(tmpV2.set(corner001).lerp(corner111, 0.5f)).nor();
			builder.rect(corner000, corner010, corner110, corner100, nor);
			builder.rect(corner011, corner001, corner101, corner111, nor.scl(-1));
			nor = tmpV1.set(corner000).lerp(corner101, 0.5f).sub(tmpV2.set(corner010).lerp(corner111, 0.5f)).nor();
			builder.rect(corner001, corner000, corner100, corner101, nor);
			builder.rect(corner010, corner011, corner111, corner110, nor.scl(-1));
			nor = tmpV1.set(corner000).lerp(corner011, 0.5f).sub(tmpV2.set(corner100).lerp(corner111, 0.5f)).nor();
			builder.rect(corner001, corner011, corner010, corner000, nor);
			builder.rect(corner100, corner110, corner111, corner101, nor.scl(-1));
		}
	}

	/** Add a box given the matrix. Requires GL_POINTS, GL_LINES or GL_TRIANGLES primitive type. */
	public static void build (MeshPartBuilder builder, Matrix4 transform) {
		build(builder, obtainV3().set(-0.5f, -0.5f, -0.5f).mul(transform), obtainV3().set(-0.5f, 0.5f, -0.5f).mul(transform),
			obtainV3().set(0.5f, -0.5f, -0.5f).mul(transform), obtainV3().set(0.5f, 0.5f, -0.5f).mul(transform),
			obtainV3().set(-0.5f, -0.5f, 0.5f).mul(transform), obtainV3().set(-0.5f, 0.5f, 0.5f).mul(transform),
			obtainV3().set(0.5f, -0.5f, 0.5f).mul(transform), obtainV3().set(0.5f, 0.5f, 0.5f).mul(transform));
		freeAll();
	}

	/** Add a box with the specified dimensions. Requires GL_POINTS, GL_LINES or GL_TRIANGLES primitive type. */
	public static void build (MeshPartBuilder builder, float width, float height, float depth) {
		build(builder, 0, 0, 0, width, height, depth);
	}

	/** Add a box at the specified location, with the specified dimensions */
	public static void build (MeshPartBuilder builder, float x, float y, float z, float width, float height, float depth) {
		final float hw = width * 0.5f;
		final float hh = height * 0.5f;
		final float hd = depth * 0.5f;
		final float x0 = x - hw, y0 = y - hh, z0 = z - hd, x1 = x + hw, y1 = y + hh, z1 = z + hd;
		build(builder, //
			obtainV3().set(x0, y0, z0), obtainV3().set(x0, y1, z0), obtainV3().set(x1, y0, z0), obtainV3().set(x1, y1, z0), //
			obtainV3().set(x0, y0, z1), obtainV3().set(x0, y1, z1), obtainV3().set(x1, y0, z1), obtainV3().set(x1, y1, z1));
		freeAll();
	}

}

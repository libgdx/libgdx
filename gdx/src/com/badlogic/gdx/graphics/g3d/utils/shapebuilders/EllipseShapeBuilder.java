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

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder.VertexInfo;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.GdxRuntimeException;

/** Helper class with static methods to build ellipse shapes using {@link MeshPartBuilder}.
 * @author xoppa */
public class EllipseShapeBuilder extends BaseShapeBuilder {
	/** Build a circle */
	public static void build (MeshPartBuilder builder, float radius, int divisions, float centerX, float centerY, float centerZ, float normalX, float normalY,
		float normalZ) {
		build(builder, radius, divisions, centerX, centerY, centerZ, normalX, normalY, normalZ, 0f, 360f);
	}

	/** Build a circle */
	public static void build (MeshPartBuilder builder, float radius, int divisions, final Vector3 center, final Vector3 normal) {
		build(builder, radius, divisions, center.x, center.y, center.z, normal.x, normal.y, normal.z);
	}

	/** Build a circle */
	public static void build (MeshPartBuilder builder, float radius, int divisions, final Vector3 center, final Vector3 normal, final Vector3 tangent,
		final Vector3 binormal) {
		build(builder, radius, divisions, center.x, center.y, center.z, normal.x, normal.y, normal.z, tangent.x, tangent.y, tangent.z,
			binormal.x, binormal.y, binormal.z);
	}

	/** Build a circle */
	public static void build (MeshPartBuilder builder, float radius, int divisions, float centerX, float centerY, float centerZ, float normalX, float normalY,
		float normalZ, float tangentX, float tangentY, float tangentZ, float binormalX, float binormalY, float binormalZ) {
		build(builder, radius, divisions, centerX, centerY, centerZ, normalX, normalY, normalZ, tangentX, tangentY, tangentZ, binormalX,
			binormalY, binormalZ, 0f, 360f);
	}

	/** Build a circle */
	public static void build (MeshPartBuilder builder, float radius, int divisions, float centerX, float centerY, float centerZ, float normalX, float normalY,
		float normalZ, float angleFrom, float angleTo) {
		build(builder, radius * 2f, radius * 2f, divisions, centerX, centerY, centerZ, normalX, normalY, normalZ, angleFrom, angleTo);
	}

	/** Build a circle */
	public static void build (MeshPartBuilder builder, float radius, int divisions, final Vector3 center, final Vector3 normal, float angleFrom, float angleTo) {
		build(builder, radius, divisions, center.x, center.y, center.z, normal.x, normal.y, normal.z, angleFrom, angleTo);
	}

	/** Build a circle */
	public static void build (MeshPartBuilder builder, float radius, int divisions, final Vector3 center, final Vector3 normal, final Vector3 tangent,
		final Vector3 binormal, float angleFrom, float angleTo) {
		build(builder, radius, divisions, center.x, center.y, center.z, normal.x, normal.y, normal.z, tangent.x, tangent.y, tangent.z,
			binormal.x, binormal.y, binormal.z, angleFrom, angleTo);
	}

	/** Build a circle */
	public static void build (MeshPartBuilder builder, float radius, int divisions, float centerX, float centerY, float centerZ, float normalX, float normalY,
		float normalZ, float tangentX, float tangentY, float tangentZ, float binormalX, float binormalY, float binormalZ,
		float angleFrom, float angleTo) {
		build(builder, radius * 2, radius * 2, 0, 0, divisions, centerX, centerY, centerZ, normalX, normalY, normalZ, tangentX, tangentY,
			tangentZ, binormalX, binormalY, binormalZ, angleFrom, angleTo);
	}

	/** Build a ellipse */
	public static void build (MeshPartBuilder builder, float width, float height, int divisions, float centerX, float centerY,
		float centerZ, float normalX, float normalY, float normalZ) {
		build(builder, width, height, divisions, centerX, centerY, centerZ, normalX, normalY, normalZ, 0f, 360f);
	}

	/** Build a ellipse */
	public static void build (MeshPartBuilder builder, float width, float height, int divisions, final Vector3 center,
		final Vector3 normal) {
		build(builder, width, height, divisions, center.x, center.y, center.z, normal.x, normal.y, normal.z);
	}

	/** Build a ellipse */
	public static void build (MeshPartBuilder builder, float width, float height, int divisions, final Vector3 center,
		final Vector3 normal, final Vector3 tangent, final Vector3 binormal) {
		build(builder, width, height, divisions, center.x, center.y, center.z, normal.x, normal.y, normal.z, tangent.x, tangent.y,
			tangent.z, binormal.x, binormal.y, binormal.z);
	}

	/** Build a ellipse */
	public static void build (MeshPartBuilder builder, float width, float height, int divisions, float centerX, float centerY,
		float centerZ, float normalX, float normalY, float normalZ, float tangentX, float tangentY, float tangentZ,
		float binormalX, float binormalY, float binormalZ) {
		build(builder, width, height, divisions, centerX, centerY, centerZ, normalX, normalY, normalZ, tangentX, tangentY, tangentZ,
			binormalX, binormalY, binormalZ, 0f, 360f);
	}

	/** Build a ellipse */
	public static void build (MeshPartBuilder builder, float width, float height, int divisions, float centerX, float centerY,
		float centerZ, float normalX, float normalY, float normalZ, float angleFrom, float angleTo) {
		build(builder, width, height, 0f, 0f, divisions, centerX, centerY, centerZ, normalX, normalY, normalZ, angleFrom, angleTo);
	}

	/** Build a ellipse */
	public static void build (MeshPartBuilder builder, float width, float height, int divisions, final Vector3 center,
		final Vector3 normal, float angleFrom, float angleTo) {
		build(builder, width, height, 0f, 0f, divisions, center.x, center.y, center.z, normal.x, normal.y, normal.z, angleFrom, angleTo);
	}

	/** Build a ellipse */
	public static void build (MeshPartBuilder builder, float width, float height, int divisions, final Vector3 center,
		final Vector3 normal, final Vector3 tangent, final Vector3 binormal, float angleFrom, float angleTo) {
		build(builder, width, height, 0f, 0f, divisions, center.x, center.y, center.z, normal.x, normal.y, normal.z, tangent.x,
			tangent.y, tangent.z, binormal.x, binormal.y, binormal.z, angleFrom, angleTo);
	}

	/** Build a ellipse */
	public static void build (MeshPartBuilder builder, float width, float height, int divisions, float centerX, float centerY,
		float centerZ, float normalX, float normalY, float normalZ, float tangentX, float tangentY, float tangentZ,
		float binormalX, float binormalY, float binormalZ, float angleFrom, float angleTo) {
		build(builder, width, height, 0f, 0f, divisions, centerX, centerY, centerZ, normalX, normalY, normalZ, tangentX, tangentY,
			tangentZ, binormalX, binormalY, binormalZ, angleFrom, angleTo);
	}

	/** Build an ellipse */
	public static void build (MeshPartBuilder builder, float width, float height, float innerWidth, float innerHeight,
		int divisions, float centerX, float centerY, float centerZ, float normalX, float normalY, float normalZ, float angleFrom,
		float angleTo) {
		tmpV1.set(normalX, normalY, normalZ).crs(0, 0, 1);
		tmpV2.set(normalX, normalY, normalZ).crs(0, 1, 0);
		if (tmpV2.len2() > tmpV1.len2()) tmpV1.set(tmpV2);
		tmpV2.set(tmpV1.nor()).crs(normalX, normalY, normalZ).nor();
		build(builder, width, height, innerWidth, innerHeight, divisions, centerX, centerY, centerZ, normalX, normalY, normalZ,
			tmpV1.x, tmpV1.y, tmpV1.z, tmpV2.x, tmpV2.y, tmpV2.z, angleFrom, angleTo);
	}

	/** Build an ellipse */
	public static void build (MeshPartBuilder builder, float width, float height, float innerWidth, float innerHeight,
		int divisions, float centerX, float centerY, float centerZ, float normalX, float normalY, float normalZ) {
		build(builder, width, height, innerWidth, innerHeight, divisions, centerX, centerY, centerZ, normalX, normalY, normalZ, 0f,
			360f);
	}

	/** Build an ellipse */
	public static void build (MeshPartBuilder builder, float width, float height, float innerWidth, float innerHeight,
		int divisions, Vector3 center, Vector3 normal) {
		build(builder, width, height, innerWidth, innerHeight, divisions, center.x, center.y, center.z, normal.x, normal.y,
			normal.z, 0f, 360f);
	}

	/** Build an ellipse */
	public static void build (MeshPartBuilder builder, float width, float height, float innerWidth, float innerHeight,
		int divisions, float centerX, float centerY, float centerZ, float normalX, float normalY, float normalZ, float tangentX,
		float tangentY, float tangentZ, float binormalX, float binormalY, float binormalZ, float angleFrom, float angleTo) {
		if (innerWidth <= 0 || innerHeight <= 0) {
			builder.ensureVertices(divisions + 2);
			builder.ensureTriangleIndices(divisions);
		} else if (innerWidth == width && innerHeight == height) {
			builder.ensureVertices(divisions + 1);
			builder.ensureIndices(divisions + 1);
			if (builder.getPrimitiveType() != GL20.GL_LINES)
				throw new GdxRuntimeException(
					"Incorrect primitive type : expect GL_LINES because innerWidth == width && innerHeight == height");
		} else {
			builder.ensureVertices((divisions + 1) * 2);
			builder.ensureRectangleIndices(divisions + 1);
		}

		final float ao = MathUtils.degreesToRadians * angleFrom;
		final float step = (MathUtils.degreesToRadians * (angleTo - angleFrom)) / divisions;
		final Vector3 sxEx = tmpV1.set(tangentX, tangentY, tangentZ).scl(width * 0.5f);
		final Vector3 syEx = tmpV2.set(binormalX, binormalY, binormalZ).scl(height * 0.5f);
		final Vector3 sxIn = tmpV3.set(tangentX, tangentY, tangentZ).scl(innerWidth * 0.5f);
		final Vector3 syIn = tmpV4.set(binormalX, binormalY, binormalZ).scl(innerHeight * 0.5f);
		VertexInfo currIn = vertTmp3.set(null, null, null, null);
		currIn.hasUV = currIn.hasPosition = currIn.hasNormal = true;
		currIn.uv.set(.5f, .5f);
		currIn.position.set(centerX, centerY, centerZ);
		currIn.normal.set(normalX, normalY, normalZ);
		VertexInfo currEx = vertTmp4.set(null, null, null, null);
		currEx.hasUV = currEx.hasPosition = currEx.hasNormal = true;
		currEx.uv.set(.5f, .5f);
		currEx.position.set(centerX, centerY, centerZ);
		currEx.normal.set(normalX, normalY, normalZ);
		final short center = builder.vertex(currEx);
		float angle = 0f;
		final float us = 0.5f * (innerWidth / width);
		final float vs = 0.5f * (innerHeight / height);
		short i1, i2 = 0, i3 = 0, i4 = 0;
		for (int i = 0; i <= divisions; i++) {
			angle = ao + step * i;
			final float x = MathUtils.cos(angle);
			final float y = MathUtils.sin(angle);
			currEx.position.set(centerX, centerY, centerZ).add(sxEx.x * x + syEx.x * y, sxEx.y * x + syEx.y * y,
				sxEx.z * x + syEx.z * y);
			currEx.uv.set(.5f + .5f * x, .5f + .5f * y);
			i1 = builder.vertex(currEx);

			if (innerWidth <= 0f || innerHeight <= 0f) {
				if (i != 0) builder.triangle(i1, i2, center);
				i2 = i1;
			} else if (innerWidth == width && innerHeight == height) {
				if (i != 0) builder.line(i1, i2);
				i2 = i1;
			} else {
				currIn.position.set(centerX, centerY, centerZ).add(sxIn.x * x + syIn.x * y, sxIn.y * x + syIn.y * y,
					sxIn.z * x + syIn.z * y);
				currIn.uv.set(.5f + us * x, .5f + vs * y);
				i2 = i1;
				i1 = builder.vertex(currIn);

				if (i != 0) builder.rect(i1, i2, i4, i3);
				i4 = i2;
				i3 = i1;
			}
		}
	}
}

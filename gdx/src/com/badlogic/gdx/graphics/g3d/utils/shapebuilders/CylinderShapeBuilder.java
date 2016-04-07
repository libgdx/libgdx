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

import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder.VertexInfo;
import com.badlogic.gdx.math.MathUtils;

/** Helper class with static methods to build cylinders shapes using {@link MeshPartBuilder}.
 * @author xoppa */
public class CylinderShapeBuilder extends BaseShapeBuilder {
	/** Build a cylinder */
	public static void build (MeshPartBuilder builder, float width, float height, float depth, int divisions) {
		build(builder, width, height, depth, divisions, 0, 360);
	}
	
	/** Build a cylinder */
	public static void build (MeshPartBuilder builder, float width, float height, float depth, int divisions, float angleFrom, float angleTo) {
		build(builder, width, height, depth, divisions, angleFrom, angleTo, true);
	}
	
	/** Build a cylinder */
	public static void build (MeshPartBuilder builder, float width, float height, float depth, int divisions, float angleFrom,
		float angleTo, boolean close) {
		// FIXME create better cylinder method (- axis on which to create the cylinder (matrix?))
		final float hw = width * 0.5f;
		final float hh = height * 0.5f;
		final float hd = depth * 0.5f;
		final float ao = MathUtils.degreesToRadians * angleFrom;
		final float step = (MathUtils.degreesToRadians * (angleTo - angleFrom)) / divisions;
		final float us = 1f / divisions;
		float u = 0f;
		float angle = 0f;
		VertexInfo curr1 = vertTmp3.set(null, null, null, null);
		curr1.hasUV = curr1.hasPosition = curr1.hasNormal = true;
		VertexInfo curr2 = vertTmp4.set(null, null, null, null);
		curr2.hasUV = curr2.hasPosition = curr2.hasNormal = true;
		short i1, i2, i3 = 0, i4 = 0;

		builder.ensureVertices(2 * (divisions + 1));
		builder.ensureRectangleIndices(divisions);
		for (int i = 0; i <= divisions; i++) {
			angle = ao + step * i;
			u = 1f - us * i;
			curr1.position.set(MathUtils.cos(angle) * hw, 0f, MathUtils.sin(angle) * hd);
			curr1.normal.set(curr1.position).nor();
			curr1.position.y = -hh;
			curr1.uv.set(u, 1);
			curr2.position.set(curr1.position);
			curr2.normal.set(curr1.normal);
			curr2.position.y = hh;
			curr2.uv.set(u, 0);
			i2 = builder.vertex(curr1);
			i1 = builder.vertex(curr2);
			if (i != 0) builder.rect(i3, i1, i2, i4); // FIXME don't duplicate lines and points
			i4 = i2;
			i3 = i1;
		}
		if (close) {
			EllipseShapeBuilder.build(builder, width, depth, 0, 0, divisions, 0, hh, 0, 0, 1, 0, 1, 0, 0, 0, 0, 1, angleFrom,
				angleTo);
			EllipseShapeBuilder.build(builder, width, depth, 0, 0, divisions, 0, -hh, 0, 0, -1, 0, -1, 0, 0, 0, 0, 1,
				180f - angleTo, 180f - angleFrom);
		}
	}
}

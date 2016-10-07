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

import com.badlogic.gdx.utils.GdxRuntimeException;
/** Helper class with static methods to build capsule shapes using {@link MeshPartBuilder}.
 * @author xoppa */
public class CapsuleShapeBuilder extends BaseShapeBuilder {
	public static void build (MeshPartBuilder builder, float radius, float height, int divisions) {
		if (height < 2f * radius) throw new GdxRuntimeException("Height must be at least twice the radius");
		final float d = 2f * radius;
		CylinderShapeBuilder.build(builder, d, height - d, d, divisions, 0, 360, false);
		SphereShapeBuilder.build(builder, matTmp1.setToTranslation(0, .5f * (height - d), 0), d, d, d, divisions, divisions, 0,
			360, 0, 90);
		SphereShapeBuilder.build(builder, matTmp1.setToTranslation(0, -.5f * (height - d), 0), d, d, d, divisions, divisions, 0,
			360, 90, 180);
	}
}

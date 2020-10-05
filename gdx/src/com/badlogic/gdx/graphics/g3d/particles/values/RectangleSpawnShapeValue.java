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

package com.badlogic.gdx.graphics.g3d.particles.values;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

/** Encapsulate the formulas to spawn a particle on a rectangle shape.
 * @author Inferno */
public final class RectangleSpawnShapeValue extends PrimitiveSpawnShapeValue {
	public RectangleSpawnShapeValue (RectangleSpawnShapeValue value) {
		super(value);
		load(value);
	}

	public RectangleSpawnShapeValue () {
	}

	@Override
	public void spawnAux (Vector3 vector, float percent) {
		float width = spawnWidth + (spawnWidthDiff * spawnWidthValue.getScale(percent));
		float height = spawnHeight + (spawnHeightDiff * spawnHeightValue.getScale(percent));
		float depth = spawnDepth + (spawnDepthDiff * spawnDepthValue.getScale(percent));
		// Where generate the point, on edges or inside ?
		if (edges) {
			int a = MathUtils.random(-1, 1);
			float tx = 0, ty = 0, tz = 0;
			if (a == -1) {
				tx = MathUtils.random(1) == 0 ? -width / 2 : width / 2;
				if (tx == 0) {
					ty = MathUtils.random(1) == 0 ? -height / 2 : height / 2;
					tz = MathUtils.random(1) == 0 ? -depth / 2 : depth / 2;
				} else {
					ty = MathUtils.random(height) - height / 2;
					tz = MathUtils.random(depth) - depth / 2;
				}
			} else if (a == 0) {
				// Z
				tz = MathUtils.random(1) == 0 ? -depth / 2 : depth / 2;
				if (tz == 0) {
					ty = MathUtils.random(1) == 0 ? -height / 2 : height / 2;
					tx = MathUtils.random(1) == 0 ? -width / 2 : width / 2;
				} else {
					ty = MathUtils.random(height) - height / 2;
					tx = MathUtils.random(width) - width / 2;
				}
			} else {
				// Y
				ty = MathUtils.random(1) == 0 ? -height / 2 : height / 2;
				if (ty == 0) {
					tx = MathUtils.random(1) == 0 ? -width / 2 : width / 2;
					tz = MathUtils.random(1) == 0 ? -depth / 2 : depth / 2;
				} else {
					tx = MathUtils.random(width) - width / 2;
					tz = MathUtils.random(depth) - depth / 2;
				}
			}
			vector.x = tx;
			vector.y = ty;
			vector.z = tz;
		} else {
			vector.x = MathUtils.random(width) - width / 2;
			vector.y = MathUtils.random(height) - height / 2;
			vector.z = MathUtils.random(depth) - depth / 2;
		}
	}

	@Override
	public SpawnShapeValue copy () {
		return new RectangleSpawnShapeValue(this);
	}
}

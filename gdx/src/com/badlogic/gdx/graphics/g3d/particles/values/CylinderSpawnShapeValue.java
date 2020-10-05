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

/** Encapsulate the formulas to spawn a particle on a cylinder shape.
 * @author Inferno */
public final class CylinderSpawnShapeValue extends PrimitiveSpawnShapeValue {

	public CylinderSpawnShapeValue (CylinderSpawnShapeValue cylinderSpawnShapeValue) {
		super(cylinderSpawnShapeValue);
		load(cylinderSpawnShapeValue);
	}

	public CylinderSpawnShapeValue () {
	}

	@Override
	public void spawnAux (Vector3 vector, float percent) {
		// Generate the point on the surface of the sphere
		float width = spawnWidth + (spawnWidthDiff * spawnWidthValue.getScale(percent));
		float height = spawnHeight + (spawnHeightDiff * spawnHeightValue.getScale(percent));
		float depth = spawnDepth + (spawnDepthDiff * spawnDepthValue.getScale(percent));

		float radiusX, radiusZ;
		float hf = height / 2;
		float ty = MathUtils.random(height) - hf;

		// Where generate the point, on edges or inside ?
		if (edges) {
			radiusX = width / 2;
			radiusZ = depth / 2;
		} else {
			radiusX = MathUtils.random(width) / 2;
			radiusZ = MathUtils.random(depth) / 2;
		}

		float spawnTheta = 0;

		// Generate theta
		boolean isRadiusXZero = radiusX == 0, isRadiusZZero = radiusZ == 0;
		if (!isRadiusXZero && !isRadiusZZero)
			spawnTheta = MathUtils.random(360f);
		else {
			if (isRadiusXZero)
				spawnTheta = MathUtils.random(1) == 0 ? -90 : 90;
			else if (isRadiusZZero) spawnTheta = MathUtils.random(1) == 0 ? 0 : 180;
		}

		vector.set(radiusX * MathUtils.cosDeg(spawnTheta), ty, radiusZ * MathUtils.sinDeg(spawnTheta));
	}

	@Override
	public SpawnShapeValue copy () {
		return new CylinderSpawnShapeValue(this);
	}

}

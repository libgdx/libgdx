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
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

/** Encapsulate the formulas to spawn a particle on a ellipse shape.
 * @author Inferno */
public final class EllipseSpawnShapeValue extends PrimitiveSpawnShapeValue {
	SpawnSide side = SpawnSide.both;

	public EllipseSpawnShapeValue (EllipseSpawnShapeValue value) {
		super(value);
		load(value);
	}

	public EllipseSpawnShapeValue () {
	}

	@Override
	public void spawnAux (Vector3 vector, float percent) {
		// Generate the point on the surface of the sphere
		float width = spawnWidth + spawnWidthDiff * spawnWidthValue.getScale(percent);
		float height = spawnHeight + spawnHeightDiff * spawnHeightValue.getScale(percent);
		float depth = spawnDepth + spawnDepthDiff * spawnDepthValue.getScale(percent);

		float radiusX, radiusY, radiusZ;
		// Where generate the point, on edges or inside ?
		float minT = 0, maxT = MathUtils.PI2;
		if (side == SpawnSide.top) {
			maxT = MathUtils.PI;
		} else if (side == SpawnSide.bottom) {
			maxT = -MathUtils.PI;
		}
		float t = MathUtils.random(minT, maxT);

		// Where generate the point, on edges or inside ?
		if (edges) {
			if (width == 0) {
				vector.set(0, height / 2 * MathUtils.sin(t), depth / 2 * MathUtils.cos(t));
				return;
			}
			if (height == 0) {
				vector.set(width / 2 * MathUtils.cos(t), 0, depth / 2 * MathUtils.sin(t));
				return;
			}
			if (depth == 0) {
				vector.set(width / 2 * MathUtils.cos(t), height / 2 * MathUtils.sin(t), 0);
				return;
			}

			radiusX = width / 2;
			radiusY = height / 2;
			radiusZ = depth / 2;
		} else {
			radiusX = MathUtils.random(width / 2);
			radiusY = MathUtils.random(height / 2);
			radiusZ = MathUtils.random(depth / 2);
		}

		float z = MathUtils.random(-1, 1f);
		float r = (float)Math.sqrt(1f - z * z);
		vector.set(radiusX * r * MathUtils.cos(t), radiusY * r * MathUtils.sin(t), radiusZ * z);
	}

	public SpawnSide getSide () {
		return side;
	}

	public void setSide (SpawnSide side) {
		this.side = side;
	}

	@Override
	public void load (ParticleValue value) {
		super.load(value);
		EllipseSpawnShapeValue shape = (EllipseSpawnShapeValue)value;
		side = shape.side;
	}

	@Override
	public SpawnShapeValue copy () {
		return new EllipseSpawnShapeValue(this);
	}

	@Override
	public void write (Json json) {
		super.write(json);
		json.writeValue("side", side);
	}

	@Override
	public void read (Json json, JsonValue jsonData) {
		super.read(json, jsonData);
		side = json.readValue("side", SpawnSide.class, jsonData);
	}
}

package com.badlogic.gdx.graphics.g3d.particles.values;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

public class LineSpawnShapeValue extends PrimitiveSpawnShapeValue {

	@Override
	public Vector3 spawn (Vector3 vector, float percent) {
		float width = spawnWidth + (spawnWidthDiff * spawnWidthValue.getScale(percent));
		float height = spawnHeight + (spawnHeightDiff * spawnHeightValue.getScale(percent));
		float depth = spawnDepth + (spawnDepthDiff * spawnDepthValue.getScale(percent));

		float a = MathUtils.random();
		vector.x = a * width;
		vector.y = a * height;
		vector.z = a * depth;
		return vector;
	}

	@Override
	public SpawnShapeValue copy () {
		LineSpawnShapeValue copy = new LineSpawnShapeValue();
		copy.load(this);
		return copy;
	}

}

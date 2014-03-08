package com.badlogic.gdx.graphics.g3d.particles.values;

import com.badlogic.gdx.math.Vector3;

public class PointSpawnShapeValue extends PrimitiveSpawnShapeValue {

	@Override
	public Vector3 spawn (Vector3 vector, float percent) {
		vector.x = spawnWidth + (spawnWidthDiff * spawnWidthValue.getScale(percent));
		vector.y = spawnHeight + (spawnHeightDiff * spawnHeightValue.getScale(percent));
		vector.z = spawnDepth + (spawnDepthDiff * spawnDepthValue.getScale(percent));
		return vector;
	}

	@Override
	public SpawnShapeValue copy () {
		PointSpawnShapeValue copy = new PointSpawnShapeValue();
		copy.load(this);
		return copy;
	}
}

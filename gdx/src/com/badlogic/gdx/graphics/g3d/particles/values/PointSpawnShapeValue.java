package com.badlogic.gdx.graphics.g3d.particles.values;

import com.badlogic.gdx.math.Vector3;

/** Encapsulate the formulas to spawn a particle on a point shape. 
 * @author Inferno */
public final class PointSpawnShapeValue extends PrimitiveSpawnShapeValue {

	public PointSpawnShapeValue (PointSpawnShapeValue value) {
		super(value);
		load(value);
	}

	public PointSpawnShapeValue () {}

	@Override
	public void spawnAux (Vector3 vector, float percent) {
		vector.x = spawnWidth + (spawnWidthDiff * spawnWidthValue.getScale(percent));
		vector.y = spawnHeight + (spawnHeightDiff * spawnHeightValue.getScale(percent));
		vector.z = spawnDepth + (spawnDepthDiff * spawnDepthValue.getScale(percent));
	}

	@Override
	public SpawnShapeValue copy () {
		return new PointSpawnShapeValue(this);
	}
}

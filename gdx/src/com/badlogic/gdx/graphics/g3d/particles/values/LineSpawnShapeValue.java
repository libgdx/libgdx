package com.badlogic.gdx.graphics.g3d.particles.values;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

public class LineSpawnShapeValue extends PrimitiveSpawnShapeValue {

	public LineSpawnShapeValue (LineSpawnShapeValue lineSpawnShapeValue) {
		super(lineSpawnShapeValue);
	}

	public LineSpawnShapeValue () {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void spawnAux (Vector3 vector, float percent) {
		float width = spawnWidth + (spawnWidthDiff * spawnWidthValue.getScale(percent));
		float height = spawnHeight + (spawnHeightDiff * spawnHeightValue.getScale(percent));
		float depth = spawnDepth + (spawnDepthDiff * spawnDepthValue.getScale(percent));

		float a = MathUtils.random();
		vector.x = a * width;
		vector.y = a * height;
		vector.z = a * depth;
	}

	@Override
	public SpawnShapeValue copy () {
		return new LineSpawnShapeValue(this);
	}

}

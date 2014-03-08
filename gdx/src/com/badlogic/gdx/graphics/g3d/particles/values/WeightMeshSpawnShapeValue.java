package com.badlogic.gdx.graphics.g3d.particles.values;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.particles.WeigthMesh;
import com.badlogic.gdx.math.Vector3;

public class WeightMeshSpawnShapeValue extends SpawnShapeValue {
	public WeigthMesh mesh;
	
	@Override
	public Vector3 spawn (Vector3 vector, float percent) {
			return mesh.getRandomPoint(vector);
	}

	@Override
	public SpawnShapeValue copy () {
		WeightMeshSpawnShapeValue copy = new WeightMeshSpawnShapeValue();
		copy.load(this);
		return copy;
	}

}

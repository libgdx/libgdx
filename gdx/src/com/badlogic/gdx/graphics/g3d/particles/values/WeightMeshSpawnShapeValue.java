package com.badlogic.gdx.graphics.g3d.particles.values;

import com.badlogic.gdx.graphics.g3d.particles.WeigthMesh;
import com.badlogic.gdx.math.Vector3;

public final class WeightMeshSpawnShapeValue extends SpawnShapeValue {
	public WeigthMesh mesh;
	
	public WeightMeshSpawnShapeValue(WeightMeshSpawnShapeValue value){
		super(value);
		load(value);
	}
	
	public WeightMeshSpawnShapeValue () {}

	@Override
	public void spawnAux (Vector3 vector, float percent) {
		mesh.getRandomPoint(vector);
	}

	@Override
	public SpawnShapeValue copy () {
		return new WeightMeshSpawnShapeValue(this);
	}

}

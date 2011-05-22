package com.badlogic.gdx.graphics.g3d.model.still;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.model.SubMesh;
import com.badlogic.gdx.math.collision.BoundingBox;

public class StillSubMesh extends SubMesh {
	public Mesh mesh;
	public int primitiveType;
	
	@Override public void getBoundingBox (BoundingBox bbox) {	
		mesh.calculateBoundingBox(bbox);
	}
}

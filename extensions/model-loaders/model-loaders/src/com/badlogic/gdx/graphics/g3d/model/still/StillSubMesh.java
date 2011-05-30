package com.badlogic.gdx.graphics.g3d.model.still;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.model.SubMesh;
import com.badlogic.gdx.math.collision.BoundingBox;

public class StillSubMesh extends SubMesh {
	final public Mesh mesh;
	final public int primitiveType;
	
	public StillSubMesh (String name, Mesh mesh, int primitiveType) {
		this.name = name;
		this.mesh = mesh;
		this.primitiveType = primitiveType;
	}
	
	@Override public void getBoundingBox (BoundingBox bbox) {	
		mesh.calculateBoundingBox(bbox);
	}
}

package com.badlogic.gdx.graphics.g3d.model;

import com.badlogic.gdx.graphics.g3d.materials.Material;
import com.badlogic.gdx.math.collision.BoundingBox;

public abstract class SubMesh {
	public String name;
	public Material material;
	
	public abstract void getBoundingBox(BoundingBox bbox);
}

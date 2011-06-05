package com.badlogic.gdx.graphics.g3d.model;

import com.badlogic.gdx.graphics.g3d.materials.Material;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.collision.BoundingBox;

public interface Model {
	public void render();

	public void render(ShaderProgram program);
	public Model getSubModel(String... subMeshNames);
	public SubMesh getSubMesh(String name);
	public SubMesh[] getSubMeshes();
	public void getBoundingBox(BoundingBox bbox);
	public void setMaterials(Material... materials);
	public void setMaterial(Material material);	
}

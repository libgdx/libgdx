package com.badlogic.gdx.graphics.g3d.model;

import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.model.skeleton.SkeletonSubMesh;

public interface Model {	
	public void render();
	public SubMesh getSubMesh(String name);
	public SubMesh[] getSubMeshes();
	public void setMaterials(Material ... materials);	
	public void setMaterial(Material material);
}

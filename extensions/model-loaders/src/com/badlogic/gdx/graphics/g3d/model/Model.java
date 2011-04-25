package com.badlogic.gdx.graphics.g3d.model;

import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.model.skeleton.SkeletonSubMesh;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public interface Model {	
	public void render();
	public void render(ShaderProgram program);
	public SubMesh getSubMesh(String name);
	public SubMesh[] getSubMeshes();
	public void setMaterials(Material ... materials);	
	public void setMaterial(Material material);
}

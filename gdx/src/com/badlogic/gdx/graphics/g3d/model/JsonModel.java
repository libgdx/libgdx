package com.badlogic.gdx.graphics.g3d.model;

import com.badlogic.gdx.utils.GdxRuntimeException;

public class JsonModel {
	public String version;
	public ModelMesh[] meshes;
	public ModelMaterial[] materials;
	public ModelNode[] nodes;
	public ModelAnimation[] animations;
	
	public void addMesh(ModelMesh mesh) {
		for(ModelMesh other: meshes) {
			if(other.id.equals(mesh.id)) {
				throw new GdxRuntimeException("Mesh with id '" + other.id + "' already in model");
			}
		}
	}
}

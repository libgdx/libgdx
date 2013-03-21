package com.badlogic.gdx.graphics.g3d.loader;

import com.badlogic.gdx.utils.GdxRuntimeException;

public class JsonModel {
	public String version;
	public JsonMesh[] meshes;
	public JsonMaterial[] materials;
	public JsonNode[] nodes;
	public JsonAnimation[] animations;
	
	public void addMesh(JsonMesh mesh) {
		for(JsonMesh other: meshes) {
			if(other.id.equals(mesh.id)) {
				throw new GdxRuntimeException("Mesh with id '" + other.id + "' already in model");
			}
		}
	}
}

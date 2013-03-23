package com.badlogic.gdx.graphics.g3d.model.data;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g3d.old.loaders.ModelLoader;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * Returned by a {@link ModelLoader}, contains meshes, materials, nodes
 * and animations. OpenGL resources like textures or vertex buffer objects
 * are not stored. Instead, a ModelData instance needs to be converted to a 
 * Model first.
 * @author badlogic
 *
 */
public class ModelData {
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

package com.badlogic.gdx.graphics.g3d.model.still;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.g3d.materials.Material;
import com.badlogic.gdx.graphics.g3d.model.Model;
import com.badlogic.gdx.graphics.g3d.model.SubMesh;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.collision.BoundingBox;

public class StillModel implements Model {
	final public StillSubMesh[] subMeshes;

	public StillModel(StillSubMesh[] subMeshes) {
		this.subMeshes = subMeshes;
	}

	@Override
	public void render() {
		int len = subMeshes.length;
		for (int i = 0; i < len; i++) {
			StillSubMesh subMesh = subMeshes[i];
			if (i == 0) {
				subMesh.material.bind();
			} else if (!subMeshes[i - 1].material.equals(subMesh.material)) {
				subMesh.material.bind();
			}
			subMesh.mesh.render(subMesh.primitiveType);
		}
	}

	@Override
	public void render(ShaderProgram program) {
		// TODO Auto-generated method stub

	}

	@Override
	public Model getSubModel(String... subMeshNames) {
		ArrayList<SubMesh> subMeshes = new ArrayList<SubMesh>();
		for (String name : subMeshNames)
			for (StillSubMesh subMesh : this.subMeshes)
				if (name.equals(subMesh.name))
					subMeshes.add(subMesh);
		if (subMeshes.size() > 0)
			return new StillModel(subMeshes.toArray(new StillSubMesh[subMeshes
					.size()]));
		return null;
	}

	@Override
	public StillSubMesh getSubMesh(String name) {
		for (StillSubMesh subMesh : subMeshes) {
			if (subMesh.name.equals(name))
				return subMesh;
		}
		return null;
	}

	@Override
	public SubMesh[] getSubMeshes() {
		return subMeshes;
	}

	@Override
	public void setMaterials(Material... materials) {
		if (materials.length != subMeshes.length)
			throw new UnsupportedOperationException(
					"number of materials must equal number of sub-meshes");
		int len = materials.length;
		for (int i = 0; i < len; i++) {
			subMeshes[i].material = materials[i];
		}
	}

	@Override
	public void setMaterial(Material material) {
		int len = subMeshes.length;
		for (int i = 0; i < len; i++) {
			subMeshes[i].material = material;
		}
	}

	private final static BoundingBox tmpBox = new BoundingBox();

	@Override
	public void getBoundingBox(BoundingBox bbox) {
		bbox.inf();
		for (int i = 0; i < subMeshes.length; i++) {
			subMeshes[i].mesh.calculateBoundingBox(tmpBox);
			bbox.ext(tmpBox);
		}
	}
}

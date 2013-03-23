package com.badlogic.gdx.graphics.g3d.model;

import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

public class ModelNode {
	public String id;
	public int boneId = -1;
	public Vector3 translation;
	public Vector3 rotation;
	public Vector3 scale;
	public String meshId;
	public ModelMeshPartMaterial[] meshPartMaterials;
	public ModelNode[] children;
}

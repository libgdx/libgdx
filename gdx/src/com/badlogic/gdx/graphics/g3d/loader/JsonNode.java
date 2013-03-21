package com.badlogic.gdx.graphics.g3d.loader;

import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

public class JsonNode {
	public String id;
	public int boneId = -1;
	public Vector3 translation;
	public Vector3 rotation;
	public Vector3 scale;
	public String meshId;
	public JsonMeshPartMaterial[] meshPartMaterials;
	public JsonNode[] children;
}

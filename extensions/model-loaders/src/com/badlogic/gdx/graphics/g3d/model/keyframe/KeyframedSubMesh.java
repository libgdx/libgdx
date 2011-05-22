package com.badlogic.gdx.graphics.g3d.model.keyframe;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.materials.Material;
import com.badlogic.gdx.graphics.g3d.model.SubMesh;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.ObjectMap;

public class KeyframedSubMesh extends SubMesh {	
	public Mesh mesh;	
	public final ObjectMap<String, KeyframedAnimation> animations = new ObjectMap<String, KeyframedAnimation>();
	public int primitiveType;
	
	@Override public void getBoundingBox (BoundingBox bbox) {	
		mesh.calculateBoundingBox(bbox);
	}
}

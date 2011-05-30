package com.badlogic.gdx.graphics.g3d.model.keyframe;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.materials.Material;
import com.badlogic.gdx.graphics.g3d.model.SubMesh;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.ObjectMap;

public class KeyframedSubMesh extends SubMesh {	
	public final String name;
	public final Mesh mesh;	
	public final float[] blendedVertices;
	public final ObjectMap<String, KeyframedAnimation> animations;
	public final int primitiveType;
	
	public KeyframedSubMesh(String name, Mesh mesh, float[] blendedVertices, ObjectMap<String, KeyframedAnimation> animations, int primitiveType) {
		this.name = name;
		this.mesh = mesh;
		this.blendedVertices = blendedVertices;
		this.animations = animations;
		this.primitiveType = primitiveType;
	}
	
	@Override public void getBoundingBox (BoundingBox bbox) {	
		mesh.calculateBoundingBox(bbox);
	}
}

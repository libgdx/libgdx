package com.badlogic.gdx.graphics.g3d.model.skeleton;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.materials.Material;
import com.badlogic.gdx.graphics.g3d.model.SubMesh;
import com.badlogic.gdx.math.collision.BoundingBox;

public class SkeletonSubMesh extends SubMesh {
	public String name;			
	public Mesh mesh;
	public short[] indices;
	public float[] vertices;
	public float[] skinnedVertices;
	public int primitiveType;
	public int[][] boneAssignments;
	public float[][] boneWeights;	
	
	@Override public void getBoundingBox (BoundingBox bbox) {	
		mesh.calculateBoundingBox(bbox);
	}
}

package com.badlogic.gdx.graphics.g3d.model.skeleton;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.materials.Material;
import com.badlogic.gdx.graphics.g3d.model.SubMesh;

public class SkeletonSubMesh extends SubMesh {
	public String name;		
	public Material material;
	public Mesh mesh;
	public short[] indices;
	public float[] vertices;
	public float[] skinnedVertices;
	public int primitiveType;
	public int[][] boneAssignments;
	public float[][] boneWeights;	
}

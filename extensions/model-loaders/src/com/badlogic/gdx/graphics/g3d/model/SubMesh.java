package com.badlogic.gdx.graphics.g3d.model;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;

public class SubMesh {
	public String name;		
	public Texture[] textures;
	public Mesh mesh;
	public short[] indices;
	public float[] vertices;
	public float[] skinnedVertices;
	public int primitiveType;
	public int[][] boneAssignments;
	public float[][] boneWeights;	
}

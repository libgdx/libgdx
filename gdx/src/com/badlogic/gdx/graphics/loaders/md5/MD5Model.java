package com.badlogic.gdx.graphics.loaders.md5;

public class MD5Model 
{
	public int numJoints;
	public MD5Joints baseSkeleton;
	public MD5Mesh[] meshes;
	
	public int getNumVertices( )
	{
		int numVertices = 0;
		
		for( int i = 0; i < meshes.length; i++ )
			numVertices += meshes[i].numVertices;
		
		return numVertices;
	}
	
	public int getNumTriangles( )
	{
		int numTriangles = 0;
		
		for( int i = 0; i < meshes.length; i++ )
			numTriangles += meshes[i].numTriangles;
		
		return numTriangles;
	}
}

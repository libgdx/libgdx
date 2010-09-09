package com.badlogic.gdx.graphics.loaders.md5;

public class MD5Model 
{
	public MD5Joint[] baseSkeleton;
	public MD5Mesh[] meshes;
	
	public int getNumVertices( )
	{
		int numVertices = 0;
		
		for( int i = 0; i < meshes.length; i++ )
			numVertices += meshes[i].vertices.length;
		
		return numVertices;
	}
	
	public int getNumTriangles( )
	{
		int numTriangles = 0;
		
		for( int i = 0; i < meshes.length; i++ )
			numTriangles += meshes[i].triangles.length;
		
		return numTriangles;
	}
}

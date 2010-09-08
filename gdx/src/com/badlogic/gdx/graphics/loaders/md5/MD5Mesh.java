package com.badlogic.gdx.graphics.loaders.md5;

import com.badlogic.gdx.math.Vector3;

public class MD5Mesh 
{
	public MD5Vertex[] vertices;
	public MD5Triangle[] triangles;
	public MD5Weight[] weights;	
	public String shader;
	
	public float[] createVertexArray( )
	{
		float[] verts = new float[vertices.length * 5];
		for( int i = 0, j = 3; i < vertices.length; i++ )
		{
			verts[j++] = vertices[i].st.x;
			verts[j++] = vertices[i].st.y;
			j+=3;
		}
		
		return verts;
	}
	
	public short[] createIndexArray( )
	{
		short[] indices = new short[triangles.length*3];
		for( int k = 0, i = 0; i < triangles.length; i++ )
			for( int j = 0; j < 3; j++, k++ )
				indices[k] = (short)triangles[i].indices[j];
		return indices;
	}
	
	private final Vector3 finalVertex = new Vector3( );
	private final MD5Quaternion quat = new MD5Quaternion( );
	private final Vector3 weightVertex = new Vector3( );
	public void calculateVertices( MD5Joint[] skeleton, float[] verts )
	{
		for( int i = 0, k = 0; i < vertices.length; i++ )
		{
			finalVertex.set( 0, 0, 0 );
			int weightIndex = vertices[i].start;
			for( int j = 0; j < vertices[i].count; j++, weightIndex++ )
			{
				MD5Weight weight = weights[weightIndex];
				MD5Joint joint = skeleton[weight.joint];
				
				quat.set( joint.orient );
				weightVertex.set( weight.pos );
				quat.rotate( weightVertex );
				
				finalVertex.x += (joint.pos.x + weightVertex.x) * weight.bias;
				finalVertex.y += (joint.pos.y + weightVertex.y) * weight.bias;
				finalVertex.z += (joint.pos.z + weightVertex.z) * weight.bias;
			}
			
			verts[k++] = finalVertex.x;
			verts[k++] = finalVertex.y;
			verts[k++] = finalVertex.z;
			k+=2;
		}
	}
}

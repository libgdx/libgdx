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
				
//				quat.x = joint.orient.x;
//				quat.y = joint.orient.y;
//				quat.z = joint.orient.z;
//				quat.w = joint.orient.w;
//				
//				weightVertex.x = weight.pos.x;
//				weightVertex.y = weight.pos.y;
//				weightVertex.z = weight.pos.z;
//				
//				quat.rotate( weightVertex );
//				
//				finalVertex.x += (joint.pos.x + weightVertex.x ) * weight.bias;
//				finalVertex.y += (joint.pos.y + weightVertex.y ) * weight.bias;
//				finalVertex.z += (joint.pos.z + weightVertex.z ) * weight.bias;
								
				float vx = weight.pos.x, vy = weight.pos.y, vz = weight.pos.z;
				float qx = joint.orient.x, qy = joint.orient.y, qz = joint.orient.z, qw = joint.orient.w;
				float ix = -qx, iy = -qy, iz = -qz, iw = qw;				
				
				float tw = -qx * vx - qy * vy - qz * vz;
				float tx =  qw * vx + qy * vz - qz * vy;
				float ty =  qw * vy + qz * vx - qx * vz;
				float tz =  qw * vz + qx * vy - qy * vx;
				
				vx = tx * iw + tw * ix + ty * iz - tz * iy;
				vy = ty * iw + tw * iy + tz * ix - tx * iz;
				vz = tz * iw + tw * iz + tx * iy - ty * ix;				
				
				finalVertex.x += (joint.pos.x + vx) * weight.bias;
				finalVertex.y += (joint.pos.y + vy) * weight.bias;
				finalVertex.z += (joint.pos.z + vz) * weight.bias;
			}
			
			verts[k++] = finalVertex.x;
			verts[k++] = finalVertex.y;
			verts[k++] = finalVertex.z;
			k+=2;
		}
	}	
}

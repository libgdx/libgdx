package com.badlogic.gdx.graphics.loaders.md5;

import com.badlogic.gdx.math.Vector3;

public class MD5Mesh 
{	
	public String shader;
	
	public int numVertices;
	public int numWeights;
	public int numTriangles;
	/** s, t, start, count **/	
	public float[] vertices;
	/** joint, bias, pos.x, pos.y, pos.z **/
	public float[] weights;
	/** idx 1, idx 2, idx 3 **/
	public short[] indices; 
	
	public float[] createVertexArray( )
	{
		float[] verts = new float[numVertices * 5];
		for( int i = 0, j = 3; i < vertices.length; i+=4 )
		{
			verts[j++] = vertices[i];
			verts[j++] = vertices[i+1];
			j+=3;
		}
		
		return verts;
	}
	
	public short[] getIndices( )
	{
		return indices;
	}
		
	private final Vector3 finalVertex = new Vector3( );	
	public void calculateVertices( MD5Joints skeleton, float[] verts )
	{
		for( int vertexOffset = 2, k = 0; vertexOffset < vertices.length; vertexOffset += 4 )
		{
			finalVertex.x = finalVertex.y = finalVertex.z = 0;
			
			int weightOffset = (int)vertices[vertexOffset];
			int weightCount = (int)vertices[vertexOffset+1];
			weightOffset = (weightOffset << 2) + weightOffset;
			
			for( int j = 0; j < weightCount; j++ )
			{								
				int jointOffset = (int)weights[weightOffset++] << 3;				
				float bias = weights[weightOffset++];				
				float vx = weights[weightOffset++], vy = weights[weightOffset++], vz = weights[weightOffset++];
				float qx = skeleton.joints[jointOffset+4], qy = skeleton.joints[jointOffset+5], qz = skeleton.joints[jointOffset+6], qw = skeleton.joints[jointOffset+7];
				float ix = -qx, iy = -qy, iz = -qz, iw = qw;				
				
				float tw = -qx * vx - qy * vy - qz * vz;
				float tx =  qw * vx + qy * vz - qz * vy;
				float ty =  qw * vy + qz * vx - qx * vz;
				float tz =  qw * vz + qx * vy - qy * vx;
				
				vx = tx * iw + tw * ix + ty * iz - tz * iy;
				vy = ty * iw + tw * iy + tz * ix - tx * iz;
				vz = tz * iw + tw * iz + tx * iy - ty * ix;				
								
				finalVertex.x += (skeleton.joints[jointOffset+1] + vx) * bias;
				finalVertex.y += (skeleton.joints[jointOffset+2] + vy) * bias;
				finalVertex.z += (skeleton.joints[jointOffset+3] + vz) * bias;
			}
			
			verts[k++] = finalVertex.x;
			verts[k++] = finalVertex.y;
			verts[k++] = finalVertex.z;
			k+=2;
		}
		
//		for( int i = 0, k = 0; i < vertices.length; i++ )
//		{
//			finalVertex.x = finalVertex.y = finalVertex.z = 0;
//			int weightIndex = vertices[i].start;
//			for( int j = 0; j < vertices[i].count; j++, weightIndex++ )
//			{
//				MD5Weight weight = weights[weightIndex];
//				MD5Joint joint = skeleton[weight.joint];			
//								
//				float vx = weight.pos.x, vy = weight.pos.y, vz = weight.pos.z;
//				float qx = joint.orient.x, qy = joint.orient.y, qz = joint.orient.z, qw = joint.orient.w;
//				float ix = -qx, iy = -qy, iz = -qz, iw = qw;				
//				
//				float tw = -qx * vx - qy * vy - qz * vz;
//				float tx =  qw * vx + qy * vz - qz * vy;
//				float ty =  qw * vy + qz * vx - qx * vz;
//				float tz =  qw * vz + qx * vy - qy * vx;
//				
//				vx = tx * iw + tw * ix + ty * iz - tz * iy;
//				vy = ty * iw + tw * iy + tz * ix - tx * iz;
//				vz = tz * iw + tw * iz + tx * iy - ty * ix;				
//				
//				finalVertex.x += (joint.pos.x + vx) * weight.bias;
//				finalVertex.y += (joint.pos.y + vy) * weight.bias;
//				finalVertex.z += (joint.pos.z + vz) * weight.bias;
//			}
//			
//			verts[k++] = finalVertex.x;
//			verts[k++] = finalVertex.y;
//			verts[k++] = finalVertex.z;
//			k+=2;
//		}
	}	
}

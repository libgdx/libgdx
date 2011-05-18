/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.badlogic.gdx.graphics.g3d.loaders.md5;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

/**
 * Represents an MD5 (Doom 3) mesh, with or without normal data.
 * @author Mario Zechner <contact@badlogicgames.com>, Nathan Sweet <admin@esotericsoftware.com>, Dave Clayton <contact@redskyforge.com>
 *
 */
public class MD5Mesh {
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
	
	public int floatsPerVertex;
	public int floatsPerWeight;

	public float[] createVertexArray () {
		return createVertexArray(5);
	}
	
	public float[] createVertexArray (int stride) {
		float[] verts = new float[numVertices * stride];
		int vertex_stride = floatsPerVertex;
		for( int i = 0, j = 0; i < vertices.length; i+=vertex_stride )
		{
			j+=3; // skip vertex
			verts[j++] = vertices[i]; // tex coord 1
			verts[j++] = vertices[i+1]; // tex coord 2
			if(stride == 8) // skip normals
				j+=3;
		}
		
		return verts;
	}

	public short[] getIndices () {
		return indices;
	}

	public void calculateVerticesN( MD5Joints skeleton, float[] verts, BoundingBox bbox )
	{
		calculateVerticesN( skeleton, weights, vertices, verts, floatsPerVertex, floatsPerWeight, bbox );
	}
	
	public void calculateVertices( MD5Joints skeleton, float[] verts, BoundingBox bbox ) {
		calculateVertices( skeleton, weights, vertices, verts, floatsPerVertex, floatsPerWeight, bbox );
	}

	static Vector3 bn = new Vector3();
	public static void calculateVerticesN( MD5Joints skeleton, float[] weights, float vertices[], float[] verts,
			int vstride, int wstride, BoundingBox bbox )
	{
		for( int vertexOffset = 2, k = 0; vertexOffset < vertices.length; vertexOffset += vstride )
		{
			float finalX = 0;
			float finalY = 0;
			float finalZ = 0;
			
			int weightOffset = (int)vertices[vertexOffset];
			int weightCount = (int)vertices[vertexOffset+1];
			weightOffset = weightOffset*wstride;
			
			// get the bind pose normal
			bn.set(vertices[vertexOffset+2], vertices[vertexOffset+3], vertices[vertexOffset+4]);
			/*float bnx = vertices[vertexOffset+2];
			float bny = vertices[vertexOffset+3];
			float bnz = vertices[vertexOffset+4];*/


			for( int j = 0; j < weightCount; j++ )
			{
				int jointOffset = (int)weights[weightOffset++] << 3;				
				float bias = weights[weightOffset++];				
				float vx = weights[weightOffset++];
				float vy = weights[weightOffset++];
				float vz = weights[weightOffset++];
				//weightOffset += 3;
				// get the weight normal
				vn.set(weights[weightOffset++],
						weights[weightOffset++],
						weights[weightOffset++]);
				
				float qx = skeleton.joints[jointOffset+4];
				float qy = skeleton.joints[jointOffset+5];
				float qz = skeleton.joints[jointOffset+6];
				float qw = skeleton.joints[jointOffset+7];

				// add to the bind pose normal:
				quat.x = qx; quat.y = qy; quat.z = qz; quat.w = qw;
				quat.rotate(vn);
				vn.mul(bias);
				bn.add(vn);
				//bnx += vn.x; bny += vn.y; bnz += vn.z;
				
				float ix = -qx, iy = -qy, iz = -qz, iw = qw;				
				
				float tw = -qx * vx - qy * vy - qz * vz;
				float tx =  qw * vx + qy * vz - qz * vy;
				float ty =  qw * vy + qz * vx - qx * vz;
				float tz =  qw * vz + qx * vy - qy * vx;
				
				vx = tx * iw + tw * ix + ty * iz - tz * iy;
				vy = ty * iw + tw * iy + tz * ix - tx * iz;
				vz = tz * iw + tw * iz + tx * iy - ty * ix;				
								
				finalX += (skeleton.joints[jointOffset+1] + vx) * bias;
				finalY += (skeleton.joints[jointOffset+2] + vy) * bias;
				finalZ += (skeleton.joints[jointOffset+3] + vz) * bias;
			}
			
			bbox.ext(finalX, finalY, finalZ);
			
			verts[k++] = finalX;
			verts[k++] = finalY;
			verts[k++] = finalZ;
			k+=2;
			
			// normals
			bn.nor();

			verts[k++] = bn.x;//bnx;
			verts[k++] = bn.y;//bny;
			verts[k++] = bn.z;//bnz;
			
			// For each weight of a vertex, transform the vertex normal by the inverse joint's orientation
			// quaternion of the weight. You now have the normal in joint's local space.

			// Then when calculating the final vertex positions, you will be able to do the same for the
			// normals, except you won't have to translate from the joint's position when converting from
			// joint's local space to object space.
		}
	}

	public static void calculateVertices( MD5Joints skeleton, float[] weights, float vertices[], float[] verts,
			int vstride, int wstride, BoundingBox bbox ) {
		for( int vertexOffset = 2, k = 0; vertexOffset < vertices.length; vertexOffset += vstride )
		{
			float finalX = 0;
			float finalY = 0;
			float finalZ = 0;
			
			int weightOffset = (int)vertices[vertexOffset];
			int weightCount = (int)vertices[vertexOffset+1];
			weightOffset = (weightOffset << 2) + weightOffset;
			
			for( int j = 0; j < weightCount; j++ )
			{								
				int jointOffset = (int)weights[weightOffset++] << 3;				
				float bias = weights[weightOffset++];				
				float vx = weights[weightOffset++];
				float vy = weights[weightOffset++];
				float vz = weights[weightOffset++];
				
				float qx = skeleton.joints[jointOffset+4];
				float qy = skeleton.joints[jointOffset+5];
				float qz = skeleton.joints[jointOffset+6];
				float qw = skeleton.joints[jointOffset+7];
				
				float ix = -qx, iy = -qy, iz = -qz, iw = qw;				
				
				float tw = -qx * vx - qy * vy - qz * vz;
				float tx =  qw * vx + qy * vz - qz * vy;
				float ty =  qw * vy + qz * vx - qx * vz;
				float tz =  qw * vz + qx * vy - qy * vx;
				
				vx = tx * iw + tw * ix + ty * iz - tz * iy;
				vy = ty * iw + tw * iy + tz * ix - tx * iz;
				vz = tz * iw + tw * iz + tx * iy - ty * ix;				
								
				finalX += (skeleton.joints[jointOffset+1] + vx) * bias;
				finalY += (skeleton.joints[jointOffset+2] + vy) * bias;
				finalZ += (skeleton.joints[jointOffset+3] + vz) * bias;
			}
			
			bbox.ext(finalX, finalY, finalZ);

			verts[k++] = finalX;
			verts[k++] = finalY;
			verts[k++] = finalZ;
			k+=2;
		}
	}	

	public void calculateVerticesJni (MD5Joints skeleton, float[] verts) {
		MD5Jni.calculateVertices(skeleton.joints, weights, vertices, verts, numVertices);
	}

	public void calculateNormalsBind(MD5Joints bindPoseSkeleton, float[] verts) {
		calculateNormalsBind( bindPoseSkeleton, weights, vertices, indices, verts, floatsPerVertex, floatsPerWeight );
	}
	
	static Vector3 _A = new Vector3();
	static Vector3 _B = new Vector3();
	static Vector3 _n = new Vector3();
	private static Vector3 calcNor(Vector3 v1, Vector3 v2, Vector3 v3)
	{
		_A = v2.cpy();
		_A.sub(v1);
		_B = v3.cpy();
		_B.sub(v2);
		_n = _A.crs(_B).nor();
		return _n;
	}
	
	static MD5Quaternion quat = new MD5Quaternion();
	static Vector3 vn = new Vector3();
	public static void calculateNormalsBind( MD5Joints skeleton, float[] weights, float vertices[], short indices[],
			float verts[], int vstride, int wstride )
	{
		for( int vertexOffset = 2, k = 0; vertexOffset < vertices.length; vertexOffset += vstride )
		{
			float finalX = 0;
			float finalY = 0;
			float finalZ = 0;
			
			int weightOffset = (int)vertices[vertexOffset];
			int weightCount = (int)vertices[vertexOffset+1];
			weightOffset = weightOffset*wstride;
			
			for( int j = 0; j < weightCount; j++ )
			{								
				int jointOffset = (int)weights[weightOffset++] << 3;				
				float bias = weights[weightOffset++];				
				float vx = weights[weightOffset++];
				float vy = weights[weightOffset++];
				float vz = weights[weightOffset++];
				weightOffset += 3; // skip normal
				
				float qx = skeleton.joints[jointOffset+4];
				float qy = skeleton.joints[jointOffset+5];
				float qz = skeleton.joints[jointOffset+6];
				float qw = skeleton.joints[jointOffset+7];
				
				float ix = -qx, iy = -qy, iz = -qz, iw = qw;				
				
				float tw = -qx * vx - qy * vy - qz * vz;
				float tx =  qw * vx + qy * vz - qz * vy;
				float ty =  qw * vy + qz * vx - qx * vz;
				float tz =  qw * vz + qx * vy - qy * vx;
				
				vx = tx * iw + tw * ix + ty * iz - tz * iy;
				vy = ty * iw + tw * iy + tz * ix - tx * iz;
				vz = tz * iw + tw * iz + tx * iy - ty * ix;				
								
				finalX += (skeleton.joints[jointOffset+1] + vx) * bias;
				finalY += (skeleton.joints[jointOffset+2] + vy) * bias;
				finalZ += (skeleton.joints[jointOffset+3] + vz) * bias;
			}
			
			verts[k++] = finalX;
			verts[k++] = finalY;
			verts[k++] = finalZ;
			k+=2;
			
			k+=3;
		}
		// compute normals in bind pose
		for(int i = 0; i < indices.length; i+=3)
		{
			// only doing this once so let's use data structures
			short i1 = indices[i];
			short i2 = indices[i+1];
			short i3 = indices[i+2];
			int vo1 = i1*8;
			int vo2 = i2*8;
			int vo3 = i3*8;
			
			Vector3 v1 = new Vector3(verts[vo1], verts[vo1+1], verts[vo1+2]);
			Vector3 v2 = new Vector3(verts[vo2], verts[vo2+1], verts[vo2+2]);
			Vector3 v3 = new Vector3(verts[vo3], verts[vo3+1], verts[vo3+2]);

			// calculate face normal. Clockwise winding.
			Vector3 fn = calcNor(v1, v2, v3);
			
			// store them back in the mesh's vertex array.
			int ovo1 = i1*7; int ovo2 = i2*7; int ovo3 = i3*7;
			
			vertices[ovo1+4] += fn.x; vertices[ovo1+5] += fn.y; vertices[ovo1+6] += fn.z;
			vertices[ovo2+4] += fn.x; vertices[ovo2+5] += fn.y; vertices[ovo2+6] += fn.z;
			vertices[ovo3+4] += fn.x; vertices[ovo3+5] += fn.y; vertices[ovo3+6] += fn.z;
		}
		
		for(int i=0; i<indices.length; i+=3)
		{
			short i1 = indices[i];
			short i2 = indices[i+1];
			short i3 = indices[i+2];
			int ovo1 = i1*7; int ovo2 = i2*7; int ovo3 = i3*7;

			vn.set(vertices[ovo1+4], vertices[ovo1+5], vertices[ovo1+6]);
			vn.nor();
			vertices[ovo1+4] = vn.x; vertices[ovo1+5] = vn.y; vertices[ovo1+6] = vn.z;
			
			vn.set(vertices[ovo2+4], vertices[ovo2+5], vertices[ovo2+6]);
			vn.nor();
			vertices[ovo2+4] = vn.x; vertices[ovo2+5] = vn.y; vertices[ovo2+6] = vn.z;

			vn.set(vertices[ovo3+4], vertices[ovo3+5], vertices[ovo3+6]);
			vn.nor();
			vertices[ovo3+4] = vn.x; vertices[ovo3+5] = vn.y; vertices[ovo3+6] = vn.z;
		}
		
		// calculate weight normals
		for( int vertexOffset = 2; vertexOffset < vertices.length; vertexOffset += vstride )
		{
			int weightOffset = (int)vertices[vertexOffset];
			int weightCount = (int)vertices[vertexOffset+1];
			weightOffset = weightOffset*wstride;
			
			for( int j = 0; j < weightCount; j++ )
			{								
				int jointOffset = (int)weights[weightOffset++] << 3;		
				// FIXME why aren't these used?
//				float bias = weights[weightOffset++];				
//				float vx = weights[weightOffset++];
//				float vy = weights[weightOffset++];
//				float vz = weights[weightOffset++];
				
				float qx = skeleton.joints[jointOffset+4];
				float qy = skeleton.joints[jointOffset+5];
				float qz = skeleton.joints[jointOffset+6];
				float qw = skeleton.joints[jointOffset+7];
				
				float vnx = vertices[vertexOffset+2];
				float vny = vertices[vertexOffset+3];
				float vnz = vertices[vertexOffset+4];
				
				vn.set(vnx, vny, vnz);
				quat.x = qx; quat.y = qy; quat.z = qz; quat.w = qw;
				quat.invert();
				quat.rotate(vn);

				weights[weightOffset++] += vn.x;
				weights[weightOffset++] += vn.y;
				weights[weightOffset++] += vn.z;
			}
		}
		// normalize weight normals
		for( int i = 0; i < weights.length; i += wstride )
		{
			vn.set(weights[i+5], weights[i+6], weights[i+7]);
			vn.nor();
			weights[i+5] = vn.x; weights[i+6] = vn.y; weights[i+7] = vn.z;
		}
	}

	public void read(DataInputStream in) throws IOException {
		shader = in.readUTF();
		numVertices = in.readInt();
		numWeights = in.readInt();
		numTriangles = in.readInt();
		floatsPerVertex = in.readInt();
		floatsPerWeight = in.readInt();
		
		vertices = new float[numVertices*floatsPerVertex];
		indices = new short[numTriangles*3];
		weights = new float[numWeights*floatsPerWeight];
		for(int i=0; i<vertices.length; i++)
		{
			vertices[i] = in.readFloat();
		}
		for(int i=0; i<indices.length; i++)
		{
			indices[i] = in.readShort();
		}
		for(int i=0; i<weights.length; i++)
		{
			weights[i] = in.readFloat();
		}
	}

	public void write(DataOutputStream out) throws IOException {
		out.writeUTF(shader);
		out.writeInt(numVertices);
		out.writeInt(numWeights);
		out.writeInt(numTriangles);
		out.writeInt(floatsPerVertex);
		out.writeInt(floatsPerWeight);
		for(int i=0; i<vertices.length; i++)
		{
			out.writeFloat(vertices[i]);
		}
		for(int i=0; i<indices.length; i++)
		{
			out.writeShort(indices[i]);
		}
		for(int i=0; i<weights.length; i++)
		{
			out.writeFloat(weights[i]);
		}
	}
}

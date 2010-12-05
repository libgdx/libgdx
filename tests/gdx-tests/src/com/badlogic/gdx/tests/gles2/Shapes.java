package com.badlogic.gdx.tests.gles2;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;

public class Shapes {
	public static Mesh genCube() {
		Mesh mesh = new Mesh(true, 24, 36, new VertexAttribute(Usage.Position, 3, "a_position"),
													  new VertexAttribute(Usage.Normal, 3, "a_normal"),
													  new VertexAttribute(Usage.TextureCoordinates, 2, "a_texcoords"));
		
		float[] cubeVerts = {
		      -0.5f, -0.5f, -0.5f, 
		      -0.5f, -0.5f,  0.5f,
		      0.5f, -0.5f,  0.5f,
		      0.5f, -0.5f, -0.5f,
		      -0.5f,  0.5f, -0.5f,
		      -0.5f,  0.5f,  0.5f,
		      0.5f,  0.5f,  0.5f,
		      0.5f,  0.5f, -0.5f,
		      -0.5f, -0.5f, -0.5f,
		      -0.5f,  0.5f, -0.5f,
		      0.5f,  0.5f, -0.5f,
		      0.5f, -0.5f, -0.5f,
		      -0.5f, -0.5f, 0.5f,
		      -0.5f,  0.5f, 0.5f,
		      0.5f,  0.5f, 0.5f, 
		      0.5f, -0.5f, 0.5f,
		      -0.5f, -0.5f, -0.5f,
		      -0.5f, -0.5f,  0.5f,
		      -0.5f,  0.5f,  0.5f,
		      -0.5f,  0.5f, -0.5f,
		      0.5f, -0.5f, -0.5f,
		      0.5f, -0.5f,  0.5f,
		      0.5f,  0.5f,  0.5f,
		      0.5f,  0.5f, -0.5f,
		};
		
		float[] cubeNormals = {
	      0.0f, -1.0f, 0.0f,
	      0.0f, -1.0f, 0.0f,
	      0.0f, -1.0f, 0.0f,
	      0.0f, -1.0f, 0.0f,
	      0.0f, 1.0f, 0.0f,
	      0.0f, 1.0f, 0.0f,
	      0.0f, 1.0f, 0.0f,
	      0.0f, 1.0f, 0.0f,
	      0.0f, 0.0f, -1.0f,
	      0.0f, 0.0f, -1.0f,
	      0.0f, 0.0f, -1.0f,
	      0.0f, 0.0f, -1.0f,
	      0.0f, 0.0f, 1.0f,
	      0.0f, 0.0f, 1.0f,
	      0.0f, 0.0f, 1.0f,
	      0.0f, 0.0f, 1.0f,
	      -1.0f, 0.0f, 0.0f,
	      -1.0f, 0.0f, 0.0f,
	      -1.0f, 0.0f, 0.0f,
	      -1.0f, 0.0f, 0.0f,
	      1.0f, 0.0f, 0.0f,
	      1.0f, 0.0f, 0.0f,
	      1.0f, 0.0f, 0.0f,
	      1.0f, 0.0f, 0.0f,
	   };

	   float[] cubeTex = {
	      0.0f, 0.0f,
	      0.0f, 1.0f,
	      1.0f, 1.0f,
	      1.0f, 0.0f,
	      1.0f, 0.0f,
	      1.0f, 1.0f,
	      0.0f, 1.0f,
	      0.0f, 0.0f,
	      0.0f, 0.0f,
	      0.0f, 1.0f,
	      1.0f, 1.0f,
	      1.0f, 0.0f,
	      0.0f, 0.0f,
	      0.0f, 1.0f,
	      1.0f, 1.0f,
	      1.0f, 0.0f,
	      0.0f, 0.0f,
	      0.0f, 1.0f,
	      1.0f, 1.0f,
	      1.0f, 0.0f,
	      0.0f, 0.0f,
	      0.0f, 1.0f,
	      1.0f, 1.0f,
	      1.0f, 0.0f,
	   };
	   
	   float[] vertices = new float[24*8];
	   int pIdx = 0;
	   int nIdx = 0;
	   int tIdx = 0;
	   for(int i = 0; i < vertices.length; ) {
	   	vertices[i++] = cubeVerts[pIdx++];
	   	vertices[i++] = cubeVerts[pIdx++];
	   	vertices[i++] = cubeVerts[pIdx++];
	   	vertices[i++] = cubeNormals[nIdx++];
	   	vertices[i++] = cubeNormals[nIdx++];
	   	vertices[i++] = cubeNormals[nIdx++];
	   	vertices[i++] = cubeTex[tIdx++];
	   	vertices[i++] = cubeTex[tIdx++];
	   }
	   
      short[] indices = {
         0, 2, 1,
         0, 3, 2, 
         4, 5, 6,
         4, 6, 7,
         8, 9, 10,
         8, 10, 11, 
         12, 15, 14,
         12, 14, 13, 
         16, 17, 18,
         16, 18, 19, 
         20, 23, 22,
         20, 22, 21
      };
		
      mesh.setVertices(vertices);
      mesh.setIndices(indices);
      
		return mesh;
	}
}

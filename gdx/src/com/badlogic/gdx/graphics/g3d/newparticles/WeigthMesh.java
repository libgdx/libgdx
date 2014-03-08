package com.badlogic.gdx.graphics.g3d.newparticles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.newparticles.CumulativeDistribution.CumulativeValue;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class WeigthMesh {
	private Mesh mesh;
	private CumulativeDistribution<Triangle> distribution;
	
	public WeigthMesh(Mesh mesh){
		distribution = new CumulativeDistribution<Triangle>(Triangle.class);
		setMesh(mesh);
	}
	
	/** Set the current Mesh, but doesn't update the weights.
	 * Each vertex of the mesh must have {@link Usage#Position} attribute.*/
	public void setMesh (Mesh mesh) {
		if(mesh.getVertexAttribute(Usage.Position) == null) 
			throw new GdxRuntimeException("Mesh vertices must have Usage.Position");
		this.mesh = mesh;
	}

	/** Calculate the weights of each triangle of the wrapped mesh.
	 * If the mesh has indices: the function will calculate the weight of those triangles.
	 * If the mesh has not indices: the function will consider the vertices as a triangle strip.*/
	public void calculateWeights(){
		distribution.clear();
		VertexAttributes attributes = mesh.getVertexAttributes();
		int indicesCount = mesh.getNumIndices();
		int vertexCount = mesh.getNumVertices();
		float sum = 0;
		int vertexSize = (short)(attributes.vertexSize / 4),
			 positionOffset = (short)(attributes.findByUsage(Usage.Position).offset/4);
		float[] vertices = new float[vertexCount*vertexSize];
		mesh.getVertices(vertices);
		if(indicesCount > 0){
			short[] indices = new short[indicesCount];
			mesh.getIndices(indices);

			//Calculate the Area
			for(int i=0; i < indicesCount; i+=3){
				int p1Offset = indices[i]*vertexSize + positionOffset,
					 p2Offset = indices[i+1]*vertexSize + positionOffset,
					 p3Offset = indices[i+2]*vertexSize + positionOffset;
				float x1 = vertices[p1Offset], y1 = vertices[p1Offset+1], z1 = vertices[p1Offset+2],
						x2 = vertices[p2Offset], y2 = vertices[p2Offset+1], z2 = vertices[p2Offset+2],
						x3 = vertices[p3Offset], y3 = vertices[p3Offset+1], z3 = vertices[p3Offset+2];
				float area = Math.abs((x1*(y2 - y3) + x2*(y3 - y1) + x3*(y1-y2))/2f);
				sum += area;
				distribution.add(new Triangle(x1, y1, z1,    x2, y2, z2,   x3, y3, z3), area);
			}
		}
		else {
			//Calculate the Area
			for(int i=0; i < vertexCount; i+=vertexSize){
				int p1Offset = i + positionOffset,
					 p2Offset = p1Offset + vertexSize,
					 p3Offset = p2Offset + vertexSize;
				float x1 = vertices[p1Offset], y1 = vertices[p1Offset+1], z1 = vertices[p1Offset+2],
						x2 = vertices[p2Offset], y2 = vertices[p2Offset+1], z2 = vertices[p2Offset+2],
						x3 = vertices[p3Offset], y3 = vertices[p3Offset+1], z3 = vertices[p3Offset+2];
				float area = Math.abs((x1*(y2 - y3) + x2*(y3 - y1) + x3*(y1-y2))/2f);
				sum += area;
				distribution.add(new Triangle(x1, y1, z1,    x2, y2, z2,   x3, y3, z3), area);
			}
		}

		//Generate cumulative distribution
		distribution.generateNormalized();
	}
	
	public Vector3 getRandomPoint(Vector3 vector){
		Triangle t = distribution.value();
		float a = MathUtils.random(), b = MathUtils.random();
		return vector.set( 	t.x1 + a*(t.x2 - t.x1) + b*(t.x3 - t.x1),
									t.y1 + a*(t.y2 - t.y1) + b*(t.y3 - t.y1),
									t.z1 + a*(t.z2 - t.z1) + b*(t.z3 - t.z1));
	}
	
}

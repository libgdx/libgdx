package com.badlogic.gdx.graphics.g3d.newparticles.values;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.newparticles.Triangle;
import com.badlogic.gdx.graphics.g3d.newparticles.WeigthMesh;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class MeshSpawnShapeValue extends SpawnShapeValue {
	protected Mesh mesh;
	private float[] vertices;
	private short[] indices;
	private int positionOffset, vertexSize, vertexCount, triangleCount;
	
	public MeshSpawnShapeValue (MeshSpawnShapeValue meshSpawnShapeValue) {
		super(meshSpawnShapeValue);
	}

	public MeshSpawnShapeValue () {
		// TODO Auto-generated constructor stub
	}

	public void setMesh(Mesh mesh){
		if(mesh.getVertexAttribute(Usage.Position) == null) 
			throw new GdxRuntimeException("Mesh vertices must have Usage.Position");
		this.mesh = mesh;
		vertexSize = mesh.getVertexSize()/4;
		positionOffset = mesh.getVertexAttribute(Usage.Position).offset/4;
		int indicesCount = mesh.getNumIndices();
		if(indicesCount >0){
			indices = new short[indicesCount];
			mesh.getIndices(indices);
			triangleCount = indices.length/3;
		}
		else indices = null;
		vertexCount = mesh.getNumVertices();
		vertices = new float[ vertexCount* vertexSize];
		mesh.getVertices(vertices);
	}
	
	@Override
	public void spawnAux (Vector3 vector, float percent) {
		if(indices == null){
			//Triangles 
			int triangleIndex = MathUtils.random(vertexCount -3)*vertexSize;
			int 	p1Offset = triangleIndex+positionOffset, 
					p2Offset = p1Offset + vertexSize, 
					p3Offset = p2Offset + vertexSize;
			float x1 = vertices[p1Offset], y1 = vertices[p1Offset+1], z1 = vertices[p1Offset+2],
					x2 = vertices[p2Offset], y2 = vertices[p2Offset+1], z2 = vertices[p2Offset+2],
					x3 = vertices[p3Offset], y3 = vertices[p3Offset+1], z3 = vertices[p3Offset+2];
			Triangle.pick(x1, y1, z1, x2, y2, z2, x3, y3, z3, vector);
		}
		else {
			//Indices
			int triangleIndex = MathUtils.random(triangleCount-1)*3;
			int p1Offset = indices[triangleIndex]*vertexSize + positionOffset,
				 p2Offset = indices[triangleIndex+1]*vertexSize + positionOffset,
				 p3Offset = indices[triangleIndex+2]*vertexSize + positionOffset;
			float x1 = vertices[p1Offset], y1 = vertices[p1Offset+1], z1 = vertices[p1Offset+2],
				x2 = vertices[p2Offset], y2 = vertices[p2Offset+1], z2 = vertices[p2Offset+2],
				x3 = vertices[p3Offset], y3 = vertices[p3Offset+1], z3 = vertices[p3Offset+2];
			Triangle.pick(x1, y1, z1, x2, y2, z2, x3, y3, z3, vector);
		}
	}

	@Override
	public SpawnShapeValue copy () {
		return new MeshSpawnShapeValue(this);
	}

}

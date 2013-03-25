package com.badlogic.gdx.graphics.g3d.model;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.materials.NewMaterial;
import com.badlogic.gdx.graphics.g3d.model.data.ModelMeshPartMaterial;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class ModelBuilder {
	private final static Vector3 tempV1 = new Vector3();
	private final static Vector3 tempV2 = new Vector3();
	private final static Vector3 tempV3 = new Vector3();
	private final static Vector3 tempV4 = new Vector3();
	private final static Vector3 tempV5 = new Vector3();
	private final static Vector3 tempV6 = new Vector3();
	private final static Vector3 tempV7 = new Vector3();
	private final static Vector3 tempV8 = new Vector3();
	
	private ModelBuilder() {}
	
	public static Model createFromMesh(final Mesh mesh, int primitiveType, final NewMaterial material) {
		return createFromMesh(mesh, 0, mesh.getNumIndices(), primitiveType, material);
	}
	
	public static Model createFromMesh(final Mesh mesh, int indexOffset, int vertexCount, int primitiveType, final NewMaterial material) {
		Model result = new Model();
		MeshPart meshPart = new MeshPart();
		meshPart.id = "part1";
		meshPart.indexOffset = indexOffset;
		meshPart.numVertices = vertexCount;
		meshPart.primitiveType = primitiveType;
		meshPart.mesh = mesh;

		MeshPartMaterial partMaterial = new MeshPartMaterial();
		partMaterial.material = material;
		partMaterial.meshPart = meshPart;
		Node node = new Node();
		node.id = "node1";
		node.meshPartMaterials.add(partMaterial);
		
		result.meshes.add(mesh);
		result.materials.add(material);
		result.nodes.add(node);
		result.meshParts.add(meshPart);
		return result;
	}
	
	public static Model createFromMesh(final float[] vertices, final VertexAttribute[] attributes, final short[] indices, int primitiveType, final NewMaterial material) {
		final Mesh mesh = new Mesh(false, vertices.length, indices.length, attributes);
		mesh.setVertices(vertices);
		mesh.setIndices(indices);
		return createFromMesh(mesh, 0, indices.length, primitiveType, material);
	}
	
	public static Mesh createPlaneMesh(float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4, float normalX, float normalY, float normalZ) {
		float[] vertices = new float[] {
			x1, y1, z1, normalX, normalY, normalZ,
			x2, y2, z2, normalX, normalY, normalZ,
			x3, y3, z3, normalX, normalY, normalZ,
			x4, y4, z4, normalX, normalY, normalZ
		};
		short[] indices = new short[] { 0, 1, 2, 1, 2, 3 };
		Mesh result = new Mesh(true, vertices.length, indices.length, new VertexAttribute(Usage.Position, 3, "a_position"), new VertexAttribute(Usage.Normal, 3, "a_normal"));
		result.setVertices(vertices);
		result.setIndices(indices);
		return result;
	}
	
	public static Mesh createBoxMesh(float width, float height, float depth) {
		final float hw = width * 0.5f;
		final float hh = height * 0.5f;
		final float hd = depth * 0.5f;
		float[] vertices = new float[] {
					hw, hh, hd, 0, 1, 0,
					hw, hh, -hd, 0, 1, 0, 
					-hw, hh, hd, 0, 1, 0,
					-hw, hh, -hd, 0, 1, 0,
					
					hw, -hh, hd, 0, -1, 0,
					hw, -hh, -hd, 0, -1, 0, 
					-hw, -hh, hd, 0, -1, 0, 
					-hw, -hh, -hd, 0, -1, 0,
					
					hw, hh, hd, 0, 0, 1,
					-hw, hh, hd, 0, 0, 1,
					hw, -hh, hd, 0, 0, 1,
					-hw, -hh, hd, 0, 0, 1,
					
					hw, hh, -hd, 0, 0, -1,
					-hw, hh, -hd, 0, 0, -1,
					hw, -hh, -hd, 0, 0, -1,
					-hw, -hh, -hd, 0, 0, -1,
					
					-hw, hh, hd, -1, 0, 0,
					-hw, hh, -hd, -1, 0, 0,
					-hw, -hh, hd, -1, 0, 0,
					-hw, -hh, -hd, -1, 0, 0,
					
					hw, hh, hd, 1, 0, 0, 
					hw, hh, -hd, 1, 0, 0,
					hw, -hh, hd, 1, 0, 0,
					hw, -hh, -hd, 1, 0, 0
			};
		short[] indices = new short[] {
					0, 1, 2, 1, 2, 3, // top
					4, 5, 6, 5, 6, 7, // bottom
					8, 9, 10, 10, 11, 9, // front
					12, 13, 14, 14, 15, 13, // back
					16, 17, 18, 18, 19, 17, // left
					20, 21, 22, 22, 23, 21 // right
			};
		Mesh result = new Mesh(true, vertices.length, indices.length, new VertexAttribute(Usage.Position, 3, "a_position"), new VertexAttribute(Usage.Normal, 3, "a_normal"));
		result.setVertices(vertices);
		result.setIndices(indices);
		return result;
	}
	
	public static Model createBox(float width, float height, float depth, final NewMaterial material) {
		final Mesh mesh = createBoxMesh(width, height, depth);
		return createFromMesh(mesh, GL10.GL_TRIANGLES, material);
	}
	
	public static Model createPlaneModel(float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4, float normalX, float normalY, float normalZ, final NewMaterial material) {
		final Mesh mesh = createPlaneMesh(x1, y1, z1, x2, y2, z2, x3, y3, z3, x4, y4, z4, normalX, normalY, normalZ);
		return createFromMesh(mesh, GL10.GL_TRIANGLES, material);		
	}
}

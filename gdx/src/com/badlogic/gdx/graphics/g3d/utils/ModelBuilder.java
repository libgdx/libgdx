package com.badlogic.gdx.graphics.g3d.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.materials.Material;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.model.MeshPartMaterial;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.data.ModelMeshPartMaterial;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pool.Poolable;

public class ModelBuilder {
	/** The model currently being build */
	private Model model;
	/** The node currently being build */
	private Node node;
	/** The mesh builders created between begin and end */
	private Array<MeshBuilder> builders = new Array<MeshBuilder>();
	
	private MeshBuilder getBuilder(final VertexAttributes attributes) {
		for (final MeshBuilder mb : builders)
			if (mb.getAttributes().equals(attributes))
				return mb;
		final MeshBuilder result = new MeshBuilder();
		result.begin(attributes);
		builders.add(result);
		return result;
	}
	
	/** Begin builder models */
	public void begin() {
		if (model != null)
			throw new GdxRuntimeException("Call end() first");
		node = null;
		model = new Model();
		builders.clear();
	}
	
	/** End building model(s) */
	public Model end() {
		if (model == null)
			throw new GdxRuntimeException("Call begin() first");
		final Model result = model;
		endnode();
		model = null;
		
		for (final MeshBuilder mb : builders)
			mb.end();
		builders.clear();
		
		for (final MeshPart mp : result.meshParts) {
			if (!result.meshes.contains(mp.mesh, true))
				result.meshes.add(mp.mesh);
		}
		return result;
	}

	public Node node() {
		if (model == null)
			throw new GdxRuntimeException("Call begin() first");
		
		endnode();
		
		node = new Node();
		node.id = "node"+model.nodes.size;
		model.nodes.add(node);
		return node;
	}
	
	private void endnode() {
		if (node != null) {
			node = null;
			// FIXME
		}
	}
	
	/** Adds the specified MeshPart to the current Node. */
	public void part(final MeshPart meshpart, final Material material) {
		// Allow to add parts right after the call to model() for models containing just one node
		if (node == null)
			node();
		
		if (!model.meshParts.contains(meshpart, true))
			model.meshParts.add(meshpart);
		if (!model.materials.contains(material, true))
			model.materials.add(material);
		node.meshPartMaterials.add(new MeshPartMaterial(meshpart, material));
	}
	
	/** Creates a new MeshPart within the current Node. Use the construction method to construct the part. */
	public MeshPartBuilder part(final String id, final VertexAttributes attributes, final Material material) {
		final MeshBuilder builder = getBuilder(attributes);
		part(builder.part(id), material);
		return builder;
	}
	
	public Model createBox(float width, float height, float depth, final Material material, final VertexAttributes attributes) {
		begin();
		part("box", attributes, material).box(width, height, depth);
		return end();
	}
	
	public Model createRect(float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4, float normalX, float normalY, float normalZ, final Material material, final VertexAttributes attributes) {
		begin();
		part("rect", attributes, material).rect(x1, y1, z1, x2, y2, z2, x3, y3, z3, x4, y4, z4, normalX, normalY, normalZ);
		return end();
	}

	public Model createCylinder(float width, float height, float depth, int divisions, final Material material, final VertexAttributes attributes) {
		begin();
		part("cylinder", attributes, material).cylinder(width, height, depth, divisions);
		return end();
	}
	
	public Model createCone(float width, float height, float depth, int divisions, final Material material, final VertexAttributes attributes) {
		begin();
		part("cone", attributes, material).cone(width, height, depth, divisions);
		return end();
	}
	
	public Model createSphere(float width, float height, float depth, int divisionsU, int divisionsV, final Material material, final VertexAttributes attributes) {
		begin();
		part("cylinder", attributes, material).sphere(width, height, depth, divisionsU, divisionsV);
		return end();
	}
	
	// Old code below this line, as for now still useful for testing. 
	@Deprecated
	public static Model createFromMesh(final Mesh mesh, int primitiveType, final Material material) {
		return createFromMesh(mesh, 0, mesh.getNumIndices(), primitiveType, material);
	}
	@Deprecated
	public static Model createFromMesh(final Mesh mesh, int indexOffset, int vertexCount, int primitiveType, final Material material) {
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
	@Deprecated
	public static Model createFromMesh(final float[] vertices, final VertexAttribute[] attributes, final short[] indices, int primitiveType, final Material material) {
		final Mesh mesh = new Mesh(false, vertices.length, indices.length, attributes);
		mesh.setVertices(vertices);
		mesh.setIndices(indices);
		return createFromMesh(mesh, 0, indices.length, primitiveType, material);
	}
}

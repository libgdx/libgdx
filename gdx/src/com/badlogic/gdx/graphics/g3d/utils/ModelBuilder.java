package com.badlogic.gdx.graphics.g3d.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.materials.NewMaterial;
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
	/** The model created between begin and end */
	private Array<Model> models = new Array<Model>();
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
		node = null;
		model = null;
		models.clear();
		builders.clear();
	}
	
	/** End building model(s) */
	public Model end() {
		final Model result = model;
		endmodel();
		
		Gdx.app.log("Test", "Builders: "+builders.size);
		for (final MeshBuilder mb : builders)
			mb.end();
		builders.clear();
		
		Gdx.app.log("Test", "model = "+models.size);
		for (final Model m : models) {
			for (final MeshPart mp : m.meshParts) {
				if (!m.meshes.contains(mp.mesh, true))
					m.meshes.add(mp.mesh);
			}
			Gdx.app.log("Test", "meshparts = "+m.meshParts.size+" meshes = "+m.meshes.size);
		}
		models.clear();
		return result;
	}
	
	/** Start building a new model, the model is not usable until the call to end().
	 * Call node() after this method to start building this model */
	public Model model() {
		endmodel();
		
		model = new Model();
		models.add(model);
		return model;
	}
	
	private void endmodel() {
		if (model != null) {
			endnode();
			
			model = null;
		}
	}

	public Node node() {
		// Allow to add nodes rights after the call to begin() for building just one model
		if (model == null)
			model();
		
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
	public void part(final MeshPart meshpart, final NewMaterial material) {
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
	public MeshPartBuilder part(final String id, final VertexAttributes attributes, final NewMaterial material) {
		final MeshBuilder builder = getBuilder(attributes);
		part(builder.part(id), material);
		return builder;
	}

	protected static ModelBuilder instance;
	public static ModelBuilder getInstance() {
		if (instance == null)
			instance = new ModelBuilder();
		return instance;
	}
	
	public static Model createBox(float width, float height, float depth, final NewMaterial material, final VertexAttributes attributes) {
		ModelBuilder mb = getInstance();
		mb.begin();
		mb.part("box", attributes, material).box(width, height, depth);
		return mb.end();
	}
	
	public static Model createRect(float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4, float normalX, float normalY, float normalZ, final NewMaterial material, final VertexAttributes attributes) {
		ModelBuilder mb = getInstance();
		mb.begin();
		mb.part("rect", attributes, material).rect(x1, y1, z1, x2, y2, z2, x3, y3, z3, x4, y4, z4, normalX, normalY, normalZ);
		return mb.end();
	}

	// FIXME: Add transform
	public static Model createCylinder(float width, float height, float depth, int divisions, final NewMaterial material, final VertexAttributes attributes) {
		ModelBuilder mb = getInstance();
		mb.begin();
		mb.part("cylinder", attributes, material).cylinder(width, height, depth, divisions);
		return mb.end();
	}
	
	// Old code below this line, as for now still useful for testing. 
	
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
	
	// along Y axis
	/*
	public static Mesh createCylinderMesh(float width, float height, float depth, int divisions) {
		final int stride = 6;
		final float hw = width * 0.5f;
		final float hh = height * 0.5f;
		final float hd = depth * 0.5f;
		final float step = MathUtils.PI2 / divisions;
		for (int i = 0; i < divisions; i++) {
			final float angle = step * i;
			vectorArray.add(vectorPool.obtain().set(MathUtils.cos(angle) * hw, 0f, MathUtils.sin(angle) * hd));
		}
		final float[] vertices = new float[divisions * 4 * stride];
		final short[] indices = new short[divisions * 6];
		int voffset = 0;
		int ioffset = 0;
		for (int i = 0; i < divisions; i++) {
			final Vector3 v1 = vectorArray.get(i);
			final Vector3 v2 = vectorArray.get((i+1)%divisions);
			final Vector3 n = tempV1.set(v1).lerp(v2, 0.5f).nor();
			vertices[voffset++] = v1.x;
			vertices[voffset++] = -hh;
			vertices[voffset++] = v1.z;
			vertices[voffset++] = n.x;
			vertices[voffset++] = n.y;
			vertices[voffset++] = n.z;
			
			vertices[voffset++] = v2.x;
			vertices[voffset++] = -hh;
			vertices[voffset++] = v2.z;
			vertices[voffset++] = n.x;
			vertices[voffset++] = n.y;
			vertices[voffset++] = n.z;
			
			vertices[voffset++] = v1.x;
			vertices[voffset++] = hh;
			vertices[voffset++] = v1.z;
			vertices[voffset++] = n.x;
			vertices[voffset++] = n.y;
			vertices[voffset++] = n.z;
			
			vertices[voffset++] = v2.x;
			vertices[voffset++] = hh;
			vertices[voffset++] = v2.z;
			vertices[voffset++] = n.x;
			vertices[voffset++] = n.y;
			vertices[voffset++] = n.z;
			
			indices[ioffset++] = (short)(i * 4);
			indices[ioffset++] = (short)(i * 4 + 1);
			indices[ioffset++] = (short)(i * 4 + 2);
			indices[ioffset++] = (short)(i * 4 + 1);
			indices[ioffset++] = (short)(i * 4 + 2);
			indices[ioffset++] = (short)(i * 4 + 3);
		}
		vectorPool.freeAll(vectorArray);
		vectorArray.clear();
		final Mesh result = new Mesh(true, vertices.length, indices.length, new VertexAttribute(Usage.Position, 3, "a_position"), new VertexAttribute(Usage.Normal, 3, "a_normal"));
		result.setVertices(vertices);
		result.setIndices(indices);
		return result;
	}*/
}

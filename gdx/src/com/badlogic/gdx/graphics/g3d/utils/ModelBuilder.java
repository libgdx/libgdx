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
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.data.ModelNodePart;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
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
			if (mb.getAttributes().equals(attributes) && mb.lastIndex() < Short.MAX_VALUE/2)
				return mb;
		final MeshBuilder result = new MeshBuilder();
		result.begin(attributes);
		builders.add(result);
		return result;
	}
	
	/** Begin building a new model */
	public void begin() {
		if (model != null)
			throw new GdxRuntimeException("Call end() first");
		node = null;
		model = new Model();
		builders.clear();
	}
	
	/** End building the model.
	 * @return The newly created model. Call the {@link Model#dispose()} method when no longer used. */
	public Model end() {
		if (model == null)
			throw new GdxRuntimeException("Call begin() first");
		final Model result = model;
		endnode();
		model = null;
		
		for (final MeshBuilder mb : builders)
			mb.end();
		builders.clear();
		
		rebuildReferences(result);
		return result;
	}
	
	private void endnode() {
		if (node != null) {
			node = null;
		}
	}

	/** Adds the {@link Node} to the model and sets it active for building. */
	protected Node node(final Node node) {
		if (model == null)
			throw new GdxRuntimeException("Call begin() first");
		
		endnode();
		
		model.nodes.add(node);
		this.node = node;
		
		return node;
	}
	
	/** Add a node to the model. 
	 * @return The node being created. */
	public Node node() {
		final Node node = new Node();
		node(node);
		node.id = "node"+model.nodes.size;
		return node;
	}
	
	/** Adds the nodes of the specified model to a new node the model being build.
	 * After this method the given model can no longer be used. Do not call the {@link Model#dispose()} method on that model. 
	 * @return The newly created node containing the nodes of the given model. */
	public Node node(final String id, final Model model) {
		final Node node = new Node();
		node.id = id;
		node.children.addAll(model.nodes);
		node(node);
		for (final Disposable disposable : model.getManagedDisposables())
			manage(disposable);
		return node;
	}
	
	/** Add the {@link Disposable} object to the model, causing it to be disposed when the model is disposed. */
	public void manage(final Disposable disposable) {
		if (model == null)
			throw new GdxRuntimeException("Call begin() first");
		model.manageDisposable(disposable);
	}
	
	/** Adds the specified MeshPart to the current Node. 
	 * The Mesh will be managed by the model and disposed when the model is disposed.
	 * The resources the Material might contain are not managed, use {@link #manage(Disposable)} to add those to the model. */
	public void part(final MeshPart meshpart, final Material material) {
		if (node == null)
			node();
		node.parts.add(new NodePart(meshpart, material));
	}
	
	/** Adds the specified mesh part to the current node.
	 * The Mesh will be managed by the model and disposed when the model is disposed.
	 * The resources the Material might contain are not managed, use {@link #manage(Disposable)} to add those to the model.
	 * @return The added MeshPart. */
	public MeshPart part(final String id, final Mesh mesh, int primitiveType, int offset, int size, final Material material) {
		final MeshPart meshPart = new MeshPart();
		meshPart.id = id;
		meshPart.primitiveType = primitiveType;
		meshPart.mesh = mesh;
		meshPart.indexOffset = offset;
		meshPart.numVertices = size;
		part(meshPart, material);
		return meshPart;
	}
	
	/** Adds the specified mesh part to the current node.
	 * The Mesh will be managed by the model and disposed when the model is disposed.
	 * The resources the Material might contain are not managed, use {@link #manage(Disposable)} to add those to the model.
	 * @return The added MeshPart. */
	public MeshPart part(final String id, final Mesh mesh, int primitiveType, final Material material) {
		return part(id, mesh, primitiveType, 0, mesh.getNumIndices(), material);
	}
	
	/** Creates a new MeshPart within the current Node.
	 * The resources the Material might contain are not managed, use {@link #manage(Disposable)} to add those to the model.
	 * @return The {@link MeshPartBuilder} you can use to build the MeshPart. */ 
	private MeshPartBuilder part(final String id, int primitiveType, final VertexAttributes attributes, final Material material) {
		final MeshBuilder builder = getBuilder(attributes);
		part(builder.part(id, primitiveType), material);
		return builder;
	}
	
	/** Creates a new MeshPart within the current Node.
	 * The resources the Material might contain are not managed, use {@link #manage(Disposable)} to add those to the model.
	 * @param attributes bitwise mask of the {@link com.badlogic.gdx.graphics.VertexAttributes.Usage}, 
	 * only Position, Color, Normal and TextureCoordinates is supported.
	 * @return The {@link MeshPartBuilder} you can use to build the MeshPart. */ 
	public MeshPartBuilder part(final String id, int primitiveType, final long attributes, final Material material) {
		return part(id, primitiveType, MeshBuilder.createAttributes(attributes), material);
	}
	
	/** Convenience method to create a model with a single node containing a box shape.
	 * The resources the Material might contain are not managed, 
	 * use {@link Model#manageDisposable(Disposable)} to add those to the model.
	 * @param attributes bitwise mask of the {@link com.badlogic.gdx.graphics.VertexAttributes.Usage}, 
	 * only Position, Color, Normal and TextureCoordinates is supported. */ 
	public Model createBox(float width, float height, float depth, final Material material, final long attributes) {
		return createBox(width, height, depth, GL10.GL_TRIANGLES, material, attributes);
	}
	
	/** Convenience method to create a model with a single node containing a box shape.
	 * The resources the Material might contain are not managed, 
	 * use {@link Model#manageDisposable(Disposable)} to add those to the model.
	 * @param attributes bitwise mask of the {@link com.badlogic.gdx.graphics.VertexAttributes.Usage}, 
	 * only Position, Color, Normal and TextureCoordinates is supported. */ 
	public Model createBox(float width, float height, float depth, int primitiveType, final Material material, final long attributes) {
		begin();
		part("box", primitiveType, attributes, material).box(width, height, depth);
		return end();
	}
	
	/** Convenience method to create a model with a single node containing a rectangle shape. 
	 * The resources the Material might contain are not managed, 
	 * use {@link Model#manageDisposable(Disposable)} to add those to the model.
	 * @param attributes bitwise mask of the {@link com.badlogic.gdx.graphics.VertexAttributes.Usage}, 
	 * only Position, Color, Normal and TextureCoordinates is supported. */
	public Model createRect(float x00, float y00, float z00, float x10, float y10, float z10, float x11, float y11, float z11, float x01, float y01, float z01, float normalX, float normalY, float normalZ, final Material material, final long attributes) {
		return createRect(x00, y00, z00, x10, y10, z10, x11, y11, z11, x01, y01, z01, normalX, normalY, normalZ, GL10.GL_TRIANGLES, material, attributes);
	}
	
	/** Convenience method to create a model with a single node containing a rectangle shape. 
	 * The resources the Material might contain are not managed, 
	 * use {@link Model#manageDisposable(Disposable)} to add those to the model.
	 * @param attributes bitwise mask of the {@link com.badlogic.gdx.graphics.VertexAttributes.Usage}, 
	 * only Position, Color, Normal and TextureCoordinates is supported. */
	public Model createRect(float x00, float y00, float z00, float x10, float y10, float z10, float x11, float y11, float z11, float x01, float y01, float z01, float normalX, float normalY, float normalZ, int primitiveType, final Material material, final long attributes) {
		begin();
		part("rect", primitiveType, attributes, material).rect(x00, y00, z00, x10, y10, z10, x11, y11, z11, x01, y01, z01, normalX, normalY, normalZ);
		return end();
	}

	/** Convenience method to create a model with a single node containing a cylinder shape.
	 * The resources the Material might contain are not managed, 
	 * use {@link Model#manageDisposable(Disposable)} to add those to the model.
	 * @param attributes bitwise mask of the {@link com.badlogic.gdx.graphics.VertexAttributes.Usage}, 
	 * only Position, Color, Normal and TextureCoordinates is supported. */
	public Model createCylinder(float width, float height, float depth, int divisions, final Material material, final long attributes) {
		return createCylinder(width, height, depth, divisions, GL10.GL_TRIANGLES, material, attributes);
	}
	
	/** Convenience method to create a model with a single node containing a cylinder shape.
	 * The resources the Material might contain are not managed, 
	 * use {@link Model#manageDisposable(Disposable)} to add those to the model.
	 * @param attributes bitwise mask of the {@link com.badlogic.gdx.graphics.VertexAttributes.Usage}, 
	 * only Position, Color, Normal and TextureCoordinates is supported. */
	public Model createCylinder(float width, float height, float depth, int divisions, int primitiveType, final Material material, final long attributes) {
		return createCylinder(width, height, depth, divisions, primitiveType, material, attributes, 0, 360);
	}
	
	/** Convenience method to create a model with a single node containing a cylinder shape.
	 * The resources the Material might contain are not managed, 
	 * use {@link Model#manageDisposable(Disposable)} to add those to the model.
	 * @param attributes bitwise mask of the {@link com.badlogic.gdx.graphics.VertexAttributes.Usage}, 
	 * only Position, Color, Normal and TextureCoordinates is supported. */
	public Model createCylinder(float width, float height, float depth, int divisions, final Material material, final long attributes, float angleFrom, float angleTo) {
		return createCylinder(width, height, depth, divisions, GL10.GL_TRIANGLES, material, attributes, angleFrom, angleTo);
	}
	
	/** Convenience method to create a model with a single node containing a cylinder shape.
	 * The resources the Material might contain are not managed, 
	 * use {@link Model#manageDisposable(Disposable)} to add those to the model.
	 * @param attributes bitwise mask of the {@link com.badlogic.gdx.graphics.VertexAttributes.Usage}, 
	 * only Position, Color, Normal and TextureCoordinates is supported. */
	public Model createCylinder(float width, float height, float depth, int divisions, int primitiveType, final Material material, final long attributes, float angleFrom, float angleTo) {
		begin();
		part("cylinder", primitiveType, attributes, material).cylinder(width, height, depth, divisions, angleFrom, angleTo);
		return end();
	}
	
	/** Convenience method to create a model with a single node containing a cone shape.
	 * The resources the Material might contain are not managed, 
	 * use {@link Model#manageDisposable(Disposable)} to add those to the model.
	 * @param attributes bitwise mask of the {@link com.badlogic.gdx.graphics.VertexAttributes.Usage}, 
	 * only Position, Color, Normal and TextureCoordinates is supported. */
	public Model createCone(float width, float height, float depth, int divisions, final Material material, final long attributes) {
		return createCone(width, height, depth, divisions, GL10.GL_TRIANGLES, material, attributes);
	}
		
	/** Convenience method to create a model with a single node containing a cone shape.
	 * The resources the Material might contain are not managed, 
	 * use {@link Model#manageDisposable(Disposable)} to add those to the model.
	 * @param attributes bitwise mask of the {@link com.badlogic.gdx.graphics.VertexAttributes.Usage}, 
	 * only Position, Color, Normal and TextureCoordinates is supported. */
	public Model createCone(float width, float height, float depth, int divisions, int primitiveType, final Material material, final long attributes) {
		return createCone(width, height, depth, divisions, primitiveType, material, attributes, 0, 360);
	}
	
	/** Convenience method to create a model with a single node containing a cone shape.
	 * The resources the Material might contain are not managed, 
	 * use {@link Model#manageDisposable(Disposable)} to add those to the model.
	 * @param attributes bitwise mask of the {@link com.badlogic.gdx.graphics.VertexAttributes.Usage}, 
	 * only Position, Color, Normal and TextureCoordinates is supported. */
	public Model createCone(float width, float height, float depth, int divisions, final Material material, final long attributes, float angleFrom, float angleTo) {
		return createCone(width, height, depth, divisions, GL10.GL_TRIANGLES, material, attributes, angleFrom, angleTo);
	}
		
	/** Convenience method to create a model with a single node containing a cone shape.
	 * The resources the Material might contain are not managed, 
	 * use {@link Model#manageDisposable(Disposable)} to add those to the model.
	 * @param attributes bitwise mask of the {@link com.badlogic.gdx.graphics.VertexAttributes.Usage}, 
	 * only Position, Color, Normal and TextureCoordinates is supported. */
	public Model createCone(float width, float height, float depth, int divisions, int primitiveType, final Material material, final long attributes, float angleFrom, float angleTo) {
		begin();
		part("cone", primitiveType, attributes, material).cone(width, height, depth, divisions, angleFrom, angleTo);
		return end();
	}

	/** Convenience method to create a model with a single node containing a sphere shape.
	 * The resources the Material might contain are not managed, 
	 * use {@link Model#manageDisposable(Disposable)} to add those to the model.
	 * @param attributes bitwise mask of the {@link com.badlogic.gdx.graphics.VertexAttributes.Usage}, 
	 * only Position, Color, Normal and TextureCoordinates is supported. */
	public Model createSphere(float width, float height, float depth, int divisionsU, int divisionsV, final Material material, final long attributes) {
		return createSphere(width, height, depth, divisionsU, divisionsV, GL10.GL_TRIANGLES, material, attributes);
	}
	
	/** Convenience method to create a model with a single node containing a sphere shape.
	 * The resources the Material might contain are not managed, 
	 * use {@link Model#manageDisposable(Disposable)} to add those to the model.
	 * @param attributes bitwise mask of the {@link com.badlogic.gdx.graphics.VertexAttributes.Usage}, 
	 * only Position, Color, Normal and TextureCoordinates is supported. */
	public Model createSphere(float width, float height, float depth, int divisionsU, int divisionsV, int primitiveType, final Material material, final long attributes) {
		return createSphere(width, height, depth, divisionsU, divisionsV, primitiveType, material, attributes, 0, 360, 0, 180);
	}
	
	/** Convenience method to create a model with a single node containing a sphere shape.
	 * The resources the Material might contain are not managed, 
	 * use {@link Model#manageDisposable(Disposable)} to add those to the model.
	 * @param attributes bitwise mask of the {@link com.badlogic.gdx.graphics.VertexAttributes.Usage}, 
	 * only Position, Color, Normal and TextureCoordinates is supported. */
	public Model createSphere(float width, float height, float depth, int divisionsU, int divisionsV, final Material material, final long attributes, float angleUFrom, float angleUTo, float angleVFrom, float angleVTo) {
		return createSphere(width, height, depth, divisionsU, divisionsV, GL10.GL_TRIANGLES, material, attributes, angleUFrom, angleUTo, angleVFrom, angleVTo);
	}
	
	/** Convenience method to create a model with a single node containing a sphere shape.
	 * The resources the Material might contain are not managed, 
	 * use {@link Model#manageDisposable(Disposable)} to add those to the model.
	 * @param attributes bitwise mask of the {@link com.badlogic.gdx.graphics.VertexAttributes.Usage}, 
	 * only Position, Color, Normal and TextureCoordinates is supported. */
	public Model createSphere(float width, float height, float depth, int divisionsU, int divisionsV, int primitiveType, final Material material, final long attributes, float angleUFrom, float angleUTo, float angleVFrom, float angleVTo) {
		begin();
		part("cylinder", primitiveType, attributes, material).sphere(width, height, depth, divisionsU, divisionsV, angleUFrom, angleUTo, angleVFrom, angleVTo);
		return end();
	}
	
	/** Convenience method to create a model with a single node containing a capsule shape.
	 * The resources the Material might contain are not managed, 
	 * use {@link Model#manageDisposable(Disposable)} to add those to the model.
	 * @param attributes bitwise mask of the {@link com.badlogic.gdx.graphics.VertexAttributes.Usage}, 
	 * only Position, Color, Normal and TextureCoordinates is supported. */
	public Model createCapsule(float radius, float height, int divisions, final Material material, final long attributes) {
		return createCapsule(radius, height, divisions, GL10.GL_TRIANGLES, material, attributes);
	}
	
	/** Convenience method to create a model with a single node containing a capsule shape.
	 * The resources the Material might contain are not managed, 
	 * use {@link Model#manageDisposable(Disposable)} to add those to the model.
	 * @param attributes bitwise mask of the {@link com.badlogic.gdx.graphics.VertexAttributes.Usage}, 
	 * only Position, Color, Normal and TextureCoordinates is supported. */
	public Model createCapsule(float radius, float height, int divisions, int primitiveType, final Material material, final long attributes) {
		begin();
		part("capsule", primitiveType, attributes, material).capsule(radius, height, divisions);
		return end();
	}
	
	/** Resets the references to materials, meshes and meshparts within the model to the ones used within it's nodes.
	 * This will make the model responsible for disposing all referenced meshes. */ 
	public static void rebuildReferences(final Model model) {
		model.materials.clear();
		model.meshes.clear();
		model.meshParts.clear();
		for (final Node node : model.nodes)
			rebuildReferences(model, node);
	}
	
	private static void rebuildReferences(final Model model, final Node node) {
		for (final NodePart mpm : node.parts) {
			if (!model.materials.contains(mpm.material, true))
				model.materials.add(mpm.material);
			if (!model.meshParts.contains(mpm.meshPart, true)) {
				model.meshParts.add(mpm.meshPart);
				if (!model.meshes.contains(mpm.meshPart.mesh, true))
					model.meshes.add(mpm.meshPart.mesh);
				model.manageDisposable(mpm.meshPart.mesh);
			}
		}
		for (final Node child : node.children)
			rebuildReferences(model, child);
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

		NodePart partMaterial = new NodePart();
		partMaterial.material = material;
		partMaterial.meshPart = meshPart;
		Node node = new Node();
		node.id = "node1";
		node.parts.add(partMaterial);
		
		result.meshes.add(mesh);
		result.materials.add(material);
		result.nodes.add(node);
		result.meshParts.add(meshPart);
		result.manageDisposable(mesh);
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

package com.badlogic.gdx.graphics.g3d;

import com.badlogic.gdx.graphics.g3d.materials.Material;
import com.badlogic.gdx.graphics.g3d.model.Animation;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.model.MeshPartMaterial;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Pool;

/**
 * An instance of a {@link Model}, allows to specify global transform and modify the materials, as it
 * has a copy of the model's materials. Multiple instances can be created from the same Model, 
 * all sharing the meshes and textures of the Model. The Model owns the meshes and textures, to 
 * dispose of these, the Model has to be disposed.</p>
 * 
 * The ModelInstance creates a full copy of all materials and nodes.
 * @author badlogic
 *
 */
public class ModelInstance implements RenderableProvider {
	/** the {@link Model} this instances derives from **/
	public final Model model;
	/** the world transform **/
	public final Matrix4 transform = new Matrix4();
	/** a copy of the materials of the original model **/
	public final Array<Material> materials = new Array<Material>();
	/** a copy of the nodes of the original model, referencing the copied materials in their {@link MeshPartMaterial} instances **/
	public final Array<Node> nodes = new Array<Node>();
	/** a copy of the animations of the original model **/
	public final Array<Animation> animations = new Array<Animation>();

	/** Constructs a new ModelInstance with all nodes and materials of the given model. */
	public ModelInstance(Model model) {
		this(model, (String[])null);
	}
	
	/** Constructs a new ModelInstance with only the specified nodes and materials of the given model. */
	public ModelInstance(Model model, final String... rootNodeIds) {
		this.model = model;
		if (rootNodeIds == null)
			copyNodes(model.nodes);
		else
			copyNodes(model.nodes, rootNodeIds);
		calculateTransforms();
	}
	
	/** Constructs a new ModelInstance with only the specified nodes and materials of the given model. */
	public ModelInstance(Model model, final Array<String> rootNodeIds) {
		this.model = model;
		copyNodes(model.nodes, rootNodeIds);
		calculateTransforms();
	}
	
	/** Constructs a new ModelInstance at the specified position. */
	public ModelInstance(Model model, Vector3 position) {
		this(model);
		this.transform.setToTranslation(position);
	}
	
	/** Constructs a new ModelInstance at the specified position. */
	public ModelInstance(Model model, float x, float y, float z) {
		this(model);
		this.transform.setToTranslation(x, y, z);
	}
	
	/** Constructs a new ModelInstance with the specified transform. */
	public ModelInstance(Model model, Matrix4 transform) {
		this(model);
		this.transform.set(transform);
	}
	
	/** Constructs a new ModelInstance which is an copy of the specified ModelInstance. */
	public ModelInstance(ModelInstance copyFrom) {
		this(copyFrom, copyFrom.transform);
	}
	
	/** Constructs a new ModelInstance which is an copy of the specified ModelInstance. */
	public ModelInstance(ModelInstance copyFrom, final Matrix4 transform) {
		this.model = copyFrom.model;
		if (transform != null)
			this.transform.set(transform);
		copyNodes(copyFrom.nodes);
		calculateTransforms();
	}
	
	/** @return A newly created ModelInstance which is a copy of this ModelInstance */
	public ModelInstance copy() {
		return new ModelInstance(this);
	}

	private void copyNodes (Array<Node> nodes) {
		for(Node node: nodes) {
			this.nodes.add(copyNode(null, node));
		}
	}
	
	private void copyNodes (Array<Node> nodes, final String... nodeIds) {
		for(Node node: nodes) {
			for (final String nodeId : nodeIds) {
				if (nodeId.equals(node.id)) {
					this.nodes.add(copyNode(null, node));
					break;
				}
			}
		}
	}
	
	private void copyNodes (Array<Node> nodes, final Array<String> nodeIds) {
		for(Node node: nodes) {
			for (final String nodeId : nodeIds) {
				if (nodeId.equals(node.id)) {
					this.nodes.add(copyNode(null, node));
					break;
				}
			}
		}
	}
	
	private Node copyNode(Node parent, Node node) {
		Node copy = new Node();
		copy.id = node.id;
		copy.boneId = node.boneId;
		copy.parent = parent;
		copy.translation.set(node.translation);
		copy.rotation.set(node.rotation);
		copy.scale.set(node.scale);
		copy.localTransform.set(node.localTransform);
		copy.worldTransform.set(node.worldTransform);
		for(MeshPartMaterial meshPart: node.meshPartMaterials) {
			copy.meshPartMaterials.add(copyMeshPart(meshPart));
		}
		for(Node child: node.children) {
			copy.children.add(copyNode(copy, child));
		}
		return copy;
	}
	
	private MeshPartMaterial copyMeshPart (MeshPartMaterial meshPart) {
		MeshPartMaterial copy = new MeshPartMaterial();
		copy.meshPart = new MeshPart();
		copy.meshPart.id = meshPart.meshPart.id;
		copy.meshPart.indexOffset = meshPart.meshPart.indexOffset;
		copy.meshPart.numVertices = meshPart.meshPart.numVertices;
		copy.meshPart.primitiveType = meshPart.meshPart.primitiveType;
		copy.meshPart.mesh = meshPart.meshPart.mesh;
		
		final int index = materials.indexOf(meshPart.material, false);
		if (index < 0)
			materials.add(copy.material = meshPart.material.copy());
		else
			copy.material = materials.get(index);
		
		return copy;
	}

	/**
	 * Calculates the local and world transform of all {@link Node} instances in this model, recursively.
	 * First each {@link Node#localTransform} transform is calculated based on the translation, rotation and
	 * scale of each Node. Then each {@link Node#calculateWorldTransform()}
	 * is calculated, based on the parent's world transform and the local transform of each Node.</p>
	 * 
	 * This method can be used to recalculate all transforms if any of the Node's local properties (translation, rotation, scale)
	 * was modified.
	 */
	public void calculateTransforms() {
		for(Node node: nodes) {
			node.calculateTransforms(true);
		}
	}
	
	/**
	 * Traverses the Node hierarchy and collects {@link Renderable} instances for every
	 * node with a graphical representation. Renderables are obtained from the provided
	 * pool. The resulting array can be rendered via a {@link ModelBatch}.
	 * 
	 * @param renderables the output array
	 * @param pool the pool to obtain Renderables from
	 */
	public void getRenderables(Array<Renderable> renderables, Pool<Renderable> pool) {
		for(Node node: nodes) {
			getRenderables(node, renderables, pool);
		}
	}
	
	private void getRenderables(Node node, Array<Renderable> renderables, Pool<Renderable> pool) {
		if(node.meshPartMaterials.size > 0) {
			for(MeshPartMaterial meshPart: node.meshPartMaterials) {
				Renderable renderable = pool.obtain();
				renderable.material = meshPart.material;
				renderable.mesh = meshPart.meshPart.mesh;
				renderable.meshPartOffset = meshPart.meshPart.indexOffset;
				renderable.meshPartSize = meshPart.meshPart.numVertices;
				renderable.primitiveType = meshPart.meshPart.primitiveType;
				renderable.transform.set(transform).mul(node.worldTransform);
				renderables.add(renderable);
			}
		}
		
		for(Node child: node.children) {
			getRenderables(child, renderables, pool);
		}
	}
}

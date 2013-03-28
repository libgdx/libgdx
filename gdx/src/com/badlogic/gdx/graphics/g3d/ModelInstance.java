package com.badlogic.gdx.graphics.g3d;

import com.badlogic.gdx.graphics.g3d.materials.NewMaterial;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.model.MeshPartMaterial;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.math.Matrix4;
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
public class ModelInstance {
	/** the {@link Model} this instances derrives from **/
	public final Model model;
	/** the world transform **/
	public final Matrix4 transform = new Matrix4();
	/** a copy of the materials of the original model **/
	public final Array<NewMaterial> materials = new Array<NewMaterial>();
	/** a copy of the nodes of the original model, referencing the copied materials in their {@link MeshPartMaterial} instances **/
	public final Array<Node> nodes = new Array<Node>();
	
	public ModelInstance(Model model) {
		this(model, new Matrix4());
	}
	
	public ModelInstance(Model model, Matrix4 transform) {
		this.model = model;
		this.transform.set(transform);
		copyMaterials(model.materials);
		copyNodes(model.nodes);
		calculateTransforms();
	}

	private void copyMaterials (Array<NewMaterial> materials) {
		for(NewMaterial material: materials) {
			this.materials.add(material.copy());
		}		
	}

	private void copyNodes (Array<Node> nodes) {
		for(Node node: nodes) {
			this.nodes.add(copyNode(null, node));
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
		
		int index = model.materials.indexOf(meshPart.material, true);
		if(index == -1) {
			throw new GdxRuntimeException("Inconsistent model, material in MeshPartMaterial not found in Model");
		}
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

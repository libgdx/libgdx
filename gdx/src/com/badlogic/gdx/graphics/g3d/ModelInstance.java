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

package com.badlogic.gdx.graphics.g3d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.model.Animation;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.model.NodeAnimation;
import com.badlogic.gdx.graphics.g3d.model.NodeKeyframe;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Pool;

/** An instance of a {@link Model}, allows to specify global transform and modify the materials, as it
 * has a copy of the model's materials. Multiple instances can be created from the same Model, 
 * all sharing the meshes and textures of the Model. The Model owns the meshes and textures, to 
 * dispose of these, the Model has to be disposed. Therefor, the Model must outlive all its ModelInstances</p>
 * 
 * The ModelInstance creates a full copy of all materials, nodes and animations.
 * @author badlogic, xoppa */
public class ModelInstance implements RenderableProvider {
	/** the materials of the model, used by nodes that have a graphical representation FIXME not sure if superfluous, allows modification of materials without having to traverse the nodes **/
	public final Array<Material> materials = new Array();
	/** root nodes of the model **/
	public final Array<Node> nodes = new Array();
	/** animations of the model, modifying node transformations **/
	public final Array<Animation> animations = new Array();
	/** the {@link Model} this instances derives from **/
	public final Model model;
	/** the world transform **/
	public Matrix4 transform;
	/** user definable value, which is passed to the shader. */
	public Object userData;
	
	/** Constructs a new ModelInstance with all nodes and materials of the given model.
	 * @param model The {@link Model} to create an instance of. */
	public ModelInstance(final Model model) {
		this(model, (String[])null);
	}
	
	/** @param model The source {@link Model}
	 * @param nodeId The ID of the root {@link Node} of the {@link Model} for the instance to contain
	 * @param mergeTransform True to apply the source node transform to the instance transform, resetting the node transform. */
	public ModelInstance(final Model model, final String nodeId, boolean mergeTransform) {
		this(model, null, nodeId, false, false, mergeTransform);
	}
	
	/** @param model The source {@link Model}
	 * @param transform The {@link Matrix4} instance for this ModelInstance to reference or null to create a new matrix. 
	 * @param nodeId The ID of the root {@link Node} of the {@link Model} for the instance to contain
	 * @param mergeTransform True to apply the source node transform to the instance transform, resetting the node transform. */
	public ModelInstance(final Model model, final Matrix4 transform, final String nodeId, boolean mergeTransform) {
		this(model, transform, nodeId, false, false, mergeTransform);
	}

	/** Recursively searches the mode for the specified node.
	 * @param model The source {@link Model}
	 * @param nodeId The ID of the {@link Node} within the {@link Model} for the instance to contain
	 * @param parentTransform True to apply the parent's node transform to the instance (only applicable if recursive is true).
	 * @param mergeTransform True to apply the source node transform to the instance transform, resetting the node transform. */
	public ModelInstance(final Model model, final String nodeId, boolean parentTransform, boolean mergeTransform) {
		this(model, null, nodeId, true, parentTransform, mergeTransform);
	}
	
	/** Recursively searches the mode for the specified node.
	 * @param model The source {@link Model}
	 * @param transform The {@link Matrix4} instance for this ModelInstance to reference or null to create a new matrix. 
	 * @param nodeId The ID of the {@link Node} within the {@link Model} for the instance to contain
	 * @param parentTransform True to apply the parent's node transform to the instance (only applicable if recursive is true).
	 * @param mergeTransform True to apply the source node transform to the instance transform, resetting the node transform. */
	public ModelInstance(final Model model, final Matrix4 transform, final String nodeId, boolean parentTransform, boolean mergeTransform) {
		this(model, transform, nodeId, true, parentTransform, mergeTransform);
	}
	
	/** @param model The source {@link Model}
	 * @param nodeId The ID of the {@link Node} within the {@link Model} for the instance to contain
	 * @param recursive True to recursively search the Model's node tree, false to only search for a root node
	 * @param parentTransform True to apply the parent's node transform to the instance (only applicable if recursive is true).
	 * @param mergeTransform True to apply the source node transform to the instance transform, resetting the node transform. */
	public ModelInstance(final Model model, final String nodeId, boolean recursive, boolean parentTransform, boolean mergeTransform) {
		this(model, null, nodeId, recursive, parentTransform, mergeTransform);
	}
	
	/** @param model The source {@link Model}
	 * @param transform The {@link Matrix4} instance for this ModelInstance to reference or null to create a new matrix. 
	 * @param nodeId The ID of the {@link Node} within the {@link Model} for the instance to contain
	 * @param recursive True to recursively search the Model's node tree, false to only search for a root node
	 * @param parentTransform True to apply the parent's node transform to the instance (only applicable if recursive is true).
	 * @param mergeTransform True to apply the source node transform to the instance transform, resetting the node transform. */
	public ModelInstance(final Model model, final Matrix4 transform, final String nodeId, boolean recursive, boolean parentTransform, boolean mergeTransform) {
		this.model = model;
		this.transform = transform == null ? new Matrix4() : transform; 
		nodePartBones.clear();
		Node node = model.getNode(nodeId, recursive), copy = node.copy(nodePartBones, materials);
		copy.parent = null;
		this.nodes.add(copy);
		if (mergeTransform) {
			this.transform.mul(parentTransform ? node.globalTransform : node.localTransform);
			copy.translation.set(0,0,0);
			copy.rotation.idt();
			copy.scale.set(1,1,1);
		} else if (parentTransform && copy.parent != null)
			this.transform.mul(node.parent.globalTransform);
		setBones();
		copyAnimations(model.animations);
		calculateTransforms();
	}
	
	/** Constructs a new ModelInstance with only the specified nodes and materials of the given model. */
	public ModelInstance(final Model model, final String... rootNodeIds) {
		this(model, null, rootNodeIds);
	}
	
	/** Constructs a new ModelInstance with only the specified nodes and materials of the given model. */
	public ModelInstance(final Model model, final Matrix4 transform, final String... rootNodeIds) {
		this.model = model;
		this.transform = transform == null ? new Matrix4() : transform;
		if (rootNodeIds == null)
			copyNodes(model.nodes);
		else
			copyNodes(model.nodes, rootNodeIds);
		copyAnimations(model.animations);
		calculateTransforms();
	}
	
	/** Constructs a new ModelInstance with only the specified nodes and materials of the given model. */
	public ModelInstance(final Model model, final Array<String> rootNodeIds) {
		this(model, null, rootNodeIds);
	}
	
	/** Constructs a new ModelInstance with only the specified nodes and materials of the given model. */
	public ModelInstance(final Model model, final Matrix4 transform, final Array<String> rootNodeIds) {
		this.model = model;
		this.transform = transform == null ? new Matrix4() : transform;
		copyNodes(model.nodes, rootNodeIds);
		copyAnimations(model.animations);
		calculateTransforms();
	}
	
	/** Constructs a new ModelInstance at the specified position. */
	public ModelInstance(final Model model, Vector3 position) {
		this(model);
		this.transform.setToTranslation(position);
	}
	
	/** Constructs a new ModelInstance at the specified position. */
	public ModelInstance(final Model model, float x, float y, float z) {
		this(model);
		this.transform.setToTranslation(x, y, z);
	}
	
	/** Constructs a new ModelInstance with the specified transform. */
	public ModelInstance(final Model model, Matrix4 transform) {
		this(model, transform, (String[])null);
	}
	
	/** Constructs a new ModelInstance which is an copy of the specified ModelInstance. */
	public ModelInstance(ModelInstance copyFrom) {
		this(copyFrom, copyFrom.transform.cpy());
	}
	
	/** Constructs a new ModelInstance which is an copy of the specified ModelInstance. */
	public ModelInstance(ModelInstance copyFrom, final Matrix4 transform) {
		this.model = copyFrom.model;
		this.transform = transform == null ? new Matrix4() : transform;
		copyNodes(copyFrom.nodes);
		copyAnimations(copyFrom.animations);
		calculateTransforms();
	}
	
	/** @return A newly created ModelInstance which is a copy of this ModelInstance */
	public ModelInstance copy() {
		return new ModelInstance(this);
	}

	private ObjectMap<NodePart, ArrayMap<Node, Matrix4>> nodePartBones = new ObjectMap<NodePart, ArrayMap<Node, Matrix4>>();
	private void copyNodes (Array<Node> nodes) {
		nodePartBones.clear();
		for(int i = 0, n = nodes.size; i<n; ++i) {
			final Node node = nodes.get(i);
			Node copy = node.copy(nodePartBones, materials);
			copy.parent = null;
			this.nodes.add(copy);
		}
		setBones();
	}
	
	private void copyNodes (Array<Node> nodes, final String... nodeIds) {
		nodePartBones.clear();
		for(int i = 0, n = nodes.size; i<n; ++i) {
			final Node node = nodes.get(i);
			for (final String nodeId : nodeIds) {
				if (nodeId.equals(node.id)) {
					Node copy = node.copy(nodePartBones, materials);
					copy.parent = null;
					this.nodes.add(copy);
					break;
				}
			}
		}
		setBones();
	}
	
	private void copyNodes (Array<Node> nodes, final Array<String> nodeIds) {
		nodePartBones.clear();
		for(int i = 0, n = nodes.size; i<n; ++i) {
			final Node node = nodes.get(i);
			for (final String nodeId : nodeIds) {
				if (nodeId.equals(node.id)) {
					Node copy = node.copy(nodePartBones, materials);
					copy.parent = null;
					this.nodes.add(copy);
					break;
				}
			}
		}
		setBones();
	}
	
	private void setBones() {
		for (ObjectMap.Entry<NodePart,ArrayMap<Node, Matrix4>> e : nodePartBones.entries()) {
			if (e.key.invBoneBindTransforms == null)
				e.key.invBoneBindTransforms = new ArrayMap<Node, Matrix4>(true, e.value.size, Node.class, Matrix4.class);
			e.key.invBoneBindTransforms.clear();
			
			for (final ObjectMap.Entry<Node, Matrix4> b : e.value.entries())
				e.key.invBoneBindTransforms.put(getNode(b.key.id), b.value); // Share the inv bind matrix with the model
			
			e.key.bones = new Matrix4[e.value.size];
			for (int i = 0; i < e.key.bones.length; i++)
				e.key.bones[i] = new Matrix4();
		}
	}
	
	/*
	private Node copyNode(Node parent, Node node) {
		Node copy = new Node();
		copy.id = node.id;
		//copy.boneId = node.boneId;
		copy.parent = parent;
		copy.translation.set(node.translation);
		copy.rotation.set(node.rotation);
		copy.scale.set(node.scale);
		copy.localTransform.set(node.localTransform);
		copy.globalTransform.set(node.globalTransform);
		for(NodePart nodePart: node.parts) {
			copy.parts.add(copyNodePart(nodePart));
		}
		for(Node child: node.children) {
			copy.children.add(copyNode(copy, child));
		}
		return copy;
	}
	
	private NodePart copyNodePart (NodePart nodePart) {
		NodePart copy = new NodePart();
		copy.meshPart = new MeshPart();
		copy.meshPart.id = nodePart.meshPart.id;
		copy.meshPart.indexOffset = nodePart.meshPart.indexOffset;
		copy.meshPart.numVertices = nodePart.meshPart.numVertices;
		copy.meshPart.primitiveType = nodePart.meshPart.primitiveType;
		copy.meshPart.mesh = nodePart.meshPart.mesh;
		
		if (nodePart.invBoneBindTransforms != null)
			nodePartBones.put(copy, nodePart.invBoneBindTransforms);
		
		final int index = materials.indexOf(nodePart.material, false);
		if (index < 0)
			materials.add(copy.material = nodePart.material.copy());
		else
			copy.material = materials.get(index);
		
		return copy;
	}
	*/
	
	private void copyAnimations (final Iterable<Animation> source) {
		for (final Animation anim : source) {
			Animation animation = new Animation();
			animation.id = anim.id;
			animation.duration = anim.duration;
			for (final NodeAnimation nanim : anim.nodeAnimations) {
				final Node node = getNode(nanim.node.id);
				if (node == null)
					continue;
				NodeAnimation nodeAnim = new NodeAnimation();
				nodeAnim.node = node;
				for (final NodeKeyframe kf : nanim.keyframes) {
					NodeKeyframe keyframe = new NodeKeyframe();
					keyframe.keytime = kf.keytime;
					keyframe.rotation.set(kf.rotation);
					keyframe.scale.set(kf.scale);
					keyframe.translation.set(kf.translation);
					nodeAnim.keyframes.add(keyframe);
				}
				if (nodeAnim.keyframes.size > 0)
					animation.nodeAnimations.add(nodeAnim);
			}
			if (animation.nodeAnimations.size > 0)
				animations.add(animation);
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
			node.getRenderables(renderables, pool, transform, userData);
			//getRenderables(node, renderables, pool);
		}
	}

	/*
	//* @return The renderable of the first node's first part.
	public Renderable getRenderable(final Renderable out) {
		return nodes.get(0).getRenderable(out, transform, userData);
		//return getRenderable(out, nodes.get(0));
	}
	
	/** @return The renderable of the node's first part.
	public Renderable getRenderable(final Renderable out, final Node node) {
		return node.getRenderable(out, node.parts.get(0), transform, userData);
	}
	
	public Renderable getRenderable(final Renderable out, final Node node, final NodePart nodePart) {
		nodePart.setRenderable(out);
		if (nodePart.bones == null && transform != null)
			out.worldTransform.set(transform).mul(node.globalTransform);
		else if (transform != null)
			out.worldTransform.set(transform);
		else
			out.worldTransform.idt();
		out.userData = userData;
		return out;
	}
	
	protected void getRenderables(Node node, Array<Renderable> renderables, Pool<Renderable> pool) {
		if(node.parts.size > 0) {
			for(NodePart nodePart: node.parts) {
				renderables.add(getRenderable(pool.obtain(), node, nodePart));
			}
		}
		
		for(Node child: node.children) {
			getRenderables(child, renderables, pool);
		}
	}
	*/
	
	/** Calculates the local and world transform of all {@link Node} instances in this model, recursively.
	 * First each {@link Node#localTransform} transform is calculated based on the translation, rotation and
	 * scale of each Node. Then each {@link Node#calculateWorldTransform()}
	 * is calculated, based on the parent's world transform and the local transform of each Node.
	 * Finally, the animation bone matrices are updated accordingly.</p>
	 * 
	 * This method can be used to recalculate all transforms if any of the Node's local properties (translation, rotation, scale)
	 * was modified.
	 */
	public void calculateTransforms() {
		final int n = nodes.size;
		for(int i = 0; i < n; i++) {
			nodes.get(i).calculateTransforms(true);
		}
		for(int i = 0; i < n; i++) {
			nodes.get(i).calculateBoneTransforms(true);
		}
	}
	
	/** Calculate the bounding box of this model instance.
	 * This is a potential slow operation, it is advised to cache the result.
	 * @param out the {@link BoundingBox} that will be set with the bounds.
	 * @return the out parameter for chaining */
	public BoundingBox calculateBoundingBox(final BoundingBox out) {
		out.inf();
		return extendBoundingBox(out);
	}
	
	/** Extends the bounding box with the bounds of this model instance.
	 * This is a potential slow operation, it is advised to cache the result.
	 * @param out the {@link BoundingBox} that will be extended with the bounds.
	 * @return the out parameter for chaining */
	public BoundingBox extendBoundingBox(final BoundingBox out) {
		final int n = nodes.size;
		for(int i = 0; i < n; i++)
			nodes.get(i).extendBoundingBox(out);
		return out;
	}

	/** @param id The ID of the animation to fetch (case sensitive).
	 * @return The {@link Animation} with the specified id, or null if not available. */
	public Animation getAnimation(final String id) {
		return getAnimation(id, true);
	}
	
	/** @param id The ID of the animation to fetch.
	 * @param ignoreCase whether to use case sensitivity when comparing the animation id.
	 * @return The {@link Animation} with the specified id, or null if not available. */
	public Animation getAnimation(final String id, boolean ignoreCase) {
		final int n = animations.size;
		Animation animation;
		if (ignoreCase) {
			for (int i = 0; i < n; i++)
				if ((animation = animations.get(i)).id.equalsIgnoreCase(id))
					return animation;
		} else {
			for (int i = 0; i < n; i++)
				if ((animation = animations.get(i)).id.equals(id))
					return animation;
		}
		return null;
	}
	
	/** @param id The ID of the material to fetch.
	 * @return The {@link Material} with the specified id, or null if not available. */
	public Material getMaterial(final String id) {
		return getMaterial(id, true);
	}
	
	/** @param id The ID of the material to fetch.
	 * @param ignoreCase whether to use case sensitivity when comparing the material id.
	 * @return The {@link Material} with the specified id, or null if not available. */
	public Material getMaterial(final String id, boolean ignoreCase) {
		final int n = materials.size;
		Material material;
		if (ignoreCase) {
			for (int i = 0; i < n; i++)
				if ((material = materials.get(i)).id.equalsIgnoreCase(id))
					return material;
		} else {
			for (int i = 0; i < n; i++)
				if ((material = materials.get(i)).id.equals(id))
					return material;
		}
		return null;
	}
	
	/** @param id The ID of the node to fetch.
	 * @return The {@link Node} with the specified id, or null if not found. */
	public Node getNode(final String id) {
		return getNode(id, true);
	}
	
	/** @param id The ID of the node to fetch.
	 * @param recursive false to fetch a root node only, true to search the entire node tree for the specified node.
	 * @return The {@link Node} with the specified id, or null if not found. */
	public Node getNode(final String id, boolean recursive) {
		return getNode(id, recursive, false);
	}
	
	/** @param id The ID of the node to fetch.
	 * @param recursive false to fetch a root node only, true to search the entire node tree for the specified node.
	 * @param ignoreCase whether to use case sensitivity when comparing the node id.
	 * @return The {@link Node} with the specified id, or null if not found. */
	public Node getNode(final String id, boolean recursive, boolean ignoreCase) {
		return Node.getNode(nodes, id, recursive, ignoreCase);
	}
}
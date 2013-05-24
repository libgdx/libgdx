package com.badlogic.gdx.graphics.g3d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.materials.Material;
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
	public Matrix4 transform;
	/** a copy of the materials of the original model **/
	public final Array<Material> materials = new Array<Material>();
	/** a copy of the nodes of the original model, referencing the copied materials in their {@link NodePart} instances **/
	public final Array<Node> nodes = new Array<Node>();
	/** a copy of the animations of the original model **/
	public final Array<Animation> animations = new Array<Animation>();
	/** user definable value, which is passed to the shader. */
	public Object userData;
	
	public Animation currentAnimation; // FIXME should allow multiple animations or at least transition?
	public float currentAnimTime; // FIXME should allow multiple animations or at least transition?
	
	/** Constructs a new ModelInstance with all nodes and materials of the given model. */
	public ModelInstance(Model model) {
		this(model, (String[])null);
	}
	
	/** Constructs a new ModelInstance with only the specified nodes and materials of the given model. */
	public ModelInstance(Model model, final String... rootNodeIds) {
		this(model, null, rootNodeIds);
	}
	
	/** Constructs a new ModelInstance with only the specified nodes and materials of the given model. */
	public ModelInstance(Model model, final Matrix4 transform, final String... rootNodeIds) {
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
	public ModelInstance(Model model, final Array<String> rootNodeIds) {
		this(model, null, rootNodeIds);
	}
	
	/** Constructs a new ModelInstance with only the specified nodes and materials of the given model. */
	public ModelInstance(Model model, final Matrix4 transform, final Array<String> rootNodeIds) {
		this.model = model;
		this.transform = transform == null ? new Matrix4() : transform;
		copyNodes(model.nodes, rootNodeIds);
		copyAnimations(model.animations);
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

	private ObjectMap<NodePart, String[]> nodePartBones = new ObjectMap<NodePart, String[]>();
	private void copyNodes (Array<Node> nodes) {
		nodePartBones.clear();
		for(Node node: nodes) {
			this.nodes.add(copyNode(null, node));
		}
		setBones();
	}
	
	private void copyNodes (Array<Node> nodes, final String... nodeIds) {
		nodePartBones.clear();
		for(Node node: nodes) {
			for (final String nodeId : nodeIds) {
				if (nodeId.equals(node.id)) {
					this.nodes.add(copyNode(null, node));
					break;
				}
			}
		}
		setBones();
	}
	
	private void copyNodes (Array<Node> nodes, final Array<String> nodeIds) {
		nodePartBones.clear();
		for(Node node: nodes) {
			for (final String nodeId : nodeIds) {
				if (nodeId.equals(node.id)) {
					this.nodes.add(copyNode(null, node));
					break;
				}
			}
		}
		setBones();
	}
	
	private void setBones() {
		for (ObjectMap.Entry<NodePart,String[]> e : nodePartBones.entries()) {
			if (e.key.bones == null)
				e.key.bones = new ArrayMap<Node, Matrix4>(true, e.value.length, Node.class, Matrix4.class);
			e.key.bones.clear();
			for (final String n : e.value) {
				final Node node = getNode(n);
				e.key.bones.put(node, node == null ? null : node.boneTransform);
			}
		}
	}
	
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
		copy.invInitialTransform.set(node.invInitialTransform);
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
		
		if (nodePart.bones != null) {
			final String bones[] = new String[nodePart.bones.size];
			for (int i = 0; i < nodePart.bones.size; i++)
				bones[i] = nodePart.bones.getKeyAt(i).id;
			nodePartBones.put(copy, bones);
		}
		
		final int index = materials.indexOf(nodePart.material, false);
		if (index < 0)
			materials.add(copy.material = nodePart.material.copy());
		else
			copy.material = materials.get(index);
		
		return copy;
	}
	
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
	
	/** Calculate the bounding box of this model instance.
	 * This is a potential slow operation, it is advised to cache the result. */
	public BoundingBox calculateBoundingBox(final BoundingBox out) {
		out.inf();
		for (final Node node : nodes)
			calculateBoundingBox(out, node);
		return out;
	}
	
	protected void calculateBoundingBox(final BoundingBox out, final Node node) {
		for (final NodePart mpm : node.parts)
			mpm.meshPart.mesh.calculateBoundingBox(out, mpm.meshPart.indexOffset, mpm.meshPart.numVertices);
		for (final Node child : node.children)
			calculateBoundingBox(out, child);
	}
	
	public Animation getAnimation(final String id) {
		for (final Animation anim : animations)
			if (anim.id.compareTo(id)==0)
				return anim;
		return null;
	}
	
	public Node getNode(final String id) {
		return getNode(id, nodes);
	}
	
	protected Node getNode(final String id, final Iterable<Node> nodes) {
		for (final Node node : nodes) {
			if (node.id.equals(id))
				return node;
			final Node n = getNode(id, node.children);
			if (n != null)
				return n;
		}
		return null;
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
		if(node.parts.size > 0) {
			for(NodePart nodePart: node.parts) {
				Renderable renderable = pool.obtain();
				renderable.material = nodePart.material;
				renderable.mesh = nodePart.meshPart.mesh;
				renderable.meshPartOffset = nodePart.meshPart.indexOffset;
				renderable.meshPartSize = nodePart.meshPart.numVertices;
				renderable.primitiveType = nodePart.meshPart.primitiveType;
				renderable.bones = nodePart.bones == null ? null : nodePart.bones.values;
				if (transform == null)
					renderable.modelTransform.idt();
				else
					renderable.modelTransform.set(transform);
				renderable.localTransform = node.globalTransform;
				renderable.userData = userData;
				renderables.add(renderable);
			}
		}
		
		for(Node child: node.children) {
			getRenderables(child, renderables, pool);
		}
	}
}

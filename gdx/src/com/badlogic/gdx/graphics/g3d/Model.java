package com.badlogic.gdx.graphics.g3d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.ModelLoader;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.materials.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.materials.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.materials.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.materials.Material;
import com.badlogic.gdx.graphics.g3d.materials.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.model.Animation;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.model.NodeAnimation;
import com.badlogic.gdx.graphics.g3d.model.NodeKeyframe;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.data.ModelAnimation;
import com.badlogic.gdx.graphics.g3d.model.data.ModelNodeAnimation;
import com.badlogic.gdx.graphics.g3d.model.data.ModelNodeKeyframe;
import com.badlogic.gdx.graphics.g3d.model.data.ModelData;
import com.badlogic.gdx.graphics.g3d.model.data.ModelMaterial;
import com.badlogic.gdx.graphics.g3d.model.data.ModelMesh;
import com.badlogic.gdx.graphics.g3d.model.data.ModelMeshPart;
import com.badlogic.gdx.graphics.g3d.model.data.ModelNodePart;
import com.badlogic.gdx.graphics.g3d.model.data.ModelNode;
import com.badlogic.gdx.graphics.g3d.model.data.ModelTexture;
import com.badlogic.gdx.graphics.g3d.utils.TextureDescriptor;
import com.badlogic.gdx.graphics.g3d.utils.TextureProvider;
import com.badlogic.gdx.graphics.g3d.utils.TextureProvider.FileTextureProvider;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Pool;

/**
 * A model represents a 3D assets. It stores a hierarchy of nodes. A node has a transform and optionally
 * a graphical part in form of a {@link MeshPart} and {@link Material}. Mesh parts reference subsets of
 * vertices in one of the meshes of the model. Animations can be applied to nodes, to modify their
 * transform (translation, rotation, scale) over time.</p>
 *
 * A model can be rendered by creating a {@link ModelInstance} from it. That instance has an additional
 * transform to position the model in the world, and allows modification of materials and nodes without
 * destroying the original model. The original model is the owner of any meshes and textures, all instances
 * created from the model share these resources. Disposing the model will automatically make all instances
 * invalid!</p>
 * 
 * A model is created from {@link ModelData}, which in turn is loaded by a {@link ModelLoader}.
 *   
 * @author badlogic
 *
 */
public class Model implements Disposable {
	/** the materials of the model, used by nodes that have a graphical representation FIXME not sure if superfluous, allows modification of materials without having to traverse the nodes **/
	public final Array<Material> materials = new Array<Material>();
	/** root nodes of the model **/
	public final Array<Node> nodes = new Array<Node>();
	/** animations of the model, modifying node transformations **/
	public final Array<Animation> animations = new Array<Animation>();
	/** the meshes of the model **/
	public final Array<Mesh> meshes = new Array<Mesh>();
	/** parts of meshes, used by nodes that have a graphical representation FIXME not sure if superfluous, stored in Nodes as well, could be useful to create bullet meshes **/
	public final Array<MeshPart> meshParts = new Array<MeshPart>();
	/** List of disposable resources like textures or meshes the Model is responsible for disposing **/
	protected final Array<Disposable> disposables = new Array<Disposable>();
	
	/** Constructs an empty model. Manual created models do not manage their resources by default. 
	 * Use {@link #manageDisposable(Disposable)} to add resources to be managed by this model. */
	public Model() {}
	
	/**
	 * Constructs a new Model based on the {@link ModelData}. Texture files
	 * will be loaded from the internal file storage via an {@link FileTextureProvider}.
	 * @param modelData
	 */
	public Model(ModelData modelData) {
		this(modelData, new FileTextureProvider());
	}
	
	public Model(ModelData modelData, TextureProvider textureProvider) {
		load(modelData, textureProvider);
	}
	
	private void load(ModelData modelData, TextureProvider textureProvider) {
		loadMeshes(modelData.meshes);
		loadMaterials(modelData.materials, textureProvider);
		loadNodes(modelData.nodes);
		loadAnimations(modelData.animations);
		calculateTransforms();
	}
	
	private void loadAnimations (Iterable<ModelAnimation> modelAnimations) {
		for (final ModelAnimation anim : modelAnimations) {
			Animation animation = new Animation();
			animation.id = anim.id;
			for (ModelNodeAnimation nanim : anim.nodeAnimations) {
				final Node node = getNode(nanim.nodeId);
				if (node == null)
					continue;
				NodeAnimation nodeAnim = new NodeAnimation();
				nodeAnim.node = node;
				for (ModelNodeKeyframe kf : nanim.keyframes) {
					if (kf.keytime > animation.duration)
						animation.duration = kf.keytime;
					NodeKeyframe keyframe = new NodeKeyframe();
					keyframe.keytime = kf.keytime;
					keyframe.rotation.set(kf.rotation == null ? node.rotation : kf.rotation);
					keyframe.scale.set(kf.scale == null ? node.scale : kf.scale);
					keyframe.translation.set(kf.translation == null ? node.translation : kf.translation);					
					nodeAnim.keyframes.add(keyframe);
				}
				if (nodeAnim.keyframes.size > 0)
					animation.nodeAnimations.add(nodeAnim);
			}
			if (animation.nodeAnimations.size > 0)
				animations.add(animation);
		}
	}

	private ObjectMap<NodePart, ArrayMap<String, Matrix4>> nodePartBones = new ObjectMap<NodePart, ArrayMap<String, Matrix4>>(); 
	private void loadNodes (Iterable<ModelNode> modelNodes) {
		nodePartBones.clear();
		for(ModelNode node: modelNodes) {
			nodes.add(loadNode(null, node));
		}
		for (ObjectMap.Entry<NodePart,ArrayMap<String, Matrix4>> e : nodePartBones.entries()) {
			if (e.key.invBoneBindTransforms == null)
				e.key.invBoneBindTransforms = new ArrayMap<Node, Matrix4>(Node.class, Matrix4.class);
			e.key.invBoneBindTransforms.clear();
			for (ObjectMap.Entry<String, Matrix4> b : e.value.entries())
				e.key.invBoneBindTransforms.put(getNode(b.key), new Matrix4(b.value).inv());
		}
	}

	private Node loadNode (Node parent, ModelNode modelNode) {
		Node node = new Node();
		node.id = modelNode.id;
		// node.boneId = modelNode.boneId; // FIXME check meaning of this, breadth first index of node hierarchy?
		node.parent = parent;
		if (modelNode.translation != null)
			node.translation.set(modelNode.translation);
		if (modelNode.rotation != null)
			node.rotation.set(modelNode.rotation);
		if (modelNode.scale != null)
			node.scale.set(modelNode.scale);
		// FIXME create temporary maps for faster lookup?
		if (modelNode.parts != null) {
			for(ModelNodePart modelNodePart: modelNode.parts) {
				MeshPart meshPart = null;
				Material meshMaterial = null;
				if(modelNodePart.meshPartId != null) {
					for(MeshPart part: meshParts) {
						// FIXME need to make sure this is unique by adding mesh id to mesh part id!
						if(modelNodePart.meshPartId.equals(part.id)) {
							meshPart = part;
							break;
						}
					}
				}
				if(modelNodePart.materialId != null) {
					for(Material material: materials) {
						if(modelNodePart.materialId.equals(material.id)) {
							meshMaterial = material;
							break;
						}
					}
				}
				
				// FIXME what if meshPart is set but meshMaterial isn't and vice versa?
				if(meshPart != null && meshMaterial != null) {
					NodePart nodePart = new NodePart();
					nodePart.meshPart = meshPart;
					nodePart.material = meshMaterial;
					node.parts.add(nodePart);
					if (modelNodePart.bones != null)
						nodePartBones.put(nodePart, modelNodePart.bones);
				}
			}
		}
		
		if(modelNode.children != null) {
			for(ModelNode child: modelNode.children) {
				node.children.add(loadNode(node, child));
			}
		}
		
		return node;
	}

	private void loadMeshes (Iterable<ModelMesh> meshes) {
		for(ModelMesh mesh: meshes) {
			convertMesh(mesh);
		}
	}

	private void convertMesh (ModelMesh modelMesh) {
		int numIndices = 0;
		for(ModelMeshPart part: modelMesh.parts) {
			numIndices += part.indices.length;
		}
		VertexAttributes attributes = new VertexAttributes(modelMesh.attributes);
		int numVertices = modelMesh.vertices.length / (attributes.vertexSize / 4);
		
		Mesh mesh = new Mesh(true, numVertices, numIndices, attributes);
		meshes.add(mesh);
		disposables.add(mesh);
		
		BufferUtils.copy(modelMesh.vertices, mesh.getVerticesBuffer(), modelMesh.vertices.length, 0);
		int offset = 0;
		mesh.getIndicesBuffer().clear();
		for(ModelMeshPart part: modelMesh.parts) {
			MeshPart meshPart = new MeshPart();
			meshPart.id = part.id; // FIXME not storing the mesh name, part ids may collide!
			meshPart.primitiveType = part.primitiveType;
			meshPart.indexOffset = offset;
			meshPart.numVertices = part.indices.length;
			meshPart.mesh = mesh;
			mesh.getIndicesBuffer().put(part.indices);
			offset += meshPart.numVertices;
			meshParts.add(meshPart);
		}
		mesh.getIndicesBuffer().position(0);
	}

	private void loadMaterials (Iterable<ModelMaterial> modelMaterials, TextureProvider textureProvider) {
		for(ModelMaterial mtl: modelMaterials) {
			this.materials.add(convertMaterial(mtl, textureProvider));
		}
	}
	
	private Material convertMaterial(ModelMaterial mtl, TextureProvider textureProvider) {
		Material result = new Material();
		result.id = mtl.id;
		if (mtl.ambient != null)
			result.set(new ColorAttribute(ColorAttribute.Ambient, mtl.ambient));
		if (mtl.diffuse != null)
			result.set(new ColorAttribute(ColorAttribute.Diffuse, mtl.diffuse));
		if (mtl.specular != null)
			result.set(new ColorAttribute(ColorAttribute.Specular, mtl.specular));
		if (mtl.emissive != null)
			result.set(new ColorAttribute(ColorAttribute.Emissive, mtl.emissive));
		if (mtl.shininess > 0f)
			result.set(new FloatAttribute(FloatAttribute.Shininess, mtl.shininess));
		if (mtl.opacity != 1.f)
			result.set(new BlendingAttribute(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA, mtl.opacity));
		
		ObjectMap<String, Texture> textures = new ObjectMap<String, Texture>();
		
		// FIXME mipmapping totally ignored, filters totally ignored, uvScaling/uvTranslation totally ignored
		if(mtl.textures != null) {
			for(ModelTexture tex: mtl.textures) {
				Texture texture;
				if(textures.containsKey(tex.fileName)) {
					texture = textures.get(tex.fileName);
				} else {
					texture = textureProvider.load(tex.fileName);
					textures.put(tex.fileName, texture);
					disposables.add(texture);
				}
				
				TextureDescriptor descriptor = new TextureDescriptor(texture);
				descriptor.minFilter = GL20.GL_LINEAR;
				descriptor.magFilter = GL20.GL_LINEAR;
				descriptor.uWrap = GL20.GL_REPEAT;
				descriptor.vWrap = GL20.GL_REPEAT;
				switch (tex.usage) {
				case ModelTexture.USAGE_DIFFUSE:
					result.set(new TextureAttribute(TextureAttribute.Diffuse, descriptor));
					break;
				case ModelTexture.USAGE_SPECULAR:
					result.set(new TextureAttribute(TextureAttribute.Specular, descriptor));
					break;
				case ModelTexture.USAGE_BUMP:
					result.set(new TextureAttribute(TextureAttribute.Bump, descriptor));
					break;
				case ModelTexture.USAGE_NORMAL:
					result.set(new TextureAttribute(TextureAttribute.Normal, descriptor));
					break;					
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Adds a {@link Disposable} to be managed and disposed by this Model. Can
	 * be used to keep track of manually loaded textures for {@link ModelInstance}.
	 * @param disposable the Disposable
	 */
	public void manageDisposable(Disposable disposable) {
		if (!disposables.contains(disposable, true))
			disposables.add(disposable);
	}
	
	public Iterable<Disposable> getManagedDisposables() {
		return disposables;
	}

	@Override
	public void dispose () {
		for(Disposable disposable: disposables) {
			disposable.dispose();
		}
	}
	
	/**
	 * Calculates the local and world transform of all {@link Node} instances in this model, recursively.
	 * First each {@link Node#localTransform} transform is calculated based on the translation, rotation and
	 * scale of each Node. Then each {@link Node#calculateWorldTransform()}
	 * is calculated, based on the parent's world transform and the local transform of each Node.
	 * Finally, the animation bone matrices are updated accordingly.</p>
	 * 
	 * This method can be used to recalculate all transforms if any of the Node's local properties (translation, rotation, scale)
	 * was modified.
	 */
	public void calculateTransforms() {
		for(Node node: nodes) {
			node.calculateTransforms(true);
		}
		for(Node node: nodes) {
			node.calculateBoneTransforms(true);
		}
	}
	
	/** Calculate the bounding box of this model instance.
	 * This is a potential slow operation, it is advised to cache the result. */
	public BoundingBox calculateBoundingBox(final BoundingBox out) {
		out.inf();
		return extendBoundingBox(out);
	}
	
	/** Extends the bounding box with the bounds of this model instance.
	 * This is a potential slow operation, it is advised to cache the result. */
	public BoundingBox extendBoundingBox(final BoundingBox out) {
		for (final Node node : nodes)
			calculateBoundingBox(out, node);
		return out;
	}
	
	protected void calculateBoundingBox(final BoundingBox out, final Node node) {
		for (final NodePart mpm : node.parts)
			mpm.meshPart.mesh.calculateBoundingBox(out, mpm.meshPart.indexOffset, mpm.meshPart.numVertices, node.globalTransform);
		for (final Node child : node.children)
			calculateBoundingBox(out, child);
	}
	
	public Animation getAnimation(final String id) {
		for (final Animation anim : animations)
			if (anim.id.compareTo(id)==0)
				return anim;
		return null;
	}
	
	public Material getMaterial(final String id) {
		for (final Material mtl : materials)
			if (mtl.id.compareTo(id)==0)
				return mtl;
		return null;
	}
	
	/** @param recursive false to fetch a root node only, true to search the entire node tree for the specified node.
	 * @return The node with the specified id, or null if not found. */
	public Node getNode(final String id, boolean recursive) {
		return getNode(id, nodes, recursive);
	}
	
	/** @return The node with the specified id, or null if not found. */
	public Node getNode(final String id) {
		return getNode(id, true);
	}
	
	protected Node getNode(final String id, final Iterable<Node> nodes, boolean recursive) {
		for (final Node node : nodes) {
			if (node.id.equals(id))
				return node;
			if (recursive) {
				final Node n = getNode(id, node.children, recursive);
				if (n != null)
					return n;
			}
		}
		return null;
	}
}
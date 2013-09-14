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
 * @author badlogic, xoppa */
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
	
	/** Constructs a new Model based on the {@link ModelData}. Texture files
	 * will be loaded from the internal file storage via an {@link FileTextureProvider}.
	 * @param modelData the {@link ModelData} got from e.g. {@link ModelLoader} */
	public Model(ModelData modelData) {
		this(modelData, new FileTextureProvider());
	}

	/** Constructs a new Model based on the {@link ModelData}.
	 * @param modelData the {@link ModelData} got from e.g. {@link ModelLoader}
	 * @param textureProvider the {@link TextureProvider} to use for loading the textures */
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
				
				if (meshPart == null || meshMaterial == null)
					throw new GdxRuntimeException("Invalid node: "+node.id);
				
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
			meshPart.id = part.id;
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
		if (mtl.reflection != null)
			result.set(new ColorAttribute(ColorAttribute.Reflection, mtl.reflection));
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
				descriptor.minFilter = Texture.TextureFilter.Linear;
				descriptor.magFilter = Texture.TextureFilter.Linear;
				descriptor.uWrap = Texture.TextureWrap.Repeat;
				descriptor.vWrap = Texture.TextureWrap.Repeat;
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
	
	/** Adds a {@link Disposable} to be managed and disposed by this Model. Can
	 * be used to keep track of manually loaded textures for {@link ModelInstance}.
	 * @param disposable the Disposable */
	public void manageDisposable(Disposable disposable) {
		if (!disposables.contains(disposable, true))
			disposables.add(disposable);
	}
	
	/** @return the {@link Disposable} objects that will be disposed when the {@link #dispose()} method is called. */
	public Iterable<Disposable> getManagedDisposables() {
		return disposables;
	}

	@Override
	public void dispose () {
		for(Disposable disposable: disposables) {
			disposable.dispose();
		}
	}
	
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
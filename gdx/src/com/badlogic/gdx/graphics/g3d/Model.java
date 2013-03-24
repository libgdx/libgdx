package com.badlogic.gdx.graphics.g3d;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.materials.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.materials.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.materials.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.materials.NewMaterial;
import com.badlogic.gdx.graphics.g3d.materials.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.model.Animation;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.model.MeshPartMaterial;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.data.ModelData;
import com.badlogic.gdx.graphics.g3d.model.data.ModelMaterial;
import com.badlogic.gdx.graphics.g3d.model.data.ModelMesh;
import com.badlogic.gdx.graphics.g3d.model.data.ModelMeshPart;
import com.badlogic.gdx.graphics.g3d.model.data.ModelMeshPartMaterial;
import com.badlogic.gdx.graphics.g3d.model.data.ModelNode;
import com.badlogic.gdx.graphics.g3d.model.data.ModelTexture;
import com.badlogic.gdx.graphics.g3d.old.loaders.ModelLoader;
import com.badlogic.gdx.graphics.g3d.old.materials.Material;
import com.badlogic.gdx.graphics.g3d.old.materials.MaterialAttribute;
import com.badlogic.gdx.graphics.g3d.utils.TextureDescriptor;
import com.badlogic.gdx.graphics.g3d.utils.TextureProvider;
import com.badlogic.gdx.graphics.g3d.utils.TextureProvider.FileTextureProvider;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Pool;

/**
 * A model represents a 3D assets. It stores a hierarchy of nodes. A node has a transform and optionally
 * a graphical part in form of a {@link MeshPart} and {@link NewMaterial}. Mesh parts reference subsets of
 * vertices in one of the meshes of the model. Animations can be applied to nodes, to modify their
 * transform (translation, rotation, scale) over time.</p>
 * 
 * A model can be converted to {@link Renderable} instances via {@link #getRenderables(Array, Pool)}, which
 * can be rendered by a {@link ModelBatch}.
 * 
 * A model is derrived from {@link ModelData}, which in turn is loaded by a {@link ModelLoader}.
 *   
 * @author badlogic
 *
 */
public class Model {
	/** the meshes of the model **/
	public Array<Mesh> meshes = new Array<Mesh>();
	/** parts of meshes, used by nodes that have a graphical representation FIXME not sure if superfluous, stored in Nodes as well, could be useful to create bullet meshes **/
	public Array<MeshPart> meshParts = new Array<MeshPart>();
	/** the materials of the model, used by nodes that have a graphical representation FIXME not sure if superfluous, allows modification of materials without having to traverse the nodes**/
	public Array<NewMaterial> materials = new Array<NewMaterial>();
	/** root nodes of the model **/
	public Array<Node> nodes = new Array<Node>();
	/** animations of the model, modifiying node transformations **/
	public Array<Animation> animation = new Array<Animation>();
	
	/**
	 * Constructs a new Model based on the {@link ModelData}. Texture files
	 * will be loaded from the internal file storage via an {@link FileTextureProvider}.
	 * @param modelData
	 */
	public Model(ModelData modelData) {
		load(modelData, new FileTextureProvider());
	}
	
	public Model(ModelData modelData, TextureProvider textureProvider) {
		load(modelData, textureProvider);
	}

	private void load (ModelData modelData, TextureProvider textureProvider) {
		loadMeshes(modelData.meshes);
		loadMaterials(modelData.materials, textureProvider);
		loadNodes(modelData.nodes);
		calculateTransforms();
	}

	private void loadNodes (Iterable<ModelNode> modelNodes) {
		for(ModelNode node: modelNodes) {
			nodes.add(loadNode(null, node));
		}
	}

	private Node loadNode (Node parent, ModelNode modelNode) {
		Node node = new Node();
		node.id = modelNode.id;
		node.boneId = modelNode.boneId; // FIXME check meaning of this, breadth first index of node hierarchy?
		node.parent = parent;
		node.translation.set(modelNode.translation);
		node.rotation.set(modelNode.rotation);
		node.scale.set(modelNode.scale);
		// FIXME create temporary maps for faster lookup?
		for(ModelMeshPartMaterial modelMeshPartMaterial: modelNode.meshPartMaterials) {
			MeshPart meshPart = null;
			NewMaterial meshMaterial = null;
			if(modelMeshPartMaterial.meshPartId != null) {
				for(MeshPart part: meshParts) {
					// FIXME need to make sure this is unique by adding mesh id to mesh part id!
					if(modelMeshPartMaterial.meshPartId.equals(part.id)) {
						meshPart = part;
						break;
					}
				}
			}
			if(modelMeshPartMaterial.materialId != null) {
				for(NewMaterial material: materials) {
					if(modelMeshPartMaterial.materialId.equals(material.id)) {
						meshMaterial = material;
						break;
					}
				}
			}
			
			// FIXME what if meshPart is set but meshMaterial isn't and vice versa?
			if(meshPart != null && meshMaterial != null) {
				MeshPartMaterial meshPartMaterial = new MeshPartMaterial();
				meshPartMaterial.meshPart = meshPart;
				meshPartMaterial.material = meshMaterial;
				node.meshPartMaterials.add(meshPartMaterial);
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
	
	private NewMaterial convertMaterial(ModelMaterial mtl, TextureProvider textureProvider) {
		NewMaterial result = new NewMaterial();
		result.id = mtl.id;
		result.add(new ColorAttribute(ColorAttribute.Ambient, mtl.ambient));
		result.add(new ColorAttribute(ColorAttribute.Diffuse, mtl.diffuse));
		result.add(new ColorAttribute(ColorAttribute.Specular, mtl.specular));
		result.add(new ColorAttribute(ColorAttribute.Emissive, mtl.emissive));
		result.add(new FloatAttribute(FloatAttribute.Shininess, mtl.shininess));
		
		ObjectMap<String, Texture> textures = new ObjectMap<String, Texture>();
		
		// FIXME mipmapping totally ignored, filters totally ignored
		if(mtl.diffuseTextures != null) {
			for(ModelTexture tex: mtl.diffuseTextures) {
				if(textures.containsKey(tex.fileName)) continue;
				Texture texture = textureProvider.load(tex.fileName);
				textures.put(tex.fileName, texture);
			}
			
			for(ModelTexture tex: mtl.diffuseTextures) {
				TextureDescriptor descriptor = new TextureDescriptor();
				descriptor.texture = textures.get(tex.fileName);
				descriptor.minFilter = GL20.GL_LINEAR;
				descriptor.magFilter = GL20.GL_LINEAR;
				descriptor.uWrap = GL20.GL_CLAMP_TO_EDGE;
				descriptor.vWrap = GL20.GL_CLAMP_TO_EDGE;
				result.add(new TextureAttribute(TextureAttribute.Diffuse, descriptor));
			}
		}
		
		return result;
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
				renderable.transform.set(node.worldTransform);
				renderables.add(renderable);
			}
		}
		
		for(Node child: node.children) {
			getRenderables(child, renderables, pool);
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
}
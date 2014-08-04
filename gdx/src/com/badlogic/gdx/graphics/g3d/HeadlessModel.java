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

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.model.Animation;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodeAnimation;
import com.badlogic.gdx.graphics.g3d.model.NodeKeyframe;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.graphics.g3d.model.data.ModelAnimation;
import com.badlogic.gdx.graphics.g3d.model.data.ModelData;
import com.badlogic.gdx.graphics.g3d.model.data.ModelMaterial;
import com.badlogic.gdx.graphics.g3d.model.data.ModelMesh;
import com.badlogic.gdx.graphics.g3d.model.data.ModelMeshPart;
import com.badlogic.gdx.graphics.g3d.model.data.ModelNode;
import com.badlogic.gdx.graphics.g3d.model.data.ModelNodeAnimation;
import com.badlogic.gdx.graphics.g3d.model.data.ModelNodeKeyframe;
import com.badlogic.gdx.graphics.g3d.model.data.ModelNodePart;
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

public class HeadlessModel implements Disposable {

	public final Array<Node> nodes = new Array();
	/** animations of the model, modifying node transformations **/
	public final Array<Animation> animations = new Array();
	/** the meshes of the model **/
	public final Array<Mesh> meshes = new Array();
	/** parts of meshes, used by nodes that have a graphical representation FIXME not sure if superfluous, stored in Nodes as well,
	 * could be useful to create bullet meshes **/
	public final Array<MeshPart> meshParts = new Array();
	/** Array of disposable resources like textures or meshes the Model is responsible for disposing **/
	protected final Array<Disposable> disposables = new Array();

	/** Constructs an empty model. Manual created models do not manage their resources by default. Use
	 * {@link #manageDisposable(com.badlogic.gdx.utils.Disposable)} to add resources to be managed by this model. */
	public HeadlessModel() {
	}

	/** Constructs a new Model based on the {@link com.badlogic.gdx.graphics.g3d.model.data.ModelData}. Texture files will be loaded from the internal file storage via an
	 * {@link com.badlogic.gdx.graphics.g3d.utils.TextureProvider.FileTextureProvider}.
	 * @param modelData the {@link com.badlogic.gdx.graphics.g3d.model.data.ModelData} got from e.g. {@link com.badlogic.gdx.assets.loaders.ModelLoader} */
	public HeadlessModel(ModelData modelData) {
		this(modelData, new FileTextureProvider());
	}

	/** Constructs a new Model based on the {@link com.badlogic.gdx.graphics.g3d.model.data.ModelData}.
	 * @param modelData the {@link com.badlogic.gdx.graphics.g3d.model.data.ModelData} got from e.g. {@link com.badlogic.gdx.assets.loaders.ModelLoader}
	 * @param textureProvider the {@link com.badlogic.gdx.graphics.g3d.utils.TextureProvider} to use for loading the textures */
	public HeadlessModel(ModelData modelData, TextureProvider textureProvider) {
		load(modelData, textureProvider);
	}

	private void load (ModelData modelData, TextureProvider textureProvider) {
		loadMeshes(modelData.meshes);
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
				if (node == null) continue;
				NodeAnimation nodeAnim = new NodeAnimation();
				nodeAnim.node = node;
				for (ModelNodeKeyframe kf : nanim.keyframes) {
					if (kf.keytime > animation.duration) animation.duration = kf.keytime;
					NodeKeyframe keyframe = new NodeKeyframe();
					keyframe.keytime = kf.keytime;
					keyframe.rotation.set(kf.rotation == null ? node.rotation : kf.rotation);
					keyframe.scale.set(kf.scale == null ? node.scale : kf.scale);
					keyframe.translation.set(kf.translation == null ? node.translation : kf.translation);
					nodeAnim.keyframes.add(keyframe);
				}
				if (nodeAnim.keyframes.size > 0) animation.nodeAnimations.add(nodeAnim);
			}
			if (animation.nodeAnimations.size > 0) animations.add(animation);
		}
	}

	private ObjectMap<NodePart, ArrayMap<String, Matrix4>> nodePartBones = new ObjectMap<NodePart, ArrayMap<String, Matrix4>>();

	private void loadNodes (Iterable<ModelNode> modelNodes) {
		nodePartBones.clear();
		for (ModelNode node : modelNodes) {
			nodes.add(loadNode(null, node));
		}
		for (ObjectMap.Entry<NodePart, ArrayMap<String, Matrix4>> e : nodePartBones.entries()) {
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

		if (modelNode.translation != null) node.translation.set(modelNode.translation);
		if (modelNode.rotation != null) node.rotation.set(modelNode.rotation);
		if (modelNode.scale != null) node.scale.set(modelNode.scale);
		// FIXME create temporary maps for faster lookup?
		if (modelNode.parts != null) {
			for (ModelNodePart modelNodePart : modelNode.parts) {
				MeshPart meshPart = null;
				Material meshMaterial = null;

				if (modelNodePart.meshPartId != null) {
					for (MeshPart part : meshParts) {
						if (modelNodePart.meshPartId.equals(part.id)) {
							meshPart = part;
							break;
						}
					}
				}

				if (meshPart == null || meshMaterial == null) throw new GdxRuntimeException("Invalid node: " + node.id);

				if (meshPart != null && meshMaterial != null) {
					NodePart nodePart = new NodePart();
					nodePart.meshPart = meshPart;
					nodePart.material = meshMaterial;
					node.parts.add(nodePart);
					if (modelNodePart.bones != null) nodePartBones.put(nodePart, modelNodePart.bones);
				}
			}
		}

		if (modelNode.children != null) {
			for (ModelNode child : modelNode.children) {
				node.children.add(loadNode(node, child));
			}
		}

		return node;
	}

	private void loadMeshes (Iterable<ModelMesh> meshes) {
		for (ModelMesh mesh : meshes) {
			convertMesh(mesh);
		}
	}

	private void convertMesh (ModelMesh modelMesh) {
		int numIndices = 0;
		for (ModelMeshPart part : modelMesh.parts) {
			numIndices += part.indices.length;
		}
		VertexAttributes attributes = new VertexAttributes(modelMesh.attributes);
		int numVertices = modelMesh.vertices.length / (attributes.vertexSize / 4);

		Mesh mesh = new Mesh(Mesh.VertexDataType.VertexArray, true, numVertices, numIndices, modelMesh.attributes);
		meshes.add(mesh);
		disposables.add(mesh);

		BufferUtils.copy(modelMesh.vertices, mesh.getVerticesBuffer(), modelMesh.vertices.length, 0);
		int offset = 0;
		mesh.getIndicesBuffer().clear();
		for (ModelMeshPart part : modelMesh.parts) {
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

	/** Adds a {@link com.badlogic.gdx.utils.Disposable} to be managed and disposed by this Model. Can be used to keep track of manually loaded textures
	 * for {@link com.badlogic.gdx.graphics.g3d.ModelInstance}.
	 * @param disposable the Disposable */
	public void manageDisposable (Disposable disposable) {
		if (!disposables.contains(disposable, true)) disposables.add(disposable);
	}

	/** @return the {@link com.badlogic.gdx.utils.Disposable} objects that will be disposed when the {@link #dispose()} method is called. */
	public Iterable<Disposable> getManagedDisposables () {
		return disposables;
	}

	@Override
	public void dispose () {
		for (Disposable disposable : disposables) {
			disposable.dispose();
		}
	}

	/** Calculates the local and world transform of all {@link com.badlogic.gdx.graphics.g3d.model.Node} instances in this model, recursively. First each
	 * {@link com.badlogic.gdx.graphics.g3d.model.Node#localTransform} transform is calculated based on the translation, rotation and scale of each Node. Then each
	 * {@link com.badlogic.gdx.graphics.g3d.model.Node#calculateWorldTransform()} is calculated, based on the parent's world transform and the local transform of each
	 * Node. Finally, the animation bone matrices are updated accordingly.</p>
	 *
	 * This method can be used to recalculate all transforms if any of the Node's local properties (translation, rotation, scale)
	 * was modified. */
	public void calculateTransforms () {
		final int n = nodes.size;
		for (int i = 0; i < n; i++) {
			nodes.get(i).calculateTransforms(true);
		}
		for (int i = 0; i < n; i++) {
			nodes.get(i).calculateBoneTransforms(true);
		}
	}

	/** Calculate the bounding box of this model instance. This is a potential slow operation, it is advised to cache the result.
	 * @param out the {@link com.badlogic.gdx.math.collision.BoundingBox} that will be set with the bounds.
	 * @return the out parameter for chaining */
	public BoundingBox calculateBoundingBox (final BoundingBox out) {
		out.inf();
		return extendBoundingBox(out);
	}

	/** Extends the bounding box with the bounds of this model instance. This is a potential slow operation, it is advised to cache
	 * the result.
	 * @param out the {@link com.badlogic.gdx.math.collision.BoundingBox} that will be extended with the bounds.
	 * @return the out parameter for chaining */
	public BoundingBox extendBoundingBox (final BoundingBox out) {
		final int n = nodes.size;
		for (int i = 0; i < n; i++)
			nodes.get(i).extendBoundingBox(out);
		return out;
	}

	/** @param id The ID of the animation to fetch (case sensitive).
	 * @return The {@link com.badlogic.gdx.graphics.g3d.model.Animation} with the specified id, or null if not available. */
	public Animation getAnimation (final String id) {
		return getAnimation(id, true);
	}

	/** @param id The ID of the animation to fetch.
	 * @param ignoreCase whether to use case sensitivity when comparing the animation id.
	 * @return The {@link com.badlogic.gdx.graphics.g3d.model.Animation} with the specified id, or null if not available. */
	public Animation getAnimation (final String id, boolean ignoreCase) {
		final int n = animations.size;
		Animation animation;
		if (ignoreCase) {
			for (int i = 0; i < n; i++)
				if ((animation = animations.get(i)).id.equalsIgnoreCase(id)) return animation;
		} else {
			for (int i = 0; i < n; i++)
				if ((animation = animations.get(i)).id.equals(id)) return animation;
		}
		return null;
	}

	/** @param id The ID of the node to fetch.
	 * @return The {@link com.badlogic.gdx.graphics.g3d.model.Node} with the specified id, or null if not found. */
	public Node getNode (final String id) {
		return getNode(id, true);
	}

	/** @param id The ID of the node to fetch.
	 * @param recursive false to fetch a root node only, true to search the entire node tree for the specified node.
	 * @return The {@link com.badlogic.gdx.graphics.g3d.model.Node} with the specified id, or null if not found. */
	public Node getNode (final String id, boolean recursive) {
		return getNode(id, recursive, false);
	}

	/** @param id The ID of the node to fetch.
	 * @param recursive false to fetch a root node only, true to search the entire node tree for the specified node.
	 * @param ignoreCase whether to use case sensitivity when comparing the node id.
	 * @return The {@link com.badlogic.gdx.graphics.g3d.model.Node} with the specified id, or null if not found. */
	public Node getNode (final String id, boolean recursive, boolean ignoreCase) {
		return Node.getNode(nodes, id, recursive, ignoreCase);
	}
}

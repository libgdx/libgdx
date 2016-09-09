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

package com.badlogic.gdx.graphics.g3d.loader;

import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.ModelLoader;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttribute;
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
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.BaseJsonReader;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.JsonValue;

public class G3dModelLoader extends ModelLoader<ModelLoader.ModelParameters> {
	public static final short VERSION_HI = 0;
	public static final short VERSION_LO = 1;
	protected final BaseJsonReader reader;

	public G3dModelLoader (final BaseJsonReader reader) {
		this(reader, null);
	}

	public G3dModelLoader (BaseJsonReader reader, FileHandleResolver resolver) {
		super(resolver);
		this.reader = reader;
	}

	@Override
	public ModelData loadModelData (FileHandle fileHandle, ModelLoader.ModelParameters parameters) {
		return parseModel(fileHandle);
	}

	public ModelData parseModel (FileHandle handle) {
		JsonValue json = reader.parse(handle);
		ModelData model = new ModelData();
		JsonValue version = json.require("version");
		model.version[0] = version.getShort(0);
		model.version[1] = version.getShort(1);
		if (model.version[0] != VERSION_HI || model.version[1] != VERSION_LO)
			throw new GdxRuntimeException("Model version not supported");

		model.id = json.getString("id", "");
		parseMeshes(model, json);
		parseMaterials(model, json, handle.parent().path());
		parseNodes(model, json);
		parseAnimations(model, json);
		return model;
	}

	private void parseMeshes (ModelData model, JsonValue json) {
		JsonValue meshes = json.get("meshes");
		if (meshes != null) {

			model.meshes.ensureCapacity(meshes.size);
			for (JsonValue mesh = meshes.child; mesh != null; mesh = mesh.next) {
				ModelMesh jsonMesh = new ModelMesh();

				String id = mesh.getString("id", "");
				jsonMesh.id = id;

				JsonValue attributes = mesh.require("attributes");
				jsonMesh.attributes = parseAttributes(attributes);
				jsonMesh.vertices = mesh.require("vertices").asFloatArray();

				JsonValue meshParts = mesh.require("parts");
				Array<ModelMeshPart> parts = new Array<ModelMeshPart>();
				for (JsonValue meshPart = meshParts.child; meshPart != null; meshPart = meshPart.next) {
					ModelMeshPart jsonPart = new ModelMeshPart();
					String partId = meshPart.getString("id", null);
					if (partId == null) {
						throw new GdxRuntimeException("Not id given for mesh part");
					}
					for (ModelMeshPart other : parts) {
						if (other.id.equals(partId)) {
							throw new GdxRuntimeException("Mesh part with id '" + partId + "' already in defined");
						}
					}
					jsonPart.id = partId;

					String type = meshPart.getString("type", null);
					if (type == null) {
						throw new GdxRuntimeException("No primitive type given for mesh part '" + partId + "'");
					}
					jsonPart.primitiveType = parseType(type);

					jsonPart.indices = meshPart.require("indices").asShortArray();
					parts.add(jsonPart);
				}
				jsonMesh.parts = parts.toArray(ModelMeshPart.class);
				model.meshes.add(jsonMesh);
			}
		}
	}

	private int parseType (String type) {
		if (type.equals("TRIANGLES")) {
			return GL20.GL_TRIANGLES;
		} else if (type.equals("LINES")) {
			return GL20.GL_LINES;
		} else if (type.equals("POINTS")) {
			return GL20.GL_POINTS;
		} else if (type.equals("TRIANGLE_STRIP")) {
			return GL20.GL_TRIANGLE_STRIP;
		} else if (type.equals("LINE_STRIP")) {
			return GL20.GL_LINE_STRIP;
		} else {
			throw new GdxRuntimeException("Unknown primitive type '" + type
				+ "', should be one of triangle, trianglestrip, line, linestrip, lineloop or point");
		}
	}

	private VertexAttribute[] parseAttributes (JsonValue attributes) {
		Array<VertexAttribute> vertexAttributes = new Array<VertexAttribute>();
		int unit = 0;
		int blendWeightCount = 0;
		for (JsonValue value = attributes.child; value != null; value = value.next) {
			String attribute = value.asString();
			String attr = (String)attribute;
			if (attr.equals("POSITION")) {
				vertexAttributes.add(VertexAttribute.Position());
			} else if (attr.equals("NORMAL")) {
				vertexAttributes.add(VertexAttribute.Normal());
			} else if (attr.equals("COLOR")) {
				vertexAttributes.add(VertexAttribute.ColorUnpacked());
			} else if (attr.equals("COLORPACKED")) {
				vertexAttributes.add(VertexAttribute.ColorPacked());
			} else if (attr.equals("TANGENT")) {
				vertexAttributes.add(VertexAttribute.Tangent());
			} else if (attr.equals("BINORMAL")) {
				vertexAttributes.add(VertexAttribute.Binormal());
			} else if (attr.startsWith("TEXCOORD")) {
				vertexAttributes.add(VertexAttribute.TexCoords(unit++));
			} else if (attr.startsWith("BLENDWEIGHT")) {
				vertexAttributes.add(VertexAttribute.BoneWeight(blendWeightCount++));
			} else {
				throw new GdxRuntimeException("Unknown vertex attribute '" + attr
					+ "', should be one of position, normal, uv, tangent or binormal");
			}
		}
		return vertexAttributes.toArray(VertexAttribute.class);
	}

	private void parseMaterials (ModelData model, JsonValue json, String materialDir) {
		JsonValue materials = json.get("materials");
		if (materials == null) {
			// we should probably create some default material in this case
		} else {
			model.materials.ensureCapacity(materials.size);
			for (JsonValue material = materials.child; material != null; material = material.next) {
				ModelMaterial jsonMaterial = new ModelMaterial();

				String id = material.getString("id", null);
				if (id == null) throw new GdxRuntimeException("Material needs an id.");

				jsonMaterial.id = id;

				// Read material colors
				final JsonValue diffuse = material.get("diffuse");
				if (diffuse != null) jsonMaterial.diffuse = parseColor(diffuse);
				final JsonValue ambient = material.get("ambient");
				if (ambient != null) jsonMaterial.ambient = parseColor(ambient);
				final JsonValue emissive = material.get("emissive");
				if (emissive != null) jsonMaterial.emissive = parseColor(emissive);
				final JsonValue specular = material.get("specular");
				if (specular != null) jsonMaterial.specular = parseColor(specular);
				final JsonValue reflection = material.get("reflection");
				if (reflection != null) jsonMaterial.reflection = parseColor(reflection);
				// Read shininess
				jsonMaterial.shininess = material.getFloat("shininess", 0.0f);
				// Read opacity
				jsonMaterial.opacity = material.getFloat("opacity", 1.0f);

				// Read textures
				JsonValue textures = material.get("textures");
				if (textures != null) {
					for (JsonValue texture = textures.child; texture != null; texture = texture.next) {
						ModelTexture jsonTexture = new ModelTexture();

						String textureId = texture.getString("id", null);
						if (textureId == null) throw new GdxRuntimeException("Texture has no id.");
						jsonTexture.id = textureId;

						String fileName = texture.getString("filename", null);
						if (fileName == null) throw new GdxRuntimeException("Texture needs filename.");
						jsonTexture.fileName = materialDir + (materialDir.length() == 0 || materialDir.endsWith("/") ? "" : "/")
							+ fileName;

						jsonTexture.uvTranslation = readVector2(texture.get("uvTranslation"), 0f, 0f);
						jsonTexture.uvScaling = readVector2(texture.get("uvScaling"), 1f, 1f);

						String textureType = texture.getString("type", null);
						if (textureType == null) throw new GdxRuntimeException("Texture needs type.");

						jsonTexture.usage = parseTextureUsage(textureType);

						if (jsonMaterial.textures == null) jsonMaterial.textures = new Array<ModelTexture>();
						jsonMaterial.textures.add(jsonTexture);
					}
				}

				model.materials.add(jsonMaterial);
			}
		}
	}

	private int parseTextureUsage (final String value) {
		if (value.equalsIgnoreCase("AMBIENT"))
			return ModelTexture.USAGE_AMBIENT;
		else if (value.equalsIgnoreCase("BUMP"))
			return ModelTexture.USAGE_BUMP;
		else if (value.equalsIgnoreCase("DIFFUSE"))
			return ModelTexture.USAGE_DIFFUSE;
		else if (value.equalsIgnoreCase("EMISSIVE"))
			return ModelTexture.USAGE_EMISSIVE;
		else if (value.equalsIgnoreCase("NONE"))
			return ModelTexture.USAGE_NONE;
		else if (value.equalsIgnoreCase("NORMAL"))
			return ModelTexture.USAGE_NORMAL;
		else if (value.equalsIgnoreCase("REFLECTION"))
			return ModelTexture.USAGE_REFLECTION;
		else if (value.equalsIgnoreCase("SHININESS"))
			return ModelTexture.USAGE_SHININESS;
		else if (value.equalsIgnoreCase("SPECULAR"))
			return ModelTexture.USAGE_SPECULAR;
		else if (value.equalsIgnoreCase("TRANSPARENCY")) return ModelTexture.USAGE_TRANSPARENCY;
		return ModelTexture.USAGE_UNKNOWN;
	}

	private Color parseColor (JsonValue colorArray) {
		if (colorArray.size >= 3)
			return new Color(colorArray.getFloat(0), colorArray.getFloat(1), colorArray.getFloat(2), 1.0f);
		else
			throw new GdxRuntimeException("Expected Color values <> than three.");
	}

	private Vector2 readVector2 (JsonValue vectorArray, float x, float y) {
		if (vectorArray == null)
			return new Vector2(x, y);
		else if (vectorArray.size == 2)
			return new Vector2(vectorArray.getFloat(0), vectorArray.getFloat(1));
		else
			throw new GdxRuntimeException("Expected Vector2 values <> than two.");
	}

	private Array<ModelNode> parseNodes (ModelData model, JsonValue json) {
		JsonValue nodes = json.get("nodes");
		if (nodes != null) {
			model.nodes.ensureCapacity(nodes.size);
			for (JsonValue node = nodes.child; node != null; node = node.next) {
				model.nodes.add(parseNodesRecursively(node));
			}
		}

		return model.nodes;
	}

	private final Quaternion tempQ = new Quaternion();

	private ModelNode parseNodesRecursively (JsonValue json) {
		ModelNode jsonNode = new ModelNode();

		String id = json.getString("id", null);
		if (id == null) throw new GdxRuntimeException("Node id missing.");
		jsonNode.id = id;

		JsonValue translation = json.get("translation");
		if (translation != null && translation.size != 3) throw new GdxRuntimeException("Node translation incomplete");
		jsonNode.translation = translation == null ? null : new Vector3(translation.getFloat(0), translation.getFloat(1),
			translation.getFloat(2));

		JsonValue rotation = json.get("rotation");
		if (rotation != null && rotation.size != 4) throw new GdxRuntimeException("Node rotation incomplete");
		jsonNode.rotation = rotation == null ? null : new Quaternion(rotation.getFloat(0), rotation.getFloat(1),
			rotation.getFloat(2), rotation.getFloat(3));

		JsonValue scale = json.get("scale");
		if (scale != null && scale.size != 3) throw new GdxRuntimeException("Node scale incomplete");
		jsonNode.scale = scale == null ? null : new Vector3(scale.getFloat(0), scale.getFloat(1), scale.getFloat(2));

		String meshId = json.getString("mesh", null);
		if (meshId != null) jsonNode.meshId = meshId;

		JsonValue materials = json.get("parts");
		if (materials != null) {
			jsonNode.parts = new ModelNodePart[materials.size];
			int i = 0;
			for (JsonValue material = materials.child; material != null; material = material.next, i++) {
				ModelNodePart nodePart = new ModelNodePart();

				String meshPartId = material.getString("meshpartid", null);
				String materialId = material.getString("materialid", null);
				if (meshPartId == null || materialId == null) {
					throw new GdxRuntimeException("Node " + id + " part is missing meshPartId or materialId");
				}
				nodePart.materialId = materialId;
				nodePart.meshPartId = meshPartId;

				JsonValue bones = material.get("bones");
				if (bones != null) {
					nodePart.bones = new ArrayMap<String, Matrix4>(true, bones.size, String.class, Matrix4.class);
					int j = 0;
					for (JsonValue bone = bones.child; bone != null; bone = bone.next, j++) {
						String nodeId = bone.getString("node", null);
						if (nodeId == null) throw new GdxRuntimeException("Bone node ID missing");

						Matrix4 transform = new Matrix4();

						JsonValue val = bone.get("translation");
						if (val != null && val.size >= 3) transform.translate(val.getFloat(0), val.getFloat(1), val.getFloat(2));

						val = bone.get("rotation");
						if (val != null && val.size >= 4)
							transform.rotate(tempQ.set(val.getFloat(0), val.getFloat(1), val.getFloat(2), val.getFloat(3)));

						val = bone.get("scale");
						if (val != null && val.size >= 3) transform.scale(val.getFloat(0), val.getFloat(1), val.getFloat(2));

						nodePart.bones.put(nodeId, transform);
					}
				}

				jsonNode.parts[i] = nodePart;
			}
		}

		JsonValue children = json.get("children");
		if (children != null) {
			jsonNode.children = new ModelNode[children.size];

			int i = 0;
			for (JsonValue child = children.child; child != null; child = child.next, i++) {
				jsonNode.children[i] = parseNodesRecursively(child);
			}
		}

		return jsonNode;
	}

	private void parseAnimations (ModelData model, JsonValue json) {
		JsonValue animations = json.get("animations");
		if (animations == null) return;

		model.animations.ensureCapacity(animations.size);

		for (JsonValue anim = animations.child; anim != null; anim = anim.next) {
			JsonValue nodes = anim.get("bones");
			if (nodes == null) continue;
			ModelAnimation animation = new ModelAnimation();
			model.animations.add(animation);
			animation.nodeAnimations.ensureCapacity(nodes.size);
			animation.id = anim.getString("id");
			for (JsonValue node = nodes.child; node != null; node = node.next) {
				ModelNodeAnimation nodeAnim = new ModelNodeAnimation();
				animation.nodeAnimations.add(nodeAnim);
				nodeAnim.nodeId = node.getString("boneId");

				// For backwards compatibility (version 0.1):
				JsonValue keyframes = node.get("keyframes");
				if (keyframes != null && keyframes.isArray()) {
					for (JsonValue keyframe = keyframes.child; keyframe != null; keyframe = keyframe.next) {
						final float keytime = keyframe.getFloat("keytime", 0f) / 1000.f;
						JsonValue translation = keyframe.get("translation"); 
						if (translation != null && translation.size == 3) {
							if (nodeAnim.translation == null)
								nodeAnim.translation = new Array<ModelNodeKeyframe<Vector3>>();
							ModelNodeKeyframe<Vector3> tkf = new ModelNodeKeyframe<Vector3>();
							tkf.keytime = keytime;
							tkf.value = new Vector3(translation.getFloat(0), translation.getFloat(1), translation.getFloat(2));
							nodeAnim.translation.add(tkf);
						}
						JsonValue rotation = keyframe.get("rotation");
						if (rotation != null && rotation.size == 4) {
							if (nodeAnim.rotation == null)
								nodeAnim.rotation = new Array<ModelNodeKeyframe<Quaternion>>();
							ModelNodeKeyframe<Quaternion> rkf = new ModelNodeKeyframe<Quaternion>();
							rkf.keytime = keytime;
							rkf.value = new Quaternion(rotation.getFloat(0), rotation.getFloat(1), rotation.getFloat(2), rotation.getFloat(3));
							nodeAnim.rotation.add(rkf);
						}
						JsonValue scale = keyframe.get("scale");
						if (scale != null && scale.size == 3) {
							if (nodeAnim.scaling == null)
								nodeAnim.scaling = new Array<ModelNodeKeyframe<Vector3>>();
							ModelNodeKeyframe<Vector3> skf = new ModelNodeKeyframe();
							skf.keytime = keytime;
							skf.value = new Vector3(scale.getFloat(0), scale.getFloat(1), scale.getFloat(2));
							nodeAnim.scaling.add(skf);
						}
					}
				} else { // Version 0.2:
					JsonValue translationKF = node.get("translation");
					if (translationKF != null && translationKF.isArray()) {
						nodeAnim.translation = new Array<ModelNodeKeyframe<Vector3>>();
						nodeAnim.translation.ensureCapacity(translationKF.size);
						for (JsonValue keyframe = translationKF.child; keyframe != null; keyframe = keyframe.next) {
							ModelNodeKeyframe<Vector3> kf = new ModelNodeKeyframe<Vector3>();
							nodeAnim.translation.add(kf);
							kf.keytime = keyframe.getFloat("keytime", 0f) / 1000.f;
							JsonValue translation = keyframe.get("value");
							if (translation != null && translation.size >= 3)
								kf.value = new Vector3(translation.getFloat(0), translation.getFloat(1), translation.getFloat(2));
						}
					}
					
					
					JsonValue rotationKF = node.get("rotation");
					if (rotationKF != null && rotationKF.isArray()) {
						nodeAnim.rotation = new Array<ModelNodeKeyframe<Quaternion>>();
						nodeAnim.rotation.ensureCapacity(rotationKF.size);
						for (JsonValue keyframe = rotationKF.child; keyframe != null; keyframe = keyframe.next) {
							ModelNodeKeyframe<Quaternion> kf = new ModelNodeKeyframe<Quaternion>();
							nodeAnim.rotation.add(kf);
							kf.keytime = keyframe.getFloat("keytime", 0f) / 1000.f;
							JsonValue rotation = keyframe.get("value");
							if (rotation != null && rotation.size >= 4)
								kf.value = new Quaternion(rotation.getFloat(0), rotation.getFloat(1), rotation.getFloat(2), rotation.getFloat(3));
						}
					}
					
					JsonValue scalingKF = node.get("scaling");
					if (scalingKF != null && scalingKF.isArray()) {
						nodeAnim.scaling = new Array<ModelNodeKeyframe<Vector3>>();
						nodeAnim.scaling.ensureCapacity(scalingKF.size);
						for (JsonValue keyframe = scalingKF.child; keyframe != null; keyframe = keyframe.next) {
							ModelNodeKeyframe<Vector3> kf = new ModelNodeKeyframe<Vector3>();
							nodeAnim.scaling.add(kf);
							kf.keytime = keyframe.getFloat("keytime", 0f) / 1000.f;
							JsonValue scaling = keyframe.get("value");
							if (scaling != null && scaling.size >= 3)
								kf.value = new Vector3(scaling.getFloat(0), scaling.getFloat(1), scaling.getFloat(2));
						}
					}
				}
			}
		}
	}
}

package com.badlogic.gdx.graphics.g3d.loader;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
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
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.JsonValue;

public class G3dModelLoader {
	protected ModelData parseModel (final JsonValue json, final String folder) {
		ModelData model = new ModelData();
		parseMeshes(model, json);
		parseMaterials(model, json, folder);
		parseNodes(model, json);
		parseAnimations(model, json);
		return model;
	}
	
	private void parseMeshes (ModelData model, JsonValue json) {
		JsonValue meshes = json.require("meshes");
		
		model.meshes.ensureCapacity(meshes.size());
		for(int i = 0; i < meshes.size(); i++) {
			JsonValue mesh = meshes.get(i);
			ModelMesh jsonMesh = new ModelMesh();
			
			String id = mesh.getString("id", "");
			jsonMesh.id = id;
			
			JsonValue attributes = mesh.require("attributes");
			jsonMesh.attributes = parseAttributes(attributes);
			
			JsonValue vertices = mesh.require("vertices");
			float[] verts = new float[vertices.size()];
			for(int j = 0; j < vertices.size(); j++) {
				final String s = vertices.getString(j);
				if (s.startsWith("0x"))  // FIXME just use double for packed colors
					verts[j] = Float.intBitsToFloat((int)Long.parseLong(s.substring(2), 16));
				else
					verts[j] = vertices.getFloat(j);
			}
			jsonMesh.vertices = verts;
			
			JsonValue meshParts = mesh.require("parts");
			Array<ModelMeshPart> parts = new Array<ModelMeshPart>();
			for(int j = 0; j < meshParts.size(); j++) {
				JsonValue meshPart = meshParts.get(j);
				ModelMeshPart jsonPart = new ModelMeshPart();
				String partId = meshPart.getString("id");
				if(id == null) {
					throw new GdxRuntimeException("Not id given for mesh part");
				}
				for(ModelMeshPart other: parts) {
					if(other.id.equals(partId)) {
						throw new GdxRuntimeException("Mesh part with id '" + partId + "' already in defined");
					}
				}
				jsonPart.id = partId;
				
				String type = meshPart.getString("type");
				if(type == null) {
					throw new GdxRuntimeException("No primitive type given for mesh part '" + partId + "'");
				}
				jsonPart.primitiveType = parseType(type);
				
				JsonValue indices = meshPart.require("indices");
				short[] partIndices = new short[indices.size()];
				for(int k = 0; k < indices.size(); k++) {
					partIndices[k] = (short)indices.getInt(k);
				}
				jsonPart.indices = partIndices;
				parts.add(jsonPart);
			}
			jsonMesh.parts = parts.toArray(ModelMeshPart.class);
			model.meshes.add(jsonMesh);
		}
	}
	
	private int parseType (String type) {
		if(type.equals("TRIANGLES")) {
			return GL10.GL_TRIANGLES;
		} else if(type.equals("LINES")) {
			return GL10.GL_LINES;
		} else if(type.equals("POINTS")) {
			return GL10.GL_POINTS;
		} else if(type.equals("TRIANGLE_STRIP")) {
			return GL10.GL_TRIANGLE_STRIP;
		} else if(type.equals("LINE_STRIP")) {
			return GL10.GL_LINE_STRIP;
		} else { 
			throw new GdxRuntimeException("Unknown primitive type '" + type + "', should be one of triangle, trianglestrip, line, linestrip, lineloop or point");
		}
	}

	private VertexAttribute[] parseAttributes (JsonValue attributes) {
		Array<VertexAttribute> vertexAttributes = new Array<VertexAttribute>();
		int unit = 0;
		int blendWeightCount = 0;
		for(int i = 0; i < attributes.size(); i++) {
			String attribute = attributes.getString(i);
			String attr = (String)attribute;
			if(attr.equals("POSITION")) {
				vertexAttributes.add(VertexAttribute.Position());
			} else if(attr.equals("NORMAL")) {
				vertexAttributes.add(VertexAttribute.Normal());
			} else if(attr.equals("COLOR")) {
				vertexAttributes.add(VertexAttribute.ColorUnpacked());
			} else if(attr.equals("COLORPACKED")) {
				vertexAttributes.add(VertexAttribute.Color());
			} else if(attr.equals("TANGENT")) {
				vertexAttributes.add(VertexAttribute.Tangent());
			} else if(attr.equals("BINORMAL")) {
				vertexAttributes.add(VertexAttribute.Binormal());
			} else if(attr.startsWith("TEXCOORD")) {
				vertexAttributes.add(VertexAttribute.TexCoords(unit++));
			} else if(attr.startsWith("BLENDWEIGHT")) {
				vertexAttributes.add(VertexAttribute.BoneWeight(blendWeightCount++));
			} else {
				throw new GdxRuntimeException("Unknown vertex attribute '" + attr + "', should be one of position, normal, uv, tangent or binormal");
			}
		}
		return vertexAttributes.toArray(VertexAttribute.class);
	}

	private void parseMaterials (ModelData model, JsonValue json, String materialDir) {
		JsonValue materials = json.get("materials");
		if(materials == null) {
			// we should probably create some default material in this case
		}
		else {
			model.materials.ensureCapacity(materials.size());
			
			for(int i = 0; i < materials.size(); i++) {
				JsonValue material = materials.get(i);
				ModelMaterial jsonMaterial = new ModelMaterial();
				
				String id = material.getString("id");
				if(id == null)
					throw new GdxRuntimeException("Material needs an id.");

				jsonMaterial.id = id;
							
				// Read material colors
				jsonMaterial.diffuse = parseColor(material.get("diffuse"), Color.WHITE);
				jsonMaterial.ambient = parseColor(material.get("ambient"), Color.BLACK);
				jsonMaterial.emissive = parseColor(material.get("emissive"), Color.BLACK);
				
			   // Read specular
				jsonMaterial.specular = parseColor(material.get("specular"), Color.BLACK);
				// Read shininess
				float shininess = material.getFloat("shininess", 0.0f);
				
				// Read textures
				JsonValue textures = material.get("textures");
				if(textures != null){
					for(int j = 0; j < textures.size(); j++) {
						JsonValue texture = textures.get(j);
						ModelTexture jsonTexture = new ModelTexture();
						
						String textureId = texture.getString("id");
						if(textureId == null)
							throw new GdxRuntimeException("Texture has no id.");
						jsonTexture.id = textureId;
						
						String fileName = texture.getString("filename");
						if(fileName == null)
							throw new GdxRuntimeException("Texture needs filename.");
						jsonTexture.fileName = materialDir + "/" + fileName;
						
						jsonTexture.uvTranslation = readVector2(texture.get("uvTranslation"), 0f, 0f);
						jsonTexture.uvScaling = readVector2(texture.get("uvScaling"), 1f, 1f);
						
						String textureType = texture.getString("type");
						if(textureType == null)
							throw new GdxRuntimeException("Texture needs type.");
						
						jsonTexture.usage = parseTextureUsage(textureType);
						
						if(jsonMaterial.textures == null)
							jsonMaterial.textures = new Array<ModelTexture>();
						jsonMaterial.textures.add(jsonTexture);
					}
				}

				model.materials.add(jsonMaterial);
			}
		}
	}
	
	private int parseTextureUsage(final String value) {
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
		else if (value.equalsIgnoreCase("TRANSPARENCY"))
			return ModelTexture.USAGE_TRANSPARENCY;
		return ModelTexture.USAGE_UNKNOWN;
	}

	private Color parseColor (JsonValue colorArray, Color defaultColor) {
		if(colorArray == null) {
			return defaultColor;
		}
		else if(colorArray.size() == 3)
			return new Color(colorArray.getFloat(0), colorArray.getFloat(1), colorArray.getFloat(2), 1.0f);
		else
			throw new GdxRuntimeException("Expected Color values <> than three.");
	}

	private Vector2 readVector2 (JsonValue vectorArray, float x, float y) {
		if(vectorArray == null)
			return new Vector2(x, y);
		else if(vectorArray.size() == 2)
			return new Vector2(vectorArray.getFloat(0), vectorArray.getFloat(1));
		else
			throw new GdxRuntimeException("Expected Vector2 values <> than two.");
	}

	private Array<ModelNode> parseNodes (ModelData model, JsonValue json) {
		JsonValue nodes = json.get("nodes");
		if(nodes == null) {
			throw new GdxRuntimeException("At least one node is required.");
		}
		
		model.nodes.ensureCapacity(nodes.size());
		
		for(int i = 0; i < nodes.size(); i++) {
			JsonValue node = nodes.get(i);
			model.nodes.add(parseNodesRecursively(node));
		}
		return model.nodes;
	}
	
	private ModelNode parseNodesRecursively(JsonValue json){
		ModelNode jsonNode = new ModelNode();
		
		String id = json.getString("id");
		if(id == null)
			throw new GdxRuntimeException("Node id missing.");
		jsonNode.id = id;
		
		JsonValue translation = json.get("translation");
		if (translation != null && translation.size() != 3)
			throw new GdxRuntimeException("Node translation incomplete");
		jsonNode.translation = translation == null ? null : new Vector3(translation.getFloat(0), translation.getFloat(1), translation.getFloat(2));
		
		JsonValue rotation = json.get("rotation");
		if(rotation != null && rotation.size() != 4)
			throw new GdxRuntimeException("Node rotation incomplete");
		jsonNode.rotation = rotation == null ? null : new Quaternion(rotation.getFloat(0), rotation.getFloat(1), rotation.getFloat(2), rotation.getFloat(3));
		
		JsonValue scale = json.get("scale");
		if(scale != null && scale.size() != 3)
			throw new GdxRuntimeException("Node scale incomplete");
		jsonNode.scale = scale == null ? null : new Vector3(scale.getFloat(0), scale.getFloat(1), scale.getFloat(2));
		
		String meshId = json.getString("mesh", null);
		if(meshId != null)
			jsonNode.meshId = meshId;
		
		JsonValue materials = json.get("parts");
		if(materials != null){
			jsonNode.parts = new ModelNodePart[materials.size()];
			
			for(int i = 0; i < materials.size(); i++) {
				JsonValue material = materials.get(i);
				ModelNodePart nodePart = new ModelNodePart();
				
				String meshPartId = material.getString("meshpartid");
				String materialId = material.getString("materialid");
				if(meshPartId == null || materialId == null){
					throw new GdxRuntimeException("Node "+id+" part is missing meshPartId or materialId");
				}
				nodePart.materialId = materialId;
				nodePart.meshPartId = meshPartId;
				
				JsonValue bones = material.get("bones");
				if (bones != null) {
					nodePart.bones = new String[bones.size()];
					for (int j = 0; j < bones.size(); j++)
						nodePart.bones[j] = bones.getString(j); 
				}
				
				jsonNode.parts[i] = nodePart;
			}
		}
		
		JsonValue children = json.get("children");
		if(children != null){
			jsonNode.children = new ModelNode[children.size()];

			for(int i = 0; i < children.size(); i++) {
				JsonValue child = children.get(i);
				jsonNode.children[i] = parseNodesRecursively(child);
			}
		}
		
		return jsonNode;
	}
	
	private void parseAnimations (ModelData model, JsonValue json) {
		JsonValue animations = json.get("animations");
		if(animations == null)
			return;
		
		model.animations.ensureCapacity(animations.size());
		
		for(int i = 0; i < animations.size(); i++) {
			JsonValue anim = animations.get(i);
			JsonValue nodes = anim.get("bones");
			if (nodes == null)
				continue;
			ModelAnimation animation = new ModelAnimation();
			model.animations.add(animation);
			animation.nodeAnimations.ensureCapacity(nodes.size());
			animation.id = anim.getString("id");
			for(int j = 0; j < nodes.size(); j++) {
				JsonValue node = nodes.get(j);
				JsonValue keyframes = node.get("keyframes");

				ModelNodeAnimation nodeAnim = new ModelNodeAnimation();
				animation.nodeAnimations.add(nodeAnim);
				nodeAnim.nodeId = node.getString("boneId");
				nodeAnim.keyframes.ensureCapacity(keyframes.size());
				
				for(int k = 0; k < keyframes.size(); k++) {
					JsonValue keyframe = keyframes.get(k);
					ModelNodeKeyframe kf = new ModelNodeKeyframe();
					nodeAnim.keyframes.add(kf);
					kf.keytime = keyframe.getFloat("keytime") / 1000.f;
					JsonValue translation = keyframe.get("translation");
					if (translation != null && translation.size() == 3)
						kf.translation = new Vector3(translation.getFloat(0), translation.getFloat(1), translation.getFloat(2));
					JsonValue rotation = keyframe.get("rotation");
					if (rotation != null && rotation.size() == 4)
						kf.rotation = new Quaternion(rotation.getFloat(0), rotation.getFloat(1), rotation.getFloat(2), rotation.getFloat(3));
					JsonValue scale = keyframe.get("scale");
					if (scale != null && scale.size() == 3)
						kf.scale = new Vector3(scale.getFloat(0), scale.getFloat(1), scale.getFloat(2));
				}
			}
		}
	}
}

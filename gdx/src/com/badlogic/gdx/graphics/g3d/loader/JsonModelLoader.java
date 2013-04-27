package com.badlogic.gdx.graphics.g3d.loader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.model.data.ModelData;
import com.badlogic.gdx.graphics.g3d.model.data.ModelMaterial;
import com.badlogic.gdx.graphics.g3d.model.data.ModelMesh;
import com.badlogic.gdx.graphics.g3d.model.data.ModelMeshPart;
import com.badlogic.gdx.graphics.g3d.model.data.ModelNodePart;
import com.badlogic.gdx.graphics.g3d.model.data.ModelNode;
import com.badlogic.gdx.graphics.g3d.model.data.ModelTexture;
import com.badlogic.gdx.graphics.g3d.model.data.ModelMaterial.MaterialType;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.OrderedMap;

/**
 * Loads the JSON format written by the 
 * <a href="https://github.com/libgdx/fbx-conv">fbx-conv</a> tool.
 * 
 * @author mzechner
 *
 */
public class JsonModelLoader {
	public static String VERSION = "1.0";
	
	public Model load (FileHandle handle) {
		ModelData jsonModel = parseModel(handle);
		return new Model(jsonModel);
	}

	public ModelData parseModel (FileHandle handle) {
		JsonReader reader = new JsonReader();
		OrderedMap<String, Object> json = (OrderedMap<String, Object>)reader.parse(handle);
		
		String version = (String)json.get("version");
		if(version == null || !version.equals(VERSION)) {
			throw new GdxRuntimeException("No or wrong JSON format version given, should be " + VERSION + ", is " + version);
		}
		
		ModelData model = new ModelData();
		parseMeshes(model, json);
		parseMaterials(model, json, handle.parent().path());
		parseNodes(model, json);
		return model;
	}
	
	private void parseMeshes (ModelData model, OrderedMap<String, Object> json) {
		Array<OrderedMap<String, Object>> meshes = (Array<OrderedMap<String, Object>>)json.get("meshes");
		if(meshes == null) {
			throw new GdxRuntimeException("No meshes found in file");
		}
		
		model.meshes.ensureCapacity(meshes.size);
		int i = 0;
		for(OrderedMap<String, Object> mesh: meshes) {
			ModelMesh jsonMesh = new ModelMesh();
			
			String id = (String)mesh.get("id", "");

			jsonMesh.id = id;
			
			Array<Object> attributes = (Array<Object>)mesh.get("attributes");
			if(attributes == null) {
				throw new GdxRuntimeException("No vertex attributes given for mesh '" + id + "'");
			}
			jsonMesh.attributes = parseAttributes(attributes);
			
			Array<Object> vertices = (Array<Object>)mesh.get("vertices");
			if(vertices == null) {
				throw new GdxRuntimeException("No vertices given for mesh '" + id + "'");
			}
			float[] verts = new float[vertices.size];
			int idx = 0;
			for(Object v: vertices) {
				verts[idx++] = (Float)v;
			}
			jsonMesh.vertices = verts;
			
			Array<OrderedMap<String, Object>> meshParts = (Array<OrderedMap<String, Object>>)mesh.get("parts");
			if(meshParts == null) {
				throw new GdxRuntimeException("No mesh parts given for mesh '" + id + "'");
			}
			Array<ModelMeshPart> parts = new Array<ModelMeshPart>();
			for(OrderedMap<String, Object> meshPart: meshParts) {
				ModelMeshPart jsonPart = new ModelMeshPart();
				String partId = (String)meshPart.get("id");
				if(id == null) {
					throw new GdxRuntimeException("Not id given for mesh part");
				}
				for(ModelMeshPart other: parts) {
					if(other.id.equals(partId)) {
						throw new GdxRuntimeException("Mesh part with id '" + partId + "' already in defined");
					}
				}
				jsonPart.id = partId;
				
				String type = (String)meshPart.get("type");
				if(type == null) {
					throw new GdxRuntimeException("No primitive type given for mesh part '" + partId + "'");
				}
				jsonPart.primitiveType = parseType(type);
				
				Array<Object> indices = (Array<Object>)meshPart.get("indices");
				if(indices == null) {
					throw new GdxRuntimeException("No indices given for mesh part '" + partId + "'");
				}
				short[] partIndices = new short[indices.size];
				idx = 0;
				for(Object index: indices) {
					partIndices[idx++] = (short)(float)(Float)index;
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
		} /* Gameplay encoder doesn't read out line loop
			else if(type.equals("lineloop")) {
			return GL10.GL_LINE_LOOP; 
		} */
			else { 
			throw new GdxRuntimeException("Unknown primitive type '" + type + "', should be one of triangle, trianglestrip, line, linestrip, lineloop or point");
		}
	}

	private VertexAttribute[] parseAttributes (Array<Object> attributes) {
		Array<VertexAttribute> vertexAttributes = new Array<VertexAttribute>();
		int unit = 0;
		for(Object attribute: attributes) {
			String attr = (String)attribute;
			if(attr.equals("POSITION")) {
				vertexAttributes.add(VertexAttribute.Position());
			} else if(attr.equals("NORMAL")) {
				vertexAttributes.add(VertexAttribute.Normal());
			} else if(attr.startsWith("TEXCOORD")) {
				vertexAttributes.add(VertexAttribute.TexCoords(unit++));
			} else if(attr.equals("TANGENT")) {
				vertexAttributes.add(VertexAttribute.Tangent());
			} else if(attr.equals("BINORMAL")) {
				vertexAttributes.add(VertexAttribute.Binormal());
			} else if(attr.equals("BLENDINDICES")) {
				vertexAttributes.add(VertexAttribute.BoneIds(4));
			} else if(attr.equals("BLENDWEIGHTS")) {
				vertexAttributes.add(VertexAttribute.BoneWeights(4));
			} else if(attr.equals("COLOR")) {
				vertexAttributes.add(VertexAttribute.Color());
			} else {
				throw new GdxRuntimeException("Unknown vertex attribuet '" + attr + "', should be one of position, normal, uv, tangent or binormal");
			}
		}
		return vertexAttributes.toArray(VertexAttribute.class);
	}

	private void parseMaterials (ModelData model, OrderedMap<String, Object> json, String materialDir) {
		Array<OrderedMap<String, Object>> materials = (Array<OrderedMap<String, Object>>)json.get("materials");
		if(materials == null) {
			// we should probably create some default material in this case
		}
		else {
			model.materials.ensureCapacity(materials.size);
			
			int i = 0;
			for(OrderedMap<String, Object> material: materials) {
				ModelMaterial jsonMaterial = new ModelMaterial();
				
				String id = (String)material.get("id");
				if(id == null)
					throw new GdxRuntimeException("Material needs an id.");

				jsonMaterial.id = id;
							
				// Read material colors
				jsonMaterial.diffuse = parseColor((Array<Object>)material.get("diffuse"), Color.WHITE);
				jsonMaterial.ambient = parseColor((Array<Object>)material.get("ambient"), Color.BLACK);
				jsonMaterial.emissive = parseColor((Array<Object>)material.get("emissive"), Color.WHITE);
				
				if(jsonMaterial.type == MaterialType.Phong){
				   // Read specular
					jsonMaterial.specular = parseColor((Array<Object>)material.get("specular"), Color.WHITE);
					
					// Read shininess
					float shininess = (Float)material.get("shininess", 1.0f);
				}
				
				// Read textures
				Array<OrderedMap<String, Object>> textures = (Array<OrderedMap<String, Object>>)material.get("textures");
				if(textures != null){
					for(OrderedMap<String, Object> texture : textures) {
						ModelTexture jsonTexture = new ModelTexture();
						
						String textureId = (String)texture.get("id");
						if(textureId == null)
							throw new GdxRuntimeException("Texture has no id.");
						jsonTexture.id = textureId;
						
						String fileName = (String)texture.get("filename");
						if(fileName == null)
							throw new GdxRuntimeException("Texture needs filename.");
						jsonTexture.fileName = materialDir + "/" + fileName;
						
						jsonTexture.uvTranslation = readVector2((Array<Object>)texture.get("uvTranslation"), 0f, 0f);
						jsonTexture.uvScaling = readVector2((Array<Object>)texture.get("uvScaling"), 1f, 1f);
						
						String textureType = (String)texture.get("type");
						if(textureType == null)
							throw new GdxRuntimeException("Texture needs type.");
						
						/* Only diffuse textures for now. Most programs don't export texture usage properly ..
						 	So we probably need to find a workaround. */
						if(textureType.equals("STANDARD")){
							if(jsonMaterial.diffuseTextures == null)
								jsonMaterial.diffuseTextures = new Array<ModelTexture>();
							jsonMaterial.diffuseTextures.add(jsonTexture);
						}
					}
				}

				model.materials.add(jsonMaterial);
			}
		}
	}

	private Color parseColor (Array<Object> colorArray, Color defaultColor) {
		if(colorArray == null) {
			return defaultColor;
		}
		else if(colorArray.size == 3)
			return new Color((Float)colorArray.get(0), (Float)colorArray.get(1), (Float)colorArray.get(2), 1.0f);
		else
			throw new GdxRuntimeException("Expected Color values <> than three.");
	}

	private Vector2 readVector2 (Array<Object> vectorArray, float x, float y) {
		if(vectorArray == null)
			return new Vector2(x, y);
		else if(vectorArray.size == 2)
			return new Vector2((Float)vectorArray.get(0), (Float)vectorArray.get(1));
		else
			throw new GdxRuntimeException("Expected Vector2 values <> than two.");
	}

	private Array<ModelNode> parseNodes (ModelData model, OrderedMap<String, Object> json) {
		Array<OrderedMap<String, Object>> nodes = (Array<OrderedMap<String, Object>>)json.get("nodes");
		if(nodes == null) {
			throw new GdxRuntimeException("At least one node is required.");
		}
		
		model.nodes.ensureCapacity(nodes.size);
		
		int i = 0;
		for(OrderedMap<String, Object> node : nodes) {
			model.nodes.add(parseNodesRecursively(node));
		}
		return model.nodes;
	}
	
	private ModelNode parseNodesRecursively(OrderedMap<String, Object> json){
		ModelNode jsonNode = new ModelNode();
		
		String id = (String)json.get("id");
		if(id == null)
			throw new GdxRuntimeException("Node id missing.");
		jsonNode.id = id;
		
		Array<Object> translation = (Array<Object>)json.get("translation");
		if (translation != null && translation.size != 3)
			throw new GdxRuntimeException("Node translation incomplete");
		jsonNode.translation = translation == null ? null : new Vector3((Float)translation.get(0), (Float)translation.get(1), (Float)translation.get(2));
		
		Array<Object> rotation = (Array<Object>)json.get("rotation");
		if(rotation != null && rotation.size != 4)
			throw new GdxRuntimeException("Node rotation incomplete");
		jsonNode.rotation = rotation == null ? null : new Quaternion((Float)rotation.get(0), (Float)rotation.get(1), (Float)rotation.get(2), (Float)rotation.get(3));
		
		Array<Object> scale = (Array<Object>)json.get("scale");
		if(scale != null && scale.size != 3)
			throw new GdxRuntimeException("Node scale incomplete");
		jsonNode.scale = scale == null ? null : new Vector3((Float)scale.get(0), (Float)scale.get(1), (Float)scale.get(2));
		
		String meshId = (String)json.get("mesh");
		if(meshId != null)
			jsonNode.meshId = meshId;
		
		Array<OrderedMap<String, Object>> materials = (Array<OrderedMap<String, Object>>)json.get("parts");
		if(materials != null){
			jsonNode.parts = new ModelNodePart[materials.size];
			
			int i = 0;
			for(OrderedMap<String, Object> material : materials) {
				ModelNodePart meshPartMaterial = new ModelNodePart();
				
				String meshPartId = (String)material.get("meshpartid");
				String materialId = (String)material.get("materialid");
				if(meshPartId == null || materialId == null){
					throw new GdxRuntimeException("Node "+id+" part is missing meshPartId or materialId");
				}
				meshPartMaterial.materialId = materialId;
				meshPartMaterial.meshPartId = meshPartId;
				
				jsonNode.parts[i++] = meshPartMaterial;
			}
		}
		
		Array<OrderedMap<String, Object>> children = (Array<OrderedMap<String, Object>>)json.get("children");
		if(children != null){
			jsonNode.children = new ModelNode[children.size];
			
			int i = 0;
			for(OrderedMap<String, Object> child : children) {
				jsonNode.children[i++] = parseNodesRecursively(child);
			}
		}
		
		return jsonNode;
	}
}

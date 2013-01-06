package com.badlogic.gdx.graphics.g3d.loaders.json;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.g3d.ModelLoaderHints;
import com.badlogic.gdx.graphics.g3d.loaders.ModelLoader;
import com.badlogic.gdx.graphics.g3d.loaders.json.JsonMaterial.MaterialType;
import com.badlogic.gdx.graphics.g3d.materials.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.materials.Material;
import com.badlogic.gdx.graphics.g3d.materials.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.model.Model;
import com.badlogic.gdx.graphics.g3d.model.SubMesh;
import com.badlogic.gdx.graphics.g3d.model.skeleton.SkeletonModel;
import com.badlogic.gdx.graphics.g3d.model.still.StillModel;
import com.badlogic.gdx.graphics.g3d.model.still.StillSubMesh;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.OrderedMap;

/**
 * {@link ModelLoader} for the JSON format written by the 
 * <a href="https://github.com/libgdx/fbx-conv">fbx-conv</a> tool.
 * 
 * @author mzechner
 *
 */
public class JsonModelLoader implements ModelLoader {
	public static String VERSION = "1.0";
	
	@Override
	public Model load (FileHandle handle, ModelLoaderHints hints) {
		JsonModel jsonModel = parseModel(handle, hints);
		Model model = null;
		
		if(jsonModel.animations == null)
			model = createStillModel(jsonModel);
		else // add hints for sampling to keyframed model
			model = createSkeletonModel(jsonModel);
		
		return model;
	}

	private SkeletonModel createSkeletonModel (JsonModel jsonModel) {
		// TODO Auto-generated method stub
		return null;
	}

	private StillModel createStillModel (JsonModel model) {
		StillModel stillModel = new StillModel(new SubMesh[model.meshes.length]);
		
		// We create the materials first
		ObjectMap<String, Material> materials = new ObjectMap<String, Material>();
		for(int i=0; i<model.materials.length; i++){
			JsonMaterial jsonMaterial = model.materials[i];
			Material material = new Material(jsonMaterial.id);
			
			// simple loader for now. Just diffuse & textures
			material.addAttribute(new ColorAttribute(jsonMaterial.diffuse, "diffuse"));

			if(jsonMaterial.diffuseTextures.size > 0){
				JsonTexture jsonTexture = jsonMaterial.diffuseTextures.get(0);
				
				// one texture unit for now
				Texture texture = new Texture(Gdx.files.internal(jsonTexture.fileName));
				material.addAttribute(new TextureAttribute(texture, 0, "diffuseTexture"));
			}
			
			materials.put(jsonMaterial.id, material);
		}
		
		// Create the meshes and assign materials to them. This is a super hack until we have hierarchy
		for(int i=0; i<model.meshes.length; i++){
			JsonMesh jsonMesh = model.meshes[i];
			// if we have more than one submesh we're screwed for now.
			JsonMeshPart jsonMeshPart = model.meshes[i].parts[0];
			
			Mesh mesh = new Mesh(false, jsonMesh.vertices.length, jsonMeshPart.indices.length, jsonMesh.attributes);
			mesh.setIndices(jsonMeshPart.indices);
			mesh.setVertices(jsonMesh.vertices);
			
			StillSubMesh subMesh = new StillSubMesh(jsonMesh.id, mesh, jsonMeshPart.primitiveType);
			// Just assumes first material. We need the node tree to work this properly
			subMesh.material = materials.get(model.materials[0].id);
			stillModel.subMeshes[i] = subMesh;
		}
		
		return stillModel;
	}

	public JsonModel parseModel (FileHandle handle, ModelLoaderHints hints) {
		JsonReader reader = new JsonReader();
		OrderedMap<String, Object> json = (OrderedMap<String, Object>)reader.parse(handle);
		
		String version = (String)json.get("version");
		if(version == null || !version.equals(VERSION)) {
			throw new GdxRuntimeException("No or wrong JSON format version given, should be " + VERSION + ", is " + version);
		}
		
		JsonModel model = new JsonModel();
		parseMeshes(model, json, hints);
		parseMaterials(model, json, hints, handle.parent().path());
		parseNodes(model, json, hints);
		return model;
	}
	
	private JsonMesh[] parseMeshes (JsonModel model, OrderedMap<String, Object> json, ModelLoaderHints hints) {
		Array<OrderedMap<String, Object>> meshes = (Array<OrderedMap<String, Object>>)json.get("meshes");
		if(meshes == null) {
			throw new GdxRuntimeException("No meshes found in file");
		}
		
		model.meshes = new JsonMesh[meshes.size];
		int i = 0;
		for(OrderedMap<String, Object> mesh: meshes) {
			JsonMesh jsonMesh = new JsonMesh();
			String id = (String)mesh.get("id");
			if(id == null) {
				throw new GdxRuntimeException("No id given for mesh");
			}
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
			Array<JsonMeshPart> parts = new Array<JsonMeshPart>();
			for(OrderedMap<String, Object> meshPart: meshParts) {
				JsonMeshPart jsonPart = new JsonMeshPart();
				String partId = (String)meshPart.get("id");
				if(id == null) {
					throw new GdxRuntimeException("Not id given for mesh part");
				}
				for(JsonMeshPart other: parts) {
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
			jsonMesh.parts = parts.toArray(JsonMeshPart.class);
			model.meshes[i++] = jsonMesh;
		}
		return model.meshes;
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

	private JsonMaterial[] parseMaterials (JsonModel model, OrderedMap<String, Object> json, ModelLoaderHints hints, String materialDir) {
		Array<OrderedMap<String, Object>> materials = (Array<OrderedMap<String, Object>>)json.get("materials");
		if(materials == null) {
			// we should probably create some default material in this case
		}
		else {
			model.materials = new JsonMaterial[materials.size];
			
			int i = 0;
			for(OrderedMap<String, Object> material: materials) {
				JsonMaterial jsonMaterial = new JsonMaterial();
				
				String id = (String)material.get("id");
				if(id == null)
					throw new GdxRuntimeException("Material needs an id.");
				
				jsonMaterial.id = id;
				
				// Read type
				String type = (String)material.get("type");
				if(type == null)
					throw new GdxRuntimeException("Material needs a type. Lambert|Phong");
				
				jsonMaterial.type = type.equals("PHONG") ? MaterialType.Phong : MaterialType.Lambert;
				
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
				for(OrderedMap<String, Object> texture : textures) {
					JsonTexture jsonTexture = new JsonTexture();
					
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
					if(type == null)
						throw new GdxRuntimeException("Texture needs type.");
					
					/* Only diffuse textures for now. Most programs don't export texture usage properly ..
					 	So we probably need to find a workaround. */
					if(textureType.equals("STANDARD")){
						if(jsonMaterial.diffuseTextures == null)
							jsonMaterial.diffuseTextures = new Array<JsonTexture>();
						jsonMaterial.diffuseTextures.add(jsonTexture);
					}
				}
				model.materials[i++] = jsonMaterial;
			}
		}
		
		return null;
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

	private JsonNode[] parseNodes (JsonModel model, OrderedMap<String, Object> json, ModelLoaderHints hints) {
		
		return null;
	}
}

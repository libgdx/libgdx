package com.badlogic.gdx.graphics.g3d.loaders.json;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.g3d.ModelLoaderHints;
import com.badlogic.gdx.graphics.g3d.loaders.ModelLoader;
import com.badlogic.gdx.graphics.g3d.loaders.StillModelLoader;
import com.badlogic.gdx.graphics.g3d.model.still.StillModel;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.OrderedMap;

/**
 * {@link ModelLoader} for the JSON format written by the 
 * <a href="https://github.com/libgdx/fbx-conv">fbx-conv</a> tool.
 * 
 * @author mzechner
 *
 */
public class JsonModelLoader implements StillModelLoader {
	public static String VERSION = "1.0";
	
	@Override
	public StillModel load (FileHandle handle, ModelLoaderHints hints) {
		JsonModel model = parseModel(handle, hints);
		
		return null;
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
		parseMaterials(model, json, hints);
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

	private JsonMaterial[] parseMaterials (JsonModel model, OrderedMap<String, Object> json, ModelLoaderHints hints) {
		
		return null;
	}

	private JsonNode[] parseNodes (JsonModel model, OrderedMap<String, Object> json, ModelLoaderHints hints) {
		return null;
	}
}

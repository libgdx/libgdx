package com.badlogic.gdx.graphics.g3d.loaders.collada;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Xml.Element;

public class Faces {
	protected static final String VERTEX = "VERTEX";
	protected static final String TEXCOORD = "TEXCOORD";
	protected static final String NORMAL = "NORMAL";
	protected static final String TANGENT = "TEXTANGENT";
	protected static final String BITANGENT = "TEXBINORMAL";	
	
	int count = 0;
	final Map<String, Source> sourcesMap;
	final Map<String, String> mappings;
	Array<Element> inputs;
	Source[] sources;
	Array<VertexIndices> triangles;
	Array<VertexIndices> vertices;
	int numVertices = 0;
	int numIndices = 0;
	int primitiveType = GL10.GL_TRIANGLES;
	
	public Faces (Element faces, Map<String, String> mappings, Map<String, Source> sources) {
		this.sourcesMap = sources;
		this.mappings = mappings;
		parseVertices(faces);
		triangulate(faces);
		this.numIndices = triangles.size;			
	}
	
	/**
	 * Conditions the inputs for a triangles/polylist list of faces and
	 * generates unique vertices while merging duplicate vertices.
	 * @param faces
	 */
	private void parseVertices(Element faces) {
		inputs = faces.getChildrenByName("input");
		if(inputs == null) throw new GdxRuntimeException("no <input> elements in <triangles>/<polylist>");		
		
		int[] offsets = new int[inputs.size];
		int stride = 0;		
		
		// normalize source references, should use the URI/address scheme of Collada FIXME
		// calculate stride and prepare to untangle the index lists mess...
		sources = new Source[inputs.size];
		for(int i = 0; i < inputs.size; i++) {
			Element input = inputs.get(i);
			
			// map source if it was defined in <vertices> tag
			String source = input.getAttribute("source").substring(1);
			if(mappings.containsKey(source)) {
				input.setAttribute("source", mappings.get(source));
			} else {
				input.setAttribute("source", source);
			}
			
			// check whether source exists
			source = input.getAttribute("source");
			if(!sourcesMap.containsKey(source)) throw new GdxRuntimeException("source '" + source + "'  not in mesh> but in <triangle>");
			sources[i] = sourcesMap.get(source);
			offsets[i] = Integer.parseInt(input.getAttribute("offset"));
			stride = Math.max(offsets[i], stride);			
		}
		
		// addjust for zero source offsets
		stride += 1;
		
		// parse <p> indices, yeah, that takes up a bit more memory.
		String[] tokens = faces.getChildByName("p").getText().split("\\s+");
		int[] indices = new int[tokens.length];
		for(int i = 0; i < tokens.length; i++) {
			indices[i] = Integer.parseInt(tokens[i]);
		}
		
		// untangle indices on a per source basis
		Map<VertexIndices, VertexIndices> indicesSet = new HashMap<VertexIndices, VertexIndices>();
		VertexIndices vertex = new VertexIndices(inputs.size);
		triangles = new Array<VertexIndices>(indices.length / stride);
		vertices = new Array<VertexIndices>(indices.length / stride);
		int index = 0;
		for(int i = 0; i < indices.length; i+= stride) {			
			for(int j = 0; j < inputs.size; j++) {
				vertex.indices[j] = indices[i+offsets[j]];
				vertex.index = index;
			}
			
			VertexIndices lookup = indicesSet.get(vertex);
			if(lookup != null) {
				triangles.add(lookup);
			} else {
				triangles.add(vertex);
				vertices.add(vertex);
				indicesSet.put(vertex, vertex);				
				vertex = new VertexIndices(inputs.size);
				index++;
			}				
		}					
		numVertices = index;
	}	
	
	/**
	 * This method triangulates the faces if they are given as a polylist. Does
	 * nothing in case the faces are given as triangles already.
	 *  
	 * @param polyList
	 */
	private void triangulate(Element polyList) {		
		if(!polyList.getName().equals("polylist")) return;
		
		Element colladaPolys = polyList.getChildByName("vcount");
		if(colladaPolys == null) throw new GdxRuntimeException("<polylist> does not contain <vcount> element");
		
		String[] tokens = colladaPolys.getText().split("\\s+");
		int[] polys = new int[tokens.length];
		int vertexCount = 0;
		for(int i = 0;i < tokens.length; i++) {
			int verts = Integer.parseInt(tokens[i]);
			polys[i] = verts;
			vertexCount += verts; 
		}
		
		Array<VertexIndices> newVertices = new Array<VertexIndices>(vertexCount);
		int idx = 0;
		for(int i = 0; i < polys.length; i++) {
			int numVertices = polys[i];
			VertexIndices baseVertex = triangles.get(idx++);
			for(int j = 1; j < numVertices-1; j++) {
				newVertices.add(baseVertex);
				newVertices.add(triangles.get(idx));
				newVertices.add(triangles.get(idx+1));
				idx++;
			}
			idx++;
		}
		triangles = newVertices;
	}
	
	public Mesh getMesh () {		
		float[] verts = new float[getVertexSize() * numVertices];
		short[] indices = new short[numIndices];
		VertexAttribute[] attributes = getVertexAttributes();
		
		for(int i = 0; i < numIndices; i++) {
			VertexIndices vertex = triangles.get(i);
			if(vertex.index > Short.MAX_VALUE ||
				vertex.index < Short.MIN_VALUE) throw new GdxRuntimeException("index to big for short: " + vertex.index);
			indices[i] = (short)vertex.index;
		}			
		
		int idx = 0;				
		int destOffset = 0;
		
		for(int i = 0; i < vertices.size; i++) {	
			VertexIndices vertex = vertices.get(i);
			
			for(int j = 0; j < sources.length; j++) {
				Source source = sources[j];
				float[] data = source.data;
				int index = vertex.indices[j];
				int components = source.components;			
				int sourceOffset = index * components;
				
				for(int k = 0; k < components; k++) {
					if((attributes[j].usage == Usage.TextureCoordinates) && k == 1) {
						verts[destOffset++] = 1- data[sourceOffset++];
					} else {
						verts[destOffset++] = data[sourceOffset++];
					}
				}			
			}			
		}
		
		Mesh mesh = new Mesh(true, vertices.size, indices.length, attributes);
		mesh.setVertices(verts);
		mesh.setIndices(indices);
		return mesh;
	}
	
	private VertexAttribute[] getVertexAttributes() {
		VertexAttribute[] attributes = new VertexAttribute[inputs.size];
				
		int texUnit = 0;
		for(int i = 0; i < inputs.size; i++) {
			Element input = inputs.get(i);
			String semantic = input.getAttribute("semantic");
			Source source = sourcesMap.get(input.getAttribute("source"));
			
			int usage = getVertexAttributeUsage(semantic);
			int components = source.components;
			String alias = getVertexAttributeAlias(semantic);
			if(alias.equals("a_tex")) alias += texUnit++;						
			attributes[i] = new VertexAttribute(usage, components, alias);
		}
		return attributes;
	}	

	private int getVertexSize() {
		int size = 0;
		for(int i = 0; i < inputs.size; i++) {
			size += sourcesMap.get(inputs.get(i).getAttribute("source")).components;
		}
		return size;
	}
	
	private int getVertexAttributeUsage(String attribute) {
		if(attribute.equals(VERTEX)) return Usage.Position;
		if(attribute.equals(TEXCOORD)) return Usage.TextureCoordinates;
		if(attribute.equals(NORMAL)) return Usage.Normal;
		return Usage.Generic;
	}	
	
	private String getVertexAttributeAlias (String attribute) {
		if(attribute.equals(VERTEX)) return "a_pos";
		if(attribute.equals(TEXCOORD)) return "a_tex";
		if(attribute.equals(NORMAL)) return "a_nor";
		if(attribute.equals(TANGENT)) return "a_tan";
		if(attribute.equals(BITANGENT)) return "a_bin";
		throw new GdxRuntimeException("can't map semantic '" + attribute + "' to alias, must be VERTEX, TEXCOORD, NORMAL, TANGENT or BITANGENT");
	}
	
	/**
	 * Helper class that stores a vertex in form of
	 * indices into sources. 
	 * 
	 * @author mzechner
	 *
	 */
	static class VertexIndices {
		int[] indices;
		int index;
		
		public VertexIndices(int size) {
			this.indices = new int[size];
		}
		@Override public int hashCode () {
			final int prime = 31;
			int result = 1;
			result = prime * result + Arrays.hashCode(indices);
			return result;
		}
		@Override public boolean equals (Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			VertexIndices other = (VertexIndices)obj;
			if (!Arrays.equals(indices, other.indices)) return false;
			return true;
		}			
		
		@Override
		public String toString() {
			return index + ": " + Arrays.toString(indices);
		}
	}
}
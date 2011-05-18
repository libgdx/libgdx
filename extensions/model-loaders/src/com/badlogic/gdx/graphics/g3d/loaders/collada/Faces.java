package com.badlogic.gdx.graphics.g3d.loaders.collada;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.Xml.Element;

public class Faces {
	protected static final String VERTEX = "VERTEX";
	protected static final String TEXCOORD = "TEXCOORD";
	protected static final String NORMAL = "NORMAL";
	protected static final String TANGENT = "TEXTANGENT";
	protected static final String BITANGENT = "TEXBINORMAL";	
	
	int count = 0;
	final Map<String, Source> sources;
	final Map<String, String> mappings;
	Array<Element> inputs;
	Array<VertexIndices> vertices;
	int uniqueVertices = 0;
	int primitiveType = GL10.GL_TRIANGLES;
	
	public Faces (Element faces, Map<String, String> mappings, Map<String, Source> sources) {
		this.sources = sources;
		this.mappings = mappings;
		parseVertices(faces);
		triangulate(faces);
		for(VertexIndices v: vertices) {
			System.out.println(v);
		}
		System.out.println("vertices: " + uniqueVertices);
		System.out.println("indices: " + vertices.size);
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
			if(!sources.containsKey(source)) throw new GdxRuntimeException("source '" + source + "'  not in mesh> but in <triangle>");
						
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
		vertices = new Array<VertexIndices>(indices.length / stride);
		int index = 0;
		for(int i = 0; i < indices.length; i+= stride) {			
			for(int j = 0; j < inputs.size; j++) {
				vertex.indices[j] = indices[i+offsets[j]];
				vertex.index = index;
			}
			
			VertexIndices lookup = indicesSet.get(vertex);
			if(lookup != null) {
				vertices.add(lookup);
			} else {
				vertices.add(vertex);
				indicesSet.put(vertex, vertex);				
				vertex = new VertexIndices(inputs.size);
				index++;
			}				
		}					
		uniqueVertices = index;
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
		for(int i = 0; i < tokens.length; i++) {
			polys[i] = Integer.parseInt(tokens[i]);
		}
		
		Array<VertexIndices> newVertices = new Array<VertexIndices>();
		int idx = 0;
		for(int i = 0; i < polys.length; i++) {
			int numVertices = polys[i];
			VertexIndices baseVertex = vertices.get(idx++);
			for(int j = 1; j < numVertices-1; j++) {
				newVertices.add(baseVertex);
				newVertices.add(vertices.get(idx));
				newVertices.add(vertices.get(idx+1));
				idx++;
			}
		}
		vertices = newVertices;
	}
	
	public Mesh getMesh () {
		return null;
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
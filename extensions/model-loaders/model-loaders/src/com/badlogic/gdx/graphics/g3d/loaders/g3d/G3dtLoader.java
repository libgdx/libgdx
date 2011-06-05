package com.badlogic.gdx.graphics.g3d.loaders.g3d;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.model.keyframe.Keyframe;
import com.badlogic.gdx.graphics.g3d.model.keyframe.KeyframedAnimation;
import com.badlogic.gdx.graphics.g3d.model.keyframe.KeyframedModel;
import com.badlogic.gdx.graphics.g3d.model.keyframe.KeyframedSubMesh;
import com.badlogic.gdx.graphics.g3d.model.still.StillModel;
import com.badlogic.gdx.graphics.g3d.model.still.StillSubMesh;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.ObjectMap;

/**
 * Class to import the G3D text format.
 * @author mzechner
 *
 */
public class G3dtLoader {
	public static KeyframedModel loadKeyframedModel(FileHandle handle, boolean flipV) {
		return loadKeyframedModel(handle.read(), flipV);
	}
	
	public static StillModel loadStillModel(FileHandle handle, boolean flipV) {
		return loadStillModel(handle.read(), flipV);
	}
	
	static int lineNum = 0;
	static String line = null;
	
	public static StillModel loadStillModel(InputStream stream, boolean flipV) {
		BufferedReader in = new BufferedReader(new InputStreamReader(stream));
		
		lineNum = 1;
		try {
			String version = readString(in);
			if(!version.equals("g3dt-still-1.0")) throw new GdxRuntimeException("incorrect version");
			int numMeshes = readInt(in);
			StillSubMesh[] subMeshes = new StillSubMesh[numMeshes];
			for(int i = 0; i < numMeshes; i++) {
				subMeshes[i] = readStillSubMesh(in, flipV);
			}
			StillModel model = new StillModel(subMeshes);						
			return model;
		} catch(Throwable e) {
			throw new GdxRuntimeException("Couldn't read keyframed model, error in line " + lineNum + ", '" + line + "' : " + e.getMessage(), e);
		}
	}
	
	private static StillSubMesh readStillSubMesh(BufferedReader in, boolean flipV) throws IOException {
		String name = readString(in);
		IntArray indices = readFaces(in);
		int numVertices = readInt(in);
		int numAttributes = readInt(in);		
		
		if(!readString(in).equals("position")) throw new GdxRuntimeException("first attribute must be position.");
		int numUvs = 0;
		boolean hasNormals = false;
		for(int i = 1; i < numAttributes; i++) {
			String attributeType = readString(in);
			
			if(!attributeType.equals("normal") && !attributeType.equals("uv")) throw new GdxRuntimeException("attribute name must be normal or uv");
			
			if(attributeType.equals("normal")) {
				if(i != 1) throw new GdxRuntimeException("attribute normal must be second attribute");
				hasNormals = true;
			}
			if(attributeType.equals("uv")) {
				numUvs++;
			}
		}				
		VertexAttribute[] vertexAttributes = createVertexAttributes(hasNormals, numUvs);		
		int vertexSize = new VertexAttributes(vertexAttributes).vertexSize / 4;
		float[] vertices = new float[numVertices * vertexSize];
		int idx = 0;
		int uvOffset = hasNormals?6:3;
		for(int i = 0; i < numVertices; i++) {
			readFloatArray(in, vertices, idx);
			if(flipV) {
				for(int j = idx + uvOffset + 1; j < idx + uvOffset + numUvs * 2; j+=2) {
					vertices[j] = 1 - vertices[j];
				}
			}
			idx+=vertexSize;		
		}
		
		Mesh mesh = new Mesh(true, numVertices, indices.size, vertexAttributes);
		mesh.setVertices(vertices);
		mesh.setIndices(convertToShortArray(indices));
		return new StillSubMesh(name, mesh, GL10.GL_TRIANGLES);
	}
	
	public static KeyframedModel loadKeyframedModel(InputStream stream, boolean flipV) {
		BufferedReader in = new BufferedReader(new InputStreamReader(stream));
		
		lineNum = 1;
		try {
			String version = readString(in);
			if(!version.equals("g3dt-keyframed-1.0")) throw new GdxRuntimeException("incorrect version");
			int numMeshes = readInt(in);
			KeyframedSubMesh[] subMeshes = new KeyframedSubMesh[numMeshes];
			for(int i = 0; i < numMeshes; i++) {
				subMeshes[i] = readMesh(in, flipV);
			}
			KeyframedModel model = new KeyframedModel(subMeshes);			
			model.setAnimation(model.getAnimations()[0].name, 0, false);
			return model;
		} catch(Throwable e) {
			throw new GdxRuntimeException("Couldn't read keyframed model, error in line " + lineNum + ", '" + line + "' : " + e.getMessage(), e);
		}		
	}	
	
	private static KeyframedSubMesh readMesh(BufferedReader in, boolean flipV) throws IOException {
		String name = readString(in);
		IntArray indices = readFaces(in);
		int numVertices = readInt(in);
		int numAttributes = readInt(in);		
		
		if(!readString(in).equals("position")) throw new GdxRuntimeException("first attribute must be position.");
		Array<FloatArray> uvSets = new Array<FloatArray>();
		boolean hasNormals = false;
		for(int i = 1; i < numAttributes; i++) {
			String attributeType = readString(in);
			
			if(!attributeType.equals("normal") && !attributeType.equals("uv")) throw new GdxRuntimeException("attribute name must be normal or uv");
			
			if(attributeType.equals("normal")) {
				if(i != 1) throw new GdxRuntimeException("attribute normal must be second attribute");
				hasNormals = true;
			}
			if(attributeType.equals("uv")) {
				uvSets.add(readUVSet(in, numVertices, flipV));
			}
		}
		int animatedComponents = hasNormals?6:3;
		
		VertexAttribute[] vertexAttributes = createVertexAttributes(hasNormals, uvSets.size);
		
		int numAnimations = readInt(in);
		ObjectMap<String, KeyframedAnimation> animations = new ObjectMap<String, KeyframedAnimation>(numAnimations);
		for(int i = 0; i < numAnimations; i++) {					
			String animationName = readString(in);
			int numKeyframes = readInt(in);
			float frameDuration = readFloat(in);					
						
			Keyframe[] keyframes = new Keyframe[numKeyframes];
			float time = 0;
			FloatArray vertex = new FloatArray(animatedComponents);
			for(int frame = 0; frame < numKeyframes; frame++) {				
				float[] vertices = new float[numVertices*(animatedComponents)];
				int idx = 0;
				for(int j = 0; j < numVertices; j++) {
					idx = readFloatArray(in, vertices, idx);					
				}
				Keyframe keyframe = new Keyframe(time, animatedComponents, vertices);				
				keyframes[frame] = keyframe;
				time += frameDuration;				
			}						
			
			KeyframedAnimation animation = new KeyframedAnimation(animationName, frameDuration, keyframes);
			animations.put(animationName, animation);
		}		
		
		KeyframedSubMesh mesh = new KeyframedSubMesh(name, 
																	new Mesh(false, numVertices, indices.size, createVertexAttributes(hasNormals, uvSets.size)),
																	buildVertices(numVertices, hasNormals, uvSets),
																	animations,
																	GL10.GL_TRIANGLES);
		mesh.mesh.setIndices(convertToShortArray(indices));
		mesh.mesh.setVertices(mesh.blendedVertices);
		return mesh;
	}

	private static float[] buildVertices(int numVertices, boolean hasNormals, Array<FloatArray> uvSets) {
		float[] vertices = new float[numVertices * (3 + (hasNormals?3:0) + uvSets.size * 2)];
		
		int idx = 0;
		int idxUv = 0;
		for(int i = 0; i < numVertices; i++) {
			vertices[idx++] = 0;
			vertices[idx++] = 0;
			vertices[idx++] = 0;
			if(hasNormals) {
				vertices[idx++] = 0;
				vertices[idx++] = 0;
				vertices[idx++] = 0;
			}
			for(int j = 0; j < uvSets.size; j++) {
				vertices[idx++] = uvSets.get(j).get(idxUv);
				vertices[idx++] = uvSets.get(j).get(idxUv+1);
			}
			idxUv+=2;
		}
		return vertices;
	}
	
	private static VertexAttribute[] createVertexAttributes (boolean hasNormals, int uvs) {
		VertexAttribute[] attributes = new VertexAttribute[1 + (hasNormals?1:0) + uvs];
		int idx = 0;
		attributes[idx++] = new VertexAttribute(Usage.Position, 3, "a_pos");
		if(hasNormals) attributes[idx++] = new VertexAttribute(Usage.Normal, 3, "a_nor");
		for(int i = 0; i < uvs; i++) {
			attributes[idx++] = new VertexAttribute(Usage.TextureCoordinates, 2, "a_tex" + i);
		}		
		return attributes;
	}

	private static FloatArray readUVSet(BufferedReader in, int numVertices, boolean flipV) throws IOException {
		FloatArray uvSet = new FloatArray(numVertices * 2);
		FloatArray uv = new FloatArray(2);
		for(int i = 0; i < numVertices; i++) {
			readFloatArray(in, uv);
			uvSet.add(uv.items[0]);
			uvSet.add(flipV?1-uv.items[1]:uv.items[1]);
		}
		return uvSet;
	}
	
	private static IntArray readFaces(BufferedReader in) throws NumberFormatException, IOException {
		int numFaces = readInt(in);
		IntArray faceIndices = new IntArray();
		IntArray triangles = new IntArray();
		IntArray indices = new IntArray();
		
		for(int face = 0; face < numFaces; face++) {			
			readIntArray(in, faceIndices);
			int numIndices = faceIndices.get(0);
			triangles.clear();
			int baseIndex = faceIndices.get(1);			
			for(int i = 2; i < numIndices; i++) {
				triangles.add(baseIndex);
				triangles.add(faceIndices.items[i]);
				triangles.add(faceIndices.items[i+1]);
			}
			indices.addAll(triangles);
		}
		
		indices.shrink();
		return indices;
	}
	
	private static short[] convertToShortArray(IntArray array) {
		short[] shortArray = new short[array.size];
		for(int i = 0; i < array.size; i++) {
			shortArray[i] = (short)array.items[i];
		}
		return shortArray;
	}
	
	private static float readFloat(BufferedReader in) throws NumberFormatException, IOException {
		lineNum++;
		return Float.parseFloat(read(in).trim());
	}
	
	private static int readInt(BufferedReader in) throws NumberFormatException, IOException {
		lineNum++;
		return (int)(Math.floor(Float.parseFloat(read(in).trim())));
	}
	
	private static String readString(BufferedReader in) throws IOException {
		lineNum++;
		return read(in);	
	}	
	
	private static void readFloatArray(BufferedReader in, FloatArray array) throws IOException {
		lineNum++;
		String[] tokens = read(in).split(","); 		
		int len = tokens.length;
		array.clear();
		for(int i = 0; i < len; i++) {
			 array.add(Float.parseFloat(tokens[i].trim()));
		}		
	}
	
	private static int readFloatArray (BufferedReader in, float[] array, int idx) throws IOException {
		lineNum++;
		String[] tokens = read(in).split(","); 		
		int len = tokens.length;		
		for(int i = 0; i < len; i++) {
			 array[idx++] = Float.parseFloat(tokens[i].trim());
		}
		return idx;
	}
		
	private static void readIntArray(BufferedReader in, IntArray array) throws IOException {
		String[] tokens = read(in).split(","); 		
		int len = tokens.length;
		array.clear();
		for(int i = 0; i < len; i++) {
			array.add(Integer.parseInt(tokens[i].trim()));
		}
	}
	
	private static String read(BufferedReader in) throws IOException {
		line = in.readLine();
		return line;
	}	
}

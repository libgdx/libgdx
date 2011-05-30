package com.badlogic.gdx.graphics.g3d.loaders.g3d.chunks;

import java.io.IOException;
import java.io.InputStream;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.g3d.loaders.g3d.G3dConstants;
import com.badlogic.gdx.graphics.g3d.loaders.g3d.chunks.ChunkReader.Chunk;
import com.badlogic.gdx.graphics.g3d.materials.Material;
import com.badlogic.gdx.graphics.g3d.model.still.StillModel;
import com.badlogic.gdx.graphics.g3d.model.still.StillSubMesh;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class G3dLoader {
	public static StillModel loadStillModel(FileHandle handle) {
		Chunk root = null;
		InputStream in = null;
		try {
			in = handle.read();
			root = ChunkReader.readChunks(in);
		
			// check root tag
			if(root.getId() != G3dConstants.G3D_ROOT) throw new GdxRuntimeException("Invalid root tag id: " + root.getId());
			
			// check version
			Chunk version = root.getChild(G3dConstants.VERSION_INFO);
			if(version == null) throw new GdxRuntimeException("No version chunk found");
			int major = version.readByte();
			int minor = version.readByte();
			if(major != 0 || minor != 1) throw new GdxRuntimeException("Invalid version, required 0.1, got " + major + "." + minor);
			
			// read stillmodel
			Chunk stillModel =  root.getChild(G3dConstants.STILL_MODEL);
			if(stillModel == null) throw new GdxRuntimeException("No stillmodel chunk found");			
			int numSubMeshes = stillModel.readInt();
			
			// read submeshes
			StillSubMesh[] meshes = new StillSubMesh[numSubMeshes];
			Chunk[] meshChunks = stillModel.getChildren(G3dConstants.STILL_SUBMESH);
			if(meshChunks.length != numSubMeshes) throw new GdxRuntimeException("Number of submeshes not equal to number specified in still model chunk, expected " + numSubMeshes + ", got " + meshChunks.length);
			for(int i = 0; i < numSubMeshes; i++) {
				// read submesh name and primitive type
				Chunk subMesh = meshChunks[i];
				String name = subMesh.readString();
				int primitiveType = subMesh.readInt();
				
				// read attributes
				Chunk attributes = subMesh.getChild(G3dConstants.VERTEX_ATTRIBUTES);
				if(attributes == null) throw new GdxRuntimeException("No vertex attribute chunk given");				
				int numAttributes = attributes.readInt();
				Chunk[] attributeChunks = attributes.getChildren(G3dConstants.VERTEX_ATTRIBUTE);
				if(attributeChunks.length != numAttributes) new GdxRuntimeException("Number of attributes not equal to number specified in attributes chunk, expected " + numAttributes + ", got " + attributeChunks.length);
				VertexAttribute[] vertAttribs = new VertexAttribute[numAttributes];
				for(int j = 0; j < numAttributes; j++) {
					vertAttribs[j] = new VertexAttribute(attributeChunks[j].readInt(), attributeChunks[j].readInt(), attributeChunks[j].readString());					
				}
				
				// read vertices
				Chunk vertices = subMesh.getChild(G3dConstants.VERTEX_LIST);
				int numVertices = vertices.readInt();
				float[] vertexData = vertices.readFloats();
				
				// read indices 
				Chunk indices = subMesh.getChild(G3dConstants.INDEX_LIST);
				int numIndices = indices.readInt();
				short[] indexData = indices.readShorts();
				
				StillSubMesh mesh = new StillSubMesh(name, new Mesh(true, numVertices, numIndices, vertAttribs), primitiveType);
				mesh.mesh.setVertices(vertexData);
				mesh.mesh.setIndices(indexData);
				mesh.material = new Material("default");
				meshes[i] = mesh;
			}
		
			return new StillModel(meshes);
		} catch(IOException e) {
			throw new GdxRuntimeException("Couldn't load still model from '" + handle.name() +"', " + e.getMessage(), e);
		} finally {
			if(in != null) try { in.close(); } catch(IOException e) { }
		}			
	}
}

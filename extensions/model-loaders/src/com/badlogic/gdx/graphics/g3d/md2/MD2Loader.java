
package com.badlogic.gdx.graphics.g3d.md2;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.model.keyframe.Keyframe;
import com.badlogic.gdx.graphics.g3d.model.keyframe.KeyframedAnimation;
import com.badlogic.gdx.graphics.g3d.model.keyframe.KeyframedModel;
import com.badlogic.gdx.graphics.g3d.model.keyframe.SubMesh;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.LittleEndianInputStream;

public class MD2Loader {

	public KeyframedModel load (FileHandle fileHandle) {
		InputStream in = fileHandle.read();
		try {
			return load(in);
		} finally {
			if (in != null) try {
				in.close();
			} catch (IOException e) {
			}
			;
		}
	}

	public KeyframedModel load (InputStream in) {				
		try {
			byte[] bytes = loadBytes(new BufferedInputStream(in));
			MD2Header header = loadHeader(bytes);
			float[] texCoords = loadTexCoords(header, bytes);
			Array<MD2Frame> frames = loadFrames(header, bytes);
			short[] triangles = loadTriangles(header, bytes);			
			return buildModel(header, triangles, frames, texCoords);						
		} catch (IOException e) {
			throw new GdxRuntimeException("Couldn't load MD2 model", e);
		}		
	}

	private KeyframedModel buildModel (MD2Header header, short[] triangles, Array<MD2Frame> frames, float[] texCoords) {
		SubMesh subMesh = new SubMesh();
		KeyframedAnimation animation = new KeyframedAnimation();
		animation.duration = frames.size * 0.2f;
		animation.keyframes = new Keyframe[frames.size];
		
		for(int i = 0; i < frames.size; i++) {			
			MD2Frame frame = frames.get(i);
			final float[] frameVertices = frame.vertices;
			
			Keyframe keyFrame = new Keyframe();
			keyFrame.timeStamp = 0;
			keyFrame.vertices = new float[header.numTris * 15];			
			
			for(int j = 0, idx = 0, idx2 = 0; j < header.numTris; j++) {				
				int v1 = triangles[idx2++] * 3;
				int v2 = triangles[idx2++] * 3;
				int v3 = triangles[idx2++] * 3;
				int t1 = triangles[idx2++] * 2;
				int t2 = triangles[idx2++] * 2;
				int t3 = triangles[idx2++] * 2;
				
				keyFrame.vertices[idx++] = frameVertices[v1];
				keyFrame.vertices[idx++] = frameVertices[v1+1];
				keyFrame.vertices[idx++] = frameVertices[v1+2];
				keyFrame.vertices[idx++] = texCoords[t1];
				keyFrame.vertices[idx++] = texCoords[t1+1];
				
				keyFrame.vertices[idx++] = frameVertices[v2];
				keyFrame.vertices[idx++] = frameVertices[v2+1];
				keyFrame.vertices[idx++] = frameVertices[v2+2];
				keyFrame.vertices[idx++] = texCoords[t2];
				keyFrame.vertices[idx++] = texCoords[t2+1];
				
				keyFrame.vertices[idx++] = frameVertices[v3];
				keyFrame.vertices[idx++] = frameVertices[v3+1];
				keyFrame.vertices[idx++] = frameVertices[v3+2];
				keyFrame.vertices[idx++] = texCoords[t3];
				keyFrame.vertices[idx++] = texCoords[t3+1];
				
				animation.keyframes[i] = keyFrame;
			}
		}
		subMesh.mesh = new Mesh(false, header.numTris * 3, 0, new VertexAttribute(Usage.Position, 3, "a_pos"), new VertexAttribute(Usage.TextureCoordinates, 2, "a_tex0"));
		subMesh.animations.put("all", animation);
		KeyframedModel model = new KeyframedModel();
		model.subMeshes = new SubMesh[] { subMesh };		
		return model;
	}

	public MD2Header loadHeader(byte[] bytes) throws IOException {
		MD2Header header = new MD2Header();
		LittleEndianInputStream in = new LittleEndianInputStream(new ByteArrayInputStream(bytes));
		header.ident = in.readInt();
		header.version = in.readInt();
		header.skinWidth = in.readInt();
		header.skinHeight = in.readInt();
		header.frameSize = in.readInt();
		header.numSkins = in.readInt();
		header.numVertices = in.readInt();
		header.numSt = in.readInt();
		header.numTris = in.readInt();
		header.numGlcmds = in.readInt();
		header.numFrames = in.readInt();
		header.offsetSkins = in.readInt();
		header.offsetSt = in.readInt();
		header.offsetTris = in.readInt();
		header.offsetFrames = in.readInt();
		header.offsetGlcmds = in.readInt();
		header.offsetEnd = in.readInt();
		in.close();
		return header;
	}

	public float[] loadTexCoords(MD2Header header, byte[] bytes) throws IOException {
		float[] texCoords = new float[2 * header.numSt];
		LittleEndianInputStream in = new LittleEndianInputStream(new ByteArrayInputStream(bytes));
		in.skipBytes(header.offsetSt);
		float invWidth = 1.0f / header.skinWidth;
		float invHeight = 1.0f / header.skinHeight;
		for(int idx = 0; idx < 2 * header.numSt; idx += 2) {
			texCoords[idx] = in.readShort() * invWidth;
			texCoords[idx+1] = in.readShort() * invHeight;
		}
		in.close();	
		return texCoords;
	}
	
	public Array<MD2Frame> loadFrames(MD2Header header, byte[] bytes) throws IOException {
		Array<MD2Frame> frames = new Array<MD2Frame>();
		LittleEndianInputStream in = new LittleEndianInputStream(new ByteArrayInputStream(bytes));
		in.skip(header.offsetFrames);
		byte[] name = new byte[16];
		for(int i = 0; i < header.numFrames; i++) {
			MD2Frame frame = new MD2Frame();
			frame.vertices = new float[3 * header.numVertices];
			float scaleX = in.readFloat(); 
			float scaleY = in.readFloat();
			float scaleZ = in.readFloat();
			float tranX = in.readFloat();
			float tranY = in.readFloat();
			float tranZ = in.readFloat();
			in.read(name);
			frame.name = new String(name);
			for(int j = 0, idx = 0; j < header.numVertices; j++) {
				float x = in.read(), y = in.read(), z = in.read();
				frame.vertices[idx++] = x * scaleX + tranX;				
				frame.vertices[idx++] = z * scaleZ + tranZ;
				frame.vertices[idx++] = y * scaleY + tranY;
				in.read();
			}
			frames.add(frame);
		}
		in.close();
		return frames;
	}
	
	public short[] loadTriangles (MD2Header header, byte[] bytes) throws IOException {
		short[] triangles = new short[6 * header.numTris];
		LittleEndianInputStream in = new LittleEndianInputStream(new ByteArrayInputStream(bytes));
		in.skip(header.offsetTris);
		for(int i = 0, idx = 0; i < header.numTris; i++) {
			triangles[idx++] = in.readShort();
			triangles[idx++] = in.readShort();
			triangles[idx++] = in.readShort();
			triangles[idx++] = in.readShort();
			triangles[idx++] = in.readShort();
			triangles[idx++] = in.readShort();
		}
		return triangles;
	}
	
	public byte[] loadBytes (InputStream in) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];

		int readBytes = 0;
		while ((readBytes = in.read(buffer)) > 0) {
			out.write(buffer, 0, readBytes);
		}

		out.close();
		return out.toByteArray();
	}
}

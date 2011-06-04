
package com.badlogic.gdx.graphics.g3d.loaders.md2;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.model.keyframe.Keyframe;
import com.badlogic.gdx.graphics.g3d.model.keyframe.KeyframedAnimation;
import com.badlogic.gdx.graphics.g3d.model.keyframe.KeyframedModel;
import com.badlogic.gdx.graphics.g3d.model.keyframe.KeyframedSubMesh;
import com.badlogic.gdx.utils.LittleEndianInputStream;
import com.badlogic.gdx.utils.ObjectMap;

public class MD2Loader {	
	public KeyframedModel load (FileHandle fileHandle, float frameDuration) {
		InputStream in = fileHandle.read();
		try {
			return load(in, frameDuration);
		} finally {
			if (in != null) try {
				in.close();
			} catch (IOException e) {
			}
			;
		}
	}

	public KeyframedModel load (InputStream in, float frameDuration) {
		try {
			byte[] bytes = loadBytes(in);

			MD2Header header = loadHeader(bytes);
			float[] texCoords = loadTexCoords(header, bytes);
			MD2Triangle[] triangles = loadTriangles(header, bytes);
			MD2Frame[] frames = loadFrames(header, bytes);

			return buildModel(header, triangles, texCoords, frames, frameDuration);
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	private KeyframedModel buildModel (MD2Header header, MD2Triangle[] triangles, float[] texCoords, MD2Frame[] frames, float frameDuration) {
		ArrayList<VertexIndices> vertCombos = new ArrayList<VertexIndices>();
		short[] indices = new short[triangles.length * 3];
		int idx = 0;
		short vertIdx = 0;
		for (int i = 0; i < triangles.length; i++) {
			MD2Triangle triangle = triangles[i];
			for (int j = 0; j < 3; j++) {
				VertexIndices vert = null;
				boolean contains = false;
				for (int k = 0; k < vertCombos.size(); k++) {
					VertexIndices vIdx = vertCombos.get(k);
					if (vIdx.vIdx == triangle.vertices[j] && vIdx.tIdx == triangle.texCoords[j]) {
						vert = vIdx;
						contains = true;
						break;
					}
				}
				if (!contains) {
					vert = new VertexIndices(triangle.vertices[j], triangle.texCoords[j], vertIdx);
					vertCombos.add(vert);
					vertIdx++;
				}

				indices[idx++] = vert.nIdx;
			}
		}

		idx = 0;
		float[] uvs = new float[vertCombos.size() * 2];
		for (int i = 0; i < vertCombos.size(); i++) {
			VertexIndices vtI = vertCombos.get(i);
			uvs[idx++] = texCoords[vtI.tIdx * 2];
			uvs[idx++] = texCoords[vtI.tIdx * 2 + 1];
		}

		for (int i = 0; i < frames.length; i++) {
			MD2Frame frame = frames[i];
			idx = 0;
			float[] newVerts = new float[vertCombos.size() * 3];

			for (int j = 0; j < vertCombos.size(); j++) {
				VertexIndices vIdx = vertCombos.get(j);
				newVerts[idx++] = frame.vertices[vIdx.vIdx * 3];
				newVerts[idx++] = frame.vertices[vIdx.vIdx * 3 + 1];
				newVerts[idx++] = frame.vertices[vIdx.vIdx * 3 + 2];
			}
			frame.vertices = newVerts;
		}

		header.numVertices = vertCombos.size();
		
		float[] blendedVertices = new float[header.numVertices * 5];
		MD2Frame frame = frames[0];		
		idx = 0;
		int idxV = 0;
		int idxT = 0;
		for (int i = 0; i < header.numVertices; i++) {
			blendedVertices[idx++] = frame.vertices[idxV++];
			blendedVertices[idx++] = frame.vertices[idxV++];
			blendedVertices[idx++] = frame.vertices[idxV++];
			blendedVertices[idx++] = uvs[idxT++];
			blendedVertices[idx++] = uvs[idxT++];
		}
		
		KeyframedAnimation animation = new KeyframedAnimation("all", frameDuration, new Keyframe[frames.length]);
		float timeStamp = 0;
		for (int frameNum = 0; frameNum < frames.length; frameNum++) {
			frame = frames[frameNum];
			float[] vertices = new float[header.numVertices * 3];
			idx = 0;			
			idxV = 0;
			for (int i = 0; i < header.numVertices; i++) {
				vertices[idx++] = frame.vertices[idxV++];
				vertices[idx++] = frame.vertices[idxV++];
				vertices[idx++] = frame.vertices[idxV++];				
			}

			Keyframe keyFrame = new Keyframe(frameNum * frameDuration, 3, vertices);			
			animation.keyframes[frameNum] = keyFrame;
		}

		
		Mesh mesh = new Mesh(false, header.numVertices, indices.length, 
									new VertexAttribute(Usage.Position, 3, "a_pos"), 
									new VertexAttribute(Usage.TextureCoordinates, 2, "a_tex0"));
		mesh.setIndices(indices);
		ObjectMap<String, KeyframedAnimation> animations = new ObjectMap<String, KeyframedAnimation>();
		animations.put("all", animation);
		
		KeyframedSubMesh subMesh = new KeyframedSubMesh("md2-mesh", mesh, blendedVertices, animations, GL10.GL_TRIANGLES);	
		KeyframedModel model = new KeyframedModel(new KeyframedSubMesh[] {subMesh});		
		model.setAnimation("all", 0, false);
		return model;
	}

	private float[] buildTexCoords (MD2Header header, MD2Triangle[] triangles, float[] texCoords) {
		float[] uvs = new float[header.numVertices * 2];

		for (int i = 0; i < triangles.length; i++) {
			MD2Triangle triangle = triangles[i];
			for (int j = 0; j < 3; j++) {
				int vertIdx = triangle.vertices[j];
				int uvIdx = vertIdx * 2;
				uvs[uvIdx] = texCoords[triangle.texCoords[j] * 2];
				uvs[uvIdx + 1] = texCoords[triangle.texCoords[j] * 2 + 1];
			}
		}

		return uvs;
	}

	private short[] buildIndices (MD2Triangle[] triangles) {
		short[] indices = new short[triangles.length * 3];

		int idx = 0;
		for (int i = 0; i < triangles.length; i++) {
			MD2Triangle triangle = triangles[i];
			indices[idx++] = triangle.vertices[0];
			indices[idx++] = triangle.vertices[1];
			indices[idx++] = triangle.vertices[2];
		}
		return indices;
	}

	private MD2Frame[] loadFrames (MD2Header header, byte[] bytes) throws IOException {
		LittleEndianInputStream in = new LittleEndianInputStream(new ByteArrayInputStream(bytes));
		in.skip(header.offsetFrames);

		MD2Frame[] frames = new MD2Frame[header.numFrames];
		for (int i = 0; i < header.numFrames; i++) {
			frames[i] = loadFrame(header, in);
		}

		in.close();

		return frames;
	}

	private final byte[] charBuffer = new byte[16];

	private MD2Frame loadFrame (MD2Header header, LittleEndianInputStream in) throws IOException {
		MD2Frame frame = new MD2Frame();
		frame.vertices = new float[header.numVertices * 3];

		float scaleX = in.readFloat(), scaleY = in.readFloat(), scaleZ = in.readFloat();
		float transX = in.readFloat(), transY = in.readFloat(), transZ = in.readFloat();
		in.read(charBuffer);

		int len = 0;
		for (int i = 0; i < charBuffer.length; i++)
			if (charBuffer[i] == 0) {
				len = i - 1;
				break;
			}

		frame.name = new String(charBuffer, 0, len);

		int vertIdx = 0;

		for (int i = 0; i < header.numVertices; i++) {
			float x = in.read() * scaleX + transX;
			float y = in.read() * scaleY + transY;
			float z = in.read() * scaleZ + transZ;

			frame.vertices[vertIdx++] = y;
			frame.vertices[vertIdx++] = z;
			frame.vertices[vertIdx++] = x;

			in.read(); // normal index
		}

		return frame;
	}

	private MD2Triangle[] loadTriangles (MD2Header header, byte[] bytes) throws IOException {
		LittleEndianInputStream in = new LittleEndianInputStream(new ByteArrayInputStream(bytes));
		in.skip(header.offsetTriangles);
		MD2Triangle[] triangles = new MD2Triangle[header.numTriangles];

		for (int i = 0; i < header.numTriangles; i++) {
			MD2Triangle triangle = new MD2Triangle();
			triangle.vertices[0] = in.readShort();
			triangle.vertices[1] = in.readShort();
			triangle.vertices[2] = in.readShort();
			triangle.texCoords[0] = in.readShort();
			triangle.texCoords[1] = in.readShort();
			triangle.texCoords[2] = in.readShort();
			triangles[i] = triangle;
		}
		in.close();

		return triangles;
	}

	private float[] loadTexCoords (MD2Header header, byte[] bytes) throws IOException {
		LittleEndianInputStream in = new LittleEndianInputStream(new ByteArrayInputStream(bytes));
		in.skip(header.offsetTexCoords);
		float[] texCoords = new float[header.numTexCoords * 2];
		float width = header.skinWidth;
		float height = header.skinHeight;

		for (int i = 0; i < header.numTexCoords * 2; i += 2) {
			short u = in.readShort();
			short v = in.readShort();
			texCoords[i] = u / width;
			texCoords[i + 1] = v / height;
		}
		in.close();
		return texCoords;
	}

	private MD2Header loadHeader (byte[] bytes) throws IOException {
		LittleEndianInputStream in = new LittleEndianInputStream(new ByteArrayInputStream(bytes));
		MD2Header header = new MD2Header();

		header.ident = in.readInt();
		header.version = in.readInt();
		header.skinWidth = in.readInt();
		header.skinHeight = in.readInt();
		header.frameSize = in.readInt();
		header.numSkins = in.readInt();
		header.numVertices = in.readInt();
		header.numTexCoords = in.readInt();
		header.numTriangles = in.readInt();
		header.numGLCommands = in.readInt();
		header.numFrames = in.readInt();
		header.offsetSkin = in.readInt();
		header.offsetTexCoords = in.readInt();
		header.offsetTriangles = in.readInt();
		header.offsetFrames = in.readInt();
		header.offsetGLCommands = in.readInt();
		header.offsetEnd = in.readInt();

		in.close();

		return header;
	}

	private byte[] loadBytes (InputStream in) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];

		int readBytes = 0;
		while ((readBytes = in.read(buffer)) > 0) {
			out.write(buffer, 0, readBytes);
		}

		out.close();
		return out.toByteArray();
	}

	public class VertexIndices {
		public VertexIndices (short vIdx, short tIdx, short nIdx) {
			this.vIdx = vIdx;
			this.tIdx = tIdx;
			this.nIdx = nIdx;
		}

		@Override public int hashCode () {
			final int prime = 31;
			int result = 1;
			result = prime * result + tIdx;
			result = prime * result + vIdx;
			return result;
		}

		@Override public boolean equals (Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			VertexIndices other = (VertexIndices)obj;
			if (tIdx != other.tIdx) return false;
			if (vIdx != other.vIdx) return false;
			return true;
		}

		public short vIdx;
		public short tIdx;
		public short nIdx;
	}
}

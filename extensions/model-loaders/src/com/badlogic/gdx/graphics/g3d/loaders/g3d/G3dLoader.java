package com.badlogic.gdx.graphics.g3d.loaders.g3d;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.model.still.StillModel;
import com.badlogic.gdx.graphics.g3d.model.still.StillSubMesh;

public class G3dLoader {
	
	// Unique IDs for chunk declarations
	private static final int G3D_HEADER =				0x4733441A;
	private static final short VERSION_INFO = 			0x0001;
	private static final short STILL_MODEL =			0x1000;
	private static final short STILL_SUBMESH =			0x1100;
	private static final short VERTEX_LIST = 			0x1110;
	private static final short VERTEX_ATTRIBUTES =		0x1120;
	private static final short VERTEX_ATTRIBUTE =		0x1121;
	
	int position, fileLength;
	byte majorVersion, minorVersion;
	
	public StillModel loadStillModel(FileHandle file) {
		return null;
//		DataInputStream dis = new DataInputStream(new BufferedInputStream(file.read()));
//		
//		position = 0;
//		fileLength = 0;
//		short chunkUID;
//		int chunkLength;
//		
//		// Check if this is actually a g3d binary file
//		if (readLong(dis) != G3D_HEADER) return null;
//		else fileLength = readInt(dis);
//		
//		StillSubMesh subMesh;
//		int activeSubMesh = 0;
//		
//		while (position <= fileLength) {
//			chunkUID = readShort(dis);
//			chunkLength = readInt(dis);
//			switch(chunkUID) {
//			case VERSION_INFO:
//				readVersionInfo(dis, chunkLength);
//			case STILL_MODEL:
//				readStillModel(dis, chunkLength);
//			case STILL_SUBMESH:
//				readStilLSumbesh(dis, chunkLength);
//			case VERTEX_LIST:
//				readVertexList(dis, chunkLength);
//			case VERTEX_ATTRIBUTES:
//				readVertexAttributes(dis, chunkLength);
//			case VERTEX_ATTRIBUTE:
//				readVertexAttribute(dis, chunkLength);
//			default:
//				skipChunk(dis, chunkLength);
//			}
//		}
//		return model;
	}
	
	private void readVersionInfo(DataInputStream dis, int len) throws IOException {
		majorVersion = readByte(dis);
		minorVersion = readByte(dis);
	}
	
	private void skipChunk(DataInputStream dis, int numBytes) throws IOException {
		dis.skipBytes(numBytes);
	}
	
	private byte readByte(DataInputStream dis) throws IOException {
		position++;
		return dis.readByte();
	}
	private short readShort(DataInputStream dis) throws IOException {
		position += 2;
		return dis.readShort();
	}
	private int readInt(DataInputStream dis) throws IOException {
		position += 4;
		return dis.readInt();
	}
	private long readLong(DataInputStream dis) throws IOException {
		position += 8;
		return dis.readLong();
	}
	private float readFloat(DataInputStream dis) throws IOException {
		position += 4;
		return Float.intBitsToFloat(dis.readInt());
	}
	private double readDouble(DataInputStream dis) throws IOException {
		position += 8;
		return Double.longBitsToDouble(dis.readLong());
	}
	private char readChar(DataInputStream dis) throws IOException {
		position++;
		return (char) (dis.readByte() & 0xFF);
	}
	private String readString(DataInputStream dis) throws IOException {
		char c;
		StringBuffer sBuf = new StringBuffer();
		while ((c = readChar(dis)) != 0x00)
			sBuf.append(c);
		position += sBuf.length() + 1;
		return sBuf.toString();
	}

}

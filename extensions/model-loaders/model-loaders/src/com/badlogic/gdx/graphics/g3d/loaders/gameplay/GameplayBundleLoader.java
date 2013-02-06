package com.badlogic.gdx.graphics.g3d.loaders.gameplay;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g3d.ModelLoaderHints;
import com.badlogic.gdx.graphics.g3d.loaders.StillModelLoader;
import com.badlogic.gdx.graphics.g3d.model.still.StillModel;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.LittleEndianInputStream;

public class GameplayBundleLoader implements StillModelLoader {
	private static final byte[] sig = { (byte)0xab, 'G', 'P', 'B', (byte)0xbb, '\r', '\n', (byte)0x1a, '\n' }; 
	private byte[] buffer = new byte[1024];
	
	private byte[] read(LittleEndianInputStream in, int len) throws IOException {
		if(buffer.length < len) {
			buffer = new byte[len];
		}
		in.read(buffer, 0, len);
		return buffer;
	}
	
	private String readString(LittleEndianInputStream in) throws IOException {
		int len = in.readInt();
		StringBuffer buffer = new StringBuffer(len);
		for(int i = 0; i < len; i++) {
			buffer.append((char)in.readByte());
		}
		return buffer.toString();
	}
	
	private void log(String msg) {
		System.out.println(msg);
	}
	
	@Override
	public StillModel load (FileHandle handle, ModelLoaderHints hints) {		
		LittleEndianInputStream in = new LittleEndianInputStream(handle.read());
		
		try {
			// check signature
			byte[] bytes = read(in, 9);
			if(Arrays.equals(bytes, sig)) {
				throw new GdxRuntimeException("Not a Gameplay Bundle file");
			}
			log("Signature is good");
			
			// get version
			int major = in.readByte();
			int minor = in.readByte();
			log("Version: " + major + "." + minor);
			
			// read the reference table
			ReferenceTable refs = readReferenceTable(in);
			
		} catch(Exception e) {
			throw new GdxRuntimeException("Couldn't load " + handle.name(), e);
		} finally {
			if(in != null) try { in.close(); } catch(Exception e) { }
		}
		
		return null;
	}
	
	private ReferenceTable readReferenceTable (LittleEndianInputStream in) throws IOException {
		int refCount = in.readInt();
		log(refCount + " references");
		ReferenceTable refs = new ReferenceTable();
		for(int i = 0; i < refCount; i++) {
			Reference ref = readReference(in);
			refs.add(ref);
			log(ref.toString());
		}
		return refs;
	}

	private Reference readReference (LittleEndianInputStream in) throws IOException {
		String id = readString(in);
		int typeId = in.readInt();
		int offset = in.readInt();
		return new Reference(id, Type.forId(typeId), offset);
	}

	public static void main (String[] args) {
		GameplayBundleLoader loader = new GameplayBundleLoader();
		loader.load(new FileHandle(new File("../model-loaders-android/assets/data/models/dude.gpb")), null);
	}
}

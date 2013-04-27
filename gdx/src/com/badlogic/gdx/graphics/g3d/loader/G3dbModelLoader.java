package com.badlogic.gdx.graphics.g3d.loader;

import java.io.DataInputStream;
import java.io.IOException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.ModelLoader;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.model.data.ModelData;
import com.badlogic.gdx.graphics.g3d.model.data.ModelMaterial;
import com.badlogic.gdx.graphics.g3d.model.data.ModelMaterial.MaterialType;
import com.badlogic.gdx.graphics.g3d.model.data.ModelMesh;
import com.badlogic.gdx.graphics.g3d.model.data.ModelMeshPart;
import com.badlogic.gdx.graphics.g3d.model.data.ModelNodePart;
import com.badlogic.gdx.graphics.g3d.model.data.ModelNode;
import com.badlogic.gdx.graphics.g3d.model.data.ModelTexture;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public class G3dbModelLoader extends ModelLoader<AssetLoaderParameters<Model>> {
	private final static int G3DB_TAG_MESH 			= 0x01;
	private final static int G3DB_TAG_ID	 			= 0x02;
	private final static int G3DB_TAG_ATTRIBUTES		= 0x03;
	private final static int G3DB_TAG_VERTICES		= 0x04;
	private final static int G3DB_TAG_MESHPART		= 0x05;
	private final static int G3DB_TAG_TYPE	 			= 0x06;
	private final static int G3DB_TAG_INDICES			= 0x07;
	private final static int G3DB_TAG_MATERIAL		= 0x08;
	private final static int G3DB_TAG_DIFFUSE			= 0x09;
	private final static int G3DB_TAG_AMBIENT			= 0x0A;
	private final static int G3DB_TAG_EMMISIVE		= 0x0B;
	private final static int G3DB_TAG_OPACITY			= 0x0C;
	private final static int G3DB_TAG_SPECULAR		= 0x0D;
	private final static int G3DB_TAG_SHININESS		= 0x0E;
	private final static int G3DB_TAG_NODE	 			= 0x0F;
	private final static int G3DB_TAG_TRANSLATE		= 0x10;
	private final static int G3DB_TAG_ROTATE 			= 0x11;
	private final static int G3DB_TAG_SCALE 			= 0x12;
	private final static int G3DB_TAG_PARTMATERIAL	= 0x13;
	private final static int G3DB_TAG_TEXTURE			= 0x14;
	private final static int G3DB_TAG_FILENAME		= 0x15;
	private final static int G3DB_TAG_ANIMATIONCLIP	= 0x16;
	private final static int G3DB_TAG_BONE				= 0x17;
	private final static int G3DB_TAG_KEYFRAME		= 0x18;
	private final static int G3DB_TAG_TIME				= 0x19;
	
	private final static int USAGE_UNKNOWN = 0;
	private final static int USAGE_POSITION = 1;
	private final static int USAGE_NORMAL = 2;
	private final static int USAGE_COLOR = 3;
	private final static int USAGE_TANGENT = 4;
	private final static int USAGE_BINORMAL = 5;
	private final static int USAGE_BLENDWEIGHTS = 6;
	private final static int USAGE_BLENDINDICES = 7;
	private final static int USAGE_TEXCOORD0 = 8;
	private final static int USAGE_TEXCOORD1 = 9;
	private final static int USAGE_TEXCOORD2 = 10;
	private final static int USAGE_TEXCOORD3 = 11;
	private final static int USAGE_TEXCOORD4 = 12;
	private final static int USAGE_TEXCOORD5 = 13;
	private final static int USAGE_TEXCOORD6 = 14;
	private final static int USAGE_TEXCOORD7 = 15;
	
	private final static int MATERIAL_LAMBERT = 0;
	private final static int MATERIAL_PHONG = 1;
	
	private final static int TYPE_MASK	= 0x3F;
	private final static int SIZE_MASK	= 0xC0;
	private final static int SMALL 		= 0x00;
	private final static int MEDIUM 		= 0x40;
	private final static int LARGE 		= 0x80;
	private final static int XLARGE 		= 0xC0;
	
	private DataInputStream din;
	private short versionHi, versionLo;
	private ModelData result;
	private FileHandle file;
	
	public G3dbModelLoader (FileHandleResolver resolver) {
		super(resolver);
	}

	@Override
	public ModelData loadModelData (FileHandle fileHandle, AssetLoaderParameters<Model> parameters) {
		din = new DataInputStream(fileHandle.read());
		file = fileHandle;
		try {
			if (!readString(4).equals("G3DB"))
				throw new IOException("Not a valid G3DB file.");
			versionHi = din.readShort();
			versionLo = din.readShort();
			din.skipBytes(32-8);
			result = new ModelData();
			result.version = versionHi+"."+versionLo;
			readModelData();
		} catch (final IOException e) {
			Gdx.app.log("G3dbModelLoader", e.getMessage());
		} finally {
			try {
				din.close();
			} catch (final IOException e) {
				Gdx.app.log("G3dbModelLoader", e.getMessage());
			}
			din = null;
		}
		final ModelData r = result;
		result = null;
		return r;
	}
	
	private void readModelData() throws IOException {
		while(din.available() > 0) {
			final byte type = din.readByte();
			final long size = readSize(type);
			if ((type & TYPE_MASK) == G3DB_TAG_MESH)
				result.meshes.add(readMesh(size));
			else if ((type & TYPE_MASK) == G3DB_TAG_MATERIAL)
				result.materials.add(readMaterial(size));
			else if ((type & TYPE_MASK) == G3DB_TAG_NODE)
				result.nodes.add(readNode(size));
			else { // unknown tag, just skip it
				din.skipBytes((int)size);
				Gdx.app.log("G3dbModelLoader", "Unknown model tag: "+type+"["+size+"], skipping");
			}
		}		
	}
	
	private ModelNode readNode(final long length) throws IOException {
		long cnt = 0;
		ModelNode node = new ModelNode();
		Array<ModelNode> children = new Array<ModelNode>();
		Array<ModelNodePart> parts = new Array<ModelNodePart>();
		result.nodes.add(node);
		while ((din.available() > 0) && (cnt < length)) {
			final byte type = din.readByte();
			final long size = readSize(type);
			cnt += 1 + getSize(type) + size;
			if ((type & TYPE_MASK) == G3DB_TAG_ID)
				node.id = readString(size);
			else if ((type & TYPE_MASK) == G3DB_TAG_BONE)
				din.skipBytes((int)size);
			else if ((type & TYPE_MASK) == G3DB_TAG_TRANSLATE)
				node.translation = readVector3(size);
			else if ((type & TYPE_MASK) == G3DB_TAG_ROTATE)
				node.rotation = readQuaternion(size);
			else if ((type & TYPE_MASK) == G3DB_TAG_SCALE)
				node.scale = readVector3(size);
			else if ((type & TYPE_MASK) == G3DB_TAG_MESH)
				node.meshId = readString(size);
			else if ((type & TYPE_MASK) == G3DB_TAG_PARTMATERIAL)
				parts.add(readMeshPartMaterial(size));
			else if ((type & TYPE_MASK) == G3DB_TAG_NODE)
				children.add(readNode(size));
			else {
				din.skipBytes((int)size);
				Gdx.app.log("G3dbModelLoader", "Unknown node tag: "+type+"["+size+"], skipping");
			}
		}
		if (children.size > 0) {
			node.children = new ModelNode[children.size];
			for (int i = 0; i < children.size; i++)
				node.children[i] = children.get(i);
		}
		if (parts.size > 0) {
			node.parts = new ModelNodePart[parts.size];
			for (int i = 0; i < parts.size; i++)
				node.parts[i] = parts.get(i);
		}
		return node;
	}
	
	private ModelNodePart readMeshPartMaterial(final long length) throws IOException {
		final ModelNodePart part = new ModelNodePart();
		long cnt = 0;
		while((din.available() > 0) && (cnt < length)) {
			final byte type = din.readByte();
			final long size = readSize(type);
			cnt += 1 + getSize(type) + size;
			if ((type & TYPE_MASK) == G3DB_TAG_MESHPART)
				part.meshPartId = readString(size);
			else if ((type & TYPE_MASK) == G3DB_TAG_MATERIAL)
				part.materialId = readString(size);
			else {
				din.skipBytes((int)size);
				Gdx.app.log("G3dbModelLoader", "Unknown meshpartmaterial tag: "+type+"["+size+"], skipping");
			}
		}
		return part;
	}
	
	private ModelMaterial readMaterial(final long length) throws IOException {
		long cnt = 0;
		ModelMaterial material = new ModelMaterial();
		while ((din.available() > 0) && (cnt < length)) {
			final byte type = din.readByte();
			final long size = readSize(type);
			cnt += 1 + getSize(type) + size;
			if ((type & TYPE_MASK) == G3DB_TAG_ID)
				material.id = readString(size);
			else if ((type & TYPE_MASK) == G3DB_TAG_TYPE) {
				material.type = din.readByte() == MATERIAL_PHONG ? MaterialType.Phong : MaterialType.Lambert;
				if (size > 1)
					din.skipBytes((int)size-1);
			} else if ((type & TYPE_MASK) == G3DB_TAG_DIFFUSE) 
				material.diffuse = readColor(size);
			else if ((type & TYPE_MASK) == G3DB_TAG_AMBIENT)
				material.ambient = readColor(size);
			else if ((type & TYPE_MASK) == G3DB_TAG_EMMISIVE)
				material.ambient = readColor(size);
			else if ((type & TYPE_MASK) == G3DB_TAG_OPACITY)
				din.skipBytes((int)size); // FIXME why is this implemented g3dj but not in ModelMaterial?
			else if ((type & TYPE_MASK) == G3DB_TAG_SPECULAR)
				material.specular = readColor(size);
			else if ((type & TYPE_MASK) == G3DB_TAG_SHININESS) {
				material.shininess = din.readFloat();
				if (size > 4)
					din.skipBytes((int)size - 4);
			} else if ((type & TYPE_MASK) == G3DB_TAG_TEXTURE) {
				if (material.diffuseTextures == null)
					material.diffuseTextures = new Array<ModelTexture>();
				material.diffuseTextures.add(readTexture(size));
			} else { // unknown tag, just skip it
				din.skipBytes((int)size);
				Gdx.app.log("G3dbModelLoader", "Unknown material tag: "+type+"["+size+"], skipping");
			}
		}
		return material;
	}
	
	private ModelTexture readTexture(final long length) throws IOException {
		final ModelTexture texture = new ModelTexture();
		long cnt = 0;
		while ((din.available() > 0) && (cnt < length)) {
			final byte type = din.readByte();
			final long size = readSize(type);
			cnt += 1 + getSize(type) + size;
			if ((type & TYPE_MASK) == G3DB_TAG_ID)
				texture.id = readString(size);
			else if ((type & TYPE_MASK) == G3DB_TAG_FILENAME)
				texture.fileName = file.parent().child(readString(size)).path();
			else if ((type & TYPE_MASK) == G3DB_TAG_TYPE)
				din.skipBytes((int)size); // FIXME
			else if ((type & TYPE_MASK) == G3DB_TAG_TRANSLATE)
				texture.uvTranslation = readVector2(size);//read
			else if ((type & TYPE_MASK) == G3DB_TAG_SCALE)
				texture.uvScaling = readVector2(size);
			else { // unknown tag, just skip it
				din.skipBytes((int)size);
				Gdx.app.log("G3dbModelLoader", "Unknown material tag: "+type+"["+size+"], skipping");
			}
		}
		return texture;
	}
	
	private ModelMesh readMesh(final long length) throws IOException {
		long cnt = 0;
		final ModelMesh mesh = new ModelMesh();
		final Array<ModelMeshPart> parts = new Array<ModelMeshPart>();
		while ((din.available() > 0) && (cnt < length)) {
			final byte type = din.readByte();
			final long size = readSize(type);
			cnt += 1 + getSize(type) + size;
			if ((type & TYPE_MASK) == G3DB_TAG_ID)
				mesh.id = readString(size);
			else if ((type & TYPE_MASK) == G3DB_TAG_ATTRIBUTES)
				mesh.attributes = readAttributes(size);
			else if ((type & TYPE_MASK) == G3DB_TAG_VERTICES)
				mesh.vertices = readVertices(size);
			else if ((type & TYPE_MASK) == G3DB_TAG_MESHPART)
				parts.add(readMeshPart(size));
			else { // unknown tag, just skip it
				din.skipBytes((int)size);
				Gdx.app.log("G3dbModelLoader", "Unknown mesh tag: "+type+"["+size+"], skipping");
			}
		}
		mesh.parts = new ModelMeshPart[parts.size];
		for (int i = 0; i < parts.size; i++)
			mesh.parts[i] = parts.get(i);
		return mesh;
	}
	
	private ModelMeshPart readMeshPart(final long length) throws IOException {
		final ModelMeshPart result = new ModelMeshPart();
		long cnt = 0;
		while (din.available() > 0 && cnt < length) {
			final byte type = din.readByte();
			final long size = readSize(type);
			cnt += 1 + getSize(type) + size;
			if ((type & TYPE_MASK) == G3DB_TAG_ID)
				result.id = readString(size);
			else if ((type & TYPE_MASK) == G3DB_TAG_TYPE) {
				result.primitiveType = din.readByte();
				if (size > 1)
					din.skipBytes((int)size-1);
			} else if ((type & TYPE_MASK) == G3DB_TAG_INDICES)
				result.indices = readIndices(size);
			else { // unknown tag, just skip it
				din.skipBytes((int)size);
				Gdx.app.log("G3dbModelLoader", "Unknown meshpart tag: "+type+"["+size+"], skipping");
			}
		}
		return result;
	}
	
	private short[] readIndices(final long length) throws IOException {
		final short result[] = new short[(int)length/2];
		for (int i = 0; i < result.length; i++)
			result[i] = din.readShort();
		return result;
	}
	
	private float[] readVertices(final long length) throws IOException {
		final float result[] = new float[(int)length/4];
		for (int i = 0; i < result.length; i++)
			result[i] = din.readFloat();
		return result;
	}
	
	private VertexAttribute[] readAttributes(final long length) throws IOException {
		VertexAttribute result[] = new VertexAttribute[(int)length];
		for (int i = 0; i < length; i++) {
			final byte type = din.readByte();
			if (type == USAGE_POSITION)
				result[i] = VertexAttribute.Position();
			else if (type == USAGE_NORMAL)
				result[i] = VertexAttribute.Normal();
			else if (type == USAGE_COLOR)
				result[i] = VertexAttribute.ColorUnpacked();
			else if (type == USAGE_TANGENT)
				result[i] = VertexAttribute.Tangent();
			else if (type == USAGE_BINORMAL)
				result[i] = VertexAttribute.Binormal();
			else if (type == USAGE_BLENDWEIGHTS)
				result[i] = VertexAttribute.BoneWeights(4);
			else if (type == USAGE_BLENDINDICES)
				result[i] = VertexAttribute.BoneIds(4);
			else if (type >= USAGE_TEXCOORD0 && type <= USAGE_TEXCOORD7)
				result[i] = VertexAttribute.TexCoords(type - USAGE_TEXCOORD0);
			else if (type == USAGE_UNKNOWN)
				;
		}
		return result;
	}
	
	private String readString(final long size) throws IOException {
		final byte data[] = new byte[(int)size];
		din.readFully(data);
		return new String(data, "UTF-8");
	}
	
	private Color readColor(final long size) throws IOException {
		final Color result = new Color(0,0,0,1);
		if (size >= 4) result.r = din.readFloat();
		if (size >= 8) result.g = din.readFloat();
		if (size >= 12) result.b = din.readFloat();
		if (size >= 16) result.a = din.readFloat();
		if (size > 16) din.skipBytes((int)size - 16);
		return result;
	}
	
	private Vector3 readVector3(final long size) throws IOException {
		final Vector3 result = new Vector3();
		if (size >= 4) result.x = din.readFloat();
		if (size >= 8) result.y = din.readFloat();
		if (size >= 12) result.z = din.readFloat();
		if (size > 12) din.skipBytes((int)size - 12);
		return result;
	}
	
	private Vector2 readVector2(final long size) throws IOException {
		final Vector2 result = new Vector2();
		if (size >= 4) result.x = din.readFloat();
		if (size >= 8) result.y = din.readFloat();
		if (size > 8) din.skipBytes((int)size - 8);
		return result;
	}
	
	private Quaternion readQuaternion(final long size) throws IOException {
		final Quaternion result = new Quaternion();
		if (size >= 4) result.x = din.readFloat();
		if (size >= 8) result.y = din.readFloat();
		if (size >= 12) result.z = din.readFloat();
		if (size >= 16) result.w = din.readFloat();
		if (size > 16) din.skipBytes((int)size - 16);
		return result;
	}
	
	private long readSize(final byte tag) throws IOException {
		if ((tag & SIZE_MASK) == SMALL)
			return din.readByte();
		if ((tag & SIZE_MASK) == MEDIUM)
			return din.readShort();
		if ((tag & SIZE_MASK) == LARGE)
			return din.readInt();
		return din.readLong();
	}
	
	private int getSize(final byte tag) {
		if ((tag & SIZE_MASK) == SMALL)
			return 1;
		if ((tag & SIZE_MASK) == MEDIUM)
			return 2;
		if ((tag & SIZE_MASK) == LARGE)
			return 4;
		return 8;
	}
}

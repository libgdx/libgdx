package com.badlogic.gdx.models;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.models.md2.LittleEndianDataInputStream;
import com.badlogic.gdx.models.md2.MD2Frame;
import com.badlogic.gdx.models.md2.MD2Header;
import com.badlogic.gdx.models.md2.MD2Mesh;
import com.badlogic.gdx.models.md2.MD2Triangle;

/**
 * Loads Quake 2 model files without normals.
 * 
 * @author mzechner
 *
 */
public class MD2Loader 
{	
	/**
	 * Loads an {@link MD2Mesh} form the given InputStream.
	 * 
	 * @param app The {@link Application}
	 * @param in The InputSteam
	 * @return The MD2Mesh.
	 */
	public static MD2Mesh load( Application app, InputStream in )
	{
		try
		{
			byte[] bytes = loadBytes( in );
			
			MD2Header header = loadHeader( bytes );
			float[] texCoords = loadTexCoords( header, bytes );
			MD2Triangle[] triangles = loadTriangles( header, bytes );
			MD2Frame[] frames = loadFrames( header, bytes );						
			
			return buildMesh( app, header, triangles, texCoords, frames );
		}
		catch( Exception ex )
		{
			ex.printStackTrace();
			return null;
		}
	}
	
	private static MD2Mesh buildMesh( Application app, MD2Header header, MD2Triangle[] triangles, float[] texCoords, MD2Frame[] frames )
	{
		ArrayList<VTIndex> vertCombos = new ArrayList<VTIndex>( );
		short[] indices = new short[triangles.length * 3];
		int idx = 0;
		short vertIdx = 0;
		for( int i = 0; i < triangles.length; i++ )
		{
			MD2Triangle triangle = triangles[i];
			for( int j = 0; j < 3; j++ )
			{
				VTIndex vert = null;
				boolean contains = false;
				for( int k = 0; k < vertCombos.size(); k++ )					
				{
					VTIndex vIdx = vertCombos.get(k);
					if( vIdx.vIdx == triangle.vertices[j] &&
						vIdx.tIdx == triangle.texCoords[j] )
					{
						vert = vIdx;
						contains = true;
						break;
					}
				}
				if( !contains )
				{
					vert = new VTIndex( triangle.vertices[j], triangle.texCoords[j], vertIdx );
					vertCombos.add( vert );
					vertIdx++;
				}
				
				indices[idx++] = vert.nIdx;
			}
		}		
		
		
		idx = 0;
		float[] uvs = new float[vertCombos.size()*2];		
		for( int i = 0; i < vertCombos.size(); i++ )
		{
			VTIndex vtI = vertCombos.get(i);
			uvs[idx++] = texCoords[vtI.tIdx*2];
			uvs[idx++] = texCoords[vtI.tIdx*2+1];
		}
						
		for( int i = 0; i < frames.length; i++ )
		{
			MD2Frame frame = frames[i];
			idx = 0;
			float[] newVerts = new float[vertCombos.size()*3];
			
			for( int j = 0; j < vertCombos.size(); j++ )
			{
				VTIndex vIdx = vertCombos.get(j);
				newVerts[idx++] = frame.vertices[vIdx.vIdx*3];
				newVerts[idx++] = frame.vertices[vIdx.vIdx*3+1];
				newVerts[idx++] = frame.vertices[vIdx.vIdx*3+2];
			}
			frame.vertices = newVerts;
			ByteBuffer buffer = ByteBuffer.allocateDirect( 4 * frame.vertices.length );
			buffer.order(ByteOrder.nativeOrder());
			FloatBuffer fbuffer = buffer.asFloatBuffer();
			fbuffer.put( frame.vertices );
			frame.verticesBuffer = fbuffer;
		}
		
		header.numVertices = vertCombos.size();
		
		return new MD2Mesh( app, frames, indices, uvs, null );
	}
	
	private static float[] buildTexCoords(MD2Header header, MD2Triangle[] triangles, float[] texCoords) 
	{
		float[] uvs = new float[header.numVertices*2];
		
		for( int i = 0; i < triangles.length; i++ )
		{
			MD2Triangle triangle = triangles[i];
			for( int j = 0; j < 3; j++ )
			{
				int vertIdx = triangle.vertices[j];
				int uvIdx = vertIdx * 2;
				uvs[uvIdx] = texCoords[triangle.texCoords[j] * 2];
				uvs[uvIdx+1] = texCoords[triangle.texCoords[j] * 2+1];
			}
		}
		
		return uvs;
	}

	private static short[] buildIndices(MD2Triangle[] triangles) 
	{
		short[] indices = new short[triangles.length*3];
		
		int idx = 0;
		for( int i = 0; i < triangles.length; i++ )
		{
			MD2Triangle triangle = triangles[i];
			indices[idx++] = triangle.vertices[0];
			indices[idx++] = triangle.vertices[1];
			indices[idx++] = triangle.vertices[2];
		}
		return indices;
	}

	private static MD2Frame[] loadFrames(MD2Header header, byte[] bytes) throws IOException {
		LittleEndianDataInputStream in = new LittleEndianDataInputStream( new ByteArrayInputStream( bytes ) );
		in.skip( header.offsetFrames );
		
		MD2Frame[] frames = new MD2Frame[header.numFrames];
		for( int i = 0; i < header.numFrames; i++ )
		{
			frames[i] = loadFrame( header, in );
		}
		
		in.close();
		
		return frames;
	}

	private static final byte[] charBuffer = new byte[16];
	
	private static MD2Frame loadFrame( MD2Header header, LittleEndianDataInputStream in) throws IOException 
	{	
		MD2Frame frame = new MD2Frame( );
		frame.vertices = new float[header.numVertices*3];
		
		float scaleX = in.readFloat() / 25.0f, scaleY = in.readFloat() / 25.0f, scaleZ = in.readFloat() / 25.0f;
		float transX = in.readFloat() / 25.0f, transY = in.readFloat() / 25.0f, transZ = in.readFloat() / 25.0f;
		in.readFully( charBuffer );
		
		int len = 0;
		for( int i = 0; i < charBuffer.length; i++ )
			if( charBuffer[i] == 0 )
			{
				len = i - 1;
				break;
			}
		
		frame.name = new String(charBuffer, 0, len );
		
		int vertIdx = 0;			
		
		for( int i = 0; i < header.numVertices; i++ )
		{
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

	private static MD2Triangle[] loadTriangles(MD2Header header, byte[] bytes ) throws IOException 
	{
		LittleEndianDataInputStream in = new LittleEndianDataInputStream( new ByteArrayInputStream( bytes ) );
		in.skip( header.offsetTriangles );
		MD2Triangle[] triangles = new MD2Triangle[header.numTriangles];
		
		for( int i = 0; i < header.numTriangles; i++ )
		{
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

	private static float[] loadTexCoords(MD2Header header, byte[] bytes) throws IOException 
	{
		LittleEndianDataInputStream in = new LittleEndianDataInputStream( new ByteArrayInputStream( bytes ) );
		in.skip( header.offsetTexCoords );
		float[] texCoords = new float[header.numTexCoords * 2];
		float width = header.skinWidth;
		float height = header.skinHeight;
		
		for( int i = 0; i < header.numTexCoords * 2; i+=2 )
		{
			short u = in.readShort();
			short v = in.readShort();
			texCoords[i] = u / width;
			texCoords[i+1] = v / height;
		}
		in.close();
		return texCoords;
	}

	private static MD2Header loadHeader( byte[] bytes ) throws IOException
	{
		LittleEndianDataInputStream in = new LittleEndianDataInputStream( new ByteArrayInputStream( bytes ) );
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
	
	private static byte[] loadBytes( InputStream in ) throws IOException
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream( );
		byte[] buffer = new byte[1024];
		
		int readBytes = 0;
		while( (readBytes = in.read( buffer ) ) > 0 )
		{
			out.write( buffer, 0, readBytes );
		}
		
		out.close();
		return out.toByteArray();
	}
	
	public static class VTIndex
	{
		public VTIndex( short vIdx, short tIdx, short nIdx )
		{
			this.vIdx = vIdx;
			this.tIdx = tIdx;
			this.nIdx = nIdx;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + tIdx;
			result = prime * result + vIdx;
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			VTIndex other = (VTIndex) obj;
			if (tIdx != other.tIdx)
				return false;
			if (vIdx != other.vIdx)
				return false;
			return true;
		}
		public short vIdx;
		public short tIdx;
		public short nIdx;				
	}
}

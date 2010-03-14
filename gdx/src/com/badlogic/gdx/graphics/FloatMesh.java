package com.badlogic.gdx.graphics;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class FloatMesh implements Mesh
{				
	/** the number of maximum indices **/
	private final int maxIndices;
	
	/** the vertex size in bytes **/
	private int vertexSize;	
	/** the offset to the colors in bytes **/
	private int colorsOffset;
	/** the offset to the normals in bytes **/
	private int normalsOffset;
	/** the offset to the tex coords in bytes **/
	private int texCoordsOffset;
	
	/** the vertex size in floats **/
	private int vertexSizeFloat;
	
	/** the number of coordinates per vertex **/
	private final int coordsSize;	
	/** the number of color components per vertex **/
	private final int colorsSize;	
	/** the number of texture coordinates per vertex per unit **/
	private final int texCoordsSize;
	
	/** whether this mesh has colors **/
	private final boolean hasColors;
	/** whether this mesh has normals **/
	private final boolean hasNormals;
	/** wheter this mesh has tex coords **/
	private final boolean hasTexCoords;
	/** how many texcoord pairs there are **/
	private final int numTexCoords;
	
	/** the vertices array **/
	private final float[] verticesArray;
	/** the vertices buffer **/
	private final FloatBuffer verticesBuffer;
	
	/** the indices array **/
	private final short[] indicesArray;
	/** the indices buffer **/
	private final ShortBuffer indicesBuffer;	
	
	public FloatMesh( int maxVertices, int coordsSize, 
					  boolean hasColors, int colorsSize,
					  boolean hasNormals, 
					  boolean hasTexCoords,  int numTexCoords, int texCoordSize,
					  boolean hasIndices, int maxIndices )
	{
		if( maxVertices < 0 )
			throw new IllegalArgumentException( "maxVertices must be > 0" );
		if( coordsSize < 2 || coordsSize > 4 )
			throw new IllegalArgumentException( "coordsSize must be >= 2 and <= 4" );
		if( hasColors && ( colorsSize < 3 || colorsSize > 4 ) )
			throw new IllegalArgumentException( "colorSize must be >= 2 and <= 4" );
		if( hasTexCoords && ( texCoordSize < 1 || texCoordSize > 4 ) )
			throw new IllegalArgumentException( "texCoordSize must be >= 1 and <= 4" );
		if( hasIndices && maxIndices < 1 )
			throw new IllegalArgumentException( "maxIndices has to be >= 1" );
		
		this.hasColors = hasColors;
		this.hasNormals = hasNormals;
		this.hasTexCoords = hasTexCoords;
		this.numTexCoords = numTexCoords;		
		
		this.coordsSize = coordsSize;
		this.colorsSize = colorsSize;
		this.texCoordsSize = texCoordSize;
				
		vertexSize += coordsSize * 4;
		colorsOffset += vertexSize;
		normalsOffset += vertexSize;
		texCoordsOffset += vertexSize;
		
		if( hasColors )
		{
			vertexSize += colorsSize * 4;
			normalsOffset += colorsSize * 4;
			texCoordsOffset += colorsSize * 4;
		}
		if( hasNormals)
		{
			vertexSize += 3 * 4;
			texCoordsOffset += 3 * 4;
		}
		if( hasTexCoords )
			vertexSize += texCoordSize * numTexCoords * 4;
		
		vertexSizeFloat = vertexSize / 4;		
		
		verticesArray = new float[vertexSize / 4 * maxVertices];
		ByteBuffer buffer = ByteBuffer.allocateDirect( vertexSize * maxVertices );
		buffer.order(ByteOrder.nativeOrder());
		verticesBuffer = buffer.asFloatBuffer();	
		
		if( hasIndices )
		{
			indicesArray = new short[maxIndices];
			buffer = ByteBuffer.allocateDirect( maxIndices * 2 );
			buffer.order(ByteOrder.nativeOrder());
			indicesBuffer = buffer.asShortBuffer();
			this.maxIndices = maxIndices;
		}
		else
		{
			this.maxIndices = 0;
			indicesArray = null;
			indicesBuffer = null;
		}
	}		
	
	public int getColorsOffset( )
	{
		return colorsOffset;
	}
	
	public int getNormalsOffset( )
	{
		return normalsOffset;
	}
	
	public int getTexCoordsOffset( )
	{
		return texCoordsOffset;
	}
	
	public int getCoordsSize( )
	{
		return coordsSize;
	}
	
	public boolean hasColors( )
	{
		return hasColors;
	}
	
	public int getColorsSize( )
	{
		return colorsSize;
	}
	
	public boolean hasNormals( )
	{
		return hasNormals;
	}
	
	public boolean hasTexCoords( )
	{
		return hasTexCoords;
	}
	
	public boolean hasIndices( )
	{
		return indicesArray != null;
	}
	
	public int getTexCoordsSize( )
	{
		return texCoordsSize;
	}
	
	public int getNumTexCoords( )
	{
		return numTexCoords;
	}
		
	public int getNumVertices( )
	{
		return verticesBuffer.limit() / vertexSizeFloat;
	}
	
	public int getNumIndices( )
	{
		return indicesBuffer.limit();
	}
	
	public int getVertexSize( )
	{
		return vertexSize;
	}
	
	public FloatBuffer getVerticesBuffer()
	{
		return verticesBuffer;
	}
	
	public float[] getVerticesArray( )
	{
		return verticesArray;
	}
	
	public ShortBuffer getIndicesBuffer( )
	{
		return indicesBuffer;
	}
	
	public short[] getIndicesArray( )
	{
		return indicesArray;
	}
	
	public void setVertices( float[] vertices )
	{
		if( vertices.length % (vertexSizeFloat) != 0 )
			throw new IllegalArgumentException( "vertices array must have a size being a multiple of " + vertexSize / 4 );
		if( vertices.length > verticesArray.length )
			throw new IllegalArgumentException( "vertices array is to large to fit into this mesh" );
		
		System.arraycopy( vertices, 0, verticesArray, 0, vertices.length );
		verticesBuffer.position(0);
		verticesBuffer.put( vertices, 0, vertices.length );
		verticesBuffer.flip();		
	}
	
	public void setIndices( short[] indices )
	{
		if( indices.length > maxIndices )
			throw new IllegalArgumentException( "indices length is bigger than maximum indices length" );
		System.arraycopy( indices, 0, indicesArray, 0, indices.length );
		indicesBuffer.position(0);
		indicesBuffer.put(indices);
		indicesBuffer.flip();		
	}
	
	public void updateVertexBufferFromArray( int numVertices )
	{
		verticesBuffer.position(0);
		verticesBuffer.put( verticesArray, 0, numVertices * vertexSize / 4 );
		verticesBuffer.flip();		
	}
	
	public void updateIndexBufferFromArray( int numIndices )
	{
		indicesBuffer.position(0);
		indicesBuffer.put( indicesArray, 0, numIndices );
		indicesBuffer.flip();
	}
	
	public static void main( String[] argv )
	{
		FloatMesh mesh = new FloatMesh( 4, 3, true, 4, true, true, 1, 2, true, 3 );
		float[] vertices = { -0.5f, -0.5f, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 
							  0.5f, -0.5f, 0, 0, 1, 0, 1, 0, 0, 1, 1, 0,
							  0.0f,  0.5f, 0, 0, 0, 1, 1, 0, 0, 1, 0.5f, 1 };
		short[] indices = { 0, 1, 2 };
		mesh.setVertices( vertices );
		mesh.setIndices( indices );
		
		System.out.println( mesh.getNumVertices() );
		System.out.println( mesh.getNumIndices() );
	}
}

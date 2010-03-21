package com.badlogic.gdx.graphics;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

/**
 * A {@link Mesh} implementation that stores all it's attributes as 16.16 fixed point values. Indices
 * are stored as shorts. Vertices and floats are stored in direct Buffers as well as
 * in arrays. If you directly manipulate one of the arrays you have to call one of the
 * methods called {@link updateVerticesBufferFromArray()} and {@link updateIndicesBufferFromArray()}
 * so that the changed vertex and index data is copied over to the direct Buffers. 
 * 
 * @author mzechner
 *
 */
public final class FixedPointMesh implements Mesh
{		
	/** the number of maximum vertices **/
	private final int maxVertices;
	
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
	
	/** the vertex size in ints **/
	private final int vertexSizeInt;
	
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
	private final int[] verticesArray;
	/** the vertices buffer **/
	private final IntBuffer verticesBuffer;
	
	/** the indices array **/
	private final short[] indicesArray;
	/** the indices buffer **/
	private final ShortBuffer indicesBuffer;	
	
	/**
	 * Constructs a new FixedPointMesh with the specified maximum number of 
	 * vertices and indices as well as the specified attributes.
	 * 
	 * @param maxVertices the maximum number of vertices this Mesh can store
	 * @param coordsSize the number of components for the coordinates. Can be 2, 3 or 4
	 * @param hasColors whether the Mesh has colors
	 * @param hasNormals whether the Mesh has normals
	 * @param hasTexCoords whether the Mesh has texture coordinates
	 * @param numTexCoords how many texture coordinate sets the Mesh has
	 * @param texCoordSize the number of components per texture coordinate set. Can be 2, 3 or 4
	 * @param hasIndices whether the Mesh has indices
	 * @param maxIndices the number of maximum indices this Mesh can store
	 */
	public FixedPointMesh( int maxVertices, int coordsSize, 
					  	   boolean hasColors,
					  	   boolean hasNormals, 
					  	   boolean hasTexCoords,  int numTexCoords, int texCoordSize,
					  	   boolean hasIndices, int maxIndices )
	{
		if( maxVertices < 0 )
			throw new IllegalArgumentException( "maxVertices must be > 0" );
		if( coordsSize < 2 || coordsSize > 4 )
			throw new IllegalArgumentException( "coordsSize must be >= 2 and <= 4" );
		if( hasTexCoords && ( texCoordSize < 1 || texCoordSize > 4 ) )
			throw new IllegalArgumentException( "texCoordSize must be >= 1 and <= 4" );
		if( hasIndices && maxIndices < 1 )
			throw new IllegalArgumentException( "maxIndices has to be >= 1" );
		
		this.maxVertices = maxVertices;
		this.hasColors = hasColors;
		this.hasNormals = hasNormals;
		this.hasTexCoords = hasTexCoords;
		this.numTexCoords = numTexCoords;		
		
		this.coordsSize = coordsSize;
		this.colorsSize = 4;
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
		
		vertexSizeInt = vertexSize / 4;		
		
		verticesArray = new int[vertexSize / 4 * maxVertices];
		ByteBuffer buffer = ByteBuffer.allocateDirect( vertexSize * maxVertices );
		buffer.order(ByteOrder.nativeOrder());
		verticesBuffer = buffer.asIntBuffer();	
		
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
	
	/**
	 * {@inheritDoc}
	 */
	public int getColorsOffset( )
	{
		return colorsOffset;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public int getNormalsOffset( )
	{
		return normalsOffset;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public int getTexCoordsOffset( )
	{
		return texCoordsOffset;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public int getCoordsSize( )
	{
		return coordsSize;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean hasColors( )
	{
		return hasColors;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public int getColorsSize( )
	{
		return colorsSize;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean hasNormals( )
	{
		return hasNormals;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean hasTexCoords( )
	{
		return hasTexCoords;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean hasIndices( )
	{
		return indicesArray != null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public int getTexCoordsSize( )
	{
		return texCoordsSize;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public int getNumTexCoords( )
	{
		return numTexCoords;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public int getNumVertices( )
	{
		return verticesBuffer.limit() / vertexSizeInt;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public int getNumIndices( )
	{
		return indicesBuffer.limit();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public int getVertexSize( )
	{
		return vertexSize;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public IntBuffer getVerticesBuffer()
	{
		return verticesBuffer;
	}
	
	/**
	 * @return the underlying array of vertices. 
	 */
	public int[] getVerticesArray( )
	{
		return verticesArray;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public ShortBuffer getIndicesBuffer( )
	{
		return indicesBuffer;
	}
	
	/**
	 * @return the underlying array of indices or null if this Mesh has no indices
	 */
	public short[] getIndicesArray( )
	{
		return indicesArray;
	}
	
	/**
	 * Sets the vertices of this Mesh. The provided array will be copied to both
	 * the underlying vertices array and the direct Buffer.
	 * 
	 * @param vertices the vertices
	 */
	public void setVertices( int[] vertices )
	{
		if( vertices.length % (vertexSizeInt) != 0 )
			throw new IllegalArgumentException( "vertices array must have a size being a multiple of " + vertexSize / 4 );
		if( vertices.length > verticesArray.length )
			throw new IllegalArgumentException( "vertices array is to large to fit into this mesh" );
		
		System.arraycopy( vertices, 0, verticesArray, 0, vertices.length );
		verticesBuffer.position(0);
		verticesBuffer.put( vertices, 0, vertices.length );
		verticesBuffer.flip();		
	}
	
	/**
	 * Sets the vertices of this Mesh. The provided array will be copied to both
	 * the underlying vertices array and the direct Buffer. The floating point 
	 * values will get converted to 16.16 fixed point values internally.
	 * 
	 * @param vertices the vertices
	 */
	public void setVertices( float[] vertices )
	{
		if( vertices.length % (vertexSizeInt) != 0 )
			throw new IllegalArgumentException( "vertices array must have a size being a multiple of " + vertexSize / 4 );
		if( vertices.length > verticesArray.length )
			throw new IllegalArgumentException( "vertices array is to large to fit into this mesh" );
		
		for( int i = 0; i < vertices.length; i++ )
			verticesArray[i] = (int)(vertices[i] * 65536); 
		verticesBuffer.position(0);
		verticesBuffer.put( verticesArray, 0, vertices.length );
		verticesBuffer.flip();		
	}
	
	/**
	 * Sets the indices of this Mesh. The provided array will be copied to both 
	 * the underlying indices array and the direct Buffer.
	 * @param indices the indices
	 */
	public void setIndices( short[] indices )
	{
		if( indices.length > maxIndices )
			throw new IllegalArgumentException( "indices length is bigger than maximum indices length" );
		System.arraycopy( indices, 0, indicesArray, 0, indices.length );
		indicesBuffer.position(0);
		indicesBuffer.put(indices);
		indicesBuffer.flip();
	}
	
	/**
	 * Updates the vertices direct Buffer from the internal vertices array starting
	 * at position 0 using numVertices vertices.
	 * 
	 * @param numVertices the number of vertices to update
	 */
	public void updateVertexBufferFromArray( int numVertices )
	{
		verticesBuffer.position(0);
		verticesBuffer.put( verticesArray, 0, numVertices * vertexSize / 4 );
		verticesBuffer.flip();		
	}
	
	/**
	 * Updates the indices direct Buffer from the internal indices array starting at 
	 * position 0 using numIndices indices.
	 * @param numIndices the number of indices to update
	 */
	public void updateIndexBufferFromArray( int numIndices )
	{
		indicesBuffer.position(0);
		indicesBuffer.put( indicesArray, 0, numIndices );
		indicesBuffer.flip();
	}
	
	public static void main( String[] argv )
	{
		FixedPointMesh mesh = new FixedPointMesh( 4, 3, true, true, true, 1, 2, true, 3 );
		float[] vertices = { -0.5f, -0.5f, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 
							  0.5f, -0.5f, 0, 0, 1, 0, 1, 0, 0, 1, 1, 0,
							  0.0f,  0.5f, 0, 0, 0, 1, 1, 0, 0, 1, 0.5f, 1 };
		short[] indices = { 0, 1, 2 };
		mesh.setVertices( vertices );
		mesh.setIndices( indices );
		
		System.out.println( mesh.getNumVertices() );
		System.out.println( mesh.getNumIndices() );
	}

	@Override
	public int getMaximumVertices() 
	{
		return maxVertices;
	}
}

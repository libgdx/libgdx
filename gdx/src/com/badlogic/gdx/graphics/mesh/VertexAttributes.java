package com.badlogic.gdx.graphics.mesh;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Instances of this class specify the vertex attributes of a mesh.
 * VertexAttributes are used by {@link Mesh} instances to define its vertex structure. Vertex 
 * attributes have an order. The order is specified by the order
 * they are added to this class.
 * 
 * @author mzechner
 *
 */
public class VertexAttributes 
{	
	/**
	 * The usage of a vertex attribute.
	 * 
	 * @author mzechner
	 *
	 */
	public enum Usage
	{
		Position,
		Color,
		Normal,
		TextureCoordinates,
		Generic
	}
	
	/** the attributes in the order they were specified **/
	private final List<VertexAttribute> attributes;
	
	/** the size of a single vertex in bytes **/
	public final int vertexSize;
	
	/**
	 * Constructor, sets the vertex attributes in a specific order 
	 */
	public VertexAttributes( VertexAttribute ... attributes )
	{
		if( attributes.length == 0 )
			throw new IllegalArgumentException( "attributes must be >= 1" );
		
		ArrayList<VertexAttribute> list = new ArrayList<VertexAttribute>();
		for( int i = 0; i < attributes.length; i++ )
			list.add( attributes[i] );
		
		this.attributes = Collections.unmodifiableList( list );
		
		checkValidity( );
		vertexSize = calculateOffsets( );
	}
	
	private int calculateOffsets( )
	{
		int count = 0;
		for( int i = 0; i < attributes.size(); i++ )
		{
			VertexAttribute attribute = attributes.get(i);
			attribute.offset = count;
			count += 4 * attribute.numComponents;
		}
		
		return count;
	}
	
	private void checkValidity( )
	{
		boolean pos = false;
		boolean cols = false;
		boolean nors = false;
		
		for( int i = 0; i < attributes.size(); i++ )
		{
			VertexAttribute attribute = attributes.get(i);
			if( attribute.usage == Usage.Position )
			{
				if( pos )
					throw new IllegalArgumentException( "two position attributes were specified" );
				pos = true;
			}
			
			if( attribute.usage == Usage.Normal )
			{
				if( nors )
					throw new IllegalArgumentException( "two normal attributes were specified" );
			}
			
			if( attribute.usage == Usage.Color )
			{
				if( cols )
					throw new IllegalArgumentException( "two color attributes were specified" );
				cols = true;
			}
		}
	}
	
	/**
	 * @return the number of attributes
	 */
	public int size()
	{
		return attributes.size();
	}
	
	/**
	 * @param index the index
	 * @return the VertexAttribute at the given index
	 */
	public VertexAttribute get( int index )
	{
		return attributes.get(index);
	}
}

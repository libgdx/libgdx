package com.badlogic.gdx.graphics;

import java.nio.Buffer;

public interface Mesh 
{
	public Buffer getVerticesBuffer( );
	
	public Buffer getIndicesBuffer( );
	
	public int getNumVertices( );
	
	public int getVertexSize( );
	
	public int getNumIndices( );
	
	public boolean hasColors( );
	
	public boolean hasNormals( );
	
	public boolean hasTexCoords( );
	
	public boolean hasIndices( );
	
	public int getCoordsSize( );
	
	public int getColorsSize( );
	
	public int getTexCoordsSize( );
	
	public int getNumTexCoords( );
	
	public int getColorsOffset( );
	
	public int getNormalsOffset( );
	
	public int getTexCoordsOffset( );
}

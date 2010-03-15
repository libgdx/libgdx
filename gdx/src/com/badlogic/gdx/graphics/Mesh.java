package com.badlogic.gdx.graphics;

import java.nio.Buffer;

/**
 * Interface for Mesh classes. A Mesh holds vertex and index data for some geometry.
 * Vertices are composed of coordinates, colors, normals and (multiple) texture coordinates.
 * Vertices must not have all of these attributes except the coordinates attribute. A Mesh
 * must also not have indices. Vertices and indices are stored in direct buffers internally and can be queried. Meshes can be
 * rendered via a {@link MeshRenderer}.  All vertex attributes are interleaved in a single
 * direct buffer. The layout for a single vertex is: coordinates, colors, normals, texture coordinates in this
 * order. If a certain attribute is not present in the mesh it is of course ommited from each 
 * vertex. As an example take a mesh that has coordinates and a single texture coordinate pair then the
 * layout would be coordinates, texture coordinates. Colors are stored as 4-component colors as r, g, b, a
 * quadruples. Normals are stored as 3-component vectors. Coordinates and texture coordinate sizes can
 * be specified at construction time of the implementations of this interface.
 * 
 * @see FloatMesh
 * @see FixedPointMesh
 * 
 * @author mzechner
 *
 */
public interface Mesh 
{
	/**
	 * @return the underlying interleaved direct Buffer holding the vertex data
	 */
	public Buffer getVerticesBuffer( );
	
	/**
	 * @return the underlying direct Buffer holding the indices or null if no indices where given.
	 */
	public Buffer getIndicesBuffer( );
	
	/**
	 * @return the number of currently defined vertices. Equals getVerticesBuffer().Remaining() / getVertexSize().
	 */
	public int getNumVertices( );
	
	/**
	 * @return the number of bytes per vertex
	 */
	public int getVertexSize( );
	
	/**
	 * @return the number of currently defined indices. Equals getIndicesBuffer().getRemaining() / 2.
	 */
	public int getNumIndices( );
	
	/**
	 * @return whether the mesh has colors
	 */
	public boolean hasColors( );
	
	/**
	 * @return whether the mesh has normals
	 */
	public boolean hasNormals( );
	
	/**
	 * @return whether themesh has texture coordinates
	 */
	public boolean hasTexCoords( );
	
	/**
	 * @return whether the mesh has indices
	 */
	public boolean hasIndices( );
	
	/**
	 * @return the size of the vertex coordinates per vertex in components. A component being a coordinate here.
	 */
	public int getCoordsSize( );
	
	/**
	 * @return the size of the color attributes per vertex in components. A component being a color channel here.
	 */
	public int getColorsSize( );
	
	/**
	 * @return the size of a single texture coordinate pair per vertex in components. A component being a texture coordinate here.
	 */
	public int getTexCoordsSize( );
	
	/**
	 * @return the number of texture coordinate pairs. 
	 */
	public int getNumTexCoords( );
	
	/**
	 * @return the offset of the color attribute of the first vertex in bytes.
	 */
	public int getColorsOffset( );
	
	/**
	 * @return the offset of the normal attribute of the first vertex in bytes.
	 */
	public int getNormalsOffset( );
	
	/**
	 * @return the offset of the first texture coordinate of the first vertex in bytes.
	 */
	public int getTexCoordsOffset( );
}

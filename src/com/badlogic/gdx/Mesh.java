package com.badlogic.gdx;

/**
 * A Mesh, either indexed or non-indexed. A mesh contains
 * vertices composed of positions, normals, colors and texture 
 * coordinates. Additionally it can have indices which refer to
 * those vertices for optimization. To define a mesh various
 * methods are provided which operate much like their opengl 
 * immediate mode counter parts. Use normal, color and texture coordinate
 * methods before you define a vertex. 
 * 
 * @author mzechner
 *
 */
public interface Mesh 
{
	/**
	 * Enum of supported rendering primitives.
	 * 
	 * @author mzechner
	 *
	 */
	public enum PrimitiveType
	{
		Points,
		Lines,
		Triangles,
		LineStrip,
		TriangleStrip,
		TriangleFan
	}
	
	/**
	 * Define a vertex.
	 * 
	 * @param x The x coordinate
	 * @param y The y coordinate
	 * @param z The z coordinate
	 */
	public void vertex( float x, float y, float z );
	
	/**
	 * Define a vertices color. All components must be
	 * in the range [0,1].
	 * 
	 * @param r The red component
	 * @param g The green component
	 * @param b The blue component
	 * @param a The alpha component
	 */
	public void color( float r, float g, float b, float a );
	
	/**
	 * Define a vertices texture coordinates. 
	 * 
	 * @param u The u component
	 * @param v The v component
	 */
	public void texCoord( float u, float v );
	
	/**
	 * Define a vertices normal. The normal must be
	 * normalized to unit length.
	 * 
	 * @param nx The x component
	 * @param ny The y component
	 * @param nz The z component
	 */
	public void normal( float nx, float ny, float nz );
	
	/**
	 * Define the meshes indices. This can be called
	 * multiple times, e.g. for each face of the mesh.
	 * 
	 * @param indices The indices
	 */
	public void index( int ... indices );
	
	/**
	 * Renders the mesh using the given primitive type.
	 * @param type The primitive type	
	 * @param numVertices The number of vertices to render
	 * @param offset if indexed then this is the offset into the index array, otherwise it is the offset into the vertex array
	 */
	public void render( PrimitiveType type, int numVertices, int offset );	

	/**
	 * Renders the mesh using as many vertices/indices as where defined
	 * @param type The primitive Type
	 */
	public void render( PrimitiveType type );
	
	/**
	 * Releases all resources of the mesh.
	 */
	public void dispose();

	/**
	 * resets the vertex pointer
	 */
	public void reset();

	/**
	 * 	@return The maximum number of vertices
	 */
	public int getMaximumVertices();
	
	/**
	 * @return the vertex array
	 */
	public float[] getVertices( );
	
	/**
	 * @return the normals array
	 */
	public float[] getNormals( );
	
	/**
	 * @return the uv array
	 */
	public float[] getUV( );
	
	/**
	 * @return the color array
	 */
	public float[] getColors( );
	
	/**
	 * @return the index array
	 */
	public short[] getIndices( );
	
	/**
	 * sets the mesh dirty so it will
	 * be updated on the next render call
	 */
	public void setDirty( );
}

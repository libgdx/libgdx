package com.badlogic.gdx.graphics;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Graphics.GraphicsType;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;

/**
 * A Mesh holds vertices composed of attributes specified by a 
 * {@link VertexAttributes} instance. The vertices are held in
 * 
 * @author mzechner
 *
 * 
 */
public class Mesh 
{
	/** list of all meshes **/
	private static final ArrayList<Mesh> meshes = new ArrayList<Mesh>( );
	
	/** the vertex attributes **/
	private final VertexAttributes attributes;
	
	/** the maximum number of vertices **/
	private final int maxVertices;
	
	/** the maximum number of indices **/
	private final int maxIndices;
	
	/** the direct byte buffer that holds the vertices **/
	private final Buffer vertices;
	
	/** a view of the vertices buffer for manipulating floats **/
	private final FloatBuffer verticesFloat;
	
	/** a view of the vertices buffer for manipulating fixed point values **/
	private final IntBuffer verticesFixed;
	
	/** the direct short buffer that holds the indices **/
	private final ShortBuffer indices;
	
	/** the VBO handle **/
	private int vertexBufferObjectHandle;
	
	/** the IBO handle **/
	private int indexBufferObjectHandle;
	
	/** dirty flag **/
	private boolean dirty = false;
	
	/** the graphics object **/
	private final Graphics graphics;
	
	/** managed? **/
	private final boolean managed;
	
	/** static? **/
	private final boolean isStatic;
	
	/** fixed point? **/
	private final boolean useFixedPoint;
	
	/** whether direct buffers are used **/
	private final boolean usesDirectBuffers;	
	
	/** whether this mesh was invalidated due to a context loss **/
	private boolean invalidated = false;
	
	/** whether attempted to create buffer the first time**/
	private boolean bufferCreatedFirstTime = false;
	
	/**
	 * Creates a new Mesh with the given attributes
	 * 
	 * @param graphics the graphics instance
	 * @param managed whether this mesh should be managed or not.
	 * @param useFixedPoint whether to use fixed point or floats
	 * @param maxVertices the maximum number of vertices this mesh can hold
	 * @param maxIndices the maximum number of indices this mesh can hold
	 * @param attributes the {@link VertexAttribute}s.
	 */
	public Mesh( Graphics graphics, boolean managed, boolean isStatic, boolean useFixedPoint, int maxVertices, int maxIndices, VertexAttribute ... attributes )
	{
		this.graphics = graphics;
		this.managed = managed;
		this.isStatic = isStatic;
		this.useFixedPoint = useFixedPoint;
		this.maxVertices = maxVertices;
		this.maxIndices = maxIndices;
		this.attributes = new VertexAttributes( attributes );
		
		if( graphics.getType() == GraphicsType.JoglGL )
			usesDirectBuffers = true;
		else
		{
			if( graphics.isGL11Available() || graphics.isGL20Available() )
				usesDirectBuffers = false;
			else
				usesDirectBuffers = true;
		}
		
		if( usesDirectBuffers == false )
		{
			vertices = useFixedPoint?IntBuffer.wrap( new int[maxVertices * this.attributes.vertexSize/4] ):FloatBuffer.wrap( new float[maxVertices * this.attributes.vertexSize/4] );
			if( useFixedPoint )
			{
				verticesFixed = (IntBuffer)vertices;
				verticesFloat = null;
			}
			else
			{
				verticesFloat = (FloatBuffer)vertices;
				verticesFixed = null;
			}
			indices = ShortBuffer.wrap( new short[maxIndices] );
		}
		else
		{
			ByteBuffer buffer = ByteBuffer.allocateDirect( maxVertices * this.attributes.vertexSize );
			buffer.order(ByteOrder.nativeOrder());
			vertices = buffer;
			verticesFixed = buffer.asIntBuffer();
			verticesFloat = buffer.asFloatBuffer();
			buffer = ByteBuffer.allocateDirect( maxIndices * 2 );
			buffer.order( ByteOrder.nativeOrder() );
			indices = buffer.asShortBuffer();
		}									
		
		bufferCreatedFirstTime = false;
		if( managed )
			meshes.add( this );
	}
	
	private void createBuffers( )
	{
		// FIXME this is a hack as there's no way to support fixed point VBOs
		if( useFixedPoint && graphics.getType() == GraphicsType.JoglGL)
			return;
		
		if( graphics.isGL11Available() == false && graphics.isGL20Available() == false )
			return;
		
		if( graphics.isGL20Available() )
			constructBufferObjects( graphics.getGL20() );
		else
			constructBufferObjects( graphics.getGL11() );
	}
	
	private void constructBufferObjects( GL11 gl )
	{
		ByteBuffer tmp = ByteBuffer.allocateDirect( 4 );
		tmp.order( ByteOrder.nativeOrder() );
		IntBuffer handle = tmp.asIntBuffer();
		
		gl.glGenBuffers( 1, handle );
		vertexBufferObjectHandle = handle.get(0);		
		int oldLimit = vertices.limit();
		int oldPosition = vertices.position();
		vertices.position(0);
		vertices.limit(vertices.capacity());		
		gl.glBindBuffer( GL11.GL_ARRAY_BUFFER, vertexBufferObjectHandle );
		gl.glBufferData( GL11.GL_ARRAY_BUFFER, getNumVertices() * attributes.vertexSize, vertices, isStatic?GL11.GL_STATIC_DRAW:GL11.GL_DYNAMIC_DRAW );
		gl.glBindBuffer( GL11.GL_ARRAY_BUFFER, 0 );
		vertices.position(oldPosition);
		vertices.limit(oldLimit);
		
		if( maxIndices > 0 )
		{
			gl.glGenBuffers( 1, handle );
			indexBufferObjectHandle = handle.get(0);			
			oldPosition = indices.position();
			oldLimit = indices.limit();
			indices.position(0);
			indices.limit(indices.capacity());
			gl.glBindBuffer( GL11.GL_ELEMENT_ARRAY_BUFFER, indexBufferObjectHandle );
			gl.glBufferData( GL11.GL_ELEMENT_ARRAY_BUFFER, indices.limit() * 2, indices, isStatic?GL11.GL_STATIC_DRAW:GL11.GL_DYNAMIC_DRAW );
			gl.glBindBuffer( GL11.GL_ELEMENT_ARRAY_BUFFER, 0 );
			indices.position(oldPosition);
			indices.limit(oldLimit);
		}
		
		dirty = false;
	}
	
	private void constructBufferObjects( GL20 gl )
	{
		ByteBuffer tmp = ByteBuffer.allocateDirect( 4 );
		tmp.order( ByteOrder.nativeOrder() );
		IntBuffer handle = tmp.asIntBuffer();
		
		gl.glGenBuffers( 1, handle );
		vertexBufferObjectHandle = handle.get(0);		
		int oldLimit = vertices.limit();
		int oldPosition = vertices.position();
		vertices.position(0);
		vertices.limit(vertices.capacity());
		gl.glBindBuffer( GL20.GL_ARRAY_BUFFER, vertexBufferObjectHandle );
		gl.glBufferData( GL20.GL_ARRAY_BUFFER, getNumVertices() * attributes.vertexSize, vertices, isStatic?GL20.GL_STATIC_DRAW:GL20.GL_DYNAMIC_DRAW );
		gl.glBindBuffer( GL20.GL_ARRAY_BUFFER, 0 );
		vertices.position(oldPosition);
		vertices.limit(oldLimit);
		
		if( maxIndices > 0 )
		{
			gl.glGenBuffers( 1, handle );
			indexBufferObjectHandle = handle.get(0);		
			oldPosition = indices.position();
			oldLimit = indices.limit();
			indices.position(0);
			indices.limit(indices.capacity());
			gl.glBindBuffer( GL11.GL_ELEMENT_ARRAY_BUFFER, indexBufferObjectHandle );
			gl.glBufferData( GL11.GL_ELEMENT_ARRAY_BUFFER, indices.limit() * 2, indices, isStatic?GL20.GL_STATIC_DRAW:GL20.GL_DYNAMIC_DRAW );
			gl.glBindBuffer( GL11.GL_ELEMENT_ARRAY_BUFFER, 0 );
			indices.position(oldPosition);
			indices.limit(oldLimit);
		}
		
		dirty = false;		
	}
	
	private void fillBuffers( )
	{
		dirty = false;
		if( graphics.isGL11Available() == false && graphics.isGL20Available() == false )
			return;
		
		if( graphics.isGL20Available() )
			fillBuffers( graphics.getGL20() );
		else
			fillBuffers( graphics.getGL11() );			
	}
	
	private void fillBuffers( GL11 gl )
	{							
		gl.glBindBuffer( GL11.GL_ARRAY_BUFFER, vertexBufferObjectHandle );
		gl.glBufferData( GL11.GL_ARRAY_BUFFER, getNumVertices() * attributes.vertexSize, vertices, isStatic?GL11.GL_STATIC_DRAW:GL11.GL_DYNAMIC_DRAW);
		// FIXME FUCK YOU QUALCOMM, your glBufferSubData is the slowest shit on earth...
//		gl.glBufferSubData( GL11.GL_ARRAY_BUFFER, 0, getNumVertices() * attributes.vertexSize, vertices );
		gl.glBindBuffer( GL11.GL_ARRAY_BUFFER, 0 );
		
		if( maxIndices > 0 )
		{
			gl.glBindBuffer( GL11.GL_ELEMENT_ARRAY_BUFFER, indexBufferObjectHandle );
			gl.glBufferSubData( GL11.GL_ELEMENT_ARRAY_BUFFER, 0, indices.limit() * 2, indices );
			gl.glBindBuffer( GL11.GL_ELEMENT_ARRAY_BUFFER, 0 );
		}
	}
	
	private void fillBuffers( GL20 gl )
	{				
		gl.glBindBuffer( GL20.GL_ARRAY_BUFFER, vertexBufferObjectHandle );
		gl.glBufferSubData( GL20.GL_ARRAY_BUFFER, 0, getNumVertices() * attributes.vertexSize, vertices );
		gl.glBindBuffer( GL20.GL_ARRAY_BUFFER, 0 );
		
		if( maxIndices > 0 )
		{
			gl.glBindBuffer( GL20.GL_ELEMENT_ARRAY_BUFFER, indexBufferObjectHandle );
			gl.glBufferSubData( GL20.GL_ELEMENT_ARRAY_BUFFER, 0, indices.limit() * 2, indices );
			gl.glBindBuffer( GL20.GL_ELEMENT_ARRAY_BUFFER, 0 );
		}
	}
	
	/**
	 * Sets the vertices of this Mesh. The attributes are assumed to be given
	 * in float format. If this mesh is configured to use fixed point an
	 * IllegalArgumentException will be thrown.
	 * 
	 * @param vertices the vertices.
	 */
	public void setVertices( float[] vertices )
	{
		if( useFixedPoint )
			throw new IllegalArgumentException( "can't set float vertices for fixed point mesh" );
				
		verticesFloat.clear();			
		verticesFloat.put( vertices );			
		verticesFloat.limit(vertices.length);			
		verticesFloat.position(0);		
		
		if( usesDirectBuffers )
		{ 			
			this.vertices.limit(verticesFloat.limit()*4);									
			this.vertices.position(0);			
		}
		dirty = true;
	}
	
	/**
	 * Sets the vertices of this Mesh. The attributes are assumed to be given
	 * in float format. If this mesh is configured to use fixed point an
	 * IllegalArgumentException will be thrown.
	 * 
	 * @param vertices the vertices.
	 * @param offset the offset into the vertices array
	 * @param count the number of floats to use
	 */
	public void setVertices(float[] vertices, int offset, int count) 
	{	
		if( useFixedPoint )
			throw new IllegalArgumentException( "can't set float vertices for fixed point mesh" );
		
		verticesFloat.clear();
		verticesFloat.put( vertices, offset, count );
		verticesFloat.limit( count );
		verticesFloat.position(0);
		if( usesDirectBuffers )
		{
			this.vertices.limit(verticesFloat.limit()*4);
			this.vertices.position(0);
		}
		dirty = true;
	}
	
	/**
	 * Sets the vertices of this Mesh. The attributes are assumed to be given
	 * in fixed point format. If this mesh is configured to use floats an
	 * IllegalArgumentException will be thrown.
	 * 
	 * @param vertices the vertices.
	 */
	public void setVertices( int[] vertices )
	{
		if( !useFixedPoint )
			throw new IllegalArgumentException( "can't set fixed point vertices for float mesh" );
		
		verticesFixed.clear();
		verticesFixed.put( vertices );
		verticesFixed.limit( vertices.length );
		verticesFixed.position(0);
		if( usesDirectBuffers )
		{
			this.vertices.limit(verticesFixed.limit()*4);
			this.vertices.position(0);
		}
		dirty = true;
	}
	
	/**
	 * Sets the vertices of this Mesh. The attributes are assumed to be given
	 * in fixed point format. If this mesh is configured to use floats an
	 * IllegalArgumentException will be thrown.
	 * 
	 * @param vertices the vertices.
	 * @param offset the offset into the vertices array
	 * @param count the number of floats to use
	 */
	public void setVertices( int[] vertices, int offset, int count )
	{
		if( !useFixedPoint )
			throw new IllegalArgumentException( "can't set fixed point vertices for float mesh" );
		
		verticesFixed.clear();
		verticesFixed.put( vertices, offset, count );
		verticesFixed.limit(count);
		verticesFixed.position(0);
		if( usesDirectBuffers )
		{
			this.vertices.limit(verticesFixed.limit()*4);
			this.vertices.position(0);
		}
		dirty = true;
	}
	
	/**
	 * Sets the indices of this Mesh
	 * 
	 * @param indices the indices
	 */
	public void setIndices( short[] indices )
	{	
		this.indices.put( indices );
		this.indices.position(0);
		dirty = true;
	}
	
	/**
	 * @return the number of defined indices
	 */
	public int getNumIndices( )
	{
		return indices.limit();
	}
	
	/**
	 * @return the number of defined vertices
	 */
	public int getNumVertices( )
	{
		return vertices.limit() / (attributes.vertexSize / (usesDirectBuffers?1:4));
	}
	
	/**
	 * @return the size of a single vertex in bytes
	 */
	public int getVertexSize( )
	{
		return attributes.vertexSize;
	}
	
	/**
	 * Renders the mesh using the given primitive type. If indices
	 * are set for this mesh then getNumIndices() / #vertices per primitive
	 * primitives are rendered. If no indices are set then getNumVertices() / #vertices per primitive are rendered.
	 * 
	 * This method is intended for use with OpenGL ES 1.x and will throw an IllegalStateException when OpenGL ES 2.0 is used.
	 * 
	 * @param primitiveType the primitive type
	 */
	public void render( int primitiveType )
	{	
		render( primitiveType, 0, maxIndices > 0? getNumIndices(): getNumVertices() );
	}
	
	/**
	 * Renders the mesh using the given primitive type. offset specifies the
	 * offset into either the vertex buffer or the index buffer depending on
	 * whether indices are defined. count specifies the number of 
	 * vertices or indices to use thus count / #vertices per primitive primitives
	 * are rendered.
	 * 
	 * This method is intended for use with OpenGL ES 1.x and will throw an IllegalStateException when OpenGL ES 2.0 is used.
	 * 
	 * @param primitiveType the primitive type
	 * @param offset the offset into the vertex or index buffer
	 * @param count number of vertices or indices to use
	 */
	public void render( int primitiveType, int offset, int count )
	{
		if( graphics.isGL20Available() )
			throw new IllegalStateException( "can't use this render method with OpenGL ES 2.0" );
		
		checkManagedAndDirty();
		
		if( vertexBufferObjectHandle != 0 )
			renderVBO( primitiveType, offset, count );
		else
			renderVA( primitiveType, offset, count );
	}
	
	private void renderVBO( int primitiveType, int offset, int count )
	{
		GL11 gl = graphics.getGL11();
		gl.glBindBuffer( GL11.GL_ARRAY_BUFFER, vertexBufferObjectHandle );
		
		int numAttributes = attributes.size();
		int type = useFixedPoint?GL11.GL_FIXED:GL11.GL_FLOAT;
		int textureUnit = 0;
		
		for( int i = 0; i < numAttributes; i++ )
		{
			VertexAttribute attribute = attributes.get( i );
			if( attribute.usage == Usage.Position )
			{
				gl.glEnableClientState( GL11.GL_VERTEX_ARRAY );
				gl.glVertexPointer( attribute.numComponents, type, attributes.vertexSize, attribute.offset );
				continue;
			}
			
			if( attribute.usage == Usage.Color )
			{
				gl.glEnableClientState( GL11.GL_COLOR_ARRAY );
				gl.glColorPointer( attribute.numComponents, type, attributes.vertexSize, attribute.offset );
				continue;
			}
			
			if( attribute.usage == Usage.Normal )
			{
				gl.glEnableClientState( GL11.GL_NORMAL_ARRAY );
				gl.glNormalPointer( type, attributes.vertexSize, attribute.offset );
				continue;
			}
			
			if( attribute.usage == Usage.TextureCoordinates )
			{
				//gl.glClientActiveTexture( GL11.GL_TEXTURE0 + textureUnit );
				gl.glEnableClientState( GL11.GL_TEXTURE_COORD_ARRAY );
				gl.glTexCoordPointer( attribute.numComponents, type, attributes.vertexSize, attribute.offset );
				textureUnit++;
				continue;
			}
		}
		
		if( maxIndices > 0 )
		{			
			gl.glBindBuffer( GL11.GL_ELEMENT_ARRAY_BUFFER, indexBufferObjectHandle );
			gl.glDrawElements( primitiveType, count, GL10.GL_UNSIGNED_SHORT, offset * 2 );
		}	
		else
		{
			gl.glDrawArrays( primitiveType, offset, count);
		}
		
		textureUnit--;
		
		for( int i = 0; i < numAttributes; i++ )
		{
			VertexAttribute attribute = attributes.get( i );
			if( attribute.usage == Usage.Color )
				gl.glDisableClientState( GL11.GL_COLOR_ARRAY );
			if( attribute.usage == Usage.Normal )
				gl.glDisableClientState( GL11.GL_NORMAL_ARRAY );
			if( attribute.usage == Usage.TextureCoordinates )
			{
				//gl.glClientActiveTexture( GL11.GL_TEXTURE0 + textureUnit );
				gl.glDisableClientState( GL11.GL_TEXTURE_COORD_ARRAY );
				textureUnit--;
			}		
		}
			
		gl.glBindBuffer( GL20.GL_ARRAY_BUFFER, 0 );
		if( maxIndices > 0 )
			gl.glBindBuffer( GL20.GL_ELEMENT_ARRAY_BUFFER, 0 );
	}
	
	private void renderVA( int primitiveType, int offset, int count )
	{
		GL10 gl = graphics.getGL10();
		
		int numAttributes = attributes.size();
		int type = useFixedPoint?GL11.GL_FIXED:GL11.GL_FLOAT;
		int textureUnit = 0;
		
		for( int i = 0; i < numAttributes; i++ )
		{
			VertexAttribute attribute = attributes.get( i );
			if( attribute.usage == Usage.Position )
			{
				gl.glEnableClientState( GL11.GL_VERTEX_ARRAY );
				vertices.position( attribute.offset );
				gl.glVertexPointer( attribute.numComponents, type, attributes.vertexSize, vertices );
				continue;
			}
			
			if( attribute.usage == Usage.Color )
			{
				gl.glEnableClientState( GL11.GL_COLOR_ARRAY );
				vertices.position( attribute.offset );
				gl.glColorPointer( attribute.numComponents, type, attributes.vertexSize, vertices );
				continue;
			}
			
			if( attribute.usage == Usage.Normal )
			{
				gl.glEnableClientState( GL11.GL_NORMAL_ARRAY );
				vertices.position( attribute.offset );
				gl.glNormalPointer( type, attributes.vertexSize, vertices );
				continue;
			}
			
			if( attribute.usage == Usage.TextureCoordinates )
			{
				//gl.glClientActiveTexture( GL11.GL_TEXTURE0 + textureUnit );
				gl.glEnableClientState( GL11.GL_TEXTURE_COORD_ARRAY );
				vertices.position( attribute.offset );
				gl.glTexCoordPointer( attribute.numComponents, type, attributes.vertexSize, vertices );
				textureUnit++;
				continue;
			}
		}
		
		if( maxIndices > 0 )
			gl.glDrawElements( primitiveType, count, GL10.GL_UNSIGNED_SHORT, indices );
		else
			gl.glDrawArrays( primitiveType, offset, count);
		
		textureUnit--;
		
		for( int i = 0; i < numAttributes; i++ )
		{
			VertexAttribute attribute = attributes.get( i );
			if( attribute.usage == Usage.Color )
				gl.glDisableClientState( GL11.GL_COLOR_ARRAY );
			if( attribute.usage == Usage.Normal )
				gl.glDisableClientState( GL11.GL_NORMAL_ARRAY );
			if( attribute.usage == Usage.TextureCoordinates )
			{
				//gl.glClientActiveTexture( GL11.GL_TEXTURE0 + textureUnit );
				gl.glDisableClientState( GL11.GL_TEXTURE_COORD_ARRAY );
				textureUnit--;
			}		
		}
		
		vertices.position(0);
	}
	
	public void render( ShaderProgram shader, int primitiveType )
	{
		render( shader, primitiveType, 0, maxIndices > 0? getNumIndices(): getNumVertices() );
	}
	
	public void render( ShaderProgram shader, int primitiveType, int offset, int count )
	{
		if( !graphics.isGL20Available() )
			throw new IllegalStateException( "can't use this render method with OpenGL ES 1.x" );
		
		checkManagedAndDirty();
		
		GL20 gl = graphics.getGL20();
		gl.glBindBuffer( GL11.GL_ARRAY_BUFFER, vertexBufferObjectHandle );
		
		int numAttributes = attributes.size();
		int type = useFixedPoint?GL11.GL_FIXED:GL11.GL_FLOAT;
		int textureUnit = 0;
		
		for( int i = 0; i < numAttributes; i++ )
		{
			VertexAttribute attribute = attributes.get( i );
			shader.enableVertexAttribute( attribute.alias );
			shader.setVertexAttribute( attribute.alias, attribute.numComponents, type, false, attributes.vertexSize, attribute.offset );
		}
		
		if( maxIndices > 0 )
		{			
			gl.glBindBuffer( GL11.GL_ELEMENT_ARRAY_BUFFER, indexBufferObjectHandle );
			gl.glDrawElements( primitiveType, count, GL10.GL_UNSIGNED_SHORT, offset * 2 );
		}	
		else
		{
			gl.glDrawArrays( primitiveType, offset, count);
		}
		
		textureUnit--;
		
		for( int i = 0; i < numAttributes; i++ )
		{
			VertexAttribute attribute = attributes.get( i );
			shader.disableVertexAttribute( attribute.alias );
		}
		
		gl.glBindBuffer( GL20.GL_ARRAY_BUFFER, 0 );
		if( maxIndices > 0 )
			gl.glBindBuffer( GL20.GL_ELEMENT_ARRAY_BUFFER, 0 );
	}	
	
	private void checkManagedAndDirty( )
	{
		if( ! bufferCreatedFirstTime ){
			createBuffers();
			bufferCreatedFirstTime = true;
		}
		
		if(vertexBufferObjectHandle == 0 )
			return;
		
		if( managed && invalidated || !bufferCreatedFirstTime)
		{
			if( graphics.isGL11Available() )
			{
				createBuffers();
				fillBuffers( );
			}
			if( graphics.isGL20Available() )
			{
				createBuffers();
				fillBuffers( );
			}			
		}
		
		
		invalidated = false;
		if( dirty )
			fillBuffers( );
	}
	
	/**
	 * Frees all resources associated with this Mesh
	 */
	public void dispose( )
	{
		meshes.remove(this);
		
		if( graphics.isGL11Available() == false && graphics.isGL20Available() == false )
			return;
		
		if( graphics.isGL20Available() )
			dispose( graphics.getGL20() );
		else
			dispose( graphics.getGL11() );			
	}
	
	private void dispose( GL11 gl )
	{
		int handle[] = new int[1];
		handle[0] = vertexBufferObjectHandle;
		gl.glDeleteBuffers( 1, handle, 0 );
		
		if( maxIndices > 0 )
		{
			handle[0] = indexBufferObjectHandle;
			gl.glDeleteBuffers( 1, handle, 0 );
		}
	}
	
	private void dispose( GL20 gl )
	{
		ByteBuffer tmp = ByteBuffer.allocateDirect( 4 );
		tmp.order( ByteOrder.nativeOrder() );
		IntBuffer handle = tmp.asIntBuffer();
		handle.put( vertexBufferObjectHandle );
		handle.position(0);
		gl.glDeleteBuffers( 1, handle );
		
		if( maxIndices > 0 )
		{
			handle.clear();
			handle.put( indexBufferObjectHandle );
			handle.position(0);
			gl.glDeleteBuffers( 1, handle );
		}
	}
	
	/**
	 * @return whether Q16 fixed point is used
	 */
	public boolean usesFixedPoint( )
	{
		return useFixedPoint;
	}
	
	/**
	 * @return the maximum number of vertices this mesh can hold
	 */
	public int getMaxVertices( )
	{
		return maxVertices;
	}
	
	/**
	 * @return the maximum number of indices this mesh can hold
	 */
	public int getMaxIndices( )
	{
		return maxIndices;		
	}

	/**
	 * Returns the first {@link VertexAttribute} having the given
	 * {@link Usage}.
	 * 
	 * @param usage the Usage.
	 * @return the VertexAttribute or null if no attribute with that usage was found.
	 */
	public VertexAttribute getVertexAttribute(int usage) 
	{	
		for( int i = 0; i < attributes.size(); i++ )
			if( attributes.get(i).usage == usage )
				return attributes.get(i);
		
		return null;
	}
	
	/**
	 * @return the vertex attributes of this Mesh
	 */
	public VertexAttributes getVertexAttributes( )
	{
		return attributes;
	}
	
	/**
	 * @return the backing ByteBuffer holding the vertices
	 */
	public Buffer getVerticesBuffer( )
	{
		return vertices;
	}
	
	/**
	 * @return the backing shortbuffer holding the indices
	 */
	public ShortBuffer getIndicesBuffer( )
	{
		return indices;
	}

	/**
	 * Returns getNumVertices() vertices in the float array
	 * @param vertices the destination array
	 */
	public void getVertices(float[] vertices) 
	{	
		if( useFixedPoint )
			throw new IllegalArgumentException( "can't get float vertices from fixed point mesh" );
		
		verticesFloat.get(vertices);
		verticesFloat.position(0);
	}
	
	/**
	 * Returns getNumVertices() vertices in the fixed point array
	 * @param vertices the destination array
	 */
	public void getVertices( int[] vertices )
	{
		if( !useFixedPoint )
			throw new IllegalArgumentException( "can't get fixed point vertices from float mesh" );
		
		verticesFixed.get(vertices);
		verticesFixed.position(0);
	}
	
	/**
	 * Returns getNumIndices() indices in the short array
	 * @param indices the destination array
	 */
	public void getIndices( short[] indices )
	{
		this.indices.get(indices);
		this.indices.position(0);
	}
	
	/**
	 * Invalidates all meshes so the next time they are rendered
	 * new VBO handles are generated.
	 */
	public static void invalidateAllMeshes( )
	{
		for( int i = 0; i < meshes.size(); i++ )
		{
			meshes.get(i).invalidated = true;
			meshes.get(i).checkManagedAndDirty();			
		}
	}
}

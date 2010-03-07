package com.badlogic.gdx.backends.jogl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.GLContext;

import com.badlogic.gdx.Mesh;
import com.badlogic.gdx.math.Bounds;

public class JoglMesh implements Mesh 
{		
	private boolean dirty = true;
	private int currentVertex = 0;
	private int currentIndex = 0;
	private int numVertices = 0;
	private int numIndices = 0;
	public float[] vertices;
	public float[] normals;
	public float[] colors;	
	public float[] uv;
	public short[] indices;
	private FloatBuffer verticesBuffer;
	private FloatBuffer normalsBuffer;
	private FloatBuffer colorsBuffer;
	private FloatBuffer uvBuffer;
	private ShortBuffer indicesBuffer;				
	private boolean isVBO = false;
	
	public Bounds bounds = new Bounds( );
	
	private int vertexHandle = -1;
	private int colorHandle = -1;
	private int normalHandle = -1;
	private int uvHandle = -1;
	private int indexHandle = -1;
	private static JoglMesh lastMesh = null;
	public static int meshes = 0;
	
	/**
	 * Instantiates a new mesh.
	 *  
	 * @param type the type of the primitives (GL10.GL_TRIANGLES, GL10.GL_QUADS etc.)
	 * @param order winding of the primitives (GL10.GL_CW, GL10.GL_CCW)
	 * @param numVertices the number of vertices
	 * @param hasColors wheter a vertex has a color
	 * @param hasNormals wheter a vertex has a normal
	 * @param hasIndices wheter indices should be used
	 * @param hasUV wheter a vertex has uv coordinates
	 * @param numIndices the number of indices given.
	 */
	protected JoglMesh( GL gl, int numVertices, boolean hasColors, boolean hasNormals, boolean hasUV, boolean hasIndices, int numIndices, boolean useVBO )
	{		
		useVBO = false;
		if( useVBO )
		{						
			isVBO = true;
			setupVertexBufferObject( (GL)gl, numVertices, hasColors, hasNormals, hasUV, hasIndices, numIndices );
		}
		else
		{
			isVBO = false;
			setupVertexArray( numVertices, hasColors, hasNormals, hasUV, hasIndices, numIndices );
		}
		
		meshes++;
	}
	
	private void setupVertexBufferObject( GL gl, int numVertices, boolean hasColors, boolean hasNormals, boolean hasUV, boolean hasIndices, int numIndices )
	{
		vertices = new float[numVertices * 3];		
		ByteBuffer byteBuffer = ByteBuffer.allocateDirect( numVertices * 4 * 3);
		byteBuffer.order(ByteOrder.nativeOrder());
		verticesBuffer = byteBuffer.asFloatBuffer();
		int[] buffer = new int[1];
		
		gl.glGenBuffers(1, buffer, 0);
		vertexHandle = buffer[0];
		
		if( hasColors )
		{
			byteBuffer = ByteBuffer.allocateDirect( numVertices * 4 * 4);
			byteBuffer.order(ByteOrder.nativeOrder());
			colorsBuffer = byteBuffer.asFloatBuffer();
			colors = new float[numVertices*4];
			gl.glGenBuffers(1, buffer, 0);
			colorHandle = buffer[0];
		}
		
		if( hasNormals )
		{
			byteBuffer = ByteBuffer.allocateDirect( numVertices * 4 * 3);
			byteBuffer.order(ByteOrder.nativeOrder());
			normalsBuffer = byteBuffer.asFloatBuffer();
			normals = new float[numVertices*3];
			gl.glGenBuffers(1, buffer, 0);
			normalHandle = buffer[0];
		}
		
		if( hasUV )
		{
			byteBuffer = ByteBuffer.allocateDirect( numVertices * 4 * 2 );
			byteBuffer.order(ByteOrder.nativeOrder());
			uvBuffer = byteBuffer.asFloatBuffer();
			uv = new float[numVertices*2];
			gl.glGenBuffers(1, buffer, 0);
			uvHandle = buffer[0];
		}
		
		if( hasIndices )			
		{
			byteBuffer = ByteBuffer.allocateDirect( numIndices * 4 );
			byteBuffer.order(ByteOrder.nativeOrder());
			indicesBuffer = byteBuffer.asShortBuffer();
			indices = new short[numIndices];
			gl.glGenBuffers(1, buffer, 0);
			indexHandle = buffer[0];
		}
	}
	
	private void setupVertexArray( int numVertices, boolean hasColors, boolean hasNormals, boolean hasUV, boolean hasIndices, int numIndices )
	{
		vertices = new float[numVertices * 3];
		ByteBuffer byteBuffer = ByteBuffer.allocateDirect( numVertices * 4 * 3);
		byteBuffer.order(ByteOrder.nativeOrder());
		verticesBuffer = byteBuffer.asFloatBuffer();
		
		if( hasColors )
		{
			byteBuffer = ByteBuffer.allocateDirect( numVertices * 4 * 4);
			byteBuffer.order(ByteOrder.nativeOrder());
			colorsBuffer = byteBuffer.asFloatBuffer();
			colors = new float[numVertices*4];
		}
		
		if( hasNormals )
		{
			byteBuffer = ByteBuffer.allocateDirect( numVertices * 4 * 3);
			byteBuffer.order(ByteOrder.nativeOrder());
			normalsBuffer = byteBuffer.asFloatBuffer();
			normals = new float[numVertices*3];
		}
		
		if( hasUV )
		{
			byteBuffer = ByteBuffer.allocateDirect( numVertices * 4 * 2 );
			byteBuffer.order(ByteOrder.nativeOrder());
			uvBuffer = byteBuffer.asFloatBuffer();
			uv = new float[numVertices*2];
		}
		
		if( hasIndices )
		{
			byteBuffer = ByteBuffer.allocateDirect( numIndices * 4 );
			byteBuffer.order(ByteOrder.nativeOrder());
			indicesBuffer = byteBuffer.asShortBuffer();
			indices = new short[numIndices];
		}			
	}
	
	private void update( GL gl )
	{
		if( dirty )
		{
			if( isVBO )
				updateVertexBufferObject( gl );
			else
				updateVertexArray( );
			dirty = false;			
		}
	}
	
	private void updateVertexArray( )
	{
		verticesBuffer.put(vertices);
		verticesBuffer.position(0);
	
		if( colorsBuffer != null )
		{
			colorsBuffer.put(colors);
			colorsBuffer.position(0);
		}
		
		if( normalsBuffer != null )
		{
			normalsBuffer.put(normals);
			normalsBuffer.position(0);
		}
		
		if( uvBuffer != null )
		{
			uvBuffer.put(uv);
			uvBuffer.position(0);
		}
		
		if( indicesBuffer != null )
		{
			indicesBuffer.put(indices);
			indicesBuffer.position(0);
		}
		
		dirty = false;
		numVertices = currentVertex;
		numIndices = currentIndex;
		currentVertex = 0;
		currentIndex = 0;	
	}
	
	private void updateVertexBufferObject( GL gl )
	{			
		updateVertexArray( );
		
		gl.glBindBuffer( GL.GL_ARRAY_BUFFER, vertexHandle );
		gl.glBufferData( GL.GL_ARRAY_BUFFER, vertices.length * 4, verticesBuffer, GL.GL_DYNAMIC_DRAW);
		
		if( colors != null )
		{
			gl.glBindBuffer( GL.GL_ARRAY_BUFFER, colorHandle );
			gl.glBufferData( GL.GL_ARRAY_BUFFER, colors.length * 4, colorsBuffer, GL.GL_DYNAMIC_DRAW);
		}
		
		if( normals != null )
		{
			gl.glBindBuffer( GL.GL_ARRAY_BUFFER, normalHandle );
			gl.glBufferData( GL.GL_ARRAY_BUFFER, normals.length * 4, normalsBuffer, GL.GL_DYNAMIC_DRAW);
		}
		
		if( uv != null )
		{
			gl.glBindBuffer( GL.GL_ARRAY_BUFFER, uvHandle );
			gl.glBufferData( GL.GL_ARRAY_BUFFER, uv.length * 4, uvBuffer, GL.GL_DYNAMIC_DRAW);
		}
		
		if( indices != null )
		{
			gl.glBindBuffer( GL.GL_ELEMENT_ARRAY_BUFFER, indexHandle );
			gl.glBufferData( GL.GL_ELEMENT_ARRAY_BUFFER, indices.length * 4, indicesBuffer, GL.GL_DYNAMIC_DRAW);
			gl.glBindBuffer( GL.GL_ELEMENT_ARRAY_BUFFER, 0 );
		}
				
		gl.glBindBuffer( GL.GL_ARRAY_BUFFER, 0 );
		
		dirty = false;
		numVertices = currentVertex;
		numIndices = currentIndex;
		currentVertex = 0;
		currentIndex = 0;
	}
	
	public void vertex( float x, float y, float z )
	{
		if( dirty == false )
			bounds.inf();
		dirty = true;
		int offset = currentVertex * 3;
		vertices[offset] = x;
		vertices[offset+1] = y;
		vertices[offset+2] = z;
		currentVertex++;
		bounds.ext( x, y, z );		
	}
	
	public void color( float r, float g, float b, float a )
	{
		dirty = true;
		int offset = currentVertex * 4;
		colors[offset] = r;
		colors[offset+1] = g;
		colors[offset+2] = b;
		colors[offset+3] = a;		
	}
	
	public void normal( float x, float y, float z )
	{
		dirty = true;
		int offset = currentVertex * 3;
		normals[offset] = x;
		normals[offset+1] = y;
		normals[offset+2] = z;
	}
	
	public void texCoord( float u, float v )
	{
		dirty = true;
		int offset = currentVertex * 2;
		uv[offset] = u;
		uv[offset+1] = v;		
	}
	
	public void index( int ... index )
	{
		for( int i = 0; i < index.length; i++ )		
			indices[currentIndex++] = (short)index[i];		
	}
	
	private void renderVertexBufferObject( GL gl, int type, int numVertices, int offset )
	{			
		if( lastMesh == this )
		{
			if( indexHandle != -1 )
	        {
	        	gl.glBindBuffer( GL.GL_ELEMENT_ARRAY_BUFFER, indexHandle );
	        	gl.glDrawElements(type, numVertices, GL.GL_UNSIGNED_SHORT, offset);    
	        }
	        else
	        	gl.glDrawArrays(type, offset, numVertices);			
			return;
		}			
		
		gl.glEnableClientState(GL.GL_VERTEX_ARRAY );
		gl.glBindBuffer( GL.GL_ARRAY_BUFFER, vertexHandle );
		gl.glVertexPointer(3, GL.GL_FLOAT, 0, 0);
        
        if( colorHandle != -1 )
        {     
        	gl.glEnableClientState(GL.GL_COLOR_ARRAY);
        	gl.glBindBuffer( GL.GL_ARRAY_BUFFER, colorHandle );
        	gl.glColorPointer(4, GL.GL_FLOAT, 0, 0);
        }
        
        if( normalHandle != -1)
        {           
        	gl.glEnableClientState(GL.GL_NORMAL_ARRAY);
        	gl.glBindBuffer( GL.GL_ARRAY_BUFFER, normalHandle );
        	gl.glNormalPointer( GL.GL_FLOAT, 0, 0);
        }
        
        if( uvHandle != -1 )
        {        	
        	gl.glEnableClientState(GL.GL_TEXTURE_COORD_ARRAY);
        	gl.glBindBuffer( GL.GL_ARRAY_BUFFER, uvHandle );
        	gl.glTexCoordPointer( 2, GL.GL_FLOAT, 0, 0 );
        }
    
		if( indexHandle != -1 )
        {
        	gl.glBindBuffer( GL.GL_ELEMENT_ARRAY_BUFFER, indexHandle );
        	gl.glDrawElements(type, numVertices, GL.GL_UNSIGNED_SHORT, offset);    
        }
        else
        	gl.glDrawArrays(type, offset, numVertices);
		
		gl.glDisableClientState(GL.GL_VERTEX_ARRAY );
		gl.glDisableClientState(GL.GL_COLOR_ARRAY );
		gl.glDisableClientState(GL.GL_TEXTURE_COORD_ARRAY );
		gl.glDisableClientState(GL.GL_NORMAL_ARRAY );
	}
	
	private void renderVertexArray( GL gl, int type, int numVertices, int offset )
	{			
		if( lastMesh != null && lastMesh.isVBO )
		{
        	gl.glBindBuffer( GL.GL_ARRAY_BUFFER, 0 );
        	gl.glBindBuffer( GL.GL_ELEMENT_ARRAY_BUFFER, 0 );
		}
		
        gl.glEnableClientState(GL.GL_VERTEX_ARRAY);    
        gl.glVertexPointer(3, GL.GL_FLOAT, 0, verticesBuffer);
        
        if( colorsBuffer != null )
        {
        	gl.glEnableClientState(GL.GL_COLOR_ARRAY);
        	gl.glColorPointer(4, GL.GL_FLOAT, 0, colorsBuffer);
        }
        
        if( normalsBuffer != null )
        {
        	gl.glEnableClientState(GL.GL_NORMAL_ARRAY);
        	gl.glNormalPointer( GL.GL_FLOAT, 0, normalsBuffer);
        }
        
        if( uvBuffer != null )
        {
        	gl.glEnableClientState(GL.GL_TEXTURE_COORD_ARRAY);
        	gl.glTexCoordPointer( 2, GL.GL_FLOAT, 0, uvBuffer );
        }
    
        if( indicesBuffer != null )
        {
        	indicesBuffer.position(offset);
        	gl.glDrawElements(type, numVertices, GL.GL_UNSIGNED_SHORT, indicesBuffer);
        }
        else
        	gl.glDrawArrays(type, offset, numVertices);
        
        gl.glDisableClientState(GL.GL_VERTEX_ARRAY );
		gl.glDisableClientState(GL.GL_COLOR_ARRAY );
		gl.glDisableClientState(GL.GL_TEXTURE_COORD_ARRAY );
		gl.glDisableClientState(GL.GL_NORMAL_ARRAY );
	}	
	
	
	@Override
	public void dispose() {
		GL gl = GLContext.getCurrent().getGL();
		if( isVBO )
		{
			if( vertexHandle != -1 )
				gl.glDeleteBuffers(1, new int[] { vertexHandle }, 0);
			if( colorHandle != -1 )
				gl.glDeleteBuffers(1, new int[] { colorHandle }, 0);
			if( normalHandle != -1 )
				gl.glDeleteBuffers(1, new int[] { normalHandle }, 0);
			if( uvHandle != -1 )
				gl.glDeleteBuffers(1, new int[] { uvHandle }, 0);
			if( indexHandle != -1 )
				gl.glDeleteBuffers(1, new int[] { indexHandle }, 0);		
		}
		lastMesh = null;
		

		vertices = null;
		verticesBuffer = null;
		colors = null;
		colorsBuffer = null;
		normals = null;
		normalsBuffer = null;
		uv = null;
		uvBuffer = null;
		indices = null;
		indicesBuffer = null;
		
		meshes--;
	}

	@Override
	public void render(PrimitiveType type, int numVertices, int offset) {
		GL gl = GLContext.getCurrent().getGL();
		update( gl );		       
        
		int t = getPrimitiveType(type);
		
		if( isVBO )
			renderVertexBufferObject( gl, t, numVertices, offset );
		else
			renderVertexArray( gl, t,numVertices, offset );	
		lastMesh = this;
		
	}
	
	private int getPrimitiveType( PrimitiveType type )
	{
		if( type == PrimitiveType.Lines )
			return GL.GL_LINES;
		else
		if( type == PrimitiveType.Triangles )
			return GL.GL_TRIANGLES;
		else
		if( type == PrimitiveType.LineStrip)
			return GL.GL_LINE_STRIP;
		else		
		if( type == PrimitiveType.TriangleStrip)
			return GL.GL_TRIANGLE_STRIP;
		else
		if( type == PrimitiveType.Points )
			return GL.GL_POINTS;
		else		
			return GL.GL_TRIANGLE_FAN;
	}

	@Override
	public void render(PrimitiveType type) 
	{
		update( GLContext.getCurrent().getGL() );
		if( indices != null )
			render( type, numIndices, 0 );
		else
			render( type, numVertices, 0 );
	}

	@Override
	public void reset() {
		dirty = true;
		currentIndex = 0;
		currentVertex = 0;
		numIndices = 0;
		numVertices = 0;
	}

	@Override
	public int getMaximumVertices() {
		return vertices.length / 3;
	}
	

	@Override
	public float[] getColors() {
		return colors;
	}

	@Override
	public short[] getIndices() {
		return indices;
	}

	@Override
	public float[] getNormals() {
		return normals;
	}

	@Override
	public float[] getUV() {
		return uv;
	}

	@Override
	public float[] getVertices() {
		return vertices;
	}
	
	@Override
	public void setDirty() {
		dirty = true;		
	}
}

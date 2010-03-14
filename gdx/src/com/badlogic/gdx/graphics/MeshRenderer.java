package com.badlogic.gdx.graphics;

public class MeshRenderer 
{
	private final GL10 gl;
	private final Mesh mesh;
	private final boolean isStatic;
	private final int type;
	private int vboVertexHandle = 0;
	private int vboIndexHandle = 0;
	
	public MeshRenderer( GL10 gl, Mesh mesh, boolean isStatic )
	{
		this.gl = gl;
		this.mesh = mesh;
		this.isStatic = isStatic;
		this.type = mesh instanceof FloatMesh?GL10.GL_FLOAT:GL10.GL_FIXED;
		createVBO( );
		fillVBO( );
	}	
	
	private void createVBO( )
	{
		if( !(gl instanceof GL11) )
			return;
	
		GL11 gl11 = (GL11)gl;
		
		int handle[] = new int[1];
		gl11.glGenBuffers( 1, handle, 0 );
		vboVertexHandle = handle[0];	
		
		if( mesh.hasIndices() )
		{
			gl11.glGenBuffers( 1, handle, 0 );
			vboIndexHandle = handle[0];
		}
	}
	
	private void fillVBO( )
	{
		if( !(gl instanceof GL11) )
			return;
	
		GL11 gl11 = (GL11)gl;
		
		gl11.glBindBuffer( GL11.GL_ARRAY_BUFFER, vboVertexHandle );
		gl11.glBufferData( GL11.GL_ARRAY_BUFFER, mesh.getVertexSize() * mesh.getNumVertices(), mesh.getVerticesBuffer(), isStatic?GL11.GL_STATIC_DRAW:GL11.GL_DYNAMIC_DRAW );
		gl11.glBindBuffer( GL11.GL_ARRAY_BUFFER, 0 );
		
		if( mesh.hasIndices() )
		{
			gl11.glBindBuffer( GL11.GL_ELEMENT_ARRAY_BUFFER, vboIndexHandle );
			gl11.glBufferData( GL11.GL_ELEMENT_ARRAY_BUFFER, mesh.getNumIndices() * 2, mesh.getIndicesBuffer(), isStatic?GL11.GL_STATIC_DRAW: GL11.GL_DYNAMIC_DRAW );
			gl11.glBindBuffer( GL11.GL_ELEMENT_ARRAY_BUFFER, 0 );
		}
	}
	
	public void update( )
	{
		if( vboVertexHandle == 0 )
			return;
		
		fillVBO( );
	}
	
	public void render( int primitiveType, int offset, int count )
	{
		if( vboVertexHandle != 0 )
			renderVBO( primitiveType, offset, count );
		else
			renderVA( primitiveType, offset, count );
	}
	
	private void renderVBO( int primitiveType, int offset, int count )
	{
		GL11 gl11 = (GL11)gl;			
		
		gl11.glBindBuffer( GL11.GL_ARRAY_BUFFER, vboVertexHandle );
		gl11.glEnableClientState( GL11.GL_VERTEX_ARRAY );		
		gl11.glVertexPointer( mesh.getCoordsSize(), type, mesh.getVertexSize(), 0 );
		
		if( mesh.hasColors() )
		{
			gl11.glEnableClientState( GL11.GL_COLOR_ARRAY );
			gl11.glColorPointer( mesh.getColorsSize(), type, mesh.getVertexSize(), mesh.getColorsOffset() );
		}
		
		if( mesh.hasNormals() )
		{
			gl11.glEnableClientState( GL11.GL_NORMAL_ARRAY );
			gl11.glNormalPointer( type, mesh.getVertexSize(), mesh.getNormalsOffset() );
		}
		
		if( mesh.hasTexCoords() )
		{
			for( int i = 0; i < mesh.getNumTexCoords(); i++ )
			{
				gl11.glClientActiveTexture( GL11.GL_TEXTURE0 + i );
				gl11.glEnableClientState( GL11.GL_TEXTURE_COORD_ARRAY );
				gl11.glTexCoordPointer( mesh.getTexCoordsSize(), type, mesh.getVertexSize(), mesh.getTexCoordsOffset() + i * mesh.getTexCoordsSize() );
			}
		}
		
		if( mesh.hasIndices() )
		{			
			gl11.glBindBuffer( GL11.GL_ELEMENT_ARRAY_BUFFER, vboIndexHandle );
			gl11.glDrawElements( primitiveType, count, GL10.GL_UNSIGNED_SHORT, offset );
		}	
		else
		{
			gl11.glDrawArrays( primitiveType, offset, count);
		}
		
		if( mesh.hasColors() )
			gl11.glDisableClientState( GL11.GL_COLOR_ARRAY );
		if( mesh.hasNormals() )
			gl11.glDisableClientState( GL11.GL_NORMAL_ARRAY );
		if( mesh.hasTexCoords() )
		{
			for( int i = 0; i < mesh.getNumTexCoords(); i++ )
			{
				gl11.glClientActiveTexture( GL11.GL_TEXTURE0 + i );
				gl11.glDisableClientState( GL11.GL_TEXTURE_COORD_ARRAY );
			}
			gl11.glClientActiveTexture( GL11.GL_TEXTURE0 );
		}
		
		gl11.glBindBuffer( GL11.GL_ELEMENT_ARRAY_BUFFER, 0 );
		gl11.glBindBuffer( GL11.GL_ARRAY_BUFFER, 0 );
	}
	
	private void renderVA( int primitiveType, int offset, int count )
	{
		
		gl.glEnableClientState( GL11.GL_VERTEX_ARRAY );		
		gl.glVertexPointer( mesh.getCoordsSize(), type, mesh.getVertexSize(), mesh.getVerticesBuffer() );
		
		if( mesh.hasColors() )
		{
			gl.glEnableClientState( GL11.GL_COLOR_ARRAY );
			mesh.getVerticesBuffer().position( mesh.getColorsOffset() / 4 );
			gl.glColorPointer( mesh.getColorsSize(), type, mesh.getVertexSize(), mesh.getVerticesBuffer() );
		}
		
		if( mesh.hasNormals() )
		{
			gl.glEnableClientState( GL11.GL_NORMAL_ARRAY );
			mesh.getVerticesBuffer().position( mesh.getNormalsOffset() / 4 );
			gl.glNormalPointer( type, mesh.getVertexSize(), mesh.getVerticesBuffer() );
		}
		
		if( mesh.hasTexCoords() )
		{
			for( int i = 0; i < mesh.getNumTexCoords(); i++ )
			{
				gl.glClientActiveTexture( GL11.GL_TEXTURE0 + i );
				gl.glEnableClientState( GL11.GL_TEXTURE_COORD_ARRAY );
				mesh.getVerticesBuffer().position( mesh.getTexCoordsOffset() / 4 + i );
				gl.glTexCoordPointer( mesh.getTexCoordsSize(), type, mesh.getVertexSize(), mesh.getVerticesBuffer() );
			}
		}
		
		if( mesh.hasIndices() )
			gl.glDrawElements( primitiveType, count, GL10.GL_UNSIGNED_SHORT, mesh.getIndicesBuffer() );
		else
			gl.glDrawArrays( primitiveType, offset, count);
		
		if( mesh.hasColors() )
			gl.glDisableClientState( GL11.GL_COLOR_ARRAY );
		if( mesh.hasNormals() )
			gl.glDisableClientState( GL11.GL_NORMAL_ARRAY );
		if( mesh.hasTexCoords() )
		{
			for( int i = 0; i < mesh.getNumTexCoords(); i++ )
			{
				gl.glClientActiveTexture( GL11.GL_TEXTURE0 + i );
				gl.glDisableClientState( GL11.GL_TEXTURE_COORD_ARRAY );
			}
			gl.glClientActiveTexture( GL11.GL_TEXTURE0 );
		}
		mesh.getVerticesBuffer().position(0);
	}
	
	public void dispose( )
	{
		if( gl instanceof GL11 )
		{
			GL11 gl11 = (GL11)gl;
			int handle[] = new int[1];
			handle[0] = vboVertexHandle;
			gl11.glDeleteBuffers(1, handle, 0 );
			
			if( mesh.hasIndices() )
			{
				handle[0] = vboIndexHandle;
				gl11.glDeleteBuffers( 1, handle, 0 );
			}
		}			
	}
}

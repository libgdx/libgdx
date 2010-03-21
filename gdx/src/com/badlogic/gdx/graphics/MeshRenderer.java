/*
 *  This file is part of Libgdx by Mario Zechner (badlogicgames@gmail.com)
 *
 *  Libgdx is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Libgdx is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with libgdx.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.badlogic.gdx.graphics;

import com.badlogic.gdx.backends.desktop.JoglGL11;


/**
 * A MeshRenderer takes a {@link Mesh} and creates an OpenGL vertex array
 * of vertex buffer object from it depending on the available OpenGL version.
 * This class of course only works with OpenGL ES 1.x. A MeshRenderer can
 * be managed meaning that in case of an OpenGL context loss any OpenGL 
 * related resources like vertex buffer object handles are recreated. Note
 * that you have to call the {@link update()} method in case you modified
 * the underlying Mesh in order for the MeshRenderer to adapt to the changes.
 * 
 * @author mzechner
 *
 */
public class MeshRenderer 
{
	private final GL10 gl;
	private final Mesh mesh;
	private final boolean isStatic;
	private final boolean isManaged;
	private final int type;
	private int vboVertexHandle = 0;
	private int vboIndexHandle = 0;	
	
	/**
	 * Constructor, creates a new MeshRenderer wrapping the given {@link Mesh}. In case
	 * managed is set to true this MeshRenderer will care for recreating any needed
	 * OpenGL resources in case the OpenGL context was lost.
	 * 
	 * @param gl the GL10 instance
	 * @param mesh the Mesh to wrap
	 * @param isStatic whether the Mesh is static or will change often. This is just a hint.
	 * @param managed whether the MeshRenderer should manage context loss
	 */
	public MeshRenderer( GL10 gl, Mesh mesh, boolean isStatic, boolean managed )
	{
		this.gl = gl;
		this.mesh = mesh;
		this.isStatic = isStatic;
		this.isManaged = managed;
		this.type = mesh instanceof FloatMesh?GL10.GL_FLOAT:GL10.GL_FIXED;
		createVBO( );
		fillVBO( );		
	}	
	
	private void createVBO( )
	{
		// FIXME the last bit is a hack. No way to support fixed point VBOs
		if( !(gl instanceof GL11) || (type == GL10.GL_FIXED && gl instanceof JoglGL11) )
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
		// FIXME the last bit is a hack. No way to support fixed point VBOs
		if( !(gl instanceof GL11) || (type == GL10.GL_FIXED && gl instanceof JoglGL11) )
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
	
	/**
	 * @return the underlying {@link Mesh}
	 */
	public Mesh getMesh( )
	{
		return mesh;
	}
	
	/**
	 * Updates the MeshRenderer based on the new data
	 * in the underlying {@link Mesh}. Call this when
	 * you modified the Mesh externally.
	 */
	public void update( )
	{
		if( vboVertexHandle == 0 )
			return;
		
		GL11 gl11 = (GL11)gl;		
		if( isManaged && gl11.glIsBuffer( vboVertexHandle ) == false )				
			createVBO( );		
		
		fillVBO( );
	}
	
	/**
	 * Renders the mesh using the supplied OpenGL primitive type.  
	 * 
	 * @param primitiveType the primitiveType. Should be one of {@link GL10.GL_TRIANGLES}, {@link Gl10.GL_TRIANGLE_STRIP}, {@link GL10.GL_TRIANGLE_FAN}, {@link GL10.GL_LINES}, {@link GL10.GL_LINE_STRIP} or {@link GL10.GL_LINE_LOOP}
	 */
	public void render(int primitiveType) 
	{	
		if( mesh.hasIndices() )
			render( primitiveType, 0, mesh.getNumIndices() );
		else
			render( primitiveType, 0, mesh.getNumVertices() );
	}
	
	/**
	 * Renders the mesh using the supplied OpenGL primitive type. Offset
	 * and count specify the offset into the vertex arrays or the indices
	 * array as well as the number of vertices to use. 
	 * 
	 * @param primitiveType the primitiveType. Should be one of {@link GL10.GL_TRIANGLES}, {@link Gl10.GL_TRIANGLE_STRIP}, {@link GL10.GL_TRIANGLE_FAN}, {@link GL10.GL_LINES}, {@link GL10.GL_LINE_STRIP} or {@link GL10.GL_LINE_LOOP}
	 * @param offset the offset into the vertex or indices array
	 * @param count the number of vertices or indices to use
	 */
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
		
		if( isManaged && gl11.glIsBuffer( vboVertexHandle ) == false )
		{
			createVBO( );
			fillVBO( );
		}
		
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
	
	/**
	 * Releases all used resources. Call this if you don't use the
	 * MeshRenderer anymore. The underlying Mesh can be reused.
	 */
	public void dispose( )
	{
		if( gl instanceof GL11 )
		{
			GL11 gl11 = (GL11)gl;
			if( gl11.glIsBuffer( vboVertexHandle ) )
			{
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
}

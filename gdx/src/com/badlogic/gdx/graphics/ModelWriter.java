package com.badlogic.gdx.graphics;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Class that exports a mesh to a specific file format
 * @author mzechner
 *
 */
public class ModelWriter 
{
	/**
	 * Writes the given mesh to the gdx3D format. Does not
	 * close the output stream.
	 * 
	 * @param mesh The mesh
	 * @param out The OutputStream to write to.
	 * @return whether the conversion worked or not.
	 */
	public static boolean writeGdx3D( Mesh mesh, OutputStream out )
	{		
		try
		{
			DataOutputStream dout = new DataOutputStream( new BufferedOutputStream( out ) );		
			
			// output vertex attributes
			dout.writeInt( mesh.getVertexAttributes().size() );			
			for( int i = 0; i < mesh.getVertexAttributes().size(); i++ )
			{
				VertexAttribute attribute = mesh.getVertexAttributes().get(i);
				dout.writeInt( attribute.usage );
				dout.writeInt( attribute.numComponents );
				byte[] bytes = attribute.alias.getBytes( "UTF8" );
				dout.writeInt( bytes.length );
				dout.write( bytes );
			}
			
			// output whether this mesh uses fixed point or not.
			dout.writeBoolean( mesh.usesFixedPoint() );
			
			// output the number of vertices and indices
			dout.writeInt( mesh.getNumVertices() );
			dout.writeInt( mesh.getNumVertices() * mesh.getVertexSize() / 4 );
			dout.writeInt( mesh.getNumIndices() );
			
			// output vertices
			if( mesh.usesFixedPoint() )
			{
				int[] vertices = new int[mesh.getNumVertices() * mesh.getVertexSize() / 4];
				mesh.getVertices( vertices );
				for( int i = 0; i < vertices.length; i++ )
					dout.writeInt( vertices[i] );
			}
			else
			{
				float[] vertices = new float[mesh.getNumVertices() * mesh.getVertexSize() / 4];
				mesh.getVertices( vertices );
				for( int i = 0; i < vertices.length; i++ )
					dout.writeFloat( vertices[i] );
			}
			
			dout.flush();
			
			// output indices if any
			if( mesh.getNumIndices() > 0 )
			{
				short[] indices = new short[mesh.getNumIndices()];
				mesh.getIndices( indices );
				for( int i = 0; i < indices.length; i++ )
					dout.writeShort(indices[i] );
			}
			
			dout.flush();
			
			return true;
		}
		catch( IOException ex )
		{
			ex.printStackTrace();
			return false;
		}		
	}
}

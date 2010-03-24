package com.badlogic.gdx.graphics;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * Allows exporting a {@link Mesh} to some format.
 * 
 * @author mzechner
 *
 */
public class ModelWriter 
{
	/**
	 * Writes a float mesh to the specified output stream.
	 * Assumes that the mesh is composed of triangles and
	 * that each vertex has only a single set of texture
	 * coordinates at most. Ignores colors. Does not close
	 * the output stream.
	 *  
	 * @param mesh the Mesh
	 * @throws IOException in case the mesh could not be written
	 */
	public static void writeObj( OutputStream out, FloatMesh mesh ) throws IOException
	{
		BufferedWriter writer = new BufferedWriter( new OutputStreamWriter( out ) );
				
		if( mesh.getNumTexCoords() > 1 )
			throw new IllegalArgumentException( "only a single texture coordinate set allowed" );
		if( mesh.getTexCoordsSize() != 2 )
			throw new IllegalArgumentException( "texture coordinates size must be 2" );
		
		if( mesh.hasIndices() && mesh.getNumIndices() % 3 != 0 )
			throw new IllegalArgumentException( "mesh must be triangulated" );
		else
			if( mesh.getNumVertices() % 3 != 0 )
				throw new IllegalArgumentException( "mesh must be triangulated" );
		
		int hop = mesh.getVertexSize() / 4;
		float[] vertices = mesh.getVerticesArray();
		for( int i = 0; i < mesh.getNumVertices(); i++ )
		{
			int idx = i * hop;
			writer.write( "v " + vertices[idx] + " " + vertices[idx+1] + " " + vertices[idx+2] + "\n" );
			idx += 3;
			if( mesh.hasColors() )
				idx += mesh.getColorsSize();
			
			if( mesh.hasNormals() )
			{								
				writer.write( "vn " + vertices[idx] + " " + vertices[idx+1] + " " + vertices[idx+2] + "\n" );
				idx+=3;
			}
			
			if( mesh.hasTexCoords())			
				writer.write( "vt " + vertices[idx] + " " + vertices[idx+1] + "\n" );			
		}
		
		if( mesh.hasIndices() )
		{
			short[] indices = mesh.getIndicesArray();
			for( int i = 0; i < indices.length; i+=3 )
			{
				int i1 = indices[i]+1;
				int i2 = indices[i]+1;
				int i3 = indices[i]+1;
				if( mesh.hasNormals() && mesh.hasTexCoords() )
					writer.write( "f " + i1 + "/" + i1 + "/" + i1 + " " + i2 + "/" + i2 + "/" + i2 + " " + i3 + "/" + i3 + "/" + i3 + "\n" );
				else
				if( mesh.hasNormals() )
					writer.write( "f " + i1 + "/" + i1 + "/" + " " + i2 + "/" + i2 + "/" + " " + i3 + "/" + i3 + "/" + "\n" );
				else
				if( mesh.hasTexCoords() )
					writer.write( "f " + i1 + "/" + "/" + i1 + " " + i2 + "/" + "/" + i2 + " " + i3 + "/" + "/" + i3 + "\n" );
				else
					writer.write( "f " + i1 + "//" + " " + i2 + "//" + " " + i3 + "//" + "\n" );
				
			}
		}
		else
		{
			int idx = 1;
			for( int i = 0; i < mesh.getNumVertices() / 3; i++, idx+=3 )			
			{
				int i1 = idx;
				int i2 = idx+1;
				int i3 = idx+2;
				if( mesh.hasNormals() && mesh.hasTexCoords() )
					writer.write( "f " + i1 + "/" + i1 + "/" + i1 + " " + i2 + "/" + i2 + "/" + i2 + " " + i3 + "/" + i3 + "/" + i3 + "\n" );
				else
				if( mesh.hasNormals() )
					writer.write( "f " + i1 + "/" + i1 + "/" + " " + i2 + "/" + i2 + "/" + " " + i3 + "/" + i3 + "/" + "\n" );
				else
				if( mesh.hasTexCoords() )
					writer.write( "f " + i1 + "/" + "/" + i1 + " " + i2 + "/" + "/" + i2 + " " + i3 + "/" + "/" + i3 + "\n" );
				else
					writer.write( "f " + i1 + "//" + " " + i2 + "//" + " " + i3 + "//" + "\n" );
			}
		}
		writer.flush();
	}
	
	public static void main( String[] argv ) throws IOException
	{
		FloatMesh mesh = (FloatMesh)ModelLoader.loadObj( new FileInputStream( "data/cube.obj" ), true );
		ModelWriter.writeObj( new FileOutputStream( "test.obj" ), mesh );
	}
}

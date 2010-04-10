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
package com.badlogic.gdx.graphics.loaders;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;

/**
 * Loads Wavefront OBJ files, ignores material files.
 * @author mzechner
 *
 */
public class ObjLoader 
{	
	/**
	 * Loads a Wavefront OBJ file from the given input stream.
	 * 
	 * @param in the InputStream
	 * @param useFloats whether to return a FloatMesh or a FixedPointMesh
	 *
	 */
	public static Mesh loadObj( Graphics graphics, InputStream in, boolean managed, boolean useFloats )
	{
		String line = "";
		
		try
		{
			BufferedReader reader = new BufferedReader( new InputStreamReader( in ) );
			StringBuffer b = new StringBuffer();
			String l = reader.readLine();
			while( l != null )
			{
				b.append( l );
				b.append( "\n" );
				l = reader.readLine();
			}
			
			line = b.toString();
			reader.close();
		}
		catch( Exception ex )
		{
			return null;
		}
		return loadObjFromString( graphics, line, managed, useFloats );
	}
	
	/**
	 * Loads a mesh from the given string in Wavefront OBJ format
	 * 
	 * @param obj The string
	 * @param useFloats whether to return a FloatMesh or a FixedPointMesh
	 * @return The Mesh
	 */
	public static Mesh loadObjFromString( Graphics graphics, String obj, boolean managed, boolean useFloats )
	{
		String[] lines = obj.split( "\n" );
		float[] vertices = new float[lines.length * 3];
		float[] normals = new float[lines.length * 3];
		float[] uv = new float[lines.length * 3];
		
		int numVertices = 0;
		int numNormals = 0;
		int numUV = 0;
		int numFaces = 0;
		
		int[] facesVerts = new int[lines.length * 3];
		int[] facesNormals = new int[lines.length * 3];
		int[] facesUV = new int[lines.length * 3];
		int vertexIndex = 0;
		int normalIndex = 0;
		int uvIndex = 0;
		int faceIndex = 0;		
		
		for( int i = 0; i < lines.length; i++ )
		{
			String line = lines[i];			
			if( line.startsWith( "v " ) )
			{
				String[] tokens = line.split( " " );
				vertices[vertexIndex] = Float.parseFloat(tokens[1]);
				vertices[vertexIndex+1] = Float.parseFloat(tokens[2]);
				vertices[vertexIndex+2] = Float.parseFloat(tokens[3]);
				vertexIndex += 3;
				numVertices++;
				continue;
			}
				
			if( line.startsWith( "vn " ) )
			{
				String[] tokens = line.split( " " );
				normals[normalIndex] = Float.parseFloat(tokens[1]);
				normals[normalIndex+1] = Float.parseFloat(tokens[2]);
				normals[normalIndex+2] = Float.parseFloat(tokens[3]);
				normalIndex += 3;
				numNormals++;
				continue;
			}
			
			if( line.startsWith( "vt" ) )
			{
				String[] tokens = line.split( " " );
				uv[uvIndex] = Float.parseFloat(tokens[1]);
				uv[uvIndex+1] = Float.parseFloat(tokens[2]);				
				uvIndex += 2;
				numUV++;
				continue;
			}
			
			if( line.startsWith( "f " ) )
			{
				String[] tokens = line.split( " " );
				
				String[] parts = tokens[1].split("/");
				facesVerts[faceIndex] = getIndex(parts[0], numVertices);
				if( parts.length > 1 )
					facesNormals[faceIndex] = getIndex(parts[2], numNormals);
				if( parts.length > 2 )
					facesUV[faceIndex] = getIndex(parts[1], numUV);
				faceIndex++;
				
				parts = tokens[2].split("/");
				facesVerts[faceIndex] = getIndex(parts[0], numVertices);
				if( parts.length > 1 )
					facesNormals[faceIndex] = getIndex(parts[2], numNormals);
				if( parts.length > 2 )
					facesUV[faceIndex] = getIndex(parts[1], numUV);
				faceIndex++;
				
				parts = tokens[3].split("/");
				facesVerts[faceIndex] = getIndex(parts[0], numVertices);
				if( parts.length > 1 )
					facesNormals[faceIndex] = getIndex(parts[2], numNormals);
				if( parts.length > 2 )
					facesUV[faceIndex] = getIndex(parts[1], numUV);
				faceIndex++;	
				numFaces++;
				continue;
			}
		}
				
		
		float[] verts = new float[ (numFaces * 3) * ( 3 + (numNormals>0?3:0) + (numUV > 0?2:0 ) )];
		
		for( int i = 0, vi=0; i < numFaces*3; i++ )
		{
			int vertexIdx = facesVerts[i] *3;
			verts[vi++] = vertices[vertexIdx];
			verts[vi++] = vertices[vertexIdx+1];
			verts[vi++] = vertices[vertexIdx+2];
			
			if( numNormals > 0 )
			{
				int normalIdx = facesNormals[i] * 3;
				verts[vi++] = normals[normalIdx];
				verts[vi++] = normals[normalIdx+1];
				verts[vi++] = normals[normalIdx+2];				
			}
			if( numUV > 0 )
			{
				int uvIdx = facesUV[i] * 2;
				verts[vi++] = uv[uvIdx];
				verts[vi++] = uv[uvIdx+1];				
			}										
		}
		
		Mesh mesh = null;
		
		ArrayList<VertexAttribute> attributes = new ArrayList<VertexAttribute>( );
		attributes.add( new VertexAttribute( Usage.Position, 3, "a_Position" ) );
		if( numNormals > 0 )
			attributes.add( new VertexAttribute( Usage.Normal, 3, "a_Normal" ) );
		if( numUV > 0 )
			attributes.add( new VertexAttribute( Usage.TextureCoordinates, 2, "a_TexCoord" ) );
				
		mesh = new Mesh( graphics, managed, true, !useFloats, numFaces * 3, 0, attributes.toArray( new VertexAttribute[attributes.size()] ) );
		if( useFloats )
			mesh.setVertices( verts );
		else
			mesh.setVertices( convertToFixedPoint( verts ) );
		return mesh;
	}	
	
	private static int[] convertToFixedPoint( float[] fverts )
	{
		int[] fpverts = new int[fverts.length];
		for( int i = 0; i < fverts.length; i++ )
			fpverts[i] = (int)(fverts[i] * 65536);;
		return fpverts;
	}
	
	private static int getIndex( String index, int size )
	{
		if( index == null || index.length() == 0 )
			return 0;
		int idx = Integer.parseInt( index );
		if( idx < 0 )
			return size + idx;
		else
			return idx - 1;
	}
}

/*******************************************************************************
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.badlogic.gdx.graphics;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import com.badlogic.gdx.graphics.loaders.ObjLoader;
import com.badlogic.gdx.graphics.loaders.OctLoader;
import com.badlogic.gdx.math.Vector3;

/**
 * A class for loading various model formats such as 
 * Wavefront OBJ or the Quake II MD2 format. Ties in
 * all the loaders from the loaders package.
 * 
 * @author mzechner
 *
 */
public class ModelLoader 
{
	/**
	 * Loads a Wavefront OBJ file from the given
	 * InputStream. The OBJ file must only contain
	 * triangulated meshes. Materials are ignored.
	 * 
	 * @param in the InputStream
	 * @param useFloats whether to use floats or fixed point
	 * @return a Mesh holding the OBJ data or null in case something went wrong.
	 */
	public static Mesh loadObj( InputStream in, boolean useFloats )
	{
		return ObjLoader.loadObj( in, useFloats);
	}
	
	/**
	 * Loads an OCT file as can be found in many of Paul Nettle's
	 * demo programs. See the source at http://www.paulnettle.com/pub/FluidStudios/CollisionDetection/Fluid_Studios_Collision_Detection_Demo_and_Source.zip
	 * for more information.
	 * 
	 * @param in the InputStream
	 * @param useFloats whether to return a {@link FloatMesh} or a {@link FixedPointMesh}
	 * @param start the start position as defined in the map
	 * @return a Mesh holding the OCT data or null in case something went wrong.
	 */
	public static Mesh loadOct( InputStream in, boolean useFloats, Vector3 start )
	{
		return OctLoader.loadOct( in, useFloats, start );
	}
	
	/**
	 * Loads a GDX3D file previously written with {@link ModelWriter.writeGdx3D}.
	 * 
	 * @param in the InputStream
	 * @return a Mesh holding the Gdx3D data or null in case something went wrong.
	 */
	public static Mesh loadGdx3D( InputStream in )
	{
		try
		{
			DataInputStream din = new DataInputStream( new BufferedInputStream( in ) );
			int numAttributes = din.readInt();
			ArrayList<VertexAttribute> attributes = new ArrayList<VertexAttribute>();
			for( int i = 0; i < numAttributes; i++ )
			{
				int usage = din.readInt();
				int numComponents = din.readInt();
				int strlen = din.readInt();
				byte[] bytes = new byte[strlen];
				din.readFully(bytes);
				String alias = new String( bytes, "UTF8" );
				
				VertexAttribute attribute = new VertexAttribute( usage, numComponents, alias);
				attributes.add( attribute );
			}
			
			boolean usesFixedPoint = din.readBoolean();
			int numVertices = din.readInt();
			int numElements = din.readInt();
			int numIndices = din.readInt();
			
			Mesh mesh = new Mesh( true, usesFixedPoint, numVertices, numIndices, attributes.toArray( new VertexAttribute[0] ) );
			
			if( usesFixedPoint )
			{
				int[] vertices = new int[numElements];
				for( int i = 0; i < numElements; i++ )
					vertices[i] = din.readInt();
				mesh.setVertices( vertices );
			}
			else
			{
				float[] vertices = new float[numElements];
				for( int i = 0; i < numElements; i++ )
					vertices[i] = din.readFloat();
				mesh.setVertices( vertices );
			}
			
			if( numIndices > 0 )
			{
				short[] indices = new short[numIndices];
				for( int i = 0; i < numIndices; i++ )				
					indices[i] = din.readShort();
				mesh.setIndices( indices );
			}
			
			return mesh;
		}
		catch( IOException ex )
		{
			ex.printStackTrace();
			return null;
		}
	}
}

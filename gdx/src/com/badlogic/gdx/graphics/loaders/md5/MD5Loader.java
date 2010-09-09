package com.badlogic.gdx.graphics.loaders.md5;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class MD5Loader 
{
	
	public static MD5Model loadModel( InputStream in )
	{
		BufferedReader reader = new BufferedReader( new InputStreamReader( in ) );
		MD5Model model = new MD5Model( );
		List<String>tokens = new ArrayList<String>( 10 );
		
		try
		{			
			String line;
			int currMesh = 0;
			
			while( (line = reader.readLine() ) != null )
			{
				tokenize( line, tokens );
				if( tokens.size() == 0 )
					continue;
				
				//
				// check version string
				//
				if( tokens.get(0).equals( "MD5Version" ) )
				{
					int version = Integer.parseInt(tokens.get(1));
					if( version != 10 )
						throw new IllegalArgumentException( "Not a valid MD5 file, go version " + version + ", need 10" );
				}		
				
				//
				// read number of joints
				//
				if( tokens.get(0).equals( "numJoints" ) )
				{
					int numJoints = Integer.parseInt( tokens.get(1) );
					model.baseSkeleton = new MD5Joint[numJoints];
				}
				
				//
				// read number of meshes
				//
				if( tokens.get(0).equals( "numMeshes" ) )
				{
					int numMeshes = Integer.parseInt( tokens.get(1) );
					model.meshes = new MD5Mesh[numMeshes];
				}
				
				//
				// read joints
				//
				if( tokens.get(0).equals( "joints") )
				{
					for( int i = 0; i < model.baseSkeleton.length; i++ )
					{
						line = reader.readLine();
						tokenize( line, tokens );
						if( tokens.size() == 0 )
						{
							i--;
							continue;
						}
							
						MD5Joint joint = new MD5Joint( );
						
						joint.name = tokens.get(0);
						joint.parent = Integer.parseInt( tokens.get(1) );
						
						joint.pos.x = Float.parseFloat( tokens.get(3) );
						joint.pos.y = Float.parseFloat( tokens.get(4) );
						joint.pos.z = Float.parseFloat( tokens.get(5) );
						
						joint.orient.x = Float.parseFloat( tokens.get(8) );
						joint.orient.y = Float.parseFloat( tokens.get(9) );
						joint.orient.z = Float.parseFloat( tokens.get(10) );
						joint.orient.computeW();
						
						model.baseSkeleton[i] = joint;
					}
				}
				
				//
				// read meshes
				//
				if( tokens.get(0).equals( "mesh" ) && tokens.get(1).equals( "{") )
				{
					MD5Mesh mesh = new MD5Mesh( );
					model.meshes[currMesh++] = mesh;
					
					int vertIndex = 0;
					int triIndex = 0;
					int weightIndex = 0;
					
					
					while( !line.contains( "}" ) )
					{
						line = reader.readLine( );
						tokenize( line, tokens );
						if( tokens.size() == 0 )
							continue;
						
						if( tokens.get(0).equals( "shader" ) )
						{
							mesh.shader = tokens.get(1);
						}
						if( tokens.get(0).equals( "numverts" ) )
						{
							mesh.vertices = new MD5Vertex[Integer.parseInt( tokens.get(1) )];
						}
						if( tokens.get(0).equals( "numtris" ) )
						{
							mesh.triangles = new MD5Triangle[Integer.parseInt( tokens.get(1) )];
						}
						if( tokens.get(0).equals( "numweights" ) )
						{
							mesh.weights = new MD5Weight[Integer.parseInt( tokens.get(1) )];
						}
						if( tokens.get(0).equals( "vert" ) )
						{
							MD5Vertex vert = new MD5Vertex( );
							vertIndex = Integer.parseInt( tokens.get(1) );
							vert.st.x = Float.parseFloat( tokens.get(3) );
							vert.st.y = Float.parseFloat( tokens.get(4) );
							vert.start = Integer.parseInt( tokens.get(6) );
							vert.count = Integer.parseInt( tokens.get(7) );
							
							mesh.vertices[vertIndex] = vert;
						}
						if( tokens.get(0).equals( "tri" ) )
						{
							MD5Triangle tri = new MD5Triangle( );
							triIndex = Integer.parseInt( tokens.get(1) );
							tri.indices[0] = Integer.parseInt( tokens.get(2) );
							tri.indices[1] = Integer.parseInt( tokens.get(3) );							
							tri.indices[2] = Integer.parseInt( tokens.get(4) );
							
							mesh.triangles[triIndex] = tri;
						}
						
						if( tokens.get(0).equals( "weight" ) )
						{
							MD5Weight weight = new MD5Weight( );
							weightIndex = Integer.parseInt( tokens.get(1) );
							weight.joint = Integer.parseInt( tokens.get(2) );
							weight.bias = Float.parseFloat( tokens.get(3) );
							weight.pos.x = Float.parseFloat( tokens.get(5) );
							weight.pos.y = Float.parseFloat( tokens.get(6) );
							weight.pos.z = Float.parseFloat( tokens.get(7) );						
							
							mesh.weights[weightIndex] = weight;
						}
					}
				}
			}
			
			return model;
		}
		catch( Exception ex )
		{
			ex.printStackTrace( );
			return null;
		}			
	}
	
	private static void tokenize( String line, List<String> tokens )
	{
		tokens.clear();
		StringTokenizer tokenizer = new StringTokenizer( line );
		while( tokenizer.hasMoreTokens() )
			tokens.add( tokenizer.nextToken() );
	}
}

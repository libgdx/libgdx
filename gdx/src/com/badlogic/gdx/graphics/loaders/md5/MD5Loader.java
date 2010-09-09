package com.badlogic.gdx.graphics.loaders.md5;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

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
	
	public static MD5Animation loadAnimation( InputStream in )
	{
		BufferedReader reader = new BufferedReader( new InputStreamReader( in ) );
		List<String> tokens = new ArrayList<String>( );
		MD5Animation animation = new MD5Animation( );
		
		try
		{
			int currFrame = 0;
			String line;
			JointInfo[] jointInfos = null;
			BaseFrameJoint[] baseFrame = null;
			float[] animFrameData = null;
			
			while( (line = reader.readLine() ) != null )
			{
				tokenize( line, tokens );
				if( tokens.size() == 0 )
					continue;
				
				if( tokens.get(0).equals( "MD5Version" ) )
				{
					if( !tokens.get(1).equals( "10" ) )
						throw new IllegalArgumentException( "Not a valid MD5 animation file, version is " + tokens.get(1) + ", expected 10" );
				}
				
				if( tokens.get(0).equals( "numFrames" ) )
				{
					int numFrames = Integer.parseInt(tokens.get(1));
					animation.frames = new MD5Joint[numFrames][];
					animation.bounds = new BoundingBox[numFrames];
				}
				
				if( tokens.get(0).equals( "numJoints" ) )
				{
					int numJoints = Integer.parseInt( tokens.get(1) );
					for( int i = 0; i < animation.frames.length; i++ )
					{
						animation.frames[i] = new MD5Joint[numJoints];
						for( int j = 0; j < numJoints; j++ )
							animation.frames[i][j] = new MD5Joint();
					}
					
					jointInfos = new JointInfo[numJoints];
					baseFrame = new BaseFrameJoint[numJoints];
				}
				
				if( tokens.get(0).equals( "frameRate" ) )
				{
					int frameRate = Integer.parseInt( tokens.get(1) );
					animation.frameRate = frameRate;
					animation.secondsPerFrame = 1.0f / frameRate;
				}
				
				if( tokens.get(0).equals( "numAnimatedComponents" ) )
				{
					int numAnimatedComponents = Integer.parseInt( tokens.get(1) );
					animFrameData = new float[numAnimatedComponents];
				}
				
				if( tokens.get(0).equals( "hierarchy" ) )
				{
					for( int i = 0; i < jointInfos.length; i++ )
					{
						line = reader.readLine();
						tokenize( line, tokens );
						if( tokens.size() == 0 || tokens.get(0).equals( "//" ))
						{
							i--;
							continue;
						}
						
						JointInfo jointInfo = new JointInfo();
						jointInfo.name = tokens.get(0);
						jointInfo.parent = Integer.parseInt( tokens.get(1) );
						jointInfo.flags = Integer.parseInt( tokens.get(2) );
						jointInfo.startIndex = Integer.parseInt( tokens.get(3) );
							
						jointInfos[i] = jointInfo;
					}
				}
				
				if( tokens.get(0).equals( "bounds" ) )
				{
					for( int i = 0; i < animation.bounds.length; i++ )
					{
						line = reader.readLine();
						tokenize( line, tokens );
						if( tokens.size() == 0 )
						{
							i--;
							continue;
						}
						
						BoundingBox bounds = new BoundingBox( );
						bounds.min.x = Float.parseFloat( tokens.get( 1 ) );
						bounds.min.y = Float.parseFloat( tokens.get( 2 ) );
						bounds.min.z = Float.parseFloat( tokens.get( 3 ) );
						
						bounds.max.x = Float.parseFloat( tokens.get( 6 ) );
						bounds.max.y = Float.parseFloat( tokens.get( 7 ) );
						bounds.max.z = Float.parseFloat( tokens.get( 8 ) );
						
						animation.bounds[i] = bounds;
					}
				}
				
				if( tokens.get(0).equals( "baseframe" ) )
				{
					for( int i = 0; i < baseFrame.length; i++ )
					{
						line = reader.readLine();
						tokenize( line, tokens );
						if( tokens.size() == 0 )
						{
							i--;
							continue;
						}
						
						BaseFrameJoint joint = new BaseFrameJoint( );
						joint.pos.x = Float.parseFloat( tokens.get(1) );
						joint.pos.y = Float.parseFloat( tokens.get(2) );
						joint.pos.z = Float.parseFloat( tokens.get(3) );
						
						joint.orient.x = Float.parseFloat( tokens.get(6) );
						joint.orient.y = Float.parseFloat( tokens.get(7) );
						joint.orient.z = Float.parseFloat( tokens.get(8) );
						joint.orient.computeW();
						
						baseFrame[i] = joint;
					}
				}
				
				if( tokens.get(0).equals( "frame" ) )
				{
					int frameIndex = Integer.parseInt( tokens.get(1) );
					
					int i = 0;
					line = reader.readLine();
					tokenize( line, tokens );
					while( tokens.get(0).equals( "}" ) == false )
					{	
						for( int j = 0; j < tokens.size(); j++ )
							animFrameData[i++] = Float.parseFloat(tokens.get(j) );
						
						line = reader.readLine();
						tokenize( line, tokens );
					}
					
					buildFrameSkeleton( jointInfos, baseFrame, animFrameData, animation, frameIndex );
				}
			}
			
			return animation;
		}
		catch( Exception ex )
		{
			ex.printStackTrace( );
			return null;
		}
	}
	
	private static void buildFrameSkeleton( JointInfo[] jointInfos, BaseFrameJoint[] baseFrame, float[] animFrameData, MD5Animation animation, int frameIndex )
	{	
		MD5Joint[] skelFrame = animation.frames[frameIndex];
		
		for( int i = 0; i < jointInfos.length; i++ )
		{
			BaseFrameJoint baseJoint = baseFrame[i];
			Vector3 animatedPos = new Vector3( );
			MD5Quaternion animatedOrient = new MD5Quaternion( );
			int j = 0;
			
			animatedPos.set( baseJoint.pos );
			animatedOrient.set( baseJoint.orient );
			
			if( (jointInfos[i].flags & 1) != 0  )
			{
				animatedPos.x = animFrameData[jointInfos[i].startIndex + j];
				j++;
			}
			
			if( (jointInfos[i].flags & 2 ) != 0 )
			{
				animatedPos.y = animFrameData[jointInfos[i].startIndex + j];
				j++;
			}
			
			if( (jointInfos[i].flags & 4) != 0 )
			{
				animatedPos.z = animFrameData[jointInfos[i].startIndex + j];
				j++;
			}
			
			if( (jointInfos[i].flags & 8) != 0 )
			{
				animatedOrient.x = animFrameData[jointInfos[i].startIndex + j];
				j++;
			}
			
			if( (jointInfos[i].flags & 16) != 0 )
			{
				animatedOrient.y = animFrameData[jointInfos[i].startIndex + j];
				j++;
			}
			
			if( (jointInfos[i].flags & 32) != 0 )
			{
				animatedOrient.z = animFrameData[jointInfos[i].startIndex + j];
				j++;
			}
			
			animatedOrient.computeW();
			
			MD5Joint thisJoint = skelFrame[i];
			
			int parent = jointInfos[i].parent;
			thisJoint.parent = parent;
			thisJoint.name = jointInfos[i].name;
			
			if( thisJoint.parent < 0 )
			{
				thisJoint.pos.set( animatedPos );
				thisJoint.orient.set( animatedOrient );
			}
			else
			{
				MD5Joint parentJoint = skelFrame[parent];
				parentJoint.orient.rotate( animatedPos );
				thisJoint.pos.x = animatedPos.x + parentJoint.pos.x;
				thisJoint.pos.y = animatedPos.y + parentJoint.pos.y;
				thisJoint.pos.z = animatedPos.z + parentJoint.pos.z;
				
				thisJoint.orient.set( parentJoint.orient );
				thisJoint.orient.multiply( animatedOrient );
				thisJoint.orient.normalize();
			}
		}
	}
	
	private static void tokenize( String line, List<String> tokens )
	{
		tokens.clear();
		StringTokenizer tokenizer = new StringTokenizer( line );
		while( tokenizer.hasMoreTokens() )
			tokens.add( tokenizer.nextToken() );
	}
	
	static class JointInfo
	{
		public String name;
		public int parent;
		public int flags;
		public int startIndex;
	}
	
	static class BaseFrameJoint
	{
		public final Vector3 pos = new Vector3( );
		public final MD5Quaternion orient = new MD5Quaternion( );
	}
}

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
		MD5Quaternion quat = new MD5Quaternion( );
		
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
					model.baseSkeleton = new MD5Joints();
					model.baseSkeleton.names = new String[numJoints];
					model.baseSkeleton.numJoints = numJoints;
					model.baseSkeleton.joints = new float[numJoints*8];
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
					for( int i = 0; i < model.baseSkeleton.numJoints; i++ )
					{
						line = reader.readLine();
						tokenize( line, tokens );
						if( tokens.size() == 0 )
						{
							i--;
							continue;
						}							
						
						int jointIdx = i << 3;
						model.baseSkeleton.names[i] = tokens.get(0);;
						model.baseSkeleton.joints[jointIdx] =  Integer.parseInt( tokens.get(1) );;
						model.baseSkeleton.joints[jointIdx+1] = Float.parseFloat( tokens.get(3) );
						model.baseSkeleton.joints[jointIdx+2] = Float.parseFloat( tokens.get(4) );
						model.baseSkeleton.joints[jointIdx+3] = Float.parseFloat( tokens.get(5) );											
						
						quat.x = Float.parseFloat( tokens.get(8) );
						quat.y = Float.parseFloat( tokens.get(9) );
						quat.z = Float.parseFloat( tokens.get(10) );
						quat.computeW();						
						
						model.baseSkeleton.joints[jointIdx+4] = quat.x;
						model.baseSkeleton.joints[jointIdx+5] = quat.y;
						model.baseSkeleton.joints[jointIdx+6] = quat.z;
						model.baseSkeleton.joints[jointIdx+7] = quat.w;						
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
							mesh.vertices = new float[Integer.parseInt( tokens.get(1) )*4];
							mesh.numVertices = mesh.vertices.length / 4;
						}
						if( tokens.get(0).equals( "numtris" ) )
						{							
							mesh.indices = new short[Integer.parseInt( tokens.get(1) )*3];
							mesh.numTriangles = mesh.indices.length / 3;
						}
						if( tokens.get(0).equals( "numweights" ) )
						{							
							mesh.weights = new float[Integer.parseInt( tokens.get(1) )*5];
							mesh.numWeights = mesh.weights.length / 5;
						}
						if( tokens.get(0).equals( "vert" ) )
						{							
							vertIndex = Integer.parseInt( tokens.get(1) );							
							
							int idx = vertIndex*4;
							mesh.vertices[idx++] = Float.parseFloat( tokens.get(3) ); // s
							mesh.vertices[idx++] = Float.parseFloat( tokens.get(4) ); // t
							mesh.vertices[idx++] = Float.parseFloat( tokens.get(6) ); // start
							mesh.vertices[idx++] = Float.parseFloat( tokens.get(7) ); // count
						}
						if( tokens.get(0).equals( "tri" ) )
						{							
							triIndex = Integer.parseInt( tokens.get(1) );													
							
							int idx = triIndex*3;
							mesh.indices[idx++] = Short.parseShort( tokens.get(2) ); // idx 1
							mesh.indices[idx++] = Short.parseShort( tokens.get(3) ); // idx 2
							mesh.indices[idx++] = Short.parseShort( tokens.get(4) ); // idx 3
						}
						
						if( tokens.get(0).equals( "weight" ) )
						{							
							weightIndex = Integer.parseInt( tokens.get(1) );													
							
							int idx = weightIndex*5;
							mesh.weights[idx++] = Integer.parseInt( tokens.get(2) ); // joint
							mesh.weights[idx++] = Float.parseFloat( tokens.get(3) ); // bias
							mesh.weights[idx++] = Float.parseFloat( tokens.get(5) ); // pos.x
							mesh.weights[idx++] = Float.parseFloat( tokens.get(6) ); // pos.y
							mesh.weights[idx++] = Float.parseFloat( tokens.get(7) ); // pos.z
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
					animation.frames = new MD5Joints[numFrames];
					animation.bounds = new BoundingBox[numFrames];
				}
				
				if( tokens.get(0).equals( "numJoints" ) )
				{
					int numJoints = Integer.parseInt( tokens.get(1) );
					for( int i = 0; i < animation.frames.length; i++ )
					{
						animation.frames[i] = new MD5Joints();
						animation.frames[i].numJoints = numJoints;
						animation.frames[i].names = new String[numJoints];
						animation.frames[i].joints = new float[numJoints * 8];						
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
	
	static MD5Quaternion thisOrient = new MD5Quaternion( );
	static MD5Quaternion parentOrient = new MD5Quaternion( );
	static Vector3 parentPos = new Vector3( );
	
	private static void buildFrameSkeleton( JointInfo[] jointInfos, BaseFrameJoint[] baseFrame, float[] animFrameData, MD5Animation animation, int frameIndex )
	{	
		MD5Joints skelFrame = animation.frames[frameIndex];
		
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
			
			int thisJointIdx = i << 3;
			skelFrame.names[i] = jointInfos[i].name;
			skelFrame.joints[thisJointIdx] = jointInfos[i].parent;									
			
			if( jointInfos[i].parent < 0 )
			{
				skelFrame.joints[thisJointIdx+1] = animatedPos.x;
				skelFrame.joints[thisJointIdx+2] = animatedPos.y;
				skelFrame.joints[thisJointIdx+3] = animatedPos.z;			
				
				skelFrame.joints[thisJointIdx+4] = animatedOrient.x;
				skelFrame.joints[thisJointIdx+5] = animatedOrient.y;
				skelFrame.joints[thisJointIdx+6] = animatedOrient.z;
				skelFrame.joints[thisJointIdx+7] = animatedOrient.w;							
			}
			else
			{
				int parentJointIdx = jointInfos[i].parent << 3;
				parentPos.x = skelFrame.joints[parentJointIdx + 1];
				parentPos.y = skelFrame.joints[parentJointIdx + 2];
				parentPos.z = skelFrame.joints[parentJointIdx + 3];
				
				parentOrient.x = skelFrame.joints[parentJointIdx + 4];
				parentOrient.y = skelFrame.joints[parentJointIdx + 5];
				parentOrient.z = skelFrame.joints[parentJointIdx + 6];
				parentOrient.w = skelFrame.joints[parentJointIdx + 7];
				
				parentOrient.rotate( animatedPos );
				skelFrame.joints[thisJointIdx+1] = animatedPos.x + parentPos.x;
				skelFrame.joints[thisJointIdx+2] = animatedPos.y + parentPos.y;
				skelFrame.joints[thisJointIdx+3] = animatedPos.z + parentPos.z;
								
				parentOrient.multiply( animatedOrient );
				parentOrient.normalize();
				skelFrame.joints[thisJointIdx+4] = parentOrient.x;
				skelFrame.joints[thisJointIdx+5] = parentOrient.y;
				skelFrame.joints[thisJointIdx+6] = parentOrient.z;
				skelFrame.joints[thisJointIdx+7] = parentOrient.w;							
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

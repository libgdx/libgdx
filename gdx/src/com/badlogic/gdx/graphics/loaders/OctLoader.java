package com.badlogic.gdx.graphics.loaders;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.badlogic.gdx.graphics.FixedPointMesh;
import com.badlogic.gdx.graphics.FloatMesh;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.ModelWriter;
import com.badlogic.gdx.math.Vector;

/**
 * Loads an OCT file as found in many of Paul Nettle's demos.
 * See the source at http://www.paulnettle.com/pub/FluidStudios/CollisionDetection/Fluid_Studios_Collision_Detection_Demo_and_Source.zip
 * for more information
 * 
 * @author mzechner
 *
 */
public class OctLoader 
{
	private static class OctVert
	{
		public float tu, tv;
		@SuppressWarnings("unused")
		public float lu, lv;
		public float x, y, z;
	}
	
	private static class OctFace
	{
		public int start;
		public int num;
		@SuppressWarnings("unused")
		public int id;
		@SuppressWarnings("unused")
		public int lid;
		@SuppressWarnings("unused")
		public float nx, ny, nz, d;
	}
	
	public static Mesh loadOct( InputStream inputStream, boolean useFloats, Vector start )
	{
		LittleEndianInputStream in =  new LittleEndianInputStream( new BufferedInputStream(inputStream) );		
		
		try
		{
			int numVertices = in.readInt();
			int numFaces = in.readInt();
			int numTextures = in.readInt();
			int numLightmaps = in.readInt();
			
			OctVert[] verts = new OctVert[numVertices];
			OctFace[] faces = new OctFace[numFaces];
			
			for( int i = 0; i < numVertices; i++ )
			{
				OctVert v = new OctVert( );
				v.tu = in.readFloat(); v.tv = in.readFloat();
				v.lu = in.readFloat(); v.lv = in.readFloat();
				v.x = in.readFloat(); v.y = in.readFloat(); v.z = in.readFloat();		
				verts[i] = v;
			}
			
			int numTriangles = 0;
			for( int i = 0; i < numFaces; i++ )
			{
				OctFace f = new OctFace( );
				f.start = in.readInt();
				f.num = in.readInt();
				f.id = in.readInt();
				f.lid = in.readInt();
				f.nx = in.readFloat(); f.ny = in.readFloat(); f.nz = in.readFloat(); f.d = in.readFloat();
				faces[i] = f;
				numTriangles += f.num - 2;
			}
			
			for( int i = 0; i < numTextures * (64 + 4); i++ )
				in.read();
			
			for( int i = 0; i < numLightmaps * (49152 + 4); i++ )
				in.read();

			start.set( in.readFloat(), in.readFloat(), in.readFloat() );			
			
			float[] triangles = new float[numTriangles * 3 * (3 + 3 + 2)];
			
			int idx = 0;
			for( int i = 0; i < numFaces; i++ )
			{
				OctFace f = faces[i];				
				OctVert v1 = verts[f.start];
				
				for( int j = 0; j < f.num - 2; j++ )				
				{					
					OctVert v2 = verts[f.start + j + 1];
					OctVert v3 = verts[f.start + j + 2];
					
					triangles[idx++] = v1.x; triangles[idx++] = v1.y; triangles[idx++] = v1.z;
					triangles[idx++] = f.nx; triangles[idx++] = f.ny; triangles[idx++] = f.nz;
					triangles[idx++] = v1.tu; triangles[idx++] = v1.tv;
					triangles[idx++] = v2.x; triangles[idx++] = v2.y; triangles[idx++] = v2.z;
					triangles[idx++] = f.nx; triangles[idx++] = f.ny; triangles[idx++] = f.nz;
					triangles[idx++] = v2.tu; triangles[idx++] = v2.tv;
					triangles[idx++] = v3.x; triangles[idx++] = v3.y; triangles[idx++] = v3.z;
					triangles[idx++] = f.nx; triangles[idx++] = f.ny; triangles[idx++] = f.nz;
					triangles[idx++] = v3.tu; triangles[idx++] = v3.tv;
				}
			}
			
			Mesh m = null;
			if( useFloats )
				m = new FloatMesh( numTriangles * 3, 3, false, true, true, 1, 2, false, 0 );
			else
				m = new FixedPointMesh( numTriangles * 3, 3, false, true, true, 1, 2, false, 0 );
			
			m.setVertices( triangles );
			
			return m;
		}
		catch( Exception ex )
		{
			ex.printStackTrace();
			return null;
		}			
	}
	
	public static void main( String[] argv ) throws IOException
	{
		Mesh m = OctLoader.loadOct( new FileInputStream( "data/house5.oct" ), true, new Vector() );
		ModelWriter.writeObj( new FileOutputStream( "data/house5.obj" ), (FloatMesh)m );
	}
}

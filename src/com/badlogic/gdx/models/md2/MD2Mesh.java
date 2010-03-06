package com.badlogic.gdx.models.md2;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Mesh;
import com.badlogic.gdx.Mesh.PrimitiveType;

public class MD2Mesh 
{
	public final MD2Frame frames[];
	public final short indices[];
	public final float texCoords[];
	public final float normals[];	
	public final Mesh mesh;
	public final Application app;
	public final int numVertices;	
	
	public FloatBuffer outBuffer;
	
	public static float interpolationTime;
	public static float renderingTime;
	
	public MD2Mesh( Application app, MD2Frame[] frames, short[] indices, float texCoords[], float normals[] )
	{
		this.app = app;
		this.mesh = app.newMesh( frames[0].vertices.length / 3, false, false, true, true, indices.length, false );
		this.frames = frames;
		this.indices = indices;
		this.texCoords = texCoords;
		this.normals = normals;
		this.numVertices = frames[0].vertices.length / 3;		
		System.arraycopy( indices, 0, mesh.getIndices(), 0, indices.length );
		System.arraycopy( texCoords, 0, mesh.getUV(), 0, texCoords.length );	
		System.arraycopy( frames[0].vertices, 0, mesh.getVertices(), 0, frames[0].vertices.length );
		
		ByteBuffer buffer = ByteBuffer.allocateDirect( 4 * mesh.getVertices().length );
		buffer.order(ByteOrder.nativeOrder());
		outBuffer = buffer.asFloatBuffer();
	}			

	public void render(int startIdx, int endIdx, float alpha) 
	{
		mesh.reset();
		long start = System.nanoTime();
		interpolateFrame( startIdx, endIdx, alpha );
		outBuffer.position(0);
		outBuffer.limit(outBuffer.capacity());
		outBuffer.get(mesh.getVertices(), 0, mesh.getVertices().length );
		interpolationTime += (System.nanoTime()-start) / 1000000.0f;
		mesh.setDirty();		
		
		start = System.nanoTime();
		mesh.render( PrimitiveType.Triangles, indices.length, 0 );
		renderingTime += (System.nanoTime()-start) / 1000000.0f;
	}
	
	public void dispose( )
	{
		mesh.dispose();
	}

	private void interpolateFrame(int startIdx, int endIdx, float alpha) 
	{	
		app.interpolate( frames[startIdx].verticesBuffer, frames[endIdx].verticesBuffer, outBuffer, alpha, mesh.getVertices().length );
//		float[] frame = mesh.getVertices();
//		float[] src = frames[startIdx].vertices;
//		float[] dst = frames[endIdx].vertices;
//		for( int i = 0; i < frame.length; i++ )
//		{
//			float d = dst[i];
//			float s = src[i];
//			frame[i] = s + (d - s) * alpha;
//		}
	}
}

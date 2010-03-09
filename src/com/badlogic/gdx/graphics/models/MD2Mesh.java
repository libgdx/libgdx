/**
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
 *  along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.badlogic.gdx.graphics.models;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Mesh.PrimitiveType;

/**
 * Encapsulates a loaded MD2Mesh. Not to useful on its own so
 * combine it with one or more {@link MD2Instance}s. Can be loaded
 * via the {@link MD2Loader} class. The MD2Mesh has to be disposed
 * via {@link MD2Mesh.dispose()} when it is no longer used.
 * 
 * @author mzechner
 *
 */
public final class MD2Mesh 
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
	
	MD2Mesh( Application app, MD2Frame[] frames, short[] indices, float texCoords[], float normals[] )
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

	/**
	 * Render an inbetween frame between frame startIdx and frame endIdx, weighted by
	 * alpha in the range [0,1].
	 * 
	 * @param startIdx The starting frame
	 * @param endIdx The ending frame
	 * @param alpha The weight between the two frames
	 */
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
	
	/**
	 * Disposes the mesh.
	 */
	public void dispose( )
	{
		mesh.dispose();
	}

	private void interpolateFrame(int startIdx, int endIdx, float alpha) 
	{	
		float[] frame = mesh.getVertices();
		float[] src = frames[startIdx].vertices;
		float[] dst = frames[endIdx].vertices;
		for( int i = 0; i < frame.length; i++ )
		{
			float d = dst[i];
			float s = src[i];
			frame[i] = s + (d - s) * alpha;
		}
	}
}

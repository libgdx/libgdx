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
package com.badlogic.gdx.math.collision;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector;

/**
 * A collision mesh holds the triangles against which collision
 * detection can be performed. Besides the triangle vertex positions
 * the triangle plane representation is also stored.   
 * 
 * @author badlogicgames@gmail.com
 *
 */
public class CollisionMesh 
{
	/** the number of triangles in this mesh **/
	private final int numTriangles;
	
	/** the triangles **/
	private final float[] triangles;
	
	/** the plane equations of the triangles **/
	private final float[] planes;
	
	/** whether the triangles are clockwise or not **/
	private final boolean isClockWise;

	/**
	 * Constructs a new CollisionMesh from a {@link FloatMesh}. The FloatMesh
	 * is assumed to contain triangles only, either indexed or non-indexed. From
	 * the triangles plane equation coefficients are constructed and stored along
	 * with the actual triangle vertex positions. The triangles in the FloatMesh
	 * all have to have the same winding order. The parameter clockWise specifies
	 * if the triangles are in clockwise order or in counter clockwise order. This
	 * information is needed to construct the plane normals properly. Given a triangle
	 * in the x/y plane defined in counter clockwise order the normal of the resulting 
	 * plane will be (0,0,1) if clockWise is set to false. Note that the standard
	 * OpenGL coordinate system is assumed with the negative z-Axis pointing inward
	 * the screen, in other words a right hand coordinate system.
	 *  
	 * @param mesh the FloatMesh to construct the CollisionMesh from
	 * @param clockWise whether the triangles are given in clockwise order or counter clockwise order
	 */
	public CollisionMesh( Mesh mesh, boolean clockWise )
	{
		if( mesh.usesFixedPoint() )
			throw new IllegalArgumentException( "Mesh must use floats" );
		if( mesh.getMaxIndices() > 0 && mesh.getNumIndices() % 3 != 0 )
			throw new IllegalArgumentException( "Mesh must hold triangles" );
		if( mesh.getMaxIndices() == 0 && mesh.getNumVertices() % 3 != 0 )
			throw new IllegalArgumentException( "Mesh must hold triangles" );
		VertexAttribute position = mesh.getVertexAttribute( Usage.Position );
		if(  position == null )
			throw new IllegalArgumentException( "Ill-shaped mesh, does not have a position vertex attribute" );
		if( position.numComponents != 3 )
			throw new IllegalArgumentException( "Vertex coordinates must be three dimensional" );
		
		numTriangles = mesh.getMaxIndices()>0?mesh.getNumIndices()/3:mesh.getNumVertices()/3;
		triangles = new float[3 * 3 * numTriangles];
		planes = new float[4 * numTriangles];
		isClockWise = clockWise;
		
		if( mesh.getMaxIndices()>0 )
			fillFromIndexedMesh( position, mesh );
		else
			fillFromMesh( position, mesh );
		calculatePlanes( clockWise );
	}
	
	private void fillFromIndexedMesh( VertexAttribute position, Mesh mesh )
	{
		int idx = 0;
		int offset = position.offset / 4;
		int stride = mesh.getVertexSize() / 4;
		float[] vertices = new float[mesh.getVerticesBuffer().limit()/4];
		mesh.getVerticesBuffer().asFloatBuffer().get(vertices);
		short[] indices = new short[mesh.getIndicesBuffer().limit() / 4];
		mesh.getIndicesBuffer().get(indices);
		
		for( int i = 0; i < mesh.getNumIndices(); i+=3 )
		{			
			int idx1 = indices[i] * stride + offset;
			int idx2 = indices[i+1] * stride + offset;
			int idx3 = indices[i+2] * stride + offset;
			
			triangles[idx++] = vertices[idx1]; 
			triangles[idx++] = vertices[idx1+1];
			triangles[idx++] = vertices[idx1+2];
			
			triangles[idx++] = vertices[idx2]; 
			triangles[idx++] = vertices[idx2+1];
			triangles[idx++] = vertices[idx2+2];
			
			triangles[idx++] = vertices[idx3]; 
			triangles[idx++] = vertices[idx3+1];
			triangles[idx++] = vertices[idx3+2];
		}
	}
	
	private void fillFromMesh( VertexAttribute position, Mesh mesh )
	{
		int idx = 0;
		int offset = position.offset / 4;
		int stride = mesh.getVertexSize() / 4;
		float[] vertices = new float[mesh.getVerticesBuffer().limit()/4];
		mesh.getVerticesBuffer().asFloatBuffer().get(vertices);
		for( int i = 0; i < mesh.getNumVertices(); i+=3 )
		{			
			int idx1 = (i) * stride + offset;
			int idx2 = (i+1) * stride + offset;
			int idx3 = (i+2) * stride + offset;
			
			triangles[idx++] = vertices[idx1]; 
			triangles[idx++] = vertices[idx1+1];
			triangles[idx++] = vertices[idx1+2];
			
			triangles[idx++] = vertices[idx2]; 
			triangles[idx++] = vertices[idx2+1];
			triangles[idx++] = vertices[idx2+2];
			
			triangles[idx++] = vertices[idx3]; 
			triangles[idx++] = vertices[idx3+1];
			triangles[idx++] = vertices[idx3+2];
		}
	}
	
	private void calculatePlanes( boolean clockWise )
	{
		Vector a = new Vector( );		
		Vector b = new Vector( );
		Vector c = new Vector( );
		Plane plane = new Plane( new Vector( 0, 0, 1 ), 0 );
		
		int idx = 0;
		int pidx = 0;
		for( int i = 0; i < numTriangles; i++ )
		{
			a.set( triangles[idx++], triangles[idx++], triangles[idx++] );
			b.set( triangles[idx++], triangles[idx++], triangles[idx++] );
			c.set( triangles[idx++], triangles[idx++], triangles[idx++] );
			
			if( clockWise )
				plane.set( c, b, a );
			else
				plane.set( a, b, c );
			
			planes[pidx++] = plane.getNormal().x;
			planes[pidx++] = plane.getNormal().y;
			planes[pidx++] = plane.getNormal().z;
			planes[pidx++] = plane.getD();
		}
	}
	
	/**  
	 * @return the number of triangles
	 */
	public int getNumTriangles( )
	{
		return numTriangles;
	}
	
	/**
	 * Returns the triangles in form of a float array holding the
	 * vertex positions. 
	 * 
	 * @return the vertex data of the triangles.
	 */
	public float[] getTriangles( )
	{
		return triangles;
	}
	
	/**
	 * Returns a float array holding the plane equation coefficients of the {@link CollisionMesh.getNumTriangles()}
	 * triangles this mesh contains. Each triangle has exactly one plane equation so you get {@link CollisionMesh.getNumTriangles()}
	 * planes. Each plane equation is stored as 4 floats, the first three holding the normal and the last holding the distance
	 * to the origin.
	 * 
	 * @return the plane equation coefficients of the triangles.
	 */
	public float[] getPlanes( )
	{
		return planes;
	}	
	
	public String toString( )
	{
		StringBuffer buffer = new StringBuffer( );
		
		int tIdx = 0;
		int pIdx = 0;
		for( int i = 0; i < numTriangles; i++ )
		{
			buffer.append( "[" );
			buffer.append( triangles[tIdx++] );
			buffer.append( "," );
			buffer.append( triangles[tIdx++] );
			buffer.append( "," );
			buffer.append( triangles[tIdx++] );
			buffer.append( "|" );
			buffer.append( triangles[tIdx++] );
			buffer.append( "," );
			buffer.append( triangles[tIdx++] );
			buffer.append( "," );
			buffer.append( triangles[tIdx++] );
			buffer.append( "|" );
			buffer.append( triangles[tIdx++] );
			buffer.append( "," );
			buffer.append( triangles[tIdx++] );
			buffer.append( "," );
			buffer.append( triangles[tIdx++] );
			buffer.append( "]-[" );
			buffer.append( planes[pIdx++] );
			buffer.append( "," );
			buffer.append( planes[pIdx++] );
			buffer.append( "," );
			buffer.append( planes[pIdx++] );
			buffer.append( "," );
			buffer.append( planes[pIdx++] );
			buffer.append( "]\n" );
		}
		
		return buffer.toString();
	}
	
	/**
	 * @return whether the triangles are given in clockwise or counter clockwise order
	 */
	public boolean isClockWise( )
	{
		return isClockWise;
	}
}

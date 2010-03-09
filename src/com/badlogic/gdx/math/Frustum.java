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
package com.badlogic.gdx.math;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.Plane.PlaneSide;

/**
 * Encapsulates a view frustum based on clipping planes. Offers
 * methods to perform culling of simple geometric objects like points,
 * spheres and {@link BoundingBox}es.
 * @author badlogicgames@gmail.com
 *
 */
public final class Frustum
{
	private static final long serialVersionUID = -7082961504074610513L;
	protected List<Plane> planes = new ArrayList<Plane>(6);
	protected float near, far, fov, aspect, near_width, near_height, far_width, far_height, tang;	
	
	/**
	 * @return The list of {@link Plane}s that make up this frustum.
	 */
	public List<Plane> getPlanes( )
	{
		return planes;
	}
	
	/**
	 * Adds a new {@link Plane} to the frustum
	 * @param plane The plane to add
	 */
	public void addPlane( Plane plane )
	{
		planes.add( plane );
	}
	
	/**
	 * Sets the camera parameters.
	 * 
	 * @param fov The field of view in degrees
	 * @param aspect The aspect ratio
	 * @param near The near plane
	 * @param far The far plane
	 */
	public void setCameraParameters( float fov, float aspect, float near, float far )
	{
		this.near = near;
		this.far = far;
		this.aspect = aspect;
		this.fov = fov;
		
		tang = (float)Math.tan( Math.toRadians( fov * 0.5 ) );
		this.near_height = near * tang;	
		this.near_width = this.near_height * aspect;
		this.far_height = far * tang;
		this.far_width = this.far_height * aspect;		
	}
	
	Vector tmp = new Vector();
	Vector tmp2 = new Vector();
	
	static Vector X = new Vector();
	static Vector Y = new Vector();
	static Vector Z = new Vector();
	
	static Vector near_tl = new Vector();
	static Vector near_tr = new Vector();
	static Vector near_bl = new Vector();
	static Vector near_br = new Vector();
	
	static Vector far_tl = new Vector();
	static Vector far_tr = new Vector();
	static Vector far_bl = new Vector();
	static Vector far_br = new Vector();
	
	static Vector near_center = new Vector( );
	static Vector far_center = new Vector( );
	
	/**
	 * Sets the camera orientation. This will add 6 planes
	 * for near, far, left, right, top and bottom of the
	 * frustum. Call {@link Frustum.setCameraParameters} before
	 * calling this function.
	 * 
	 * @param pos The camera position
	 * @param dir The camera direction with unit length
	 * @param up The camera up vector with unit length
	 */
	public void setCameraOrientation( Vector pos, Vector dir, Vector up )
	{				
		X.set(0,0,0);
		Y.set(0,0,0);
		Z.set(0,0,0);
		
		Z.set( dir.tmp().mul(-1) ).nor();
		X.set( up.tmp().crs( Z ) ).nor();
		Y.set( Z.tmp().crs(X) ).nor();
		near_center.set( pos.tmp().sub( tmp.set(Z).mul( near ) ) );
		far_center.set( pos.tmp().sub( tmp.set(Z).mul( far ) ) );			
		
		near_tl.set( near_center.tmp().add( tmp.set(Y).mul( near_height) ).sub( tmp2.set(X).mul( near_width ) ) );
		near_tr.set( near_center.tmp().add( tmp.set(Y).mul( near_height) ).add( tmp2.set(X).mul( near_width ) ) );
		near_bl.set( near_center.tmp().sub( tmp.set(Y).mul( near_height) ).sub( tmp2.set(X).mul( near_width ) ) );
		near_br.set( near_center.tmp().sub( tmp.set(Y).mul( near_height) ).add( tmp2.set(X).mul( near_width ) ) );		
		
		far_tl.set( far_center.tmp().add( tmp.set(Y).mul( far_height) ).sub( tmp2.set(X).mul( far_width ) ) );
		far_tr.set( far_center.tmp().add( tmp.set(Y).mul( far_height) ).add( tmp2.set(X).mul( far_width ) ) );
		far_bl.set( far_center.tmp().sub( tmp.set(Y).mul( far_height) ).sub( tmp2.set(X).mul( far_width ) ) );
		far_br.set( far_center.tmp().sub( tmp.set(Y).mul( far_height) ).add( tmp2.set(X).mul( far_width ) ) );
				
		if( planes.size() != 6 )
		{
			planes.clear();
			planes.add( new Plane( near_tr, near_tl, far_tl ) );
			planes.add( new Plane( near_bl, near_br, far_br ) );
			planes.add( new Plane( near_tl, near_bl, far_bl ) );
			planes.add( new Plane( near_br, near_tr, far_br ) );
			planes.add( new Plane( near_tl, near_tr, near_br ) );
			planes.add( new Plane( far_tr, far_tl, far_bl ) );
		}
		else
		{
			planes.get(0).set(near_tr, near_tl, far_tl);
			planes.get(1).set(near_bl, near_br, far_br);
			planes.get(2).set(near_tl, near_bl, far_bl);
			planes.get(3).set(near_br, near_tr, far_br);
			planes.get(4).set(near_tl, near_tr, near_br);
			planes.get(5).set(far_tr, far_tl, far_bl);
			
		}
	}
	
	public String toString( )
	{
		StringBuilder builder = new StringBuilder( );
		
		for( Plane plane: planes )
		{
			builder.append( plane.normal );
			builder.append( plane.d );
			builder.append( "\n" );
		}
		
		return builder.toString();
	}
	
	/**
	 * Returns wheter the point is in the frustum.
	 * 
	 * @param point The point
	 * @return Wheter the point is in the frustum.
	 */
	public boolean pointInFrustum( Vector point )
	{
		for( int i = 0; i < planes.size(); i++ )		
		{
			PlaneSide result = planes.get(i).testPoint( point );
			if( result == PlaneSide.Back )
				return false;
		}
		
		return true;
	}
	
	/**
	 * Returns wheter the given sphere is in the frustum.
	 * 
	 * @param center The center of the sphere
	 * @param radius The radius of the sphere
	 * @return Wheter the sphere is in the frustum
	 */
	public boolean sphereInFrustum( Vector center, float radius )
	{
		for( int i = 0; i < planes.size(); i++ )		
			if( planes.get(i).distance( center ) < -radius )
				return false;		
		
		return true;
	}
	
	/**
	 * Returns wheter the given sphere is in the frustum not checking
	 * wheter it is behind the near and far clipping plane.
	 * 
	 * @param center The center of the sphere
	 * @param radius The radius of the sphere
	 * @return Wheter the sphere is in the frustum
	 */
	public boolean sphereInFrustumWithoutNearFar( Vector center, float radius )
	{
		for( int i = 0; i < planes.size(); i++ )		
			if( planes.get(i).distance( center ) < -radius )
				return false;		
		
		return true;
	}
	
	/**
	 * Returns wheter the given {@link BoundingBox} is in the frustum.
	 * 
	 * @param bounds The bounding box
	 * @return Wheter the bounding box is in the frustum
	 */
	public boolean boundsInFrustum( BoundingBox bounds )
	{
		for( int i = 0; i < planes.size(); i++ )
		{
			int out = 0;
			for( int j = 0; j < bounds.getCorners().length; j++ )			
				if( planes.get(i).testPoint(bounds.getCorners()[j]) == PlaneSide.Back )
					out++;			
			
			if( out == 8 )
				return false;
		}
		
		return true;
	}
	
	/**
	 * Calculates the pick ray for the given window coordinates. Assumes
	 * the window coordinate system has it's y downwards. The returned
	 * Ray is a member of this instance so don't reuse it outside this 
	 * class. 
	 * 
	 * @param screen_width The window width in pixels
	 * @param screen_height The window height in pixels
	 * @param mouse_x The window x-coordinate
	 * @param mouse_y The window y-coordinate
	 * @param pos The camera position
	 * @param dir The camera direction, having unit length
	 * @param up The camera up vector, having unit length
	 * @return the picking ray.
	 */		
	Ray ray = new Ray( new Vector(), new Vector() );
	public Ray calculatePickRay( float screen_width, float screen_height, float mouse_x, float mouse_y, 
								  Vector pos, Vector dir, Vector up )
	{
		float n_x = mouse_x - screen_width / 2.0f;
		float n_y = mouse_y - screen_height / 2.0f;
		n_x /= screen_width / 2.0f;
		n_y /= screen_height / 2.0f;					
		
		Z.set( dir.tmp().mul(-1) ).nor();
		X.set( up.tmp().crs( Z ) ).nor();
		Y.set( Z.tmp().crs(X) ).nor();
		near_center.set( pos.tmp3().sub( Z.tmp2().mul( near ) ) );		
		Vector near_point = X.tmp3().mul( near_width ).mul( n_x ).add( Y.tmp2().mul( near_height).mul(n_y ) );
		near_point.add( near_center );		
		
		return ray.set( near_point.tmp(), near_point.sub( pos ).nor() );
	}
}

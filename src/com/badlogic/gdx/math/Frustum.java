package com.badlogic.gdx.math;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.Plane.Intersection;

public class Frustum implements Serializable
{
	private static final long serialVersionUID = -7082961504074610513L;
	protected List<Plane> planes = new ArrayList<Plane>(6);
	protected float near, far, fov, aspect, near_width, near_height, far_width, far_height, tang;	
	
	public List<Plane> getPlanes( )
	{
		return planes;
	}
	
	public void addPlane( Plane plane )
	{
		planes.add( plane );
	}
	
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
	
	public boolean pointInFrustum( Vector p )
	{
		for( int i = 0; i < planes.size(); i++ )		
		{
			Intersection result = planes.get(i).testPoint( p );
			if( result == Intersection.Back )
				return false;
		}
		
		return true;
	}
	
	public boolean sphereInFrustum( Vector center, float radius )
	{
		for( int i = 0; i < planes.size(); i++ )		
			if( planes.get(i).distance( center ) < -radius )
				return false;		
		
		return true;
	}
	
	
	public boolean sphereInFrustumWithoutNearFar( Vector center, float radius )
	{
		for( int i = 0; i < planes.size(); i++ )		
			if( planes.get(i).distance( center ) < -radius )
				return false;		
		
		return true;
	}
	
	public boolean boundsInFrustum( Bounds bounds )
	{
		for( int i = 0; i < planes.size(); i++ )
		{
			int out = 0;
			for( int j = 0; j < bounds.getCorners().length; j++ )			
				if( planes.get(i).testPoint(bounds.getCorners()[j]) == Intersection.Back )
					out++;			
			
			if( out == 8 )
				return false;
		}
		
		return true;
	}
	
	/**
	 * calculates the pick ray for the given window coordinates. assumes
	 * the window coordinate system has it's y downwards. returns the pick
	 * ray in the provided start and dir parameters. 
	 * 
	 * @param mouse_x
	 * @param mouse_y
	 * @param start
	 * @param dir
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
	
	public static void main( String[] argv )
	{
		Frustum f = new Frustum( );
		f.setCameraParameters(60, 1, 1, 100 );
		f.setCameraOrientation( new Vector( 0, 0, 0 ), new Vector( 0, 0, -1 ), new Vector( 0, 1, 0 ) );
		System.out.println( f );
		System.out.println( f.pointInFrustum( new Vector( -10, 0, 0 ) ) );
		System.out.println( f.pointInFrustum( new Vector( -10, 0, -100 ) ) );
		System.out.println( f.sphereInFrustum( new Vector( 0, 0, 0), 0.5f));
		System.out.println( f.sphereInFrustum( new Vector( 0, 0, 0), 1f));
		System.out.println( f.sphereInFrustum( new Vector( 0, 0, -100.5f), 0.5f));
		System.out.println( f.sphereInFrustum( new Vector( 0, 0, -50), 0.5f));
		System.out.println( f.boundsInFrustum( new Bounds( new Vector(0, 0, -100), new Vector(-1, -1, 0) )));
		System.out.println( f.boundsInFrustum( new Bounds( new Vector(-1000, -1000, -1000), new Vector(1000, 1000, 1000) )));		
	}
	
}

package com.badlogic.gdx;

import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.math.Matrix;
import com.badlogic.gdx.math.Ray;
import com.badlogic.gdx.math.Vector;

public class PerspectiveCamera 
{
	protected Matrix tmp = new Matrix( );
	protected Matrix proj = new Matrix( );
	protected Matrix model = new Matrix( );
	protected Matrix comb = new Matrix( );
	private final Vector direction = new Vector( 0, 0, -1 );
	private final Vector up = new Vector( 0, 1, 0 );
	private final Vector right = new Vector( 1, 0, 0 );
	private final Vector position = new Vector( );
	private final Frustum frustum = new Frustum( );
	
	private float near = 1;
	private float far = 1000;
	private float fov = 90;	
	private float viewportWidth = 640;
	private float viewportHeight = 480;
	
	public float getNear( ) 
	{
		return near;
	}
	
	public void setNear( float near )
	{
		this.near = near;
	}
	
	public float getFar( )
	{
		return far;
	}
	
	public void setFar( float far )
	{
		this.far = far;
	}
	
	public float getFov() {
		return fov;
	}
	
	public void setFov(float fov) {
		this.fov = fov;
	}
	
	public float getViewportWidth() {
		return viewportWidth;
	}
	
	
	public void setViewport(float viewportWidth, float viewportHeight) {
		this.viewportWidth = viewportWidth;
		this.viewportHeight = viewportHeight;
	}	
	
	
	public Matrix getProjectionMatrix() 
	{		
		return proj;
	}
	
	public Matrix getModelviewMatrix()
	{
		return model;
	}
	
	public Matrix getCombinedMatrix( )
	{
		return comb;
	}
	
	public Frustum getFrustum( )
	{
		return frustum;
	}
	
	public void update( )
	{
		float aspect = viewportWidth / viewportHeight;
		
		frustum.setCameraParameters( fov, aspect, near, far );
		frustum.setCameraOrientation(position, direction, up);

		right.set( direction ).crs( up );
		
		proj.setToProjection( near, far, fov, aspect );
		model.setToLookat( direction, up );		
		model.mul( tmp.setToTranslation( position.tmp().mul(-1) ) );
		comb.set( proj ).mul( model );		
	}
	
	public Vector getDir() {
		return direction;
	}
	
	public Vector getUp() {
		return up;
	}
	
	public Vector getPosition() {
		return position;
	}	
	
	public Ray getPickRay( int mouse_x, int mouse_y )
	{
		return frustum.calculatePickRay( viewportWidth, viewportHeight, mouse_x, viewportHeight - mouse_y - 1, position, direction, up );
	}
	
	public void project( Vector pos )
	{
		Matrix m = getCombinedMatrix();		
		pos.prj( m );
		pos.setX( viewportWidth * ( pos.getX() + 1 ) / 2 );
		pos.setY( viewportHeight * ( pos.getY() + 1 ) / 2 );			
	}

	public Vector getRight() {
		return right;
	}
}

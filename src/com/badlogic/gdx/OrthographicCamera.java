package com.badlogic.gdx;

import com.badlogic.gdx.math.Matrix;
import com.badlogic.gdx.math.Ray;
import com.badlogic.gdx.math.Vector;

public class OrthographicCamera 
{
	public enum Side
	{
		FRONT,
		BACK,
		TOP,
		BOTTOM,
		LEFT,
		RIGHT
	}
	
	private Side side;
	private Vector position = new Vector( );
    private Vector direction = new Vector( 0, 0, -1 );
    private Vector up = new Vector( 0, 0, -1 );
    private Vector axis = new Vector( 0, 1, 0 );    
	private float near = -1000;
	private float far = 1000;
	private float scale = 1.0f;
	private float viewportWidth = 0;
	private float viewportHeight = 0;
	
	private final Matrix proj = new Matrix();
	private final Matrix model = new Matrix();
	private final Matrix combined = new Matrix();
	private final Matrix rotationMatrix = new Matrix();
	
	public OrthographicCamera( )
	{
		setSide(Side.FRONT);
	}
	
	public Side getSide() {
		return side;
	}
	public void setSide(Side side) {
		this.side = side;
		calculateRotationMatrix( );
	}
	public float getNear() {
		return near;
	}
	public void setNear(float near) {
		this.near = near;
	}
	public float getFar() {
		return far;
	}
	public void setFar(float far) {
		this.far = far;
	}
	public float getScale() {
		return scale;
	}
	public void setScale(float scale) {
		this.scale = scale;
	}
	public Vector getPosition() {
		return position;
	}
	
	public void setViewport( float width, float height )
	{
		this.viewportWidth = width;
		this.viewportHeight = height;
	}
	
	Vector tmp = new Vector( );
	public void update( )
	{
		proj.setToOrtho2D(0, 0, (viewportWidth * scale), (float)(viewportHeight * scale), near, far );
		model.idt();
		model.setToTranslation( tmp.set( (float)(-position.getX() + (viewportWidth / 2) * scale), (float)(-position.getY() + (viewportHeight / 2) * scale), (float)(-position.getZ()) ) );
		combined.set( proj );
		combined.mul( model );
		combined.mul( rotationMatrix );
	}
	
	private Matrix calculateRotationMatrix( )
	{
		float rotation = 0;
		if( side == Side.FRONT )
		{
			direction.set( 0, 0, -1 );
			up.set( 0, 1, 0 );
		}
		else if( side == Side.BACK )
		{
			axis.set( 0, 1, 0 );
			rotation = 180;
			direction.set( 0, 0, 1 );
			up.set( 0, 1, 0 );
		}
		else if( side == Side.TOP )			
		{
			axis.set( 1, 0, 0 );
			rotation = 90;
			direction.set( 0, -1, 0 );
			up.set( 0, 0, -1 );
		}
		else if( side == Side.BOTTOM )			
		{
			axis.set( 1, 0, 0 );
			rotation = -90;
			direction.set( 0, 1, 0 );
			up.set( 0, 0, -1 );
		}
		else if( side == Side.LEFT )			
		{
			axis.set( 0, 1, 0 );
			rotation = 90;
			direction.set( -1, 0, 0 );
			up.set( 0, 1, 0 );
		}
		else if( side == Side.RIGHT )			
		{
			axis.set( 0, 1, 0 );
			rotation = -90;
			direction.set( 1, 0, 0 );
			up.set( 0, 1, 0 );
		}
		
		rotationMatrix.setToRotation(axis, rotation);
		return rotationMatrix;
	}
	
	public float getScreenToWorldX( float mouse_x )
	{
		return  ( mouse_x * scale ) - ( viewportWidth * scale ) / 2 + position.getX();
	}
	
	public int getWorldToScreenX( float world_x )
	{
		return (int)((world_x + ( viewportWidth * scale ) / 2 - position.getX()) / scale);
	}	
	
	public float getScreenToWorldY( float mouse_y )
	{
		return ( (viewportHeight - mouse_y-1) * scale ) - ( viewportHeight * scale ) / 2 + position.getY();
	}
	
	public int getWorldToScreenY( float world_y )
	{
		return (int)(-( -world_y + (viewportHeight * scale ) / 2 + position.getY() - viewportHeight * scale ) / scale); 
	}	
		
	Ray ray = new Ray( new Vector( ), new Vector( ) );
	Vector tmp2 = new Vector( );
	public Ray getPickRay( int mouse_x, int mouse_y )
	{
		float x = getScreenToWorldX( mouse_x );
		float y = getScreenToWorldY( mouse_y );		
		
		if( side == Side.TOP )
		{
			return ray.set( x, 1000 / 2, -y , 0, -1, 0 );
		}
		else if( side == Side.BOTTOM )
		{
			return ray.set( x, 1000 / 2, y, 0, 1, 0 );
		}
		else
			return ray.set(  tmp2.set( x, y, 10000 / 2 ).mul( rotationMatrix ), tmp.set( 0, 0, -1 ).mul(rotationMatrix) );
	}

	public Matrix getCombinedMatrix() 
	{	
		return combined;
	}
}

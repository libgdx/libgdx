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
package com.badlogic.gdx.graphics;

import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.math.Matrix;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;

/**
 * An orthographic camera having a position and a scale value for zooming. Looks
 * at one of the sides given in the {@link Side} enums, default is front, looking
 * along the z-Axis. Generally you will want to alter the camera's scale and position
 * and then set its matrices via a call to {@link OrthographicCamera.setMatrices()} which 
 * takes a {@link Graphics} instance from which it retrieves an GL10 instance to
 * set the matrices. You can get a picking ray via a call to {@link OrthographicCamera.getPickRay()}.
 * For OpenGL ES 2.0 you can get the matrices via {@link OrthographicCamera.getCombinedMatrix()} to
 * set them as a uniform. For this you have to call {@link OrthographicCamera.update()} before you 
 * retrieve the matrices. 
 * 
 * @author badlogicgames@gmail.com
 *
 */
public final class OrthographicCamera 
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
	private Vector3 position = new Vector3( );
    private Vector3 direction = new Vector3( 0, 0, -1 );
    private Vector3 up = new Vector3( 0, 0, -1 );
    private Vector3 axis = new Vector3( 0, 1, 0 );    
	private float near = -1000;
	private float far = 1000;
	private float scale = 1.0f;
	private float viewportWidth = 0;
	private float viewportHeight = 0;
	
	private final Matrix proj = new Matrix();
	private final Matrix model = new Matrix();
	private final Matrix combined = new Matrix();
	private final Matrix rotationMatrix = new Matrix();
	
	private final Graphics graphics;
	
	/**
	 * Constructor, sets side to {@link Side.FRONT}
	 */
	public OrthographicCamera( Graphics graphics )
	{
		this.graphics = graphics;
		setSide(Side.FRONT);
	}
	
	/**
	 * @return Which side the camera looks at
	 */
	public Side getSide() {
		return side;
	}
	
	/**
	 * Sets the side the camera looks at.
	 * @param side The side.
	 */
	public void setSide(Side side) {
		this.side = side;
		calculateRotationMatrix( );
	}
	
	/**
	 * @return The near plane.
	 */
	public float getNear() {
		return near;
	}
	
	/**
	 * Sets the near plane.
	 * 
	 * @param near The near plane
	 */
	public void setNear(float near) {
		this.near = near;
	}
	
	/**
	 * @return The far plane.
	 */
	public float getFar() {
		return far;
	}
	
	/**
	 * Sets the far plane
	 * @param far the far plane
	 */
	public void setFar(float far) {
		this.far = far;
	}
	
	/**
	 * @return The scale
	 */
	public float getScale() {
		return scale;
	}
	
	/**
	 * @param scale Sets the scale
	 */
	public void setScale(float scale) {
		this.scale = scale;
	}
	
	/**
	 * @return The position
	 */
	public Vector3 getPosition() {
		return position;
	}
	
	/**
	 * Sets the viewport
	 *  
	 * @param width The viewport width in pixels
	 * @param height The viewport height in pixels
	 */
	public void setViewport( float width, float height )
	{
		this.viewportWidth = width;
		this.viewportHeight = height;
	}
	
	Vector3 tmp = new Vector3( );
	
	/**
	 * Updates the frustum as well as the matrices of the camera
	 * based on the near and far plane, the position, the side and
	 * the scale.
	 */
	public void update( )
	{
		proj.setToOrtho2D(0, 0, (viewportWidth * scale), (float)(viewportHeight * scale), near, far );
		model.idt();
		model.setToTranslation( tmp.set( (float)(-position.x + (viewportWidth / 2) * scale), (float)(-position.y + (viewportHeight / 2) * scale), (float)(-position.z) ) );
		combined.set( proj );
		combined.mul( model );
		combined.mul( rotationMatrix );
	}
	
	/**
	 * Sets the projection matrix that also incorporates the camera's scale
	 * and position and loads an identity to the model view matrix of OpenGL ES 1.x. 
	 * The current matrices get overwritten. The matrix mode will be left in the model view state after
	 * a call to this.
	 * 
	 * @param app The application to set the matrices for.
	 */
	public void setMatrices( Graphics graphics )
	{
		update();
		GL10 gl = graphics.getGL10();
		gl.glMatrixMode( GL10.GL_PROJECTION );
		gl.glLoadMatrixf( getCombinedMatrix().val, 0 );
		gl.glMatrixMode( GL10.GL_MODELVIEW );
		gl.glLoadIdentity();
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
	
	/**
	 * Calculates the world coordinates of the given screen coordinates and stores
	 * the result in world
	 * @param graphics the Graphics instance used to determine the real screen size
	 * @param screenX the x-coordinate of the screen position
	 * @param screenY the y-coordinate of the screen position
	 * @param world the vector to store the result in
	 */
	public void getScreenToWorld( float screenX, float screenY, Vector2 world )
	{
		screenX = screenX / graphics.getWidth() * viewportWidth;
		screenY = screenY / graphics.getHeight() * viewportHeight;
		
		world.set( ( screenX * scale ) - ( viewportWidth * scale ) / 2 + position.x,
				   ( (viewportHeight -screenY-1) * scale ) - ( viewportHeight * scale ) / 2 + position.y );
					
	}
	
	/**
	 * Returns the given screen x-coordinates as a world x-coordinate
	 * @param screenX The screen x-coordinate
	 * @return The world x-coordinate
	 */
	public float getScreenToWorldX( float screenX )
	{
		return  ( screenX * scale ) - ( viewportWidth * scale ) / 2 + position.x;
	}	
	
	/**
	 * Returns the given world x-coordinate as a screen x-coordinate
	 * @param worldX The world x-coordinate
	 * @return The screen x-coordinate
	 */
	public int getWorldToScreenX( float worldX )
	{
		return (int)((worldX + ( viewportWidth * scale ) / 2 - position.x) / scale);
	}	
	
	/**
	 * Returns the given screen y-coordinates as a world x-coordinate
	 * @param screenX The screen y-coordinate
	 * @return The world y-coordinate
	 */
	public float getScreenToWorldY( float mouse_y )
	{
		return ( (viewportHeight - mouse_y-1) * scale ) - ( viewportHeight * scale ) / 2 + position.y;
	}
	
	/**
	 * Returns the given world y-coordinate as a screen x-coordinate
	 * @param worldX The world y-coordinate
	 * @return The screen y-coordinate
	 */
	public int getWorldToScreenY( float world_y )
	{
		return (int)(-( -world_y + (viewportHeight * scale ) / 2 + position.y - viewportHeight * scale ) / scale); 
	}	
		
	Ray ray = new Ray( new Vector3( ), new Vector3( ) );
	Vector3 tmp2 = new Vector3( );
	
	/**
	 * Returns a ray in world space form the given screen coordinates.
	 * This can be used for picking. The returned Ray is an internal
	 * member of this class to reduce memory allocations. Do not reuse
	 * it outside of this class.
	 * 
	 * @param screenX The screen x-coordinate
	 * @param mouse_y The screen y-coordinate
	 * @return The picking ray.
	 */
	public Ray getPickRay( int screenX, int screenY )
	{
		float x = getScreenToWorldX( screenX );
		float y = getScreenToWorldY( screenY );		
		
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

	/**
	 * @return The combined matrix.
	 */
	public Matrix getCombinedMatrix() 
	{	
		return combined;
	}
}

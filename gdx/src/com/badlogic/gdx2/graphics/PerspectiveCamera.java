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
package com.badlogic.gdx2.graphics;

import com.badlogic.gdx2.Graphics;
import com.badlogic.gdx2.math.Frustum;
import com.badlogic.gdx2.math.Matrix;
import com.badlogic.gdx2.math.Ray;
import com.badlogic.gdx2.math.Vector;



/**
 * A perspective camera, having a position, a direction an up vector, 
 * a near plane, a far plane and a field of view. Use the {@link PerspectiveCamera.setViewport()}
 * method to set the viewport from which the aspect ratio is derrived from, then 
 * call {@link PerspectiveCamera.update()} to update all the camera matrices as well as the
 * {@link Frustum}. The combined matrix (projection, modelview) can be retrieved via a call 
 * to {@link PerspectiveCamera.getCombinedMatrix()} and directly passed to OpenGL. A convenience
 * method called {@link PerspectiveCamera.setMatrices()} exists for OpenGL ES 1.x that will
 * update the matrices and set them via a {@link Graphics} instance to OpenGL.  
 * You can also get a picking ray via {@link PerspectiveCamera.getPickRay()}.
 * 
 * @author badlogicgames@gmail.com
 *
 */
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
	
	/**
	 * @return The near plane.
	 */
	public float getNear( ) 
	{
		return near;
	}
	
	/**
	 * Sets the near plane.
	 * @param near The near plane
	 */
	public void setNear( float near )
	{
		this.near = near;
	}
	
	/**
	 * @return The far plane
	 */
	public float getFar( )
	{
		return far;
	}
	
	/**
	 * Sets the far plane
	 * @param far The far plane
	 */
	public void setFar( float far )
	{
		this.far = far;
	}
	
	/**
	 * @return The field of view in degrees
	 */
	public float getFov() {
		return fov;
	}
	
	/**
	 * Sets the field of view in degrees
	 * @param fov The field of view
	 */
	public void setFov(float fov) {
		this.fov = fov;
	}
	
	/**
	 * @return The viewport height
	 */
	public float getViewportWidth() {
		return viewportWidth;
	}
	
	/**
	 * Sets the viewport dimensions. 
	 * @param viewportWidth The viewport width in pixels.
	 * @param viewportHeight The viewport height in pixels.
	 */
	public void setViewport(float viewportWidth, float viewportHeight) {
		this.viewportWidth = viewportWidth;
		this.viewportHeight = viewportHeight;
	}	
	
	/**
	 * @return The projection matrix.
	 */
	public Matrix getProjectionMatrix() 
	{		
		return proj;
	}
	
	/**
	 * @return The modelview matrix.
	 */
	public Matrix getModelviewMatrix()
	{
		return model;
	}
	
	/**
	 * @return The combined matrix, projection * modelview
	 */
	public Matrix getCombinedMatrix( )
	{
		return comb;
	}
	
	/**
	 * @return The {@link Frustum}
	 */
	public Frustum getFrustum( )
	{
		return frustum;
	}
	
	Vector tmp2 = new Vector();
	/**
	 * Updates all matrices as well as the Frustum based on the
	 * last set parameters for position, direction, up vector,
	 * field of view, near and far plane and viewport.
	 */	
	public void update( )
	{
		float aspect = viewportWidth / viewportHeight;
		
		frustum.setCameraParameters( fov, aspect, near, far );
		frustum.setCameraOrientation(position, direction, up);

		right.set( direction ).crs( up );
		
		proj.setToProjection( near, far, fov, aspect );
		model.setToLookat( direction, up );		
		model.mul( tmp.setToTranslation( tmp2.set(position).mul(-1) ) );
		comb.set( proj ).mul( model );		
	}
	
	/**
	 * Sets the projection and model view matrix of OpenGL ES 1.x to
	 * this camera's projection and model view matrix. Any previously set 
	 * matrices are overwritten. Upon returning from this
	 * method the matrix mode will be GL10.GL_MODELVIEW.
	 * 
	 * @param graphics the Graphics instance.
	 */
	public void setMatrices( Graphics graphics )
	{
		setViewport(graphics.getWidth(), graphics.getHeight());
		update();
		GL10 gl = graphics.getGL10();
		gl.glMatrixMode( GL10.GL_PROJECTION );
		gl.glLoadMatrixf( getCombinedMatrix().val, 0 );
		gl.glMatrixMode( GL10.GL_MODELVIEW );
		gl.glLoadIdentity();	
	}
	
	/**
	 * @return The direction vector.
	 */
	public Vector getDirection() {
		return direction;
	}
	
	/**
	 * @return The right vector
	 */
	public Vector getRight() {
		return right;
	}
	
	/**
	 * @return The up vector. 
	 */
	public Vector getUp() {
		return up;
	}
	
	/**
	 * @return The position.
	 */
	public Vector getPosition() {
		return position;
	}	
	
	/**
	 * Returns a ray in world space form the given screen coordinates.
	 * This can be used for picking.  The returned Ray is an internal
	 * member of this class to reduce memory allocations. Do not reuse
	 * it outside of this class.
	 * 
	 * @param screenX The screen x-coordinate
	 * @param mouse_y The screen y-coordinate
	 * @return The picking ray
	 */
	public Ray getPickRay( int screenX, int screenY )
	{
		return frustum.calculatePickRay( viewportWidth, viewportHeight, screenX, viewportHeight - screenY - 1, position, direction, up );
	}
	
	/**
	 * Projects the given vector in world space to screen space, overwritting
	 * the x- and y-coordinate of the provided vector.
	 * @param pos The vector to project
	 */
	public void project( Vector pos )
	{
		Matrix m = getCombinedMatrix();		
		pos.prj( m );
		pos.x = viewportWidth * ( pos.x + 1 ) / 2;
		pos.y = viewportHeight * ( pos.y + 1 ) / 2;			
	}
}

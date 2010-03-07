package com.badlogic.gdx;

import com.badlogic.gdx.Application.MatrixMode;
import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.math.Matrix;
import com.badlogic.gdx.math.Ray;
import com.badlogic.gdx.math.Vector;

/**
 * A perspective camera, having a position, a direction an up vector, 
 * a near plane, a far plane and a field of view. Use the {@link PerspectiveCamera.setViewport()}
 * method to set the viewport from which the aspect ratio is derrived from, then 
 * call {@link PerspectiveCamera.update()} to update all the camera matrices as well as the
 * {@link Frustum}. The combined matrix (projection, modelview) can be retrieved via a call 
 * to {@link PerspectiveCamera.getCombinedMatrix()} and directly passed to {@link 
 * Application.loadMatrix()}.
 * 
 * @author mzechner
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
		model.mul( tmp.setToTranslation( position.tmp().mul(-1) ) );
		comb.set( proj ).mul( model );		
	}
	
	/**
	 * Sets the projection and model view matrix of the {@link Application} to
	 * this camera's projection and model view matrix. Any previously set 
	 * matrices of the Application are overwritten. Upon returning from this
	 * method the matrix mode of the Application is set to model view.
	 * 
	 * @param app The Application.
	 */
	public void setMatrices( Application app )
	{
		setViewport(app.getViewportWidth(), app.getViewportHeight());
		app.setMatrixMode( MatrixMode.Projection );
		app.loadMatrix( getCombinedMatrix().val );
		app.setMatrixMode( MatrixMode.ModelView );
		app.loadIdentity();	
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
	 * This can be used for picking.
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
		pos.setX( viewportWidth * ( pos.getX() + 1 ) / 2 );
		pos.setY( viewportHeight * ( pos.getY() + 1 ) / 2 );			
	}
}

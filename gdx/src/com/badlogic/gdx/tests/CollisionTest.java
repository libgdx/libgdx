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
package com.badlogic.gdx.tests;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.RenderListener;
import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.backends.desktop.JoglApplication;
import com.badlogic.gdx.graphics.FloatMesh;
import com.badlogic.gdx.graphics.Font;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.MeshRenderer;
import com.badlogic.gdx.graphics.ModelLoader;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Text;
import com.badlogic.gdx.math.Matrix;
import com.badlogic.gdx.math.Vector;
import com.badlogic.gdx.math.collision.CollisionMesh;
import com.badlogic.gdx.math.collision.EllipsoidCollider;
import com.badlogic.gdx.math.collision.Segment;
import com.badlogic.gdx.math.collision.SlideResponse;

public class CollisionTest implements RenderListener
{
	Font font;
	Text text;
	final float SCALE = 1;
	final float VERY_CLOSE_DISTANCE = 0.001f;
	final float SPEED = 1 * SCALE;	
	CollisionMesh cMesh;
	EllipsoidCollider collider;
	boolean colliding = false;
	MeshRenderer mesh;
	PerspectiveCamera cam;		
	float[] lightColor = { 1, 1, 1, 0 };
	float[] lightPosition = { 2, 5, 10, 0 }; 
	float yAngle = 0;
	Matrix mat = new Matrix();	
	Vector axis = new Vector( 0, 1, 0 );
	Vector position = new Vector( 0, 0, 0 );
	Vector velocity = new Vector( 0, 0, 0 );
	Vector gravity = new Vector( 0, 0, 0 );
	Segment segment = new Segment( new Vector(), new Vector() );
	Vector start = new Vector( 0, 3 * SCALE, 0 );
	
	float ax;
	float ay;
	
	@Override
	public void surfaceCreated(Application app) 
	{		
		ax = app.getInput().getAccelerometerX();
		ay = app.getInput().getAccelerometerZ();
		FloatMesh m = (FloatMesh)ModelLoader.loadObj( app.getFiles().readFile( "data/scene2.obj", FileType.Internal ), true );
		
		mesh = new MeshRenderer( app.getGraphics().getGL10(), m, true, true );			
		cam = new PerspectiveCamera();
		cam.setFov( 45 );
		cam.setNear( 0.1f );
		cam.setFar( 1000 );		
		position.set(start).y += SCALE * 3;		
		
		cMesh = new CollisionMesh( m, false );
		collider = new EllipsoidCollider( 0.5f * SCALE, 1 * SCALE, 0.5f * SCALE, new SlideResponse() );		
	}
	
	@Override
	public void render(Application app) 
	{
		processInput( app.getInput(), app.getGraphics().getDeltaTime() );
		
		GL10 gl = app.getGraphics().getGL10();
		gl.glClearColor( 0, 0, 0, 0 );
		gl.glClear( GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT );
		
		render3D( gl, app.getGraphics() );			
	}			
	
	private void render3D( GL10 gl, Graphics g )
	{
		cam.getPosition().set(position);
		gl.glEnable( GL10.GL_DEPTH_TEST );
		cam.setMatrices( g );
		setupLights( gl );					
		mesh.render(GL10.GL_TRIANGLES );
		gl.glDisable( GL10.GL_DEPTH_TEST );
	}
	
	private void setupLights( GL10 gl )
	{
		gl.glEnable( GL10.GL_LIGHTING );
		gl.glEnable( GL10.GL_COLOR_MATERIAL );
		gl.glEnable( GL10.GL_LIGHT0 );				
		gl.glLightfv( GL10.GL_LIGHT0, GL10.GL_DIFFUSE, lightColor, 0 );
		gl.glLightfv( GL10.GL_LIGHT0, GL10.GL_POSITION, lightPosition, 0 );
	}
	
	private void processInput( Input input, float deltaTime )
	{
		deltaTime = 0.017f;
		if( input.isKeyPressed( Input.Keys.KEYCODE_ENTER ) )
			position.set( start ).y += SCALE * 2;			
		
		if( ( input.isAccelerometerAvailable() && input.getAccelerometerY() < 0 ) )	
			yAngle += deltaTime * 120 * Math.min( 1, Math.abs( input.getAccelerometerY() )  );		
		if( ( input.isAccelerometerAvailable() && input.getAccelerometerY() > 0 ) )		
			yAngle -= deltaTime * 120 * Math.min( 1,  input.getAccelerometerY() );		
		
		if( input.isKeyPressed( Input.Keys.KEYCODE_DPAD_LEFT ) )			
			yAngle += deltaTime * 120;
		
		if( input.isKeyPressed( Input.Keys.KEYCODE_DPAD_RIGHT ) )
			yAngle -= deltaTime * 120;		
		
		cam.getDirection().set( 0, 0, -1 );
		mat.setToRotation( axis, yAngle );
		cam.getDirection().rot( mat );
		
	

		if( input.isKeyPressed( Input.Keys.KEYCODE_DPAD_UP ) || ( input.isAccelerometerAvailable() && input.getAccelerometerX() < 5 ) )		
			velocity.add(cam.getDirection().tmp().mul( SPEED * deltaTime));
		if( input.isKeyPressed( Input.Keys.KEYCODE_DPAD_DOWN ) || ( input.isAccelerometerAvailable() && input.getAccelerometerX() > 6 ) )
			velocity.add(cam.getDirection().tmp().mul(SPEED * -deltaTime));
										
		velocity.add( 0, -1f * SCALE * deltaTime, 0 );				
		
		colliding = collider.collide( cMesh, position, velocity, VERY_CLOSE_DISTANCE );				
		velocity.mul( 0.90f ); 
		gravity.mul( 0.90f );
	}
				
	
	@Override
	public void dispose(Application app) 
	{	
		
	}
	
	@Override
	public void surfaceChanged(Application app, int width, int height) 
	{	
		
	}

	public static void main( String[] argv )
	{
		JoglApplication app = new JoglApplication( "Collision Test", 480, 320, false );
		app.getGraphics().setRenderListener( new CollisionTest() );
	}
}

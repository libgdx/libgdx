/*******************************************************************************
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.badlogic.gdx.tests;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.RenderListener;
import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.ModelLoader;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Matrix;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.CollisionMesh;
import com.badlogic.gdx.math.collision.EllipsoidCollider;
import com.badlogic.gdx.math.collision.Segment;
import com.badlogic.gdx.math.collision.SlideResponse;

public class CollisionTest implements RenderListener
{	
	final float SCALE = 1;
	final float VERY_CLOSE_DISTANCE = 0.001f;
	final float SPEED = 1 * SCALE;	
	CollisionMesh cMesh;
	EllipsoidCollider collider;
	boolean colliding = false;
	Mesh mesh;
	PerspectiveCamera cam;		
	float[] lightColor = { 1, 1, 1, 0 };
	float[] lightPosition = { 2, 5, 10, 0 }; 
	float yAngle = 0;
	Matrix mat = new Matrix();	
	Vector3 axis = new Vector3( 0, 1, 0 );
	Vector3 position = new Vector3( 0, 0, 0 );
	Vector3 velocity = new Vector3( 0, 0, 0 );
	Vector3 gravity = new Vector3( 0, 0, 0 );
	Segment segment = new Segment( new Vector3(), new Vector3() );
	Vector3 start = new Vector3( 0, 3 * SCALE, 0 );
	
	float ax;
	float ay;
	
	@Override
	public void surfaceCreated(Application app) 
	{		
		ax = app.getInput().getAccelerometerX();
		ay = app.getInput().getAccelerometerZ();
		mesh = ModelLoader.loadObj( app.getGraphics(), app.getFiles().readFile( "data/scene.obj", FileType.Internal ), true, true );
				
		cam = new PerspectiveCamera();
		cam.setFov( 45 );
		cam.setNear( 0.1f );
		cam.setFar( 1000 );		
		position.set(start).y += SCALE * 3;		
		
		cMesh = new CollisionMesh( mesh, false );
		collider = new EllipsoidCollider( 0.5f * SCALE, 1 * SCALE, 0.5f * SCALE, new SlideResponse() );		
	}
	
	@Override
	public void render(Application app) 
	{
		processInput( app.getInput(), app.getGraphics().getDeltaTime() );
		
		GL10 gl = app.getGraphics().getGL10();
		gl.glClearColor( 0, 0, 0, 0 );
		gl.glClear( GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT );
		gl.glViewport( 0, 0, app.getGraphics().getWidth(), app.getGraphics().getHeight() );
		
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

	
}

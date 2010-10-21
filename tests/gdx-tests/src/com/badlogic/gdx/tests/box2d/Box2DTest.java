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
package com.badlogic.gdx.tests.box2d;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputListener;
import com.badlogic.gdx.RenderListener;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;

/**
 * Base class for all Box2D Testbed tests, all subclasses 
 * must implement the createWorld() method.
 * 
 * @author badlogicgames@gmail.com
 *
 */
public abstract class Box2DTest implements RenderListener, InputListener
{			
	/** the camera **/
	protected OrthographicCamera camera;
	
	/** the renderer **/
	protected Box2DDebugRenderer renderer;
	
	/** our box2D world **/
	protected World world;		
		
	/** ground body to connect the mouse joint to **/
	protected Body groundBody;
	
	/** our mouse joint **/
	protected MouseJoint mouseJoint = null;
	
	/** a hit body **/
	protected Body hitBody = null;	

	protected abstract void createWorld( World world );
	
	/** temp vector **/
	protected Vector2 tmp = new Vector2();
	
	@Override
	public void render() 
	{
		// update the world with a fixed time step
		world.step( Gdx.app.getGraphics().getDeltaTime(), 8, 3 );
		
		// clear the screen and setup the projection matrix
		GL10 gl = Gdx.app.getGraphics().getGL10();		
		gl.glClear( GL10.GL_COLOR_BUFFER_BIT );		
		camera.setMatrices( );
		
		// render the world using the debug renderer
		renderer.render( world );
	}

	@Override
	public void surfaceChanged(int width, int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void surfaceCreated( ) 
	{
		// setup the camera. In Box2D we operate on a 
		// meter scale, pixels won't do it. So we use
		// an orthographic camera with a viewport of
		// 48 meters in width and 32 meters in height.
		// We also position the camera so that it 
		// looks at (0,16) (that's where the middle of the
		// screen will be located).
		camera = new OrthographicCamera( );		
		camera.setViewport( 48, 32 );
		camera.getPosition().set( 0, 15, 0 );
		
		// create the debug renderer
		renderer = new Box2DDebugRenderer( );
		
		// create the world
		world = new World( new Vector2( 0, -10 ), true );
		
		// we also need an invisible zero size ground body
		// to which we can connect the mouse joint
		BodyDef bodyDef = new BodyDef();
		groundBody = world.createBody(bodyDef);
		
		// finally we register ourself as an InputListener so we
		// can manipulate our world
		Gdx.input.addInputListener( this );
		
		// call abstract method to populate the world
		createWorld( world );
	}
	
	@Override
	public void dispose( ) 
	{	
		Gdx.input.removeInputListener( this );
		
		renderer.dispose();
		world.dispose();
		
		renderer = null;
		world = null;
		mouseJoint = null;
		hitBody = null;		
	}

	@Override
	public boolean keyDown(int keycode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		// TODO Auto-generated method stub
		return false;
	}

	/** we instantiate this vector and the callback here so we don't irritate the GC **/
	Vector2 testPoint = new Vector2( );
	QueryCallback callback = new QueryCallback() {			
		@Override
		public boolean reportFixture(Fixture fixture) 
		{							
			// if the hit point is inside the fixture of the body
			// we report it
			if( fixture.testPoint( testPoint ) )
			{
				hitBody = fixture.getBody();
				return false;
			}
			else
				return true;
		}
	};
	@Override
	public boolean touchDown(int x, int y, int pointer) 
	{
		// translate the mouse coordinates to world coordinates
		camera.getScreenToWorld( x, y, testPoint );				
		// ask the world which bodies are within the given 
		// bounding box around the mouse pointer
		hitBody = null;
		world.QueryAABB( callback, testPoint.x - 0.0001f, testPoint.y - 0.0001f, testPoint.x + 0.0001f, testPoint.y + 0.0001f );
		
		if( hitBody == groundBody )
			hitBody = null;
		
		// ignore kinematic bodies, they don't work with the mouse joint
		if( hitBody != null && hitBody.getType() == BodyType.KinematicBody )
			return false;
		
		// if we hit something we create a new mouse joint
		// and attach it to the hit body. 
		if( hitBody != null)
		{
			MouseJointDef def = new MouseJointDef();
			def.bodyA = groundBody;
			def.bodyB = hitBody;
			def.collideConnected = true;
			def.target.set( testPoint );
			def.maxForce = 1000.0f * hitBody.getMass();
			
			mouseJoint = (MouseJoint)world.createJoint( def );
			hitBody.setAwake(true);
		}
				
		return false;
	}

	/** another temporary vector **/
	Vector2 target = new Vector2( );
	@Override
	public boolean touchDragged(int x, int y, int pointer) {
		// if a mouse joint exists we simply update
		// the target of the joint based on the new
		// mouse coordinates
		if( mouseJoint != null )
		{			
			camera.getScreenToWorld( x, y, target );					
			mouseJoint.setTarget( target );			
		}
		return false;	
	}

	@Override
	public boolean touchUp(int x, int y, int pointer) {
		// if a mouse joint exists we simply destroy it
		if( mouseJoint != null )
		{
			world.destroyJoint(mouseJoint);
			mouseJoint = null;
		}
		return false;
	}
}

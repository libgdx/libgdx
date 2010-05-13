package com.badlogic.gdx.tests;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.InputListener;
import com.badlogic.gdx.RenderListener;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Font;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.ImmediateModeRenderer;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.SpriteBatch;
import com.badlogic.gdx.graphics.Font.FontStyle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.WorldManifold;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;

public class Box2DTest implements RenderListener, InputListener
{
	ImmediateModeRenderer renderer;
	OrthographicCamera cam;
	SpriteBatch batch;
	Font font;
	
	World world;
	List<Body> boxes = new ArrayList<Body>( );
	Body ground;
	MouseJoint mouseJoint = null;
	
	@Override
	public void dispose(Application app) 
	{	
		
	}

	int frames = 0;
	long start = System.nanoTime();
	String fps = "0 fps";
	
	@Override
	public void render(Application app) 
	{	
		app.getGraphics().getGL10().glClear( GL10.GL_COLOR_BUFFER_BIT );		
		cam.setMatrices(app.getGraphics());	
				
		long s = System.nanoTime();
		world.step( app.getGraphics().getDeltaTime(), 8, 3 );
		float updateTime = (System.nanoTime()-s)/1000000000.0f;
		
		for( int i = 0; i < boxes.size(); i++ )				
			renderBox( app.getGraphics().getGL10(), boxes.get(i) );		
		
		if( mouseJoint != null )
		{
			renderer.begin(GL10.GL_POINTS );
				renderer.color(1, 0, 0, 1 );
				renderer.vertex( mouseJoint.getAnchorB().x, mouseJoint.getAnchorB().y, 0 );
			renderer.end();
		}
		
//		List<Contact> contacts = world.getContactList();
//		renderer.begin( GL10.GL_POINTS );
//		for( int i = 0; i < contacts.size(); i++ )
//		{
//			Contact contact = contacts.get(i);
//			if( contact.isTouching() )
//			{
//				WorldManifold manifold = contact.GetWorldManifold();
//				int numContactPoints = manifold.getNumberOfContactPoints();
//				Vector2[] contactPoints = manifold.getPoints();
//				for( int j = 0; j < numContactPoints; j++ )
//				{			
//					renderer.color(0, 1, 0, 1 );
//					renderer.vertex( contactPoints[j].x, contactPoints[j].y, 0 );
//				}
//			}
//		}
//		renderer.end();
		
		batch.begin();
		batch.drawText( font, fps + " update: " + updateTime, 0, 16, Color.RED );
		batch.end();
		
		if( System.nanoTime() - start > 1000000000 )
		{
			fps = frames + " fps";
			frames = 0;
			start = System.nanoTime();
		}
		frames++;
	}

	private void renderBox( GL10 gl, Body body )
	{
		gl.glPushMatrix();
		Vector2 pos = body.getWorldCenter();
		float angle = body.getAngle();
		gl.glTranslatef(pos.x, pos.y, 0 );
		gl.glRotatef( (float)Math.toDegrees(angle), 0, 0, 1 );
		renderer.begin( GL10.GL_LINE_STRIP );
		renderer.color( 1, 1, 1, 1 );
		renderer.vertex( -1, -1, 0 );
		renderer.color( 1, 1, 1, 1 );
		renderer.vertex( -1,  1, 0 );
		renderer.color( 1, 1, 1, 1 );
		renderer.vertex(  1,  1, 0 );
		renderer.color( 1, 1, 1, 1 );
		renderer.vertex(  1, -1, 0 );
		renderer.color( 1, 1, 1, 1 );
		renderer.vertex( -1, -1, 0 );
		renderer.end();
		gl.glPopMatrix();
		
//		System.out.println( "bodies: " + world.getBodyCount() + ", contacts: " + world.getContactCount() + ", joints: " + world.getJointCount() );
	}
	
	@Override
	public void surfaceChanged(Application app, int width, int height) 
	{	

	}
	
	private void createWorld( )
	{
		world = new World(new Vector2(0,-10), true);
		BodyDef bodyDef = new BodyDef( );
		bodyDef.type = BodyType.StaticBody;			
		PolygonShape groundBox = new PolygonShape();
		groundBox.setAsBox(24, 1);		
		ground = world.createBody( bodyDef );
		ground.createFixture( groundBox, 1 );
		groundBox.dispose();
		
		for( int i = 0; i < 60; i++ )
		{			
			boxes.add(createBox( ));
		}			
	}
	
	private Body createBox( )
	{
		BodyDef bodyDef = new BodyDef( );
		bodyDef.type = BodyType.DynamicBody;
		bodyDef.position.x = -24 + (float)(Math.random() * 48);
		bodyDef.position.y = 10 +  (float)(Math.random() * 100);		
		Body box = world.createBody( bodyDef );
		
		PolygonShape poly = new PolygonShape( );
		poly.setAsBox( 1, 1 );	
		
		FixtureDef fixture = new FixtureDef();
		fixture.shape = poly;
		fixture.density = 10;
		fixture.friction = 0.4f;
		fixture.restitution = 0.1f;		
		box.createFixture(fixture);
//		box.createFixture(poly, 10);
		poly.dispose();
		return box;
	}

	@Override
	public void surfaceCreated(Application app) 
	{	
		if( renderer == null )
		{
			renderer = new ImmediateModeRenderer(app.getGraphics().getGL10());
			cam = new OrthographicCamera();
			cam.setViewport( app.getGraphics().getWidth(), app.getGraphics().getHeight() );
			cam.setScale( 0.1f );
			cam.getPosition().set( 0, 16, 0 );
			
			batch = new SpriteBatch(app.getGraphics());
			font = app.getGraphics().newFont( "Arial", 16, FontStyle.Plain, true );
			
			createWorld( );					
			app.getInput().addInputListener( this );
		}
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

	Body hitBody = null;
	Vector2 testPoint = new Vector2( );
	@Override
	public boolean touchDown(int x, int y, int pointer) 
	{
		testPoint.set( cam.getScreenToWorldX(x), cam.getScreenToWorldY(y) );
		
		QueryCallback callback = new QueryCallback() {			
			@Override
			public boolean reportFixture(Fixture fixture) 
			{				
				if( fixture.testPoint( testPoint ) )
				{
					hitBody = fixture.getBody();
					return false;
				}
				else
					return true;
			}
		};
		
		hitBody = null;
		world.QueryAABB( callback, testPoint.x - 0.1f, testPoint.y - 0.1f, testPoint.x + 0.1f, testPoint.y + 0.1f );
		
		if( hitBody != null )
		{
			MouseJointDef def = new MouseJointDef();
			def.bodyA = ground;
			def.bodyB = hitBody;
			def.collideConnected = true;
			def.target.set( testPoint );
			def.maxForce = 1000.0f * hitBody.getMass();
			
			mouseJoint = (MouseJoint)world.createJoint( def );
			hitBody.setAwake(true);
		}
		
		return false;
	}

	final Vector2 target = new Vector2( );
	@Override	
	public boolean touchDragged(int x, int y, int pointer) 
	{
		if( mouseJoint != null )
		{			
			float wX = cam.getScreenToWorldX(x);
			float wY = cam.getScreenToWorldY(y);
			target.set( wX, wY );		
			mouseJoint.setTarget( target );			
		}
		return false;
	}

	@Override
	public boolean touchUp(int x, int y, int pointer) 
	{
		if( mouseJoint != null )
		{
			world.destroyJoint(mouseJoint);
			mouseJoint = null;
		}
		return false;
	}
	
}

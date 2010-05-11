package com.badlogic.gdx.tests;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.RenderListener;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.ImmediateModeRenderer;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

public class Box2DTest implements RenderListener
{
	ImmediateModeRenderer renderer;
	OrthographicCamera cam;
	
	World world;
	List<Body> boxes = new ArrayList<Body>( );
	Body ground;
	
	@Override
	public void dispose(Application app) 
	{	
		
	}

	@Override
	public void render(Application app) 
	{	
		app.getGraphics().getGL10().glClear( GL10.GL_COLOR_BUFFER_BIT );		
		cam.setMatrices(app.getGraphics());	
				
		world.step( app.getGraphics().getDeltaTime(), 5, 5 );		
		
		for( int i = 0; i < boxes.size(); i++ )				
			renderBox( app.getGraphics().getGL10(), boxes.get(i) );		
		
//		try {
//			Thread.sleep( 16 );
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
	}

	private void renderBox( GL10 gl, Body body )
	{
		gl.glPushMatrix();
		Vector2 pos = body.getWorldCenter();
		float angle = body.getAngle();
		gl.glTranslatef(pos.x, pos.y, 0 );
		gl.glRotatef( (float)Math.toDegrees(angle), 0, 0, 1 );
		renderer.begin( GL10.GL_LINE_STRIP );
		renderer.vertex( -1, -1, 0 );
		renderer.vertex( -1,  1, 0 );
		renderer.vertex(  1,  1, 0 );
		renderer.vertex(  1, -1, 0 );
		renderer.vertex( -1, -1, 0 );
		renderer.end();
		gl.glPopMatrix();
	}
	
	@Override
	public void surfaceChanged(Application app, int width, int height) 
	{	
		renderer = new ImmediateModeRenderer(app.getGraphics().getGL10());
		cam = new OrthographicCamera();
		cam.setViewport( 48, 32 );
		cam.getPosition().set( 0, 16, 0 );
		
		createWorld( );
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
		
		for( int i = 0; i < 100; i++ )
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
		
		PolygonShape poly = new PolygonShape( );
		poly.setAsBox( 1, 1 );
		Body box = world.createBody( bodyDef );
		box.createFixture(poly, 1);
		poly.dispose();
		return box;
	}

	@Override
	public void surfaceCreated(Application app) 
	{	
		
	}
	
//	public static void main( String[] argv )
//	{
//		JoglApplication app = new JoglApplication( "Box2D Test", 480, 320, false );
//		app.getGraphics().setRenderListener( new com.badlogic.gdx.tests.Box2DTest() );
//	}
}

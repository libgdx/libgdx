package com.badlogic.gdx.tests;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.RenderListener;
import com.badlogic.gdx.backends.desktop.JoglApplication;
import com.badlogic.gdx.graphics.FloatMesh;
import com.badlogic.gdx.graphics.Font;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.MeshRenderer;
import com.badlogic.gdx.graphics.ModelLoader;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Text;
import com.badlogic.gdx.graphics.Font.FontStyle;
import com.badlogic.gdx.math.Matrix;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector;
import com.badlogic.gdx.math.collision.CollisionDetection;
import com.badlogic.gdx.math.collision.CollisionMesh;
import com.badlogic.gdx.math.collision.EllipsoidCollider;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.math.collision.Segment;
import com.badlogic.gdx.math.collision.SlideResponse;
import com.badlogic.gdx.math.collision.Sphere;

public class CollisionTest implements RenderListener
{
	Font font;
	Text text;
	final float VERY_CLOSE_DISTANCE = 0.001f;
	final float SPEED = 1f;	
	CollisionMesh cMesh;
	EllipsoidCollider collider;
	MeshRenderer mesh;
	PerspectiveCamera cam;		
	float[] lightColor = { 1, 1, 1, 0 };
	float[] lightPosition = { 2, 5, 10, 0 }; 
	float yAngle = 0;
	Matrix mat = new Matrix();	
	Vector axis = new Vector( 0, 1, 0 );
	Vector velocity = new Vector( 0, 0, 0 );
	Vector gravity = new Vector( 0, 0, 0 );
	Segment segment = new Segment( new Vector(), new Vector() );

	@Override
	public void surfaceCreated(Application app) 
	{			
		FloatMesh m = (FloatMesh)ModelLoader.loadObj( app.getFiles().readInternalFile( "data/scene.obj" ), true );
		
//		FloatMesh m = new FloatMesh( 4, 3, false, false, false, 0, 0, true, 6 );
//		m.setVertices( new float[] { -5, -2.5f, 5,
//									  5, -2.5f, 5,
//									  5,  2.5f, -5,
//									 -5,  2.5f, -5} );
//		m.setIndices( new short[] { 0, 1, 2, 2, 3, 0 } );
		
		
//		FloatMesh m = new FloatMesh( 3, 3, false, true, false, 0, 0, false, 0 );
//		m.setVertices( new float[] { -10, 0, 10, 0, 1, 0,
//									  10, 0, 10, 0, 1, 0,
//									   0, 0,-10, 0, 1, 0 
//				
//		});
		
//		FloatMesh m = new FloatMesh( 8, 3, false, true, false, 0, 0, true, 12 );
//		m.setVertices( new float[] { -2, 0, -2, 0, 1, 0, 
//									 -2, 0, 2, 0, 1, 0,
//									 2, 0, 2, 0, 1, 0,
//									 2, 0, -2, 0, 1, 0,
//									 
//									 -2, 0, -2, 0, 0, 1,
//									 2, 0, -2, 0, 0, 1,
//									 2, 2, -2, 0, 0, 1, 
//									 -2, 2, -2, 0, 0, 1,
//									 	
//									 
//		} );						
//		m.setIndices( new short[] { 0, 1, 2, 2, 3, 0, 4, 5, 6, 6, 7, 4 } );
		
		mesh = new MeshRenderer( app.getGraphics().getGL10(), m, true, true );			
		cam = new PerspectiveCamera();
		cam.setFov( 45 );
		cam.setNear( 0.1f );
		cam.setFar( 1000 );
		cam.getPosition().y = 3f;
		cam.getPosition().z = 0f;
		
		cMesh = new CollisionMesh( m, false );
		collider = new EllipsoidCollider( 1, 1, 1, new SlideResponse() );
		
//		font = app.getGraphics().newFont( "Arial", 16, FontStyle.Plain, true );
//		text = font.newText();
	}
	
	@Override
	public void render(Application app) 
	{
		processInput( app.getInput(), app.getGraphics().getDeltaTime() );
		
		GL10 gl = app.getGraphics().getGL10();
		gl.glClearColor( 0, 0, 0, 0 );
		gl.glClear( GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT );
		
		render3D( gl, app.getGraphics() );
//		renderStats( gl, app.getGraphics() );
	}	
		
	private void renderStats( GL10 gl, Graphics g )
	{
		mat.setToOrtho2D( 0, 0, g.getWidth(), g.getHeight() );
		gl.glMatrixMode( GL10.GL_PROJECTION );
		gl.glLoadMatrixf( mat.val, 0 );
		gl.glMatrixMode( GL10.GL_MODELVIEW );
		gl.glLoadIdentity();
		
		gl.glEnable( GL10.GL_TEXTURE_2D );
		gl.glEnable( GL10.GL_BLEND );
		gl.glBlendFunc( GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA );
		text.setText( cam.getPosition().toString() );
		gl.glColor4f( 1, 0, 0, 1 );
		text.render();
		gl.glColor4f( 1, 1, 1, 1 );
		gl.glDisable( GL10.GL_BLEND );
		gl.glDisable( GL10.GL_TEXTURE_2D );		
	}
	
	private void render3D( GL10 gl, Graphics g )
	{
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
		if( input.isKeyPressed( Input.Keys.KEYCODE_ENTER ) )
			cam.getPosition().set( 0, 1, 0 );
		
		if( input.isKeyPressed( Input.Keys.KEYCODE_DPAD_LEFT ) )
			yAngle += deltaTime * 120;
		if( input.isKeyPressed( Input.Keys.KEYCODE_DPAD_RIGHT ) )
			yAngle -= deltaTime * 120;
		
		cam.getDirection().set( 0, 0, -1 );
		mat.setToRotation( axis, yAngle );
		cam.getDirection().rot( mat );
		
	

		if( input.isKeyPressed( Input.Keys.KEYCODE_DPAD_UP ) )		
			velocity.add(cam.getDirection().tmp().mul( SPEED * deltaTime));
		if( input.isKeyPressed( Input.Keys.KEYCODE_DPAD_DOWN ) )
			velocity.add(cam.getDirection().tmp().mul(SPEED * -deltaTime));
								
		System.out.println( "vel col:");
		velocity.add( 0, 0.5f * - deltaTime, 0 );
		collider.collide( cMesh, cam.getPosition(), velocity, 0.00005f );
		
		segment.a.set( cam.getPosition() );
		segment.b.set( cam.getPosition() ).y -= 1.1f;
//		if( !CollisionDetection.testMeshSegment( cMesh, segment) )
//		{
//			System.out.println( "grav col:");
//			gravity.add( 0, -0.1f * deltaTime, 0 );
//			collider.collide( cMesh, cam.getPosition(), gravity, 0.00005f );
//			System.out.println( gravity );
//		}		
		velocity.mul( 0.90f ); // decay
		gravity.mul( 0.9f );					
	}
			
	public void collide( CollisionMesh mesh, Vector origin, Vector velocity )
	{
		float distToTravel = velocity.len();
		
		if( distToTravel < CollisionDetection.EPSILON )
			return;
		
		boolean collisionFound = false;
		float nearestDistance = -1.0f;
		Vector nearestIntersectionPoint = new Vector();
		Vector nearestPolygonIntersectionPoint = new Vector( );
		
		Vector p1 = new Vector( );
		Vector p2 = new Vector( );
		Vector p3 = new Vector( );
		Plane plane = new Plane( new Vector(), 0 );
		
		float[] triangles = mesh.getTriangles();
		int numTriangles = mesh.getNumTriangles();
		int idx = 0;
		for( int i = 0; i < numTriangles; i++ )
		{
			p1.set( triangles[idx++], triangles[idx++], triangles[idx++] );
			p2.set( triangles[idx++], triangles[idx++], triangles[idx++] );
			p3.set( triangles[idx++], triangles[idx++], triangles[idx++] );
			
			if( mesh.isClockWise() )
				plane.set( p3, p2, p1 );
			else
				plane.set( p1, p2, p3 );
			
			
			float pDist = CollisionDetection.signedDistanceToPlane( plane, origin );
			Vector sphereIntersectionPoint = new Vector( );
			Vector planeIntersectionPoint = new Vector( );
			
			if( pDist < 0 )
				continue;
			else
			if( pDist <= 1.0 )
			{
				Vector temp = new Vector( plane.normal ).mul( -pDist );
				planeIntersectionPoint.set( origin ).add( temp );
			}
			else
			{
				sphereIntersectionPoint.set( origin ).sub(plane.normal );
				if( !CollisionDetection.intersectRayPlane( new Ray( sphereIntersectionPoint, velocity), plane, planeIntersectionPoint) )
					continue;				
			}
			
			Vector polygonIntersectionPoint = new Vector( planeIntersectionPoint );
			if( !CollisionDetection.isPointInTriangle( planeIntersectionPoint, p1, p2, p3 ) )
			{
				Vector in = new Vector();
				CollisionDetection.closestPointToTriangle( p1, p2, p3, planeIntersectionPoint, in );
				polygonIntersectionPoint.set(in);
			}
			
			Vector in = new Vector();
			Vector negativeVelocityVector = new Vector( velocity ).mul(-1);
			if( CollisionDetection.intersectRaySphere( new Ray( polygonIntersectionPoint, negativeVelocityVector ), new Sphere( origin, 1), in) )
			{
				float t = origin.dst( in );
				if( !collisionFound || t < nearestDistance )
				{
					nearestDistance = t;
					nearestIntersectionPoint.set( in );
					nearestPolygonIntersectionPoint.set( polygonIntersectionPoint );
					collisionFound = true;
				}
			}
		}
		
		if( !collisionFound )		
			return;		
		
		Vector v = new Vector( velocity ).nor().mul( nearestDistance - CollisionDetection.EPSILON );
		origin.add( v );
		
		v.nor().mul( distToTravel - nearestDistance );
		Vector destinationPoint = new Vector( nearestPolygonIntersectionPoint ).add( v );
		
		Vector slidePlaneOrigin = new Vector( nearestPolygonIntersectionPoint );
		Vector slidePlaneNormal = new Vector( nearestPolygonIntersectionPoint ).sub(origin).nor();
				
		float time = CollisionDetection.intersectRayPlane( new Ray( destinationPoint, slidePlaneNormal ), new Plane( slidePlaneOrigin, slidePlaneNormal ) );
		slidePlaneNormal.nor().mul(time);
		Vector destinationProjectionNormal = new Vector( slidePlaneNormal );
		Vector newDestinationPoint = new Vector( destinationPoint ).add( destinationProjectionNormal );
		Vector newVelocityVector = newDestinationPoint.sub(nearestPolygonIntersectionPoint);
		
		collide( mesh, origin, newVelocityVector );
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

package com.badlogic.gdx.tests;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.RenderListener;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Font;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.SpriteBatch;
import com.badlogic.gdx.graphics.Font.FontStyle;
import com.badlogic.gdx.graphics.loaders.md5.MD5Loader;
import com.badlogic.gdx.graphics.loaders.md5.MD5Model;
import com.badlogic.gdx.graphics.loaders.md5.MD5Renderer;

public class MD5Test implements RenderListener
{
	MD5Model model;
	MD5Renderer renderer;
	PerspectiveCamera camera;
	SpriteBatch batch;
	Font font;
	
	@Override
	public void surfaceCreated(Application app) 
	{	
		if( model == null )
		{
			model = MD5Loader.loadModel( app.getFiles().readFile( "data/zfat.md5mesh", FileType.Internal ) );
			
			app.log( "MD5 Test", "num triangles: " + model.getNumTriangles() );
			app.log( "MD5 Test", "num vertices: " + model.getNumVertices( ) );
			
			renderer = new MD5Renderer( app.getGraphics(), model, true );
			renderer.setSkeleton( model.baseSkeleton );
			
			camera = new PerspectiveCamera();
			camera.setFov( 60 );
			camera.getPosition().set( 0, 25, 100 );
			camera.setNear( 1 );
			camera.setFar( 1000 );
			
			batch = new SpriteBatch( app.getGraphics() );
			font = app.getGraphics().newFont( "Arial", 12, FontStyle.Plain, true );
			
			GL10 gl = app.getGraphics().getGL10();
			gl.glViewport( 0, 0, app.getGraphics().getWidth(), app.getGraphics().getHeight() );							
		}
	}

	@Override
	public void surfaceChanged(Application app, int width, int height) 
	{	
		
	}

	float angle = 0;
	@Override
	public void render(Application app) 
	{	
		GL10 gl = app.getGraphics().getGL10();
		gl.glClear( GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT );
		gl.glEnable( GL10.GL_DEPTH_TEST );
		gl.glPolygonMode( GL10.GL_FRONT_AND_BACK, GL10.GL_FILL );
		
		camera.setMatrices( app.getGraphics() );
		
		angle+=app.getGraphics().getDeltaTime() * 20;
		for( int z = 0; z < 1000; z+= 50 )
		{					
			gl.glLoadIdentity();
			gl.glTranslatef( 0, 0, -z );
			gl.glRotatef( angle, 0, 1, 0 );
			gl.glRotatef( -90, 1, 0, 0 );
			
			renderer.setSkeleton( model.baseSkeleton );
			renderer.render( );
		}
		
		gl.glDisable( GL10.GL_DEPTH_TEST );
		gl.glPolygonMode( GL10.GL_FRONT_AND_BACK, GL10.GL_FILL );	
		batch.begin();
		batch.drawText( font, "fps: " + app.getGraphics().getFramesPerSecond() + ", delta: " + app.getGraphics().getDeltaTime(), 0, 20, Color.WHITE );
		batch.end();
	}

	@Override
	public void dispose(Application app) 
	{	
		
	}
}

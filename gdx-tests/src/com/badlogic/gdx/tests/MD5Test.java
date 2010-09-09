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
import com.badlogic.gdx.math.Vector3;

public class MD5Test implements RenderListener
{
	PerspectiveCamera camera;
	MD5Model model;
	MD5Renderer renderer;
	SpriteBatch batch;
	Font font;

	
	@Override
	public void surfaceCreated(Application app) 
	{
		if( model == null )
		{
			model = MD5Loader.loadModel( app.getFiles().readFile( "data/zfat.md5mesh", FileType.Internal) );
			renderer = new MD5Renderer( app.getGraphics(), model, true );
		
			camera = new PerspectiveCamera();
			camera.getPosition().set( 0, 25, 100 );
			camera.setFov(60);
			camera.setNear( 1 );
			camera.setFar( 1000 );
			camera.setViewport( app.getGraphics().getWidth(), app.getGraphics().getHeight() );
		
			batch = new SpriteBatch( app.getGraphics() );
			font = app.getGraphics().newFont( "Arial", 12, FontStyle.Plain, true );		
			app.getGraphics().getGL10().glViewport( 0, 0, app.getGraphics().getWidth(), app.getGraphics().getHeight() );
		}
	}

	@Override
	public void surfaceChanged(Application app, int width, int height) {
		// TODO Auto-generated method stub
		
	}

	float angle = 0;
	
	@Override
	public void render(Application app) 
	{
		GL10 gl = app.getGraphics().getGL10();
		gl.glClear( GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT );
		
		gl.glEnable( GL10.GL_DEPTH_TEST );
		camera.setMatrices( app.getGraphics() );
		angle+=app.getGraphics().getDeltaTime() * 20;
		
		for( int z = 0; z < 500; z += 50 )
		{
			gl.glLoadIdentity();
			gl.glTranslatef( 0, 0, -z );
			gl.glRotatef( angle, 0, 1, 0 );
			gl.glRotatef(-90, 1, 0, 0 );
			
			renderer.setSkeleton( model.baseSkeleton );
			renderer.render();
		}
		
		gl.glDisable( GL10.GL_DEPTH_TEST );
		batch.begin();
		batch.drawText( font, "fps: " + app.getGraphics().getFramesPerSecond(), 10, 20, Color.WHITE );
		batch.end();
	}

	@Override
	public void dispose(Application app) {
		// TODO Auto-generated method stub
		
	}

}

package com.badlogic.gdx.tests;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.InputListener;
import com.badlogic.gdx.RenderListener;
import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.backends.desktop.JoglApplication;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.MeshRenderer;
import com.badlogic.gdx.graphics.ModelLoader;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Vector;

public class OctTest implements RenderListener, InputListener
{
	PerspectiveCamera cam;
	MeshRenderer mesh;	
	float angleY = 0;
	float angleX = 0;
	float[] lightColor = { 1, 1, 1, 0 };
	float[] lightPosition = { 2, 5, 10, 0 }; 
	float touchStartX = 0;
	float touchStartY = 0;
	
	long frameStart;
	int frames = 0;

	@Override
	public void surfaceCreated(Application app) 
	{	
		if( mesh == null )
		{
			app.getInput().addInputListener( this );
			
			Mesh m = ModelLoader.loadOct( app.getFiles().readFile( "data/steps.oct", FileType.Internal ), false, new Vector() );
			mesh = new MeshRenderer( app.getGraphics().getGL10(), m, true, true );									
			
			cam = new PerspectiveCamera();
			cam.getPosition().set( 2, 2, 2 );
			cam.getDirection().set( -1, -1, -1 );							
		}
		frameStart = System.nanoTime();
	}

	@Override
	public void surfaceChanged(Application app, int width, int height) 
	{	
		
	}
	
	@Override
	public void render(Application app) 
	{	
		GL10 gl = app.getGraphics().getGL10();
				
		gl.glClear( GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT );
		gl.glEnable( GL10.GL_DEPTH_TEST );
		gl.glEnable( GL10.GL_LIGHTING );		
		gl.glEnable( GL10.GL_COLOR_MATERIAL );
		gl.glEnable( GL10.GL_TEXTURE_2D );
		
		cam.setMatrices( app.getGraphics() );							
		
		gl.glEnable( GL10.GL_LIGHT0 );				
		gl.glLightfv( GL10.GL_LIGHT0, GL10.GL_DIFFUSE, lightColor, 0 );
		gl.glLightfv( GL10.GL_LIGHT0, GL10.GL_POSITION, lightPosition, 0 );
		
		gl.glRotatef(angleY, 0, 1, 0);	
		gl.glRotatef(angleX, 1, 0, 0 );		
		mesh.render( GL10.GL_TRIANGLES );
		
		if( System.nanoTime() - frameStart > 1000000000 )
		{
			app.log( "Obj Test", "fps: " + frames );
			frames = 0;
			frameStart = System.nanoTime();
		}
		
		frames++;
	}

	@Override
	public void dispose(Application app) 
	{	
		
	}	

	public static void main( String[] argv )
	{
		JoglApplication app = new JoglApplication( "Oct Test", 480, 320, false );
		app.getGraphics().setRenderListener( new OctTest() );
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

	@Override
	public boolean touchDown(int x, int y, int pointer) {
		touchStartX = x;
		touchStartY = y;
		return false;
	}

	@Override
	public boolean touchDragged(int x, int y, int pointer) 
	{		
		angleY += (x - touchStartX);
		angleX += (y - touchStartY);
		touchStartX = x;
		touchStartY = y;
		return false;
	}

	@Override
	public boolean touchUp(int x, int y, int pointer) {
		// TODO Auto-generated method stub
		return false;
	}
}

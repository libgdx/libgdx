package com.badlogic.gdx;

import java.applet.Applet;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.backends.applet.AppletApplication;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.ModelLoader;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.math.Matrix;

public class SimpleTestApplet extends Applet implements RenderListener, InputListener
{
	public void init()
	{
		AppletApplication app = new AppletApplication( this, false, false );		
		app.getGraphics().setRenderListener( this );
	}

	PerspectiveCamera cam;
	Mesh mesh;
	Texture texture;
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
			
			mesh = ModelLoader.loadObj( app.getGraphics(), app.getFiles().readFile( "data/cube.obj", FileType.Internal ), true, true );			
			
			Pixmap pixmap = app.getGraphics().newPixmap( app.getFiles().readFile( "data/badlogic.jpg", FileType.Internal));
			texture = app.getGraphics().newTexture( pixmap, TextureFilter.MipMap, TextureFilter.Linear, TextureWrap.ClampToEdge, TextureWrap.ClampToEdge, true );					
			
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
				
		gl.glViewport( 0, 0, app.getGraphics().getWidth(), app.getGraphics().getHeight() );
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
		texture.bind();
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

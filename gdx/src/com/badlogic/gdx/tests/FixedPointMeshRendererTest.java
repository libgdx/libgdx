package com.badlogic.gdx.tests;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.RenderListener;
import com.badlogic.gdx.backends.desktop.JoglApplication;
import com.badlogic.gdx.graphics.FixedPointMesh;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.MeshRenderer;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;

public class FixedPointMeshRendererTest implements RenderListener
{
	MeshRenderer mesh;
	Texture texture;
	
	@Override
	public void dispose(Application app) 
	{	
		mesh.dispose();
	}

	@Override
	public void render(Application app) 
	{
		GL10 gl = app.getGraphics().getGL10();
		gl.glViewport( 0, 0, app.getGraphics().getWidth(), app.getGraphics().getHeight() );
		gl.glClearColor( 0.7f, 0.7f, 0.7f, 1 );
		gl.glClear( GL10.GL_COLOR_BUFFER_BIT );
		gl.glEnable(GL10.GL_TEXTURE_2D );
		texture.bind();
		mesh.render( GL10.GL_TRIANGLES, 0, 3);
	}

	@Override
	public void surfaceCreated(Application app) 
	{			
		FixedPointMesh m = new FixedPointMesh( 3, 3, true, false, true, 1, 2, true, 3 );
		m.setVertices( new float[] { -0.5f, -0.5f, 0, 1, 0, 0, 1, 0, 0,  
									  0.5f, -0.5f, 0, 0, 1, 0, 1, 1, 0,
									  0.0f,  0.5f, 0, 0, 0, 1, 1, 0.5f, 1} );
		m.setIndices( new short[] { 0, 1, 2 } );
		mesh = new MeshRenderer( app.getGraphics().getGL10(), m, true, true );		
		
		Pixmap pixmap = app.getGraphics().newPixmap(256, 256, Format.RGBA8888 );
		pixmap.setColor(1, 1, 1, 1 );
		pixmap.fill();
		pixmap.setColor(0, 0, 0, 1 );
		pixmap.drawLine(0, 0, 256, 256);
		pixmap.drawLine(256, 0, 0, 256);		
		texture = app.getGraphics().newTexture( pixmap, TextureFilter.MipMap, TextureFilter.Linear, TextureWrap.ClampToEdge, TextureWrap.ClampToEdge, true );
	}

	@Override
	public void surfaceChanged(Application app, int width, int height) {
		// TODO Auto-generated method stub
		
	}
	
	public static void main( String[] argv )
	{
		JoglApplication app = new JoglApplication( "Fixed Point MeshRenderer Test", 480, 320, false );
		app.getGraphics().setRenderListener( new FixedPointMeshRendererTest() );
	}
}

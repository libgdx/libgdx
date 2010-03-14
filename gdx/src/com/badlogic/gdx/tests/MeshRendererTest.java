package com.badlogic.gdx.tests;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.RenderListener;
import com.badlogic.gdx.backends.desktop.JoglApplication;
import com.badlogic.gdx.graphics.FloatMesh;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.MeshRenderer;

public class MeshRendererTest implements RenderListener
{
	MeshRenderer mesh;
	
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
		mesh.render( GL10.GL_TRIANGLES, 0, 3);
	}

	@Override
	public void setup(Application app) 
	{
		FloatMesh m = new FloatMesh( 3, 3, true, 4, false, false, 0, 0, false, 0 );
		m.setVertices( new float[] { -0.5f, -0.5f, 0, 1, 0, 0, 1,  
									  0.5f, -0.5f, 0, 0, 1, 0, 1,
									  0.0f,  0.5f, 0, 0, 0, 1, 1} );
		mesh = new MeshRenderer( app.getGraphics().getGL10(), m, true );
	}

	public static void main( String[] argv )
	{
		JoglApplication app = new JoglApplication( "MeshRenderer Test", 480, 320, false );
		app.getGraphics().setRenderListener( new MeshRendererTest() );
	}
}

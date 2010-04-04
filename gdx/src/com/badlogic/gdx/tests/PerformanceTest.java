package com.badlogic.gdx.tests;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.RenderListener;
import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.backends.desktop.JoglApplication;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.MeshRenderer;
import com.badlogic.gdx.graphics.ModelLoader;

public class PerformanceTest implements RenderListener
{
	boolean fixed = true;
	
	MeshRenderer fpMesh;
	MeshRenderer flMesh;

	long startTime = System.nanoTime();
	int frames = 0;
	
	@Override
	public void dispose(Application app) 
	{	
		
	}

	@Override
	public void render(Application app) 
	{
		MeshRenderer m = null;
		if( fixed )		
			m = fpMesh;
		else		
			m = flMesh;
		
		for( int i = 0; i < 10; i++ )
		{
			m.render(GL10.GL_TRIANGLES );
		}
		
		if( (System.nanoTime() - startTime ) > 1000000000 )
		{
			app.log( "Performance", frames + " fps, " + ( m.getMesh().getNumVertices() / 3 ) * frames * 10 + " tris/s" );
			frames = 0;
			startTime = System.nanoTime();
		}
		frames++;
	}

	@Override
	public void surfaceChanged(Application app, int width, int height) 
	{	
		
	}

	@Override
	public void surfaceCreated(Application app) 
	{	
		Mesh m = ModelLoader.loadObj( app.getFiles().readFile( "data/heavysphere.obj", FileType.Internal), false );
		fpMesh = new MeshRenderer( app.getGraphics().getGL10(), m, true, true );
		m = ModelLoader.loadObj( app.getFiles().readFile( "data/heavysphere.obj", FileType.Internal), true );
		flMesh = new MeshRenderer( app.getGraphics().getGL10(), m, true, true );
	}
	
	public static void main( String[] argv )
	{
		JoglApplication app = new JoglApplication( "Performance Test", 480, 320, false );
		app.getGraphics().setRenderListener( new PerformanceTest() );
	}
}

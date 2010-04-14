package com.badlogic.gdx.tests;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.RenderListener;
import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.ModelLoader;

public class PerformanceTest implements RenderListener
{
	boolean fixed = false;
	
	Mesh fpMesh;
	Mesh flMesh;

	long startTime = System.nanoTime();
	int frames = 0;
	
	@Override
	public void dispose(Application app) 
	{	
		
	}

	@Override
	public void render(Application app) 
	{
		Mesh m = null;
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
			app.log( "Performance", frames + " fps, " + ( m.getNumVertices() / 3 ) * frames * 10 + " tris/s" );
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
		fpMesh = ModelLoader.loadObj( app.getGraphics(), app.getFiles().readFile( "data/heavysphere.obj", FileType.Internal), false, false );		
		flMesh = ModelLoader.loadObj( app.getGraphics(), app.getFiles().readFile( "data/heavysphere.obj", FileType.Internal), false, true );		
	}
		
}

package com.badlogic.gdx.samples;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.RenderListener;
import com.badlogic.gdx.Mesh;
import com.badlogic.gdx.Mesh.PrimitiveType;
import com.badlogic.gdx.backends.jogl.JoglApplication;

public class TriangleTest implements RenderListener
{
	Mesh mesh;	

	@Override
	public void setup(Application application) 
	{
		mesh = application.newMesh( 3, false, false, false, false, 0, true );
		mesh.vertex( -0.5f, -0.5f, 0 );
		mesh.vertex( 0.5f, -0.5f, 0 );
		mesh.vertex( 0, 0.5f, 0 );		
	}
	
	@Override
	public void render(Application application) 
	{
		mesh.render( PrimitiveType.Triangles );
	}
	
	@Override
	public void dispose(Application application) 
	{	
		
	}

	public static void main( String[] argv )
	{
		JoglApplication app = new JoglApplication( "Triangle", 480, 320 );
		app.addRenderListener( new TriangleTest() );
	}
}

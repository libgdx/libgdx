package com.badlogic.gdx.samples;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.GraphicListener;
import com.badlogic.gdx.Mesh;
import com.badlogic.gdx.Mesh.PrimitiveType;
import com.badlogic.gdx.backends.jogl.JoglApplication;

public class Triangle implements GraphicListener
{
	Mesh mesh;
	
	@Override
	public void dispose(Application application) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void render(Application application) {
		mesh.render( PrimitiveType.Triangles );
	}

	@Override
	public void setup(Application application) {
		mesh = application.newMesh( 3, false, false, false, false, 0, true );
		mesh.vertex( -0.5f, -0.5f, 0 );
		mesh.vertex( 0.5f, -0.5f, 0 );
		mesh.vertex( 0, 0.5f, 0 );		
	}

	public static void main( String[] argv )
	{
		JoglApplication app = new JoglApplication( "Triangle", 480, 320 );
		app.addGraphicListener( new Triangle() );
	}
}

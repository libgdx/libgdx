package com.badlogic.gdx.samples;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Mesh;
import com.badlogic.gdx.PerspectiveCamera;
import com.badlogic.gdx.RenderListener;
import com.badlogic.gdx.Mesh.PrimitiveType;

public class AccelerometerTest implements RenderListener 
{
	Mesh mesh;	
	PerspectiveCamera camera;

	@Override
	public void setup(Application application) 
	{
		camera = new PerspectiveCamera();
		camera.getPosition().set( 0, 0, 4 );
		camera.setFov( 90 );		
		
		mesh = application.newMesh( 3, false, false, false, false, 0, true );
		mesh.vertex( -0.5f, -0.5f, 0 );
		mesh.vertex( 0.5f, -0.5f, 0 );
		mesh.vertex( 0, 0.5f, 0 );
	}
	
	@Override
	public void render(Application application) 
	{
		application.clear( true, false, false );
		camera.setMatrices( application );
		application.rotate( 90 * application.getAccelerometerY() / 10, 0, 0, 1 );
		
		mesh.render(PrimitiveType.Triangles );
		
	}

	@Override
	public void dispose(Application application) {
		// TODO Auto-generated method stub
		
	}	
}

package com.badlogic.gdx;

import java.applet.Applet;

import com.badlogic.gdx.backends.applet.AppletApplication;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.math.Matrix;

public class SimpleTestApplet extends Applet implements RenderListener
{
	Mesh mesh;
	PerspectiveCamera camera;
	Matrix matrix = new Matrix();
	float angle = 0;
	
	public void init()
	{
		AppletApplication app = new AppletApplication( this, false );		
		app.getGraphics().setRenderListener( this );
	}

	@Override
	public void surfaceCreated(Application app) 
	{
		mesh = new Mesh( app.getGraphics(), true, true, false, 8, 36, new VertexAttribute( Usage.Position, 3, "a_Position" ) );

		float hsize = 0.5f;
		float vsize = 0.5f;
		float zsize = 0.5f;
		mesh.setVertices( new float[] 
		                            {
				-hsize, vsize, -zsize,
				hsize, vsize, -zsize,
				-hsize, -vsize, -zsize,
				hsize, -vsize, -zsize,
				-hsize, vsize, zsize,
				hsize, vsize, zsize,
				-hsize, -vsize, zsize,
				hsize, -vsize, zsize	
		                            });
		mesh.setIndices( new short[] {
				0, 1, 2, 2, 1, 3,
				1, 5, 3, 3, 5, 7,
				5, 4, 7, 7, 4, 6,
				4, 0, 6, 6, 0, 2,
				0, 4, 1, 1, 4, 5,
				2, 6, 3, 3, 6, 7 
		});

		camera = new PerspectiveCamera();
		camera.getPosition().set( 2, 2, 2 );
		camera.getDirection().set( -1, -1, -1 );		
	}
	
	@Override
	public void render(Application app) 
	{
		GL10 gl = app.getGraphics().getGL10();
		gl.glClearColor(1, 0, 0, 0);
		gl.glClear( GL10.GL_COLOR_BUFFER_BIT );
		camera.setMatrices( app.getGraphics() );

		angle += app.getGraphics().getDeltaTime() * 45;
		matrix.setFromEulerAngles( angle, angle, 0 );
		gl.glMultMatrixf( matrix.val, 0);

		mesh.render( GL10.GL_TRIANGLES );

		try {
			Thread.sleep( 20 );
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void surfaceChanged(Application app, int width, int height) 
	{	

	}
	
	@Override
	public void dispose(Application app) 
	{	

	}
}

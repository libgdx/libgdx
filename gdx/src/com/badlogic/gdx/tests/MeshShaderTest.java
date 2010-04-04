package com.badlogic.gdx.tests;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.RenderListener;
import com.badlogic.gdx.backends.desktop.JoglApplication;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.ShaderProgram;
import com.badlogic.gdx.graphics.mesh.Mesh;
import com.badlogic.gdx.graphics.mesh.VertexAttribute;
import com.badlogic.gdx.graphics.mesh.VertexAttributes.Usage;
import com.badlogic.gdx.math.Matrix;

public class MeshShaderTest implements RenderListener
{
	ShaderProgram shader;
	Mesh mesh;		
	Matrix matrix = new Matrix();

	@Override
	public void surfaceCreated(Application app) 
	{
		String vertexShader =  "attribute vec4 a_position;    \n" +
							   "attribute vec4 a_color;\n" +
							   "uniform mat4 worldView;\n" +
							   "varying vec3 v_color;" +
							   "void main()                  \n" +
							   "{                            \n" +
							   "   v_color = vec4(a_color.x, a_color.y, a_color.z, 1); \n" +
							   "   gl_Position =  worldView * a_position;  \n" +
							   "}                            \n";
		String fragmentShader = "precision mediump float;\n" +
								"varying vec4 v_color;\n" +
								"void main()                                  \n" +
							    "{                                            \n" +
							    "  gl_FragColor = v_color;\n" +
							    "}";  
		
		shader = new ShaderProgram( app.getGraphics().getGL20(), vertexShader, fragmentShader);
		if( shader.isCompiled() == false )
			app.log( "ShaderTest", shader.getLog() );
		
		mesh = new Mesh( app.getGraphics(), true, true, false, 3, 0, 
						 new VertexAttribute( Usage.Position, 3, "a_position" ),
						 new VertexAttribute( Usage.Color, 3, "a_color" ) );
		
		mesh.setVertices( new float[] { -0.5f, -0.5f, 0, 1, 0, 0,
										 0.5f, -0.5f, 0, 0, 1, 0,
										 0, 0.5f, 0, 0, 0, 1 } );	
		
		matrix.setToTranslation( 0.3f, 0.2f, 0 );
	}

	@Override
	public void render(Application app) 
	{
		shader.begin();		
		shader.setUniformMatrix( "worldView", matrix );
		mesh.render( shader, GL10.GL_TRIANGLES );
		shader.end();
	}
	
	@Override
	public void surfaceChanged(Application app, int width, int height) 
	{	
		
	}
	
	@Override
	public void dispose(Application app) 
	{
		
	}
	
	public static void main( String[] argv )
	{
		JoglApplication app = new JoglApplication( "Mesh Test", 480, 320, true );
		app.getGraphics().setRenderListener( new MeshShaderTest() );
	}
}

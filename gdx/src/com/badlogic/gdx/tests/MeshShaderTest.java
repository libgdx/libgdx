package com.badlogic.gdx.tests;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.RenderListener;
import com.badlogic.gdx.backends.desktop.JoglApplication;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.ShaderProgram;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.mesh.Mesh;
import com.badlogic.gdx.graphics.mesh.VertexAttribute;
import com.badlogic.gdx.graphics.mesh.VertexAttributes.Usage;
import com.badlogic.gdx.math.Matrix;

public class MeshShaderTest implements RenderListener
{
	ShaderProgram shader;
	Mesh mesh;		
	Texture texture;
	Matrix matrix = new Matrix();

	@Override
	public void surfaceCreated(Application app) 
	{
		String vertexShader =  "attribute vec4 a_position;    \n" +
							   "attribute vec4 a_color;\n" +
							   "attribute vec2 a_texCoords;\n" +							   
							   "uniform mat4 u_worldView;\n" +							   
							   "varying vec4 v_color;" +
							   "varying vec2 v_texCoords;" +							   
							   "void main()                  \n" +
							   "{                            \n" +
							   "   v_color = vec4(a_color.x, a_color.y, a_color.z, 1); \n" +
							   "   v_texCoords = a_texCoords; \n" +
							   "   gl_Position =  u_worldView * a_position;  \n" +
							   "}                            \n";
		String fragmentShader = "precision mediump float;\n" +
								"varying vec4 v_color;\n" +
								"varying vec2 v_texCoords;\n" +								
								"uniform sampler2D u_texture;\n" +
								"void main()                                  \n" +
							    "{                                            \n" +							    							   
							    "  gl_FragColor = v_color * texture2D(u_texture, v_texCoords);\n" +
							    "}";  
		
		shader = new ShaderProgram( app.getGraphics().getGL20(), vertexShader, fragmentShader);
		if( shader.isCompiled() == false )
		{
			app.log( "ShaderTest", shader.getLog() );
			System.exit(0);
		}
		
		mesh = new Mesh( app.getGraphics(), true, true, false, 3, 0, 
						 new VertexAttribute( Usage.Position, 3, "a_position" ),
						 new VertexAttribute( Usage.Color, 3, "a_color" ),
						 new VertexAttribute( Usage.TextureCoordinates, 2, "a_texCoords" ) );
		
		mesh.setVertices( new float[] { -0.5f, -0.5f, 0, 1, 0, 0, 0, 0,
										 0.5f, -0.5f, 0, 0, 1, 0, 1, 0,
										 0, 0.5f, 0, 0, 0, 1, 0.5f, 1 } );	
		
		matrix.setToTranslation( 0.3f, 0.2f, 0 );
		
		Pixmap pixmap = app.getGraphics().newPixmap(256, 256, Format.RGBA8888 );
		pixmap.setColor(1, 1, 1, 1 );
		pixmap.fill();
		pixmap.setColor(0, 0, 0, 1 );
		pixmap.drawLine(0, 0, 256, 256);
		pixmap.drawLine(256, 0, 0, 256);
		texture = app.getGraphics().newTexture( pixmap, TextureFilter.MipMap, TextureFilter.Linear, TextureWrap.ClampToEdge, TextureWrap.ClampToEdge, true );
	}

	@Override
	public void render(Application app) 
	{
		app.getGraphics().getGL20().glEnable( GL20.GL_TEXTURE_2D );
		texture.bind();
		shader.begin();		
		shader.setUniformMatrix( "u_worldView", matrix );
		shader.setUniformi( "u_texture", 0 );
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

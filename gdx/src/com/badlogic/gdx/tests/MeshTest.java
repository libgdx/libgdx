package com.badlogic.gdx.tests;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.RenderListener;
import com.badlogic.gdx.backends.desktop.JoglApplication;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.mesh.Mesh;
import com.badlogic.gdx.graphics.mesh.VertexAttribute;
import com.badlogic.gdx.graphics.mesh.VertexAttributes.Usage;

public class MeshTest implements RenderListener
{
	Mesh mesh;
	Texture texture;
	
	@Override
	public void dispose(Application app) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void render(Application app) 
	{
		app.getGraphics().getGL10().glEnable( GL10.GL_TEXTURE_2D );
		texture.bind();
		mesh.render( GL10.GL_TRIANGLES );
	}

	@Override
	public void surfaceChanged(Application app, int width, int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void surfaceCreated(Application app) 
	{
		mesh = new Mesh( app.getGraphics(), true, true, false, 3, 0, 
						 new VertexAttribute( Usage.Position, 3, "pos" ),
						 new VertexAttribute( Usage.Color, 3, "col" ),
						 new VertexAttribute( Usage.TextureCoordinates, 2, "texCoord0" ) );
		mesh.setVertices( new float[] { -0.5f, -0.5f, 0, 1, 0, 0, 0, 0,
										 0.5f, -0.5f, 0, 0, 1, 0, 1, 0,
										 0, 0.5f, 0, 0, 0, 1, 0.5f, 1 } );
		
		Pixmap pixmap = app.getGraphics().newPixmap(256, 256, Format.RGBA8888 );
		pixmap.setColor(1, 1, 1, 1 );
		pixmap.fill();
		pixmap.setColor(0, 0, 0, 1 );
		pixmap.drawLine(0, 0, 256, 256);
		pixmap.drawLine(256, 0, 0, 256);
		texture = app.getGraphics().newTexture( pixmap, TextureFilter.MipMap, TextureFilter.Linear, TextureWrap.ClampToEdge, TextureWrap.ClampToEdge, true );
	}

	public static void main( String[] argv )
	{
		JoglApplication app = new JoglApplication( "Mesh Test", 480, 320, false );
		app.getGraphics().setRenderListener( new MeshTest() );
	}
}

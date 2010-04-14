package com.badlogic.gdx.tests;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.RenderListener;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;

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
		mesh.render( GL10.GL_TRIANGLES, 0, 3 );
	}

	@Override
	public void surfaceChanged(Application app, int width, int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void surfaceCreated(Application app) 
	{		
		if( mesh == null )
		{
			mesh = new Mesh( app.getGraphics(), true, true, false, 3, 3, 
					 new VertexAttribute( Usage.Position, 3, "a_position" ), 
					 new VertexAttribute( Usage.Color, 4, "a_color" ),
					 new VertexAttribute( Usage.TextureCoordinates, 2, "a_texCoords" ) );		
			
			mesh.setVertices( new float[] { -0.5f, -0.5f, 0, 1, 0, 0, 1, 0, 0,
											 0.5f, -0.5f, 0, 0, 1, 0, 1, 1, 0,
											 0, 0.5f, 0, 0, 0, 1, 1, 0.5f, 1 } );	
			mesh.setIndices( new short[] { 0, 1, 2 } );
	
			Pixmap pixmap = app.getGraphics().newPixmap(256, 256, Format.RGBA8888 );
			pixmap.setColor(1, 1, 1, 1 );
			pixmap.fill();
			pixmap.setColor(0, 0, 0, 1 );
			pixmap.drawLine(0, 0, 256, 256);
			pixmap.drawLine(256, 0, 0, 256);
			texture = app.getGraphics().newTexture( pixmap, TextureFilter.MipMap, TextureFilter.Linear, TextureWrap.ClampToEdge, TextureWrap.ClampToEdge, true );
		}
	}

}

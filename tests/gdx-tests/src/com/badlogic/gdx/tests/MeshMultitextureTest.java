package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.RenderListener;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;

public class MeshMultitextureTest implements RenderListener
{
	Texture tex1;
	Texture tex2;
	Mesh mesh;
	
	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void render() 
	{	
		GL10 gl = Gdx.graphics.getGL10();
		gl.glEnable( GL10.GL_TEXTURE_2D );
		gl.glActiveTexture( GL10.GL_TEXTURE0 );
		tex1.bind();
		gl.glActiveTexture( GL10.GL_TEXTURE1 );
		tex2.bind();
		mesh.render( GL10.GL_TRIANGLES );
	}

	@Override
	public void surfaceChanged(int width, int height) 
	{	
		
	}

	@Override
	public void surfaceCreated() 
	{	
		Pixmap pixmap = Gdx.graphics.newPixmap(256, 256, Format.RGBA8888 );
		pixmap.setColor(1, 1, 1, 1 );
		pixmap.fill();
		pixmap.setColor(0, 0, 0, 1 );
		pixmap.drawLine(0, 0, 256, 256);
		pixmap.drawLine(256, 0, 0, 256);		
		tex1 = Gdx.graphics.newUnmanagedTexture( pixmap, TextureFilter.Linear, TextureFilter.Linear, TextureWrap.ClampToEdge, TextureWrap.ClampToEdge );
		
		pixmap = Gdx.graphics.newPixmap( 256, 256, Format.RGBA8888 );
		pixmap.setColor( 1, 1, 1, 1 );
		pixmap.fill();
		pixmap.setColor( 0, 0, 0, 1 );
		pixmap.drawLine( 128, 0, 128, 256 );
		tex2 = Gdx.graphics.newUnmanagedTexture( pixmap, TextureFilter.Linear, TextureFilter.Linear, TextureWrap.ClampToEdge, TextureWrap.ClampToEdge);
		
		mesh = new Mesh( true, false, 3, 0, new VertexAttribute( VertexAttributes.Usage.Color, 4, "a_Color" ),
											new VertexAttribute( VertexAttributes.Usage.TextureCoordinates, 2, "a_texCoords1" ),
											new VertexAttribute( VertexAttributes.Usage.TextureCoordinates, 2, "a_texCoords2" ),
											new VertexAttribute( VertexAttributes.Usage.Position, 3, "a_Position" ) );
		
		mesh.setVertices( new float[] {
				 1, 0, 0, 1,
				  0, 1,
				  0, 1,
				  -0.5f, -0.5f, 0,
				  
				  0, 1, 0, 1,
				  1, 1,
				  1, 1,
				  0.5f, -0.5f, 0,
				   
				  0, 0, 1, 1,
				  0.5f, 0,
				  0.5f, 0,
				  0, 0.5f, 0,
		});
	}

}

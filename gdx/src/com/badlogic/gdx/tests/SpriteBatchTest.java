package com.badlogic.gdx.tests;

import javax.media.opengl.GL;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.RenderListener;
import com.badlogic.gdx.backends.desktop.JoglApplication;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Font;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.SpriteBatch;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.Font.FontStyle;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;

public class SpriteBatchTest implements RenderListener 
{
	int SPRITES = 1;
	
	long startTime = System.nanoTime();
	int frames = 0;
		
	Texture texture;
	Texture texture2;
	Font font;
	SpriteBatch spriteBatch;
	int coords[] = new int[SPRITES*2];
	int coords2[] = new int[SPRITES*2];
	
	Mesh mesh;
	float vertices[] = new float[SPRITES * 6 * ( 2 + 2 + 4 )];
	
	@Override
	public void dispose(Application app) 
	{	
		
	}

	@Override
	public void render(Application app) 
	{	
		GL10 gl = app.getGraphics().getGL10();
		gl.glClear( GL.GL_COLOR_BUFFER_BIT );
		
		spriteBatch.begin();				
			renderSpriteBatch();
		spriteBatch.end();			
		
		if( System.nanoTime() - startTime > 1000000000 )
		{
			app.log( "SpriteBatch", "fps: " + frames );
			frames = 0;
			startTime = System.nanoTime();
		}
		frames++;
	}
	
	private void renderMesh( )
	{
		int srcX = 0;
		int srcY = 0;
		int srcWidth = 16;
		int srcHeight = 16;
		float invTexWidth = 1.0f / texture.getWidth();
		float invTexHeight = 1.0f / texture.getHeight();	
		int idx = 0;
		Color tint = Color.WHITE;
				
		for( int i = 0; i < coords.length; i+= 2 )
		{
			int x = coords[i];
			int y = coords[i+1];
			float u = srcX * invTexWidth;
			float v = srcY * invTexHeight;
			float u2 = (srcX + srcWidth) * invTexWidth;
			float v2 = (srcY + srcHeight) * invTexHeight;
			float fx = (float)x;
			float fy = (float)y;
			float fx2 = (float)(x + srcWidth);
			float fy2 = (float)(y - srcHeight);		
			
			vertices[idx++] = fx;
			vertices[idx++] = fy;
			vertices[idx++] = tint.r; vertices[idx++] = tint.g; vertices[idx++] = tint.b; vertices[idx++] = tint.a;
			vertices[idx++] = u; vertices[idx++] = v; 
			
			vertices[idx++] = fx;
			vertices[idx++] = fy2;
			vertices[idx++] = tint.r; vertices[idx++] = tint.g; vertices[idx++] = tint.b; vertices[idx++] = tint.a;
			vertices[idx++] = u; vertices[idx++] = v2;
			
			vertices[idx++] = fx2;
			vertices[idx++] = fy2;
			vertices[idx++] = tint.r; vertices[idx++] = tint.g; vertices[idx++] = tint.b; vertices[idx++] = tint.a;
			vertices[idx++] = u2; vertices[idx++] = v2;
			
			vertices[idx++] = fx2;
			vertices[idx++] = fy2;
			vertices[idx++] = tint.r; vertices[idx++] = tint.g; vertices[idx++] = tint.b; vertices[idx++] = tint.a;
			vertices[idx++] = u2; vertices[idx++] = v2;
			
			vertices[idx++] = fx2;
			vertices[idx++] = fy;
			vertices[idx++] = tint.r; vertices[idx++] = tint.g; vertices[idx++] = tint.b; vertices[idx++] = tint.a;
			vertices[idx++] = u2; vertices[idx++] = v;
			
			vertices[idx++] = fx;
			vertices[idx++] = fy;
			vertices[idx++] = tint.r; vertices[idx++] = tint.g; vertices[idx++] = tint.b; vertices[idx++] = tint.a;
			vertices[idx++] = u; vertices[idx++] = v; 
		}		
		
		texture.bind();
		mesh.setVertices( vertices );
		mesh.render(GL10.GL_TRIANGLES);
		
		idx = 0;
		for( int i = 0; i < coords2.length; i+= 2 )
		{
			int x = coords2[i];
			int y = coords2[i+1];
			float u = srcX * invTexWidth;
			float v = srcY * invTexHeight;
			float u2 = (srcX + srcWidth) * invTexWidth;
			float v2 = (srcY + srcHeight) * invTexHeight;
			float fx = (float)x;
			float fy = (float)y;
			float fx2 = (float)(x + srcWidth);
			float fy2 = (float)(y - srcHeight);		
			
			vertices[idx++] = fx;
			vertices[idx++] = fy;
			vertices[idx++] = tint.r; vertices[idx++] = tint.g; vertices[idx++] = tint.b; vertices[idx++] = tint.a;
			vertices[idx++] = u; vertices[idx++] = v; 
			
			vertices[idx++] = fx;
			vertices[idx++] = fy2;
			vertices[idx++] = tint.r; vertices[idx++] = tint.g; vertices[idx++] = tint.b; vertices[idx++] = tint.a;
			vertices[idx++] = u; vertices[idx++] = v2;
			
			vertices[idx++] = fx2;
			vertices[idx++] = fy2;
			vertices[idx++] = tint.r; vertices[idx++] = tint.g; vertices[idx++] = tint.b; vertices[idx++] = tint.a;
			vertices[idx++] = u2; vertices[idx++] = v2;
			
			vertices[idx++] = fx2;
			vertices[idx++] = fy2;
			vertices[idx++] = tint.r; vertices[idx++] = tint.g; vertices[idx++] = tint.b; vertices[idx++] = tint.a;
			vertices[idx++] = u2; vertices[idx++] = v2;
			
			vertices[idx++] = fx2;
			vertices[idx++] = fy;
			vertices[idx++] = tint.r; vertices[idx++] = tint.g; vertices[idx++] = tint.b; vertices[idx++] = tint.a;
			vertices[idx++] = u2; vertices[idx++] = v;
			
			vertices[idx++] = fx;
			vertices[idx++] = fy;
			vertices[idx++] = tint.r; vertices[idx++] = tint.g; vertices[idx++] = tint.b; vertices[idx++] = tint.a;
			vertices[idx++] = u; vertices[idx++] = v; 
		}		
		
		texture2.bind();
		mesh.setVertices( vertices );
		mesh.render(GL10.GL_TRIANGLES);
	}
	
	private void renderSpriteBatch( )
	{
		int len = coords.length;
		
		for( int i = 0; i < len; i+=2 )		
			spriteBatch.draw( texture, coords[i], coords[i+1], 0, 0, 16, 16, Color.WHITE );
		
		for( int i = 0; i < coords2.length; i+=2 )		
			spriteBatch.draw( texture2, coords2[i], coords2[i+1], 0, 0, 16, 16, Color.WHITE );
				
		spriteBatch.drawText( font, "and another this is a test", 100, 100, Color.WHITE );		
		spriteBatch.drawText( font, "and another this is a test", 200, 100, Color.WHITE );
		spriteBatch.drawText( font, "all hail and another this is a test", 200, 200, Color.WHITE );
	}
	
	private void renderSpriteBatchBulk( )
	{				
		spriteBatch.draw( texture, coords, 0, 0, 16, 16, Color.WHITE );					
		spriteBatch.draw( texture2, coords2, 0, 0, 16, 16, Color.WHITE );		
		spriteBatch.drawText( font, "and another this is a test", 100, 100, Color.WHITE );		
		spriteBatch.drawText( font, "and another this is a test", 200, 100, Color.WHITE );
		spriteBatch.drawText( font, "all hail and another this is a test", 200, 200, Color.WHITE );
	}

	@Override
	public void surfaceChanged(Application app, int width, int height) 
	{	
		
	}

	@Override
	public void surfaceCreated(Application app) 
	{					
		spriteBatch = new SpriteBatch( app.getGraphics() );
		
		Pixmap pixmap = app.getGraphics().newPixmap(32, 32, Format.RGBA8888 );
		pixmap.setColor(1, 0, 0, 1 );
		pixmap.fill();
		pixmap.setColor(0, 1, 0, 1 );
		pixmap.drawLine(0, 0, 32, 32);
		pixmap.drawLine(32, 0, 0, 32);
		pixmap.setColor(0, 0, 1, 1 );
		pixmap.drawLine(0, 0, 31, 0 );
		pixmap.drawLine(0, 0, 0, 31 );
		pixmap.drawLine(31, 0, 31, 31 );
		pixmap.drawLine(0, 31, 31, 31 );		
		texture = app.getGraphics().newTexture( pixmap, TextureFilter.Linear, TextureFilter.Linear, TextureWrap.ClampToEdge, TextureWrap.ClampToEdge, true );
		
		pixmap = app.getGraphics().newPixmap(32, 32, Format.RGBA8888 );
		pixmap.setColor(1, 1, 0, 1 );
		pixmap.fill();
		texture2 = app.getGraphics().newTexture( pixmap, TextureFilter.Nearest, TextureFilter.Nearest, TextureWrap.ClampToEdge, TextureWrap.ClampToEdge, true );
		
		font = app.getGraphics().newFont( "Arial", 44, FontStyle.Plain, true );
		
		for( int i = 0; i < coords.length; i+=2 )
		{
			coords[i] = (int)(Math.random() * app.getGraphics().getWidth());
			coords[i+1] = (int)(Math.random() * app.getGraphics().getHeight());
			coords2[i] = (int)(Math.random() * app.getGraphics().getWidth());
			coords2[i+1] = (int)(Math.random() * app.getGraphics().getHeight());
		}
		
		mesh = new Mesh( app.getGraphics(), true, false, false, 6 * SPRITES, 0, new VertexAttribute( Usage.Position, 2, "a_position" ),
																				new VertexAttribute( Usage.Color, 4, "a_color" ),
																				new VertexAttribute( Usage.TextureCoordinates, 2, "a_texCoords" ) );
	}

	public static void main( String[] argv )
	{
		JoglApplication app = new JoglApplication( "SpriteBatch Test", 480, 320, false );
		app.getGraphics().setRenderListener( new SpriteBatchTest() );
	}
}

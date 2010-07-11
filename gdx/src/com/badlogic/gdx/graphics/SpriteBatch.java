/*******************************************************************************
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.badlogic.gdx.graphics;

import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.graphics.Font.Glyph;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.math.Matrix;

/**
 * A SpriteBatch is used to draw 2D rectangles that reference a 
 * texture. The class will batch the drawing commands and optimize
 * them for processing by the GPU.
 * 
 * To draw something with a SpriteBatch one has to first call the
 * {@link SpriteBatch.begin()} method which will setup apropriate
 * render states. When you are done with drawing you have to call
 * {@link SpriteBatch.end()} which will actually draw the things you
 * specified.
 * 
 * All drawing commands of the SpriteBatch operate in screen coordinates.
 * The screen coordinate system has an x-axis pointing to the right, an
 * y-axis pointing upwards and the origin is in the lower left corner of
 * the screen.
 * 
 * A sprite rendered via this batch has an origin relative to it's
 * top left corner and a position in screen coordinates for that origin.
 * 
 * A sprite has a width and height in screen coordinates
 * 
 * A sprite can be scaled on the x and y axis.
 * 
 * A sprite can be rotated around the origin by some angle.
 * 
 * A sprite references a portion of a texture where the portion is specified in texels.
 * 
 * @author mzechner
 *
 */
public  class SpriteBatch
{	
	private static final int MAX_VERTICES = 6 * 5000;
	
	/** the mesh used to transfer the data to the GPU **/
	private final Mesh mesh;
	
	/** the graphics instance **/
	protected final Graphics graphics;
	
	/** the transform to be applied to all sprites **/
	protected final Matrix transform = new Matrix();
	
	/** the view matrix holding the orthogonal projection **/
	protected final Matrix viewMatrix = new Matrix();
	
	/** the vertex storage **/
	protected final float[] vertices = new float[MAX_VERTICES * (2 + 4 + 2)];
	
	/** last texture **/
	protected Texture lastTexture = null;
	
	/** current index into vertices **/
	protected int idx = 0;
	
	/** drawing flag **/
	protected boolean drawing = false;
	
	/** inverse texture width and height **/
	protected float invTexWidth = 0;
	protected float invTexHeight = 0;
	
	/** whether to use the blend mode for text or for sprites **/
	protected boolean useTextBlend = false;
	
	/** blend function src & target **/
	private int blendSrcFunc = GL11.GL_SRC_ALPHA;
	private int blendDstFunc = GL11.GL_ONE_MINUS_SRC_ALPHA;
	
	/** the shader for opengl 2.0 **/
	protected ShaderProgram shader;
	
	/** number of render calls **/
	public int renderCalls = 0;

	/** whether blending is enabled or not **/
	protected boolean blendingDisabled;
	
	/**
	 * Consturctor, sets the {@link Graphics} instance
	 * to use.
	 * 
	 * @param graphics the Graphics instance
	 */
	public SpriteBatch( Graphics graphics )
	{
		this.graphics = graphics;		
		this.mesh = new Mesh( graphics, true, false, false, MAX_VERTICES, 0, 
							  new VertexAttribute( Usage.Position, 2, "a_position" ),
							  new VertexAttribute( Usage.Color, 4, "a_color" ),
							  new VertexAttribute( Usage.TextureCoordinates, 2, "a_texCoords" ) );
		viewMatrix.setToOrtho2D( 0, 0, graphics.getWidth(), graphics.getHeight() );
		
		if( graphics.isGL20Available() )
			createShader( );
	}
	
	private void createShader( )
	{
		String vertexShader = "attribute vec4 a_position; \n" +
							  "attribute vec4 a_color; \n" + 
							  "attribute vec2 a_texCoords; \n" +
							  "uniform mat4 u_projectionViewMatrix; \n" +
							  "varying vec4 v_color; \n" +
							  "varying vec2 v_texCoords; \n" +
							  " \n" + 
							  "void main()                  \n" +
							  "{                            \n" +
							  "   v_color = a_color; \n" +
							  "   v_texCoords = a_texCoords; \n" +
							  "   gl_Position =  u_projectionViewMatrix * a_position;  \n" +
							  "}  \n";
		String fragmentShader = "precision mediump float;\n" +
								"varying vec4 v_color;\n" +
								"varying vec2 v_texCoords;\n" +								
								"uniform sampler2D u_texture;\n" +
								"void main()                                  \n" +
							    "{                                            \n" +							    							   
							    "  gl_FragColor = v_color * texture2D(u_texture, v_texCoords);\n" +
							    "}"; 
		
		shader = new ShaderProgram( graphics.getGL20(), vertexShader, fragmentShader, true);
		if( shader.isCompiled() == false )
			throw new IllegalArgumentException( "couldn't compile shader: " + shader.getLog()  );
	}
	
	/**
	 * Sets up the SpriteBatch for drawing. This will disable
	 * depth buffer testing and writting, culling and lighting.
	 * It enables blending and alpha testing. It sets the projection
	 * matrix to an orthographic matrix and the modelview and texture
	 * matrix to identity. If you have more texture units enabled than
	 * the first one you have to disable them before calling this.  
	 */
	public void begin( )
	{		
		transform.idt();		
		begin( transform );
	}
	
	/**
	 * Sets up the SpriteBatch for drawing. This will disable
	 * depth buffer testing and writting, culling and lighting.
	 * It enables blending and alpha testing. If you have more texture units enabled than
	 * the first one you have to disable them before calling this. Applies
	 * the given transformation {@link Matrix} to all subsequently specified sprites. Loads
	 * an orthographic projection matrix with the full screen as the viewport.
	 * 
	 * @param transform the transformation matrix.
	 */
	public void begin( Matrix transform )
	{		
		viewMatrix.setToOrtho2D( 0, 0, graphics.getWidth(), graphics.getHeight() );
		begin( viewMatrix, transform );
	}
	
	/**
	 * Sets up the SpriteBatch for drawing. This will disable
	 * depth buffer testing and writting, culling and lighting.
	 * It enables blending and alpha testing. If you have more texture units enabled than
	 * the first one you have to disable them before calling this. Applies
	 * the given transformation {@link Matrix} to all subsequently specified sprites. Uses
	 * the provided projection matrix.
	 * 
	 * @param projection the projection matrix;
	 * @param transform the transformation matrix.
	 */
	public void begin( Matrix projection, Matrix transform )
	{
		renderCalls = 0;
		if( graphics.isGL20Available() == false )
		{										
			GL10 gl = graphics.getGL10();
			gl.glViewport( 0, 0, graphics.getWidth(), graphics.getHeight() );
			gl.glDisable( GL10.GL_LIGHTING );
			gl.glDisable( GL10.GL_DEPTH_TEST );
			gl.glDisable( GL10.GL_CULL_FACE );
			gl.glDepthMask ( false );
			
			gl.glEnable( GL10.GL_TEXTURE_2D );
			//gl.glActiveTexture( GL10.GL_TEXTURE0 );
			
			if( !blendingDisabled )
			{
				gl.glEnable( GL10.GL_BLEND );
				gl.glBlendFunc( GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA );							
			}			
			else
			{
				gl.glDisable( GL10.GL_BLEND );				
			}
			
			gl.glMatrixMode( GL10.GL_PROJECTION );
			gl.glLoadMatrixf( projection.val, 0 );
			gl.glMatrixMode( GL10.GL_MODELVIEW );
			gl.glLoadMatrixf( transform.val, 0 );
		}		
		else
		{
			viewMatrix.set(projection).mul(transform);

			GL20 gl = graphics.getGL20();
			gl.glViewport( 0, 0, graphics.getWidth(), graphics.getHeight() );
			gl.glDisable( GL20.GL_DEPTH_TEST );
			gl.glDisable( GL20.GL_CULL_FACE );
			gl.glDepthMask ( false );

			gl.glEnable( GL20.GL_TEXTURE_2D );
			//gl.glActiveTexture( GL20.GL_TEXTURE0 );

			if( !blendingDisabled )
			{
				gl.glEnable( GL20.GL_BLEND );
				gl.glBlendFunc( GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA );
			}
			else
			{
				gl.glDisable( GL20.GL_BLEND );
			}

			shader.begin();
			shader.setUniformMatrix( "u_projectionViewMatrix", viewMatrix );
			shader.setUniformi( "u_texture", 0 );
		}
		
		idx = 0;
		lastTexture = null;
		drawing = true;
	}
	
	
	/**
	 * Finishes off rendering of the last batch of sprites
	 */
	public void end( )
	{		
		if( idx > 0 )
			renderMesh();
		lastTexture = null;
		idx = 0;
		drawing = false;
		
		if( graphics.isGL20Available() == false )
		{
			GL10 gl = graphics.getGL10();
			gl.glDepthMask ( true );			
			gl.glDisable( GL10.GL_BLEND );			
			gl.glDisable( GL10.GL_TEXTURE_2D );
		}
		else
		{
			shader.end();
			GL20 gl = graphics.getGL20();
			gl.glDepthMask ( true );			
			gl.glDisable( GL20.GL_BLEND );
			gl.glDisable( GL20.GL_TEXTURE_2D );						
		}
	}	
	
	/**
	 * Draws a rectangle with the top left corner at x,y having
	 * the given width and height in pixels. The rectangle is offset by originX, originY relative to the
	 * origin. Scale specifies the scaling factor by which the rectangle should be scaled around originX,originY. 
	 * Rotation specifies the angle of counter clockwise rotation of the rectangle around originX, originY.
	 * The portion of the {@link Texture}
	 * given by srcX, srcY and srcWidth, srcHeight is used. These coordinates and
	 * sizes are given in texels. The rectangle will have the given tint {@link Color}. FlipX
	 * and flipY specify whether the texture portion should be fliped horizontally or vertically.
	 *  
	 * @param texture the Texture
	 * @param x the x-coordinate in screen space
	 * @param y the y-coordinate in screen space
	 * @param originX the x-coordinate of the scaling and rotation origin relative to the screen space coordinates
	 * @param originY the y-coordinate of the scaling and rotation origin relative to the screen space coordinates
	 * @param width the width in pixels
	 * @param height the height in pixels
	 * @param scale the scale of the rectangle around originX/originY
	 * @param rotation the angle of counter clockwise rotation of the rectangle around originX/originY
	 * @param srcX the x-coordinate in texel space
	 * @param srcY the y-coordinate in texel space
	 * @param srcWidth the source with in texels
	 * @param srcHeight the source height in texels
	 * @param tint the tint Color
	 * @param flipX whether to flip the sprite horizontally
	 * @param flipY whether to flip the sprite vertically
	 */
	public void draw(Texture texture, float x, float y, float originX, float originY, float width, float height, float scale, float rotation, int srcX, int srcY, int srcWidth, int srcHeight, Color tint, boolean flipX, boolean flipY )
	{
		if( !drawing )
			throw new IllegalStateException( "you have to call SpriteBatch.begin() first" );
		
		if( texture != lastTexture )
		{		
			renderMesh( );
			lastTexture = texture;
			invTexWidth = 1.0f / texture.getWidth();
			invTexHeight = 1.0f / texture.getHeight();
		}
		
		useTextBlend = false;			
		
		// top left and bottom right corner points relative to origin
		final float worldOriginX = x + originX;
		final float worldOriginY = y + originY;
		float fx = x - worldOriginX;
		float fy = y - worldOriginY;
		float fx2 = x + width - worldOriginX;
		float fy2 = y - height - worldOriginY;
		
		// scale
		fx *= scale;
		fy *= scale;
		fx2 *= scale;
		fy2 *= scale;
		
		// construct corner points, start from top left and go counter clockwise
		final float p1x = fx;
		final float p1y = fy;
		final float p2x = fx;
		final float p2y = fy2;
		final float p3x = fx2;
		final float p3y = fy2;
		final float p4x = fx2;
		final float p4y = fy;
		
		// rotate
		final float cos = (float)Math.cos( Math.toRadians( rotation ) );
		final float sin = (float)Math.sin( Math.toRadians( rotation ) );
		
		float x1 = cos * p1x - sin * p1y;
		float y1 = sin * p1x + cos * p1y;
		
		float x2 = cos * p2x - sin * p2y;
		float y2 = sin * p2x + cos * p2y;
		
		float x3 = cos * p3x - sin * p3y;
		float y3 = sin * p3x + cos * p3y;
		
		float x4 = cos * p4x - sin * p4y;
		float y4 = sin * p4x + cos * p4y;			
		
		// translate to worldspace
		final float worldX = x + originX;
		final float worldY = y + originY;
		
		x1 += worldX; y1 += worldY;
		x2 += worldX; y2 += worldY;
		x3 += worldX; y3 += worldY;
		x4 += worldX; y4 += worldY;
		
		float u = srcX * invTexWidth;
		float v = srcY * invTexHeight;
		float u2 = (srcX + srcWidth) * invTexWidth;
		float v2 = (srcY + srcHeight) * invTexHeight;
		
		if( flipX )
		{
			float tmp = u;
			u = u2;
			u2 = tmp;
		}
		
		if( flipY )
		{
			float tmp = v;
			v = v2;
			v2 = tmp;
		}			
		
		vertices[idx++] = x1;
		vertices[idx++] = y1;
		vertices[idx++] = tint.r; vertices[idx++] = tint.g; vertices[idx++] = tint.b; vertices[idx++] = tint.a;
		vertices[idx++] = u; vertices[idx++] = v; 
		
		vertices[idx++] = x2;
		vertices[idx++] = y2;
		vertices[idx++] = tint.r; vertices[idx++] = tint.g; vertices[idx++] = tint.b; vertices[idx++] = tint.a;
		vertices[idx++] = u; vertices[idx++] = v2;
		
		vertices[idx++] = x3;
		vertices[idx++] = y3;
		vertices[idx++] = tint.r; vertices[idx++] = tint.g; vertices[idx++] = tint.b; vertices[idx++] = tint.a;
		vertices[idx++] = u2; vertices[idx++] = v2;
		
		vertices[idx++] = x3;
		vertices[idx++] = y3;
		vertices[idx++] = tint.r; vertices[idx++] = tint.g; vertices[idx++] = tint.b; vertices[idx++] = tint.a;
		vertices[idx++] = u2; vertices[idx++] = v2;
		
		vertices[idx++] = x4;
		vertices[idx++] = y4;
		vertices[idx++] = tint.r; vertices[idx++] = tint.g; vertices[idx++] = tint.b; vertices[idx++] = tint.a;
		vertices[idx++] = u2; vertices[idx++] = v;
		
		vertices[idx++] = x1;
		vertices[idx++] = y1;
		vertices[idx++] = tint.r; vertices[idx++] = tint.g; vertices[idx++] = tint.b; vertices[idx++] = tint.a;
		vertices[idx++] = u; vertices[idx++] = v;
		
		if( idx == vertices.length )
			renderMesh();
	}
	
	/**
	 * Draws a rectangle with the top left corner at x,y having
	 * the given width and height in pixels. The portion of the {@link Texture}
	 * given by srcX, srcY and srcWidth, srcHeight is used. These coordinates and
	 * sizes are given in texels. The rectangle will have the given tint {@link Color}. FlipX
	 * and flipY specify whether the texture portion should be fliped horizontally or vertically.
	 *  
	 * @param texture the Texture
	 * @param x the x-coordinate in screen space
	 * @param y the y-coordinate in screen space
	 * @param width the width in pixels
	 * @param height the height in pixels
	 * @param srcX the x-coordinate in texel space
	 * @param srcY the y-coordinate in texel space
	 * @param srcWidth the source with in texels
	 * @param srcHeight the source height in texels
	 * @param tint the tint Color
	 * @param flipX whether to flip the sprite horizontally
	 * @param flipY whether to flip the sprite vertically
	 */
	public void draw(Texture texture, float x, float y, float width, float height, int srcX, int srcY, int srcWidth, int srcHeight, Color tint, boolean flipX, boolean flipY ) 
	{
		if( !drawing )
			throw new IllegalStateException( "you have to call SpriteBatch.begin() first" );
		
		if( texture != lastTexture )
		{		
			renderMesh( );
			lastTexture = texture;
			invTexWidth = 1.0f / texture.getWidth();
			invTexHeight = 1.0f / texture.getHeight();
		}
		
		useTextBlend = false;
		
		float u = srcX * invTexWidth;
		float v = srcY * invTexHeight;
		float u2 = (srcX + srcWidth) * invTexWidth;
		float v2 = (srcY + srcHeight) * invTexHeight;
        final float fx2 = x + width;
		final float fy2 = y - height;
		
		if( flipX )
		{
			float tmp = u;
			u = u2;
			u2 = tmp;
		}
		
		if( flipY )
		{
			float tmp = v;
			v = v2;
			v2 = tmp;
		}			
		
		vertices[idx++] = x;
		vertices[idx++] = y;
		vertices[idx++] = tint.r; vertices[idx++] = tint.g; vertices[idx++] = tint.b; vertices[idx++] = tint.a;
		vertices[idx++] = u; vertices[idx++] = v; 
		
		vertices[idx++] = x;
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
		vertices[idx++] = y;
		vertices[idx++] = tint.r; vertices[idx++] = tint.g; vertices[idx++] = tint.b; vertices[idx++] = tint.a;
		vertices[idx++] = u2; vertices[idx++] = v;
		
		vertices[idx++] = x;
		vertices[idx++] = y;
		vertices[idx++] = tint.r; vertices[idx++] = tint.g; vertices[idx++] = tint.b; vertices[idx++] = tint.a;
		vertices[idx++] = u; vertices[idx++] = v;
		
		if( idx == vertices.length )
			renderMesh();
	}
	
	/**
	 * Draws a rectangle with the top left corner at x,y having
	 * the given width and height in pixels. The portion of the {@link Texture}
	 * given by srcX, srcY and srcWidth, srcHeight are used. These coordinates and
	 * sizes are given in texels. The rectangle will have the given tint {@link Color}.
	 *  
	 * @param texture the Texture
	 * @param x the x-coordinate in screen space
	 * @param y the y-coordinate in screen space
	 * @param srcX the x-coordinate in texel space
	 * @param srcY the y-coordinate in texel space
	 * @param srcWidth the source with in texels
	 * @param srcHeight the source height in texels
	 * @param tint the tint Color
	 */
	public void draw( Texture texture, float x, float y, int srcX, int srcY, int srcWidth, int srcHeight, Color tint )
	{		
		if( !drawing )
			throw new IllegalStateException( "you have to call SpriteBatch.begin() first" );
		
		if( texture != lastTexture )
		{		
			renderMesh( );
			lastTexture = texture;
			invTexWidth = 1.0f / texture.getWidth();
			invTexHeight = 1.0f / texture.getHeight();
		}
		
		useTextBlend = false;
		
		final float u = srcX * invTexWidth;
		final float v = srcY * invTexHeight;
		final float u2 = (srcX + srcWidth) * invTexWidth;
		final float v2 = (srcY + srcHeight) * invTexHeight;
        final float fx2 = x + srcWidth;
		final float fy2 = y - srcHeight;
		
		vertices[idx++] = x;
		vertices[idx++] = y;
		vertices[idx++] = tint.r; vertices[idx++] = tint.g; vertices[idx++] = tint.b; vertices[idx++] = tint.a;
		vertices[idx++] = u; vertices[idx++] = v; 
		
		vertices[idx++] = x;
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
		vertices[idx++] = y;
		vertices[idx++] = tint.r; vertices[idx++] = tint.g; vertices[idx++] = tint.b; vertices[idx++] = tint.a;
		vertices[idx++] = u2; vertices[idx++] = v;
		
		vertices[idx++] = x;
		vertices[idx++] = y;
		vertices[idx++] = tint.r; vertices[idx++] = tint.g; vertices[idx++] = tint.b; vertices[idx++] = tint.a;
		vertices[idx++] = u; vertices[idx++] = v; 
		
		if( idx == vertices.length )
			renderMesh();
	}		
	
	protected void renderMesh( )
	{
		if( idx == 0 )
			return;
			
		renderCalls++;
		
		lastTexture.bind();		
		mesh.setVertices(vertices, 0, idx);	
		if( graphics.isGL20Available() )
		{
			if( useTextBlend )
			{
				graphics.getGL20().glBlendFunc( GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA );
				if( blendingDisabled )
					graphics.getGL20().glEnable( GL20.GL_BLEND );
			}
			else
				graphics.getGL20().glBlendFunc( blendSrcFunc, blendDstFunc );
			mesh.render( shader, GL10.GL_TRIANGLES );
			if( useTextBlend && blendingDisabled )
			{				
				graphics.getGL20().glDisable( GL20.GL_BLEND );
			}
		}
		else
		{
			if( useTextBlend )
			{
				graphics.getGL10().glBlendFunc( GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA );
				if( blendingDisabled )
					graphics.getGL10().glEnable( GL20.GL_BLEND );
			}
			else
				graphics.getGL10().glBlendFunc( blendSrcFunc, blendDstFunc );
			mesh.render( GL10.GL_TRIANGLES );
			if( useTextBlend && blendingDisabled )
			{				
				graphics.getGL10().glDisable( GL20.GL_BLEND );
			}
		}
		idx = 0;
	}	
	
	/**
	 * Draws a text with its bounding box's top left corner at x,y using
	 * the given {@link Font}. This will ignore any newlines, tabs and 
	 * other special characters.
	 * 
	 * @param font the Font
	 * @param text the text
	 * @param x the x-coordinate of the strings bounding box's upper left corner
	 * @param y the y-coordinate of the strings bounding box's upper left corner
	 * @param tint the tint of the text
	 */
	public void drawText( Font font, String text, float x, float y, Color tint )
	{
		if( !drawing )
			throw new IllegalStateException( "you have to call SpriteBatch.begin() first" );
		
		if( font.getTexture() != lastTexture )
		{		
			renderMesh( );
			lastTexture = font.getTexture();			
		}						
				
		useTextBlend = true;			
		
		int len = text.length();		
		for( int i = 0; i < len; i++ )
		{
			char c = text.charAt(i);
			Glyph g = font.getGlyph(c);
			
			final float fx = x;
            final float fx2 = x + g.width;
			final float fy2 = y - g.height;
			final float u = g.u;
			final float v = g.v;
			final float u2 = g.u + g.uWidth;
			final float v2 = g.v + g.vHeight;
			
			vertices[idx++] = fx;
			vertices[idx++] = y;
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
			vertices[idx++] = y;
			vertices[idx++] = tint.r; vertices[idx++] = tint.g; vertices[idx++] = tint.b; vertices[idx++] = tint.a;
			vertices[idx++] = u2; vertices[idx++] = v;
			
			vertices[idx++] = fx;
			vertices[idx++] = y;
			vertices[idx++] = tint.r; vertices[idx++] = tint.g; vertices[idx++] = tint.b; vertices[idx++] = tint.a;
			vertices[idx++] = u; vertices[idx++] = v; 
			
			x += g.advance;
			
			if( idx == vertices.length )
				renderMesh();
		}
	}		
	
	/**
	 * Disables blending for drawing sprites. Does not disable blending for text rendering
	 */
	public void disableBlending( )
	{
		renderMesh();
		blendingDisabled = true;		
	}
	
	/**
	 * Enables blending for sprites
	 */
	public void enableBlending( )
	{
		renderMesh();
		blendingDisabled = false;
	}
	
	/**
	 * Sets the blending function to be used when rendering sprites. This will
	 * have no effect on the blend function used for text rendering!
	 * 
	 * @param srcFunc the source function, e.g. GL11.GL_SRC_ALPHA
	 * @param dstFunc the destination function, e.g. GL11.GL_ONE_MINUS_SRC_ALPHA
	 */
	public void setBlendFunction( int srcFunc, int dstFunc )
	{
		renderMesh( );
		blendSrcFunc = srcFunc;
		blendDstFunc = dstFunc;
	}
	
	/**
	 * Disposes all resources associated with this SpriteBatch
	 */
	public void dispose( )
	{
		mesh.dispose();
		if( shader != null )
			shader.dispose();
	}
}

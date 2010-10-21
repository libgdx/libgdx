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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.graphics.Font.Glyph;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.MathUtils;

/**
 * <p>
 * A SpriteBatch is used to draw 2D rectangles that reference a 
 * texture (region). The class will batch the drawing commands and optimize
 * them for processing by the GPU.
 * </p>
 * 
 * <p>
 * To draw something with a SpriteBatch one has to first call the
 * {@link SpriteBatch.begin()} method which will setup appropriate
 * render states. When you are done with drawing you have to call
 * {@link SpriteBatch.end()} which will actually draw the things you
 * specified.
 * </p>
 * 
 * <p>
 * All drawing commands of the SpriteBatch operate in screen coordinates.
 * The screen coordinate system has an x-axis pointing to the right, an
 * y-axis pointing upwards and the origin is in the lower left corner of
 * the screen. You can also provide your own transformation and projection
 * matrices if you so wish.
 * </p>
 * 
 * <p>
 * A sprite rendered via this batch has an origin relative to it's
 * top left corner and a position in screen coordinates for that origin.
 * </p>
 * 
 * <p>
 * A sprite has a width and height in screen coordinates
 * </p>
 * 
 * <p>
 * A sprite can be scaled on the x and y axis.
 * </p>
 *
 * <p>
 * A sprite can be rotated around the origin by some angle.
 * </p>
 * 
 * <p>
 * A sprite references a portion of a texture where the portion is specified in texels.
 * </p>
 * 
 * <p>A SpriteBatch can be managed. In case the OpenGL context is lost all OpenGL resources
 * a SpriteBatch uses internally get invalidated. A context is lost when a user switches to
 * another application or receives an incoming call. A managed SpriteBatch will be automatically
 * reloaded after the OpenGL context is restored.
 * </p>
 * 
 * <p>
 * A SpriteBatch is a pretty heavy object so you should only ever have one in your program.
 * </p>
 * 
 * <p>
 * A SpriteBatch works with OpenGL ES 1.x and 2.0. In the case of a 2.0 context it will use
 * its own custom shader to draw all provided sprites. Specifying your own shader does not work
 * (yet).
 * </p>
 * 
 * <p>
 * A SpriteBatch has to be disposed if it is no longer used.
 * </p>
 * 
 * @author mzechner
 *
 */
public  class SpriteBatch
{		
	private static final int VERTEX_SIZE = 2 + 1 + 2;
	private static final int SPRITE_SIZE = 6 * VERTEX_SIZE;
	
	/** the mesh used to transfer the data to the GPU **/
	private final Mesh mesh;
	
	/** the transform to be applied to all sprites **/
	protected final Matrix4 transformMatrix = new Matrix4();
	
	/** the view matrix holding the orthogonal projection **/
	protected final Matrix4 projectionMatrix = new Matrix4();
	
	/** the combined transform and view matrix **/
	protected final Matrix4 combinedMatrix = new Matrix4( );
	
	/** the vertex storage **/
	protected final float[] vertices;
	
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
	protected boolean blendingDisabled=false;
	
	/**
	 * Consturctor, sets the {@link Graphics} instance
	 * to use.
	 * 
	 * @param graphics the Graphics instance
	 */
	public SpriteBatch( )
	{		
		this( 1000 );
	}

	/**
	 * Consturctor, sets the {@link Graphics} instance
	 * to use.
	 * 
	 * @param graphics the Graphics instance
	 */
	public SpriteBatch( int size )
	{
		this.mesh = new Mesh( false, false, size * 6, 0, 
				  new VertexAttribute( Usage.Position, 2, "a_position" ),
				  new VertexAttribute( Usage.ColorPacked, 4, "a_color" ),
				  new VertexAttribute( Usage.TextureCoordinates, 2, "a_texCoords" ) );
		projectionMatrix.setToOrtho2D( 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight() );
		
		vertices = new float[size * SPRITE_SIZE];
		
		if( Gdx.graphics.isGL20Available() )
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
							    "  gl_FragColor = v_color * texture2D(u_texture, v_texCoords); \n" +
							    "}"; 
		
		shader = new ShaderProgram( vertexShader, fragmentShader );
		if( shader.isCompiled() == false )
			throw new IllegalArgumentException( "couldn't compile shader: " + shader.getLog()  );
	}
	
	/**
	 * Sets up the SpriteBatch for drawing. This will disable
	 * depth buffer testing and writting, culling and lighting.
	 * It enables blending and alpha testing. It sets the projection
	 * matrix to an orthographic matrix and the modelview and texture
	 * matrix to identity. If you have more texture units enabled than
	 * the first one you have to disable them before calling this. The
	 * coordinate system used will have it's origin in the lower left
	 * corner, x pointing to the right and y point up. The coordinates
	 * for sprites will be interpreted in pixels.
	 */
	public void begin( )
	{		
		transformMatrix.idt();		
		begin( transformMatrix );
	}
	
	/**
	 * Sets up the SpriteBatch for drawing. This will disable
	 * depth buffer testing and writting, culling and lighting.
	 * It enables blending and alpha testing. If you have more texture units enabled than
	 * the first one you have to disable them before calling this. Applies
	 * the given transformation {@link Matrix4} to all subsequently specified sprites. Loads
	 * an orthographic projection matrix with the full screen as the viewport.
	 * The coordinate system used will have it's origin in the lower left
	 * corner, x pointing to the right and y point up. The coordinates
	 * for sprites will be interpreted in pixels.
	 * 
	 * @param transform the transformation matrix.
	 */
	public void begin( Matrix4 transform )
	{		
		projectionMatrix.setToOrtho2D( 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight() );
		begin( projectionMatrix, transform );
	}
	
	/**
	 * Sets up the SpriteBatch for drawing. This will disable
	 * depth buffer testing and writting, culling and lighting.
	 * It enables blending and alpha testing. If you have more texture units enabled than
	 * the first one you have to disable them before calling this. Applies
	 * the given transformation {@link Matrix4} to all subsequently specified sprites. Uses
	 * the provided projection matrix and therefore does not necessarily work
	 * in screen coordinates anymore. You have to know what you do if you use this.
	 * 
	 * @param projection the projection matrix;
	 * @param transform the transformation matrix.
	 */
	public void begin( Matrix4 projection, Matrix4 transform )
	{
		renderCalls = 0;
		this.projectionMatrix.set( projection );
		this.transformMatrix.set( transform );
		if( Gdx.graphics.isGL20Available() == false )
		{										
			GL10 gl = Gdx.graphics.getGL10();
			gl.glViewport( 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight() );
			gl.glDisable( GL10.GL_LIGHTING );
			gl.glDisable( GL10.GL_DEPTH_TEST );
			gl.glDisable( GL10.GL_CULL_FACE );
			gl.glDepthMask ( false );
			
			gl.glEnable( GL10.GL_TEXTURE_2D );
			//gl.glActiveTexture( GL10.GL_TEXTURE0 );
			
			gl.glMatrixMode( GL10.GL_PROJECTION );
			gl.glLoadMatrixf( projectionMatrix.val, 0 );
			gl.glMatrixMode( GL10.GL_MODELVIEW );
			gl.glLoadMatrixf( transformMatrix.val, 0 );
		}		
		else
		{
			combinedMatrix.set(projection).mul(transform);

			GL20 gl = Gdx.graphics.getGL20();
			gl.glViewport( 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight() );
			gl.glDisable( GL20.GL_DEPTH_TEST );
			gl.glDisable( GL20.GL_CULL_FACE );
			gl.glDepthMask ( false );

			gl.glEnable( GL20.GL_TEXTURE_2D );
			//gl.glActiveTexture( GL20.GL_TEXTURE0 );

			shader.begin();
			shader.setUniformMatrix( "u_projectionViewMatrix", combinedMatrix );
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
		
		if( Gdx.graphics.isGL20Available() == false )
		{
			GL10 gl = Gdx.graphics.getGL10();
			gl.glDepthMask ( true );			
			gl.glDisable( GL10.GL_BLEND );			
			gl.glDisable( GL10.GL_TEXTURE_2D );
		}
		else
		{
			shader.end();
			GL20 gl = Gdx.graphics.getGL20();
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
	 * @param scaleX the scale of the rectangle around originX/originY in x
	 * @param scaleY the scale of the rectangle around originX/originY in y
	 * @param rotation the angle of counter clockwise rotation of the rectangle around originX/originY
	 * @param srcX the x-coordinate in texel space
	 * @param srcY the y-coordinate in texel space
	 * @param srcWidth the source with in texels
	 * @param srcHeight the source height in texels
	 * @param tint the tint Color
	 * @param flipX whether to flip the sprite horizontally
	 * @param flipY whether to flip the sprite vertically
	 */
	public void draw(Texture texture, float x, float y, float originX, float originY, float width, float height, float scaleX, float scaleY, float rotation, int srcX, int srcY, int srcWidth, int srcHeight, Color tint, boolean flipX, boolean flipY )
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
		
		// bottom left and top right corner points relative to origin
		final float worldOriginX = x + originX;
		final float worldOriginY = y + originY;
		float fx = -originX;
		float fy = -originY;
		float fx2 = width - originX;
		float fy2 = height - originY;
		
		// scale
		if( scaleX != 1 || scaleY != 1 )
		{
			fx *= scaleX;
			fy *= scaleY;
			fx2 *= scaleX;
			fy2 *= scaleY;
		}
		
		// construct corner points, start from top left and go counter clockwise
		final float p1x = fx;
		final float p1y = fy;
		final float p2x = fx;
		final float p2y = fy2;
		final float p3x = fx2;
		final float p3y = fy2;
		final float p4x = fx2;
		final float p4y = fy;
		
		float x1;
		float y1;
		float x2;
		float y2;
		float x3;
		float y3;
		float x4;
		float y4;
		
		
		// rotate
		if( rotation != 0 )
		{
			final float cos = MathUtils.cosDeg( rotation );
			final float sin = MathUtils.sinDeg( rotation );						
			
			x1 = cos * p1x - sin * p1y;
			y1 = sin * p1x + cos * p1y;
			
			x2 = cos * p2x - sin * p2y;
			y2 = sin * p2x + cos * p2y;
			
			x3 = cos * p3x - sin * p3y;
			y3 = sin * p3x + cos * p3y;
			
			x4 = cos * p4x - sin * p4y;
			y4 = sin * p4x + cos * p4y;			
		}
		else
		{
			x1 = p1x;
			y1 = p1y;
			
			x2 = p2x;
			y2 = p2y;
			
			x3 = p3x;
			y3 = p3y;
			
			x4 = p4x;
			y4 = p4y;
		}			
		
		x1 += worldOriginX; y1 += worldOriginY;
		x2 += worldOriginX; y2 += worldOriginY;
		x3 += worldOriginX; y3 += worldOriginY;
		x4 += worldOriginX; y4 += worldOriginY;
		
		float u = srcX * invTexWidth;
		float v = (srcY + srcHeight) * invTexHeight;
		float u2 = (srcX + srcWidth) * invTexWidth;
		float v2 = srcY * invTexHeight;
		
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
		
		final float color = tint.toFloatBits();
		
		vertices[idx++] = x1;
		vertices[idx++] = y1;
		vertices[idx++] = color;
		vertices[idx++] = u; vertices[idx++] = v; 
		
		vertices[idx++] = x2;
		vertices[idx++] = y2;
		vertices[idx++] = color;
		vertices[idx++] = u; vertices[idx++] = v2;
		
		vertices[idx++] = x3;
		vertices[idx++] = y3;
		vertices[idx++] = color; 
		vertices[idx++] = u2; vertices[idx++] = v2;
		
		vertices[idx++] = x3;
		vertices[idx++] = y3;
		vertices[idx++] = color;
		vertices[idx++] = u2; vertices[idx++] = v2;
		
		vertices[idx++] = x4;
		vertices[idx++] = y4;
		vertices[idx++] = color;
		vertices[idx++] = u2; vertices[idx++] = v;
		
		vertices[idx++] = x1;
		vertices[idx++] = y1;
		vertices[idx++] = color;
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
		float v = (srcY + srcHeight) * invTexHeight;
		float u2 = (srcX + srcWidth) * invTexWidth;
		float v2 =  srcY * invTexHeight;
        final float fx2 = x + width;
		final float fy2 = y + height;
		
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
		
		final float color = tint.toFloatBits();
		
		vertices[idx++] = x;
		vertices[idx++] = y;
		vertices[idx++] = color;
		vertices[idx++] = u; vertices[idx++] = v; 
		
		vertices[idx++] = x;
		vertices[idx++] = fy2;
		vertices[idx++] = color;
		vertices[idx++] = u; vertices[idx++] = v2;
		
		vertices[idx++] = fx2;
		vertices[idx++] = fy2;
		vertices[idx++] = color;
		vertices[idx++] = u2; vertices[idx++] = v2;
		
		vertices[idx++] = fx2;
		vertices[idx++] = fy2;
		vertices[idx++] = color;
		vertices[idx++] = u2; vertices[idx++] = v2;
		
		vertices[idx++] = fx2;
		vertices[idx++] = y;
		vertices[idx++] = color;
		vertices[idx++] = u2; vertices[idx++] = v;
		
		vertices[idx++] = x;
		vertices[idx++] = y;
		vertices[idx++] = color;
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
		final float v = (srcY + srcHeight) * invTexHeight;
		final float u2 = (srcX + srcWidth) * invTexWidth;
		final float v2 =  srcY * invTexHeight;
        final float fx2 = x + srcWidth;
		final float fy2 = y + srcHeight;
		
		final float color = tint.toFloatBits();
		
		vertices[idx++] = x;
		vertices[idx++] = y;
		vertices[idx++] = color;
		vertices[idx++] = u; vertices[idx++] = v; 
		
		vertices[idx++] = x;
		vertices[idx++] = fy2;
		vertices[idx++] = color;
		vertices[idx++] = u; vertices[idx++] = v2;
		
		vertices[idx++] = fx2;
		vertices[idx++] = fy2;
		vertices[idx++] = color;
		vertices[idx++] = u2; vertices[idx++] = v2;
		
		vertices[idx++] = fx2;
		vertices[idx++] = fy2;
		vertices[idx++] = color;
		vertices[idx++] = u2; vertices[idx++] = v2;
		
		vertices[idx++] = fx2;
		vertices[idx++] = y;
		vertices[idx++] = color;
		vertices[idx++] = u2; vertices[idx++] = v;
		
		vertices[idx++] = x;
		vertices[idx++] = y;
		vertices[idx++] = color;
		vertices[idx++] = u; vertices[idx++] = v; 
		
		if( idx == vertices.length )
			renderMesh();
	}			
	
	public void draw( Sprite sprite )
	{
		if( !drawing )
			throw new IllegalStateException( "you have to call SpriteBatch.begin() first" );
		
		if( sprite.texture != lastTexture )
		{		
			renderMesh( );
			lastTexture = sprite.texture;
			invTexWidth = 1.0f / sprite.texture.getWidth();
			invTexHeight = 1.0f / sprite.texture.getHeight();
		}
		
		useTextBlend = false;
		
		System.arraycopy(sprite.vertices, 0, vertices, idx, 30);
		idx += 30;
		
		if( idx == vertices.length )
			renderMesh();
	}
	
	public void draw( Sprite2 sprite )
	{
		if( !drawing )
			throw new IllegalStateException( "you have to call SpriteBatch.begin() first" );
		
		if( sprite.texture != lastTexture )
		{		
			renderMesh( );
			lastTexture = sprite.texture;
			invTexWidth = 1.0f / sprite.texture.getWidth();
			invTexHeight = 1.0f / sprite.texture.getHeight();
		}
		
		useTextBlend = false;

		sprite.computeVertices( vertices, idx );
		idx += 30;
		
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
		
		if( Gdx.graphics.isGL20Available() )
		{
			if( useTextBlend )
			{
				Gdx.graphics.getGL20().glBlendFunc( GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA );
				Gdx.graphics.getGL20().glEnable( GL20.GL_BLEND );
			}
			else
			{
				if( blendingDisabled )
				{
					Gdx.graphics.getGL20().glDisable( GL20.GL_BLEND );
				}
				else
				{
					Gdx.graphics.getGL20().glEnable( GL20.GL_BLEND );
					Gdx.graphics.getGL20().glBlendFunc( blendSrcFunc, blendDstFunc );
				}
			}
			
			mesh.render( shader, GL10.GL_TRIANGLES );
		}
		else
		{
			if( useTextBlend )
			{
				Gdx.graphics.getGL10().glBlendFunc( GL10.GL_ONE, GL10.GL_ONE_MINUS_SRC_ALPHA );
				Gdx.graphics.getGL10().glEnable( GL10.GL_BLEND );
			}
			else
			{
				if( blendingDisabled )
				{
					Gdx.graphics.getGL10().glDisable( GL10.GL_BLEND );
				}
				else
				{
					Gdx.graphics.getGL10().glEnable( GL10.GL_BLEND );
					Gdx.graphics.getGL10().glBlendFunc( blendSrcFunc, blendDstFunc );
				}
			}
			mesh.render( GL10.GL_TRIANGLES );
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
		
		final float color = tint.toFloatBits();
		
		int len = text.length();		
		for( int i = 0; i < len; i++ )
		{
			char c = text.charAt(i);
			Glyph g = font.getGlyph(c);
			
			final float fx = x;
            final float fx2 = x + g.width;
			final float fy2 = y + g.height;
			final float u = g.u;
			final float v = g.v + g.vHeight;
			final float u2 = g.u + g.uWidth;
			final float v2 = g.v;
			
			vertices[idx++] = fx;
			vertices[idx++] = y;
			vertices[idx++] = color;
			vertices[idx++] = u; vertices[idx++] = v; 
			
			vertices[idx++] = fx;
			vertices[idx++] = fy2;
			vertices[idx++] = color;
			vertices[idx++] = u; vertices[idx++] = v2;
			
			vertices[idx++] = fx2;
			vertices[idx++] = fy2;
			vertices[idx++] = color;
			vertices[idx++] = u2; vertices[idx++] = v2;
			
			vertices[idx++] = fx2;
			vertices[idx++] = fy2;
			vertices[idx++] = color;
			vertices[idx++] = u2; vertices[idx++] = v2;
			
			vertices[idx++] = fx2;
			vertices[idx++] = y;
			vertices[idx++] = color;
			vertices[idx++] = u2; vertices[idx++] = v;
			
			vertices[idx++] = fx;
			vertices[idx++] = y;
			vertices[idx++] = color;
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
	
	/**
	 * Returns the current projection matrix. Changing this
	 * will result in undefined behaviour.
	 * 
	 * @return the currently set projection matrix
	 */
	public Matrix4 getProjectionMatrix( )
	{
		return projectionMatrix;
	}
	
	/**
	 * Returns the current transform matrix. Changing this
	 * will result in undefined behaviour.
	 * 
	 * @return the currently set transform matrix
	 */
	public Matrix4 getTransformMatrix( )
	{
		return transformMatrix;
	}

	/**
	 * @return whether blending for sprites is enabled
	 */
	public boolean isBlendingEnabled() 
	{
		return !blendingDisabled;
	}
}

package com.badlogic.gdx.graphics;

import com.badlogic.gdx.Graphics;

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
 * A sprite rendered via this patch has an origin relative to it's
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
public final class SpriteBatch 
{
	/**
	 * Sprite helper class
	 * @author mzechner
	 *
	 */
	private final class Sprite
	{
		/** the texture to use for this sprite **/
		public Texture texture;
		/** the source rect in texture coordinates **/
		public float srcX, srcY, srcWidth, srcHeight;
		/** the x and y coordinate on screen of the origin **/
		public float x, y;
		/** the origin relative to the upper left corner of the sprite **/
		public float originX, originY;
		/** the scale on the x and y axis **/
		public float scaleX, scaleY;
		/** the rotation around the origin **/
		public float rotation;					
	}
	
	/** the graphics instance **/
	private final Graphics g;	
	
	/**
	 * Consturctor, sets the {@link Graphics} instance
	 * to use.
	 * 
	 * @param graphics the Graphics instance
	 */
	public SpriteBatch( Graphics graphics )
	{
		this.g = graphics;
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
		
	}
	
	/**
	 * Renders all the things specified between a call to this
	 * and a call to {@link SpriteBatch.begin()}.
	 */
	public void end( )
	{
		
	}
}

package com.badlogic.gdx.graphics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

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
		/** the x and y coordinate on screen of the origin, z is the layer depth of the sprite **/
		public int x, y, depth;
		/** width and height of the sprite **/
		public int width, height;
		/** the origin relative to the upper left corner of the sprite **/
		public int originX, originY;
		/** the scale on the x and y axis **/
		public float scaleX, scaleY;
		/** the rotation around the origin **/
		public float rotation;
		/** the color **/
		public Color tint = new Color( 1, 1, 1, 1 );
	}
	
	/** the graphics instance **/
	private final Graphics graphics;	
	
	/** the renderer **/
	private final ImmediateModeRenderer renderer;
	
	/** list of free sprites **/
	private final ArrayList<Sprite> freeSprites = new ArrayList<Sprite>( 300 );
	
	/** list of sprites to draw **/
	private final ArrayList<Sprite> sprites = new ArrayList<Sprite>( 300 );
	
	/** depth sorter **/
	private final SpriteSorter sorter = new SpriteSorter();
	
	/**
	 * Consturctor, sets the {@link Graphics} instance
	 * to use.
	 * 
	 * @param graphics the Graphics instance
	 */
	public SpriteBatch( Graphics graphics )
	{
		this.graphics = graphics;
		this.renderer = null;
	}
	
	private Sprite getFreeSprite( )
	{
		if( freeSprites.size() == 0 )
			return new Sprite( );
		else
			return freeSprites.remove( freeSprites.size()-1);
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
		freeSprites.addAll( sprites );
		sprites.clear();
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
	 * @param width the width in pixels
	 * @param height the height in pixels
	 * @param srcX the x-coordinate in texel space
	 * @param srcY the y-coordinate in texel space
	 * @param srcWidth the source with in texels
	 * @param srcHeight the source height in texels
	 * @param tint the tint Color
	 */
	public void draw( Texture texture, int x, int y, int width, int height, int srcX, int srcY, int srcWidth, int srcHeight, Color tint )
	{
		Sprite sprite = getFreeSprite( );
		sprite.texture = texture;
		sprite.x = x; 
		sprite.y = y;
		sprite.depth = 0;
		sprite.width = width;
		sprite.height = height;
		sprite.srcX = srcX / texture.getWidth();
		sprite.srcY = srcY / texture.getHeight();
		sprite.srcHeight = srcHeight / texture.getHeight();
		sprite.srcWidth = srcWidth / texture.getWidth();
		sprite.originX = 0;
		sprite.originY = 0;
		sprite.scaleX = 0;
		sprite.scaleY = 0;
		sprite.rotation = 0;
		sprite.tint.set(Color.WHITE);
		
	}
	
	/**
	 * Draws a rectangle with its origin at x,y having
	 * the given width and height in pixels. The origin is given relative to the
	 * rectangles top left corner. Rotation specifies the rotation of the rectangle
	 * around the origin. ScaleX and scaleY specify the scaling in x and y. The portion of the {@link Texture}
	 * given by srcX, srcY and srcWidth, srcHeight is used. These coordinates and
	 * sizes are given in texels. The rectangle will have the given tint {@link Color}. The
	 * depth specifies at which layer the sprite should be rendered. Sprites are sorted
	 * upon a call to {@link SpriteBatch.end()} according to their depth.
	 *  
	 * @param texture the Texture
	 * @param x the x-coordinate in screen space
	 * @param y the y-coordinate in screen space
	 * @param width the width in pixels
	 * @param height the height in pixels
	 * @param originX the x-coordinate of the origin relative to the top left corner
	 * @param originY the y-coordinate of the origin relative to the top left corner
	 * @param rotation the rotation around the origin
	 * @param scaleX the scale on the x-axis
	 * @param scaleY the scale on the y-axis
	 * @param srcX the x-coordinate in texel space
	 * @param srcY the y-coordinate in texel space
	 * @param srcWidth the source with in texels
	 * @param srcHeight the source height in texels
	 * @param tint the tint Color
	 * @param depth the sorting depth
	 */
	public void draw( Texture texture, int x, int y, int width, int height, int originX, int originY, float rotation, int scaleX, int scaleY, int srcX, int srcY, int srcWidth, int srcHeight, Color tint, int depth )
	{
		Sprite sprite = getFreeSprite( );
		sprite.texture = texture;
		sprite.x = x; 
		sprite.y = y;
		sprite.depth = depth;
		sprite.width = width;
		sprite.height = height;
		sprite.srcX = srcX / texture.getWidth();
		sprite.srcY = srcY / texture.getHeight();
		sprite.srcHeight = srcHeight / texture.getHeight();
		sprite.srcWidth = srcWidth / texture.getWidth();
		sprite.originX = originX;
		sprite.originY = originY;
		sprite.scaleX = scaleX;
		sprite.scaleY = scaleY;
		sprite.rotation = rotation;
		sprite.tint.set( tint );
	}
	
	/**
	 * Renders all the things specified between a call to this
	 * and a call to {@link SpriteBatch.begin()}. Sprites get
	 * sorted by their depth in descending order (higher depth
	 * gets drawn first). 
	 */
	public void end( )
	{
		Collections.sort( sprites, sorter );
	}
	
	
	final static class SpriteSorter implements Comparator<Sprite>
	{
		@Override
		public int compare(Sprite sprite1, Sprite sprite2) 
		{	
			float value = sprite1.depth - sprite2.depth;
			if( value < 0 )
				return -1;
			if( value > 0 )
				return 1;
			return 0;
		}
		
	}
	
}

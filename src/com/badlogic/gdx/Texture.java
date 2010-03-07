package com.badlogic.gdx;

/**
 * A Texture represents a bitmap to be applied to a {@link Mesh}. 
 * It is constructed from a platform dependent bitmap with various
 * parameters. It might get resized to better fit the architectures
 * needs, e.g. to a power of two.
 * 
 * @author mzechner
 *
 */
public interface Texture 
{
	/**
	 * Binds this texture
	 */
	public void bind( );

	/**
	 * Draws the given bitmap to the texture at position x, y
	 * @param bitmap The bitmap
	 * @param x The x coordinate in pixels
	 * @param y The y coordinate in pixels
	 */
	public void draw( Object bitmap, int x, int y );
	
	/**
	 * 
	 * @return the width of the original image in pixels
	 */
	public int getImageWidth( );
	
	/**
	 * 
	 * @return the height of the original image in pixels
	 */
	public int getImageHeight( );
	
	/**
	 * 
	 * @return the width of the texture in pixels
	 */
	public int getWidth( );
	
	/**
	 * 
	 * @return the height of the texture in pixels
	 */
	public int getHeight( );

	/**
	 * Disposes all resources associated with the texture
	 * @return
	 */
	public void dispose( );
}

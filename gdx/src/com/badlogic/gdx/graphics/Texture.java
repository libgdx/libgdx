/**
 *  This file is part of Libgdx by Mario Zechner (badlogicgames@gmail.com)
 *
 *  Libgdx is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Libgdx is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.badlogic.gdx.graphics;

import com.badlogic.gdx.Application;

/**
 * A Texture represents a bitmap to be applied to a {@link Mesh}. 
 * It is constructed by an {@link Application} via one of the
 * {@link Application.newTexture()} methods. It might get resized 
 * to better fit the architectures needs, e.g. to a power of two.
 * To play it save only use power of two textures!
 * 
 * @author badlogicgames@gmail.com
 *
 */
public interface Texture 
{
	/**
	 * Binds this texture. You have to enable texturing via
	 * {@link Application.enable( RenderState.Texturing )} in 
	 * order for the texture to actually be applied to geometry.
	 */
	public void bind( );

	/**
	 * Draws the given {@link Pixmap} to the texture at position x, y.
	 * 
	 * @param pixmap The Pixmap
	 * @param x The x coordinate in pixels
	 * @param y The y coordinate in pixels
	 */
	public void draw( Pixmap pixmap, int x, int y );
	
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

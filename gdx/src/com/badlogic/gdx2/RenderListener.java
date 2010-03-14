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
package com.badlogic.gdx2;

import com.badlogic.gdx2.Application;

/**
 * A RenderListener can be hooked to a {@link Graphics} instance
 * and will receive setup, render and dispose events. In case
 * of a setup event the listener can create any resources it
 * needs later on to draw. The setup method is also called when
 * the OpenGL surface has been recreated. This happens only on
 * Android in case the application was paused and resumed. All
 * OpenGL resources like textures and shaders are lost and have to
 * be recreated. The render method is called whenever the {@link Application} is redrawn. The 
 * dispose method is called before the Application is closed 
 * or the RenderListener is unregistered from the Graphics instance.
 * 
 * The methods will be invoked in the rendering thread of the
 * application and not in the UI thread!
 * 
 * 
 * @author badlogicgames@gmail.com
 *
 */
public interface RenderListener 
{
	/** 
	 * The setup method is called once upon initialization of
	 * the {@link Application} and every time the OpenGL surface
	 * is recreated. On Android this happens when the application
	 * is unpaused after it was paused by a press of the home button
	 * or an incoming call. All textures, shaders and other OpenGL
	 * resources will be lost and have to be recreated.
	 * 
	 * @param app The application
	 */
	public void setup( Application app );	
	
	/**
	 * The render method is called every time a new frame 
	 * should be rendered.  
	 *
	 * @param app The application
	 */
	public void render( Application app );
	
	/**
	 * The dispose method is called when the application is closing
	 * or the {@link RenderListener} has been unregistered form the
	 * Graphics instance.
	 * 
	 * @param app The application
	 */	
	public void dispose( Application app );
}

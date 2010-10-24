/*******************************************************************************
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx;

/**
 * A RenderListener can be hooked to a {@link Graphics} instance and will receive setup, render and dispose events. In case of a
 * setup event the listener can create any resources it needs later on to draw. The setup method is also called when the OpenGL
 * surface has been recreated after a context loss. This happens only on Android in case the application was paused and resumed.
 * All OpenGL resources like textures, meshes and shaders are lost and have to be recreated. The render method is called whenever
 * the {@link Application} is redrawn. The dispose method is called before the Application is closed or the RenderListener is
 * unregistered from the Graphics instance.
 * 
 * The methods will be invoked in the rendering thread of the application and not in the UI thread!
 * 
 * 
 * @author badlogicgames@gmail.com
 * 
 */
public interface RenderListener {
	/**
	 * The setup method is called once upon initialization of the {@link Application} and every time the OpenGL surface is
	 * recreated. On Android this happens when the application is unpaused after it was paused by a press of the home button or an
	 * incoming call. All unmanaged textures have to be recreated manually, all other resources will be reloaded automatically.
	 * 
	 */
	public void surfaceCreated ();

	/**
	 * Called when the OpenGL surface changed it's dimensions.
	 * 
	 * @param width the width of the surface in pixels
	 * @param height the height of the surface in pixels
	 */
	public void surfaceChanged (int width, int height);

	/**
	 * The render method is called every time a new frame should be rendered.
	 */
	public void render ();

	/**
	 * The dispose method is called when the application is closing or the {@link RenderListener} has been unregistered from the
	 * Graphics instance.
	 */
	public void dispose ();
}

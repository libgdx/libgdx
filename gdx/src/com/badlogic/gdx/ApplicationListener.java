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
 * <p>
 * An <code>ApplicationListener</code> is called when the {@link Application} is resumed, paused and destroyed. This allows you to
 * save any states you want to save. The methods are not called from within the rendering thread so care has to be taken. Do not
 * load or unload any Graphics related resources in the provided by this interface!
 * </p>
 * 
 * <p>
 * The <code>ApplicationListener</code> interface follows the standard Android activity life-cycle and is emulated on the desktop
 * accordingly. The pause method will be called after the rendering thread has been paused. The resume method will be called
 * before the rendering thread has been started.
 * 
 * @author mzechner
 * 
 */
public interface ApplicationListener {
	/**
	 * Called when the {@link Application} is paused. An Application is paused before it is destroyed or when a user pressed the
	 * Home button on Android. This will not be called in the rendering thread. Instead the rendering thread will be paused before
	 * this method is called.
	 */
	public void pause ();

	/**
	 * Called when the {@link Application} is resumed from a paused state or the Application was just created. This will not be
	 * called in the rendering thread. Instead this callback will be first called, then the rendering thread will be resumed.
	 */
	public void resume ();

	/**
	 * Called when the {@link Application} is destroyed. This will not be called in the rendering thread.
	 */
	public void destroy ();
}

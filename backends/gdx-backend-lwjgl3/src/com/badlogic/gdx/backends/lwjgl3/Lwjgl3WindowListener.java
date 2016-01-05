/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
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


package com.badlogic.gdx.backends.lwjgl3;

import com.badlogic.gdx.ApplicationListener;

// FIXME wire this up with multi-window api
// as a parameter to app#newWindow
public interface Lwjgl3WindowListener {
	/**
	 * Called when the window is iconified, i.e. its minimize button
	 * was clicked. The window's {@link ApplicationListener} will
	 * be paused
	 */
	void iconified();
	
	/**
	 * Called when the window is deiconified, i.e. its task bar
	 * icon was clicked. The window's {@link ApplicationListener}
	 * will be resumed.
	 */
	void deiconified();
	
	/**
	 * Called when the window lost focus to another window. The
	 * window's {@link ApplicationListener} will continue to be
	 * called.
	 */
	void focusLost();
	
	/**
	 * Called when the window gained focus.
	 */
	void focusGained();		
	
	/** Called when the window is about to close due to a user action,
	 * e.g. clicking the close button or pressing the window closing
	 * keyboard shortcut.
	 *  
	 * @return whether the window should actually close **/
	boolean windowIsClosing();
}

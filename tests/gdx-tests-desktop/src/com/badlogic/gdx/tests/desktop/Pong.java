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

package com.badlogic.gdx.tests.desktop;

import com.badlogic.gdx.backends.desktop.JoglApplication;

/**
 * A simple Pong remake showing how easy it is to quickly prototype a game with libgdx.
 * 
 * @author mzechner
 * 
 */
public class Pong {
	public static void main (String[] argv) {
		// we create a new JoglApplication and register a new Pong instances as the RenderListener
		JoglApplication app = new JoglApplication("Pong", 480, 320, false);
		app.getGraphics().setRenderListener(new com.badlogic.gdx.tests.Pong());
	}
}

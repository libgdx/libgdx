/*
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com), Nathan Sweet (admin@esotericsoftware.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.badlogic.gdxinvaders;

import com.badlogic.gdx.backends.jogl.JoglApplication;
import com.badlogic.gdx.backends.jogl.JoglApplicationConfiguration;

/** Entry point for desktop version of Gdx Invaders. Constructs a JoglApplication and registers the renderer.
 * @author mzechner */
public class GdxInvadersDesktop {
	public static void main (String[] argv) {
		JoglApplicationConfiguration config = new JoglApplicationConfiguration();
		config.title = "Gdx Invaders";
		config.vSyncEnabled = true;
		config.useGL20 = true;
		new JoglApplication(new GdxInvaders(), config);
	}
}

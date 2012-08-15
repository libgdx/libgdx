/*
 * Copyright 2011 Rod Hyde (rod@badlydrawngames.com)
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

package com.badlydrawngames.veryangryrobots;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;

public class DesktopStarter {

	private final static int WINDOW_WIDTH = 800;
	private final static int WINDOW_HEIGHT = 480;

	public static void main (String[] args) {
		new LwjglApplication(new VeryAngryRobotsGame(), "Very Angry Robots", WINDOW_WIDTH, WINDOW_HEIGHT, false);
	}
}

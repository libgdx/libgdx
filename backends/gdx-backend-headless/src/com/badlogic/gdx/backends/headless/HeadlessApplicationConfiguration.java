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

package com.badlogic.gdx.backends.headless;

public class HeadlessApplicationConfiguration {
	/** The minimum time (in seconds) between each call to the render method or negative to not call the render method at all. */
	public float renderInterval = 1f / 60f;
	/** Preferences directory for headless. Default is ".prefs/". */
	public String preferencesDirectory = ".prefs/";
}

/*******************************************************************************
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com)
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
package com.badlogic.gdx;

/**
 * <p>Environment class holding references to the Application, Graphics, Audio,
 * Files and Input instances. The references are held in public static fields.
 * Do not mess with this! This essentially allows you static access to all sub
 * systems. It is your responsiblity to keep things thread safe. Don't use Graphics
 * in a thread that is not the rendering thread or things will go crazy. Really.</p> 
 * 
 * <p>This is kind of messy but better than throwing around Graphics and similar instances.
 * I'm aware of the design faux pas</p>
 * @author mzechner
 *
 */
public class Gdx 
{
	public static Application app;
	public static Graphics graphics;
	public static Audio audio;
	public static Input input;
	public static Files files;
}

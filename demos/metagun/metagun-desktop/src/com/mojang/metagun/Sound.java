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

 
package com.mojang.metagun;

import com.badlogic.gdx.Gdx;

public class Sound {

	public static com.badlogic.gdx.audio.Sound boom;
	public static com.badlogic.gdx.audio.Sound hit;
	public static com.badlogic.gdx.audio.Sound splat;
	public static com.badlogic.gdx.audio.Sound launch;
	public static com.badlogic.gdx.audio.Sound pew;
	public static com.badlogic.gdx.audio.Sound oof;
	public static com.badlogic.gdx.audio.Sound gethat;
	public static com.badlogic.gdx.audio.Sound death;
	public static com.badlogic.gdx.audio.Sound startgame;
	public static com.badlogic.gdx.audio.Sound jump;

	public static void load () {
		boom = load("res/boom.wav");
		hit = load("res/hit.wav");
		splat = load("res/splat.wav");
		launch = load("res/launch.wav");
		pew = load("res/pew.wav");
		oof = load("res/oof.wav");
		gethat = load("res/gethat.wav");
		death = load("res/death.wav");
		startgame = load("res/startgame.wav");
		jump = load("res/jump.wav");
	}

	private static com.badlogic.gdx.audio.Sound load (String name) {
		return Gdx.audio.newSound(Gdx.files.internal(name));
	}
}

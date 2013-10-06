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

 
package com.mojang.metagun.screen;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.mojang.metagun.Art;
import com.mojang.metagun.Input;
import com.mojang.metagun.Sound;

public class TitleScreen extends Screen {
	private int time = 0;

	@Override
	public void render () {
		int yOffs = 480 - time * 2;
		if (yOffs < 0) yOffs = 0;
		spriteBatch.begin();
		draw(Art.bg, 0, 0);
		draw(Art.titleScreen, 0, -yOffs);
		if (time > 240) {
			String msg = null;
			if (Gdx.app.getType() == ApplicationType.Android)
				msg = "TOUCH TO START";
			else
				msg = "PRESS X TO START";
			drawString(msg, 160 - msg.length() * 3, 140 - 3 - (int)Math.abs(Math.sin(time * 0.1) * 10));

		}
		if (time >= 0) {
			String msg = "COPYRIGHT MOJANG 2010";
			drawString(msg, 2, 240 - 6 - 2);
		}
		spriteBatch.end();
	}

	@Override
	public void tick (Input input) {
		time++;
		if (time > 240) {
			if (input.buttons[Input.SHOOT] && !input.oldButtons[Input.SHOOT] || Gdx.input.isTouched()) {
				Sound.startgame.play();
				setScreen(new GameScreen());
				input.releaseAllKeys();
			}
		}
		if (time > 60 * 10) {
			setScreen(new ExpositionScreen());
		}
	}
}

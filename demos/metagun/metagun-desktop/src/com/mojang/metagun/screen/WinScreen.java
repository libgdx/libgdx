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

import com.mojang.metagun.Art;
import com.mojang.metagun.Input;
import com.mojang.metagun.Stats;

public class WinScreen extends Screen {
	private int time = 0;

	@Override
	public void render () {
		spriteBatch.begin();
		int w = 240 * 8; // Art.bg.getHeight();
		draw(Art.bg, 0, -(time * 2 % w));
		draw(Art.bg, 0, -(time * 2 % w) + w);

		int offs0 = 500 - time * 10;
		if (offs0 < 0) offs0 = 0;
		int offs1 = 1200 - time * 16;
		if (offs1 < 0) offs1 = 0;
		int yOffs = 600 - time * 5;
		if (yOffs < -120) yOffs = -120;
		if (yOffs > 0) yOffs = 0;
		draw(Art.winScreen1, offs0, yOffs + 30);
		draw(Art.winScreen2, -offs1, yOffs * 2 / 3 + 30);

		int tt = time - (60 * 2 + 30);
		int yo = 130;
		int xo = 120 - 8 * 3;
		if (tt >= 0) {
			drawString("       TIME: " + Stats.instance.getTimeString(), xo, yo + 0 * 6);
			drawString("     DEATHS: " + Stats.instance.deaths, xo, yo + 1 * 6);
			drawString("    FEDORAS: " + Stats.instance.hats + "/" + 7, xo, yo + 2 * 6);
			drawString("SHOTS FIRED: " + Stats.instance.shots, xo, yo + 3 * 6);
			drawString("FINAL SCORE: " + timeScale(Stats.instance.getFinalScore(), tt - 30 * 5), xo, yo + 5 * 6);

			drawString(timeHideScale(Stats.instance.getSpeedScore(), tt - 30 * 1), xo + 20 * 6, yo + 0 * 6);
			drawString(timeHideScale(Stats.instance.getDeathScore(), tt - 30 * 2), xo + 20 * 6, yo + 1 * 6);
			drawString(timeHideScale(Stats.instance.getHatScore(), tt - 30 * 3), xo + 20 * 6, yo + 2 * 6);
			drawString(timeHideScale(Stats.instance.getShotScore(), tt - 30 * 4), xo + 20 * 6, yo + 3 * 6);
		}

		if (time > 60 * 7 && time / 30 % 2 == 0) {
			String msg = "PRESS X TO RESET THE GAME";
			drawString(msg, 160 - msg.length() * 3, yo + 10 * 6);
		}
		spriteBatch.end();
	}

	private String timeHideScale (int val, int time) {
		if (time < 10) return "";
// if (time>60+60) return "";
		if (time < 0) time = 0;
		if (time > 60) time = 60;
		return "+" + val * time / 60;
	}

	private String timeScale (int val, int time) {
		if (time < 0) time = 0;
		if (time > 60) time = 60;
		return "" + val * time / 60;
	}

	@Override
	public void tick (Input input) {
		time++;
		if (time > 60 * 7) {
			if (input.buttons[Input.SHOOT] && !input.oldButtons[Input.SHOOT]) {
				setScreen(new TitleScreen());
			}
		}
	}
}

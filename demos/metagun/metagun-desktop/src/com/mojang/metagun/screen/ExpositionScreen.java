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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.mojang.metagun.Art;
import com.mojang.metagun.Input;

public class ExpositionScreen extends Screen {
	private int time = 0;

	private final List<String> lines = new ArrayList<String>();

	public ExpositionScreen () {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(Gdx.files.internal("res/exposition.txt").read()));

			String line = "";
			while ((line = br.readLine()) != null) {
				lines.add(line);
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public void render () {
		int w = -Art.bg.getRegionHeight();
		spriteBatch.begin();
		draw(Art.bg, 0, -(time / 8 % w));
		draw(Art.bg, 0, -(time / 8 % w) + w);

		int yo = time / 4;
		for (int y = 0; y <= 240 / 6; y++) {
			int yl = yo / 6 - 240 / 6 + y;
			if (yl >= 0 && yl < lines.size()) {
				drawString(lines.get(yl), (320 - 40 * 6) / 2, y * 6 - yo % 6);
			}
		}
		spriteBatch.end();
	}

	@Override
	public void tick (Input input) {
		time++;
		if (time / 4 > lines.size() * 6 + 250) {
			setScreen(new TitleScreen());
		}
		if (input.buttons[Input.SHOOT] && !input.oldButtons[Input.SHOOT] || Gdx.input.isTouched()) {
			setScreen(new TitleScreen());
		}
		if (input.buttons[Input.ESCAPE] && !input.oldButtons[Input.ESCAPE]) {
			setScreen(new TitleScreen());
		}
	}
}


package com.mojang.metagun.screen;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.mojang.metagun.Art;
import com.mojang.metagun.Input;
import com.mojang.metagun.Metagun;
import com.mojang.metagun.Stats;
import com.mojang.metagun.level.Camera;
import com.mojang.metagun.level.Level;

public class GameScreen extends Screen {
	public static final int MAX_HATS = 7;

	private static final boolean DEBUG_MODE = false;
	private int xLevel = DEBUG_MODE ? 8 : 0;
	private int yLevel = DEBUG_MODE ? 4 : 0;

	Level level = new Level(this, 32, 24, xLevel, yLevel, 0, 0);
	private Camera camera = new Camera(Metagun.GAME_WIDTH, Metagun.GAME_HEIGHT);

	public boolean mayRespawn = false;
	private int gunLevel = DEBUG_MODE ? 2 : 0;
	private int hatCount = 1;

	public GameScreen () {
		Stats.reset();

		level.player.gunLevel = gunLevel;
		level.player.hatCount = hatCount;
	}

	public void tick (Input input) {
		Stats.instance.time++;
		if (!input.oldButtons[Input.ESCAPE] && input.buttons[Input.ESCAPE]) {
			setScreen(new PauseScreen(this));
			return;
		}
		if (!level.player.removed)
			level.player.tick(input);
		else if (mayRespawn) {
			if (input.buttons[Input.SHOOT] && !input.oldButtons[Input.SHOOT]) {
				respawnRoom();
				mayRespawn = false;
			}
		}
		level.tick();
		Stats.instance.hats = level.player.hatCount;
	}

	public void transition (int xa, int ya) {
		Stats.instance.hats = level.player.hatCount;
		xLevel += xa;
		yLevel += ya;
		if (yLevel > 10) {
			setScreen(new WinScreen());
			return;
		}
		level.player.x -= xa * 300;
		level.player.y -= ya * 220;
		hatCount = level.player.hatCount;
		if (ya != 0) level.player.y -= 10;
		Level newLevel = new Level(this, 32, 24, xLevel, yLevel, (int)(level.player.x), (int)(level.player.y + ya * 5));
		newLevel.player.remove();
		newLevel.player = level.player;
		newLevel.add(newLevel.player);
		setScreen(new LevelTransitionScreen(this, xLevel - xa, yLevel - ya, level, newLevel, xa, ya));
		this.level = newLevel;
		level.player.gunLevel = gunLevel;
		level.player.hatCount = hatCount;
		level.player.damage = 0;
	}

	public void render () {
		spriteBatch.begin();
// draw(Art.bg, -xLevel * 160, -yLevel * 120);
		draw(Art.bg, 0, 0);
		spriteBatch.end();
		level.render(this, camera);

		spriteBatch.begin();
		if (mayRespawn) {
			String msg = "PRESS X TO TRY AGAIN";
			drawString(msg, 160 - msg.length() * 3, 120 - 3);
		}
		if (Gdx.app.getType() == ApplicationType.Android) {
			draw(Art.buttons[0][0], 0, 240 - 32);
			draw(Art.buttons[1][0], 32, 240 - 32);

			draw(Art.buttons[4][0], 160 - 32, 240 - 32);
			draw(Art.buttons[5][0], 160, 240 - 32);

			draw(Art.buttons[2][0], 320 - 64, 240 - 32);
			draw(Art.buttons[3][0], 320 - 32, 240 - 32);
		}
		spriteBatch.end();
	}

	public void readSign (int id) {
		setScreen(new SignReadScreen(this, id));
	}

	public void respawnRoom () {
		Level newLevel = new Level(this, 32, 24, xLevel, yLevel, level.xSpawn, level.ySpawn);
		this.level = newLevel;
		level.player.gunLevel = gunLevel;
		if (hatCount < 1) hatCount = 1;
		level.player.hatCount = hatCount;
		level.player.damage = 0;
	}

	public void getGun (int level) {
		gunLevel = level;
	}
}


package com.mojang.metagun.entity;

import com.mojang.metagun.Art;
import com.mojang.metagun.level.Camera;
import com.mojang.metagun.screen.Screen;

public class Sign extends Entity {
	public int id;
	public boolean autoRead = false;

	public Sign (int x, int y, int id) {
		this.x = x;
		this.y = y;
		this.w = 6;
		this.h = 6;
		xa = ya = 0;
		this.id = id;
		autoRead = id == 1;
		if (id == 6) autoRead = true;
		if (id == 15) autoRead = true;
	}

	@Override
	public void tick () {
		if (id == 6 && level.player.gunLevel >= 1) remove();
		if (id == 15 && level.player.gunLevel >= 2) remove();
		java.util.List<Entity> entities = level.getEntities((int)x, (int)y, 6, 6);
		for (int i = 0; i < entities.size(); i++) {
			Entity e = entities.get(i);
			if (e instanceof Player) {
				Player player = (Player)e;
				player.readSign(this);
			}
		}
	}

	@Override
	public void render (Screen g, Camera camera) {
		if (id == 6 && level.player.gunLevel >= 1) return;
		if (id == 15 && level.player.gunLevel >= 2) return;
		if (id == 6) {
			g.draw(Art.walls[5][0], (int)x, (int)y);
		} else if (id == 15) {
			g.draw(Art.walls[6][0], (int)x, (int)y);
		} else {
			g.draw(Art.walls[4][0], (int)x, (int)y);
		}
	}
}

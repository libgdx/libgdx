
package com.mojang.metagun.entity;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mojang.metagun.Art;
import com.mojang.metagun.Sound;
import com.mojang.metagun.level.Camera;
import com.mojang.metagun.level.Level;
import com.mojang.metagun.screen.Screen;

public class Hat extends Entity {
	double time = 0;
	int noTakeTime = 30;
	private final int xPos, yPos;
	public double xxa;

	public Hat (double x, double y) {
		this(x, y, -1, -1);
	}

	public Hat (double x, double y, int xPos, int yPos) {
		this.x = x;
		this.y = y;
		this.xPos = xPos;
		this.yPos = yPos;
		w = 6;
		h = 3;
		bounce = 0;
		ya = -1;
		time = Math.PI * 0.5;
	}

	@Override
	public void tick () {
		tryMove(xa, ya);
		if (onGround) {
			time = 0;
		} else {
			time++;
		}

		xa = xxa + Math.sin(time * 0.05) * 0.2;
		xxa *= 0.95;
		ya *= 0.95;
		ya += Level.GRAVITY * 0.1;

		if (noTakeTime > 0)
			noTakeTime--;
		else {
			java.util.List<Entity> entities = level.getEntities((int)x, (int)y, w, h);
			for (int i = 0; i < entities.size(); i++) {
				Entity e = entities.get(i);
				if (e instanceof Player) {
					Player player = (Player)e;
					player.hatCount++;

					if (xPos >= 0 && yPos >= 0) {
						Art.level.setColor(0, 0, 0, 0);
						Art.level.drawPixel(xPos, yPos);
					}
					Sound.gethat.play();
					remove();
				}
			}
		}
	}

	@Override
	public void render (Screen g, Camera camera) {
		int dir = 1;
		int xp = (int)x - (16 - w) / 2;
		int yp = (int)y - 2;
		TextureRegion[][] sheet = dir == 1 ? Art.player1 : Art.player2;

		int xFrame = (int)(xa * 10);
		if (xFrame < -1) xFrame = -1;
		if (xFrame > +1) xFrame = +1;
		g.draw(sheet[1 + xFrame][1], xp, yp);
	}

	@Override
	public boolean shot (Bullet bullet) {
		Sound.hit.play();
		xa += bullet.xa * 0.5;
		ya += bullet.ya * 0.5;

		return true;
	}
}

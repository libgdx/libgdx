
package com.mojang.metagun.entity;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mojang.metagun.Art;
import com.mojang.metagun.Input;
import com.mojang.metagun.Sound;
import com.mojang.metagun.Stats;
import com.mojang.metagun.level.Camera;
import com.mojang.metagun.level.Level;
import com.mojang.metagun.screen.Screen;

public class Player extends Entity {
	private int dir = 1;
	private int yAim = 0;
	private int frame = 0;
	public int hatCount = 1;
	public int damage = 0;
	private int noHurtTime = 0;
	public int gunLevel = 0;
	public boolean readSign = false;
	private int shootTime = 0;

	public Player (int x, int y) {
		this.x = x;
		this.y = y;
		w = 8;
		h = 18;
		bounce = 0;
	}

	@Override
	public void tick () {
	}

	@Override
	public void render (Screen g, Camera camera) {
		int xp = (int)x - (16 - w) / 2;
		int yp = (int)y - 2;

		int stepFrame = frame / 4 % 4;

		TextureRegion[][] sheet = dir == 1 ? Art.player1 : Art.player2;
		if (!onGround) {
			int yya = (int)Math.round(-ya);
			stepFrame = 4;
			if (yya < -1) stepFrame = 5;
			yp += yya;
		}
		g.draw(sheet[3 + stepFrame][hatCount > 0 ? 0 : 1], xp, yp);

		yp += stepFrame == 3 ? 1 : 0;
		for (int i = 1; i < hatCount; i++) {
			g.draw(sheet[0][1], xp, yp - i * 2);
		}

		if (gunLevel > 0) {
			if (!onGround) {
				int yya = (int)Math.round(-ya);
				if (yya < -1) yya = -1;
				if (yya > 1) yya = 1;
				yp += yya;
			}
			g.draw(sheet[1 + yAim][(gunLevel - 1) * 2], xp, yp);
		}
	}

	public void tick (Input input) {
		readSign = true; // onGround && input.buttons[Input.UP] && !input.oldButtons[Input.UP];
		if (noHurtTime > 0) noHurtTime--;
		double speed = 0.4;
		double aimAngle = -0.2;
		yAim = 0;
		if (input.buttons[Input.UP]) {
			aimAngle -= 0.8;
			yAim--;
		}
		if (input.buttons[Input.DOWN]) {
			aimAngle += 0.8;
			yAim++;
		}
		boolean walk = false;
		if (input.buttons[Input.LEFT]) {
			walk = true;
			xa -= speed;
			dir = -1;
		}
		if (input.buttons[Input.RIGHT]) {
			walk = true;
			xa += speed;
			dir = 1;
		}
		if (walk)
			frame++;
		else
			frame = 0;
		if (input.buttons[Input.JUMP] && !input.oldButtons[Input.JUMP] && onGround) {
			Sound.jump.play();
			ya -= 2 + Math.abs(xa) * 0.5;
		}

		tryMove(xa, ya);

		xa *= 0.7;
		if (ya < 0 && input.buttons[Input.JUMP]) {
			ya *= 0.992;
			ya += Level.GRAVITY * 0.5;
		} else {
			ya *= Level.FRICTION;
			ya += Level.GRAVITY;
		}

		boolean shooting = false;
		if (gunLevel > 0 && input.buttons[Input.SHOOT] && !input.oldButtons[Input.SHOOT]) shooting = true;
		if (gunLevel > 1 && input.buttons[Input.SHOOT] && (!input.oldButtons[Input.SHOOT] || shootTime > 0)) {
			shooting = shootTime++ % 3 == 0;
		} else {
			shootTime = 0;
		}
		if (shooting) {
			double pow = 3;
			Sound.launch.play();

			double xx = x + w / 2.0 - 2.5 + dir * 7;
			double yy = y + h / 2.0 - 2.5 + yAim * 2;
			for (int i = 0; i < 4; i++) {
				double xAim = Math.cos(aimAngle + 0.2) * dir * pow;
				double yAim = Math.sin(aimAngle + 0.2) * pow;
				double xxa = xa + xAim * 0.2;
				double yya = ya + yAim * 0.2;
				level.add(new Spark(xx, yy + (-2 + i) * 0.5, xxa, yya));
			}
			double xAim = Math.cos(aimAngle) * dir * pow;
			double yAim = Math.sin(aimAngle) * pow;
			double xxa = xa + xAim;
			double yya = ya + yAim;
			if (gunLevel == 2) {
				xa -= xAim * 0.1;
				ya -= yAim * 0.1;
			}
			xx = x + w / 2.0 - 2.5;
			Stats.instance.shots++;
			level.add(new Gunner(xx, yy, xxa, yya));
		}

		if (y < 5) level.transition(0, -1);
		if (y > 240 - w + 10 - 5) level.transition(0, 1);
		if (x < 0 + 5) level.transition(-1, 0);
		if (x > 320 - h + 10 - 5) level.transition(1, 0);
	}

	@Override
	public void hitSpikes () {
		die();
	}

	public void die () {
		if (removed) return;
		if (hatCount > 0) {
			level.add(new Hat(x, y - hatCount * 2));
			hatCount--;
		}
		Sound.death.play();
		for (int i = 0; i < 16; i++) {
			level.add(new PlayerGore(x + random.nextDouble() * w, y + random.nextDouble() * h));
		}
		Stats.instance.deaths++;
		remove();
	}

	@Override
	public boolean shot (Bullet bullet) {
		Sound.pew.play();
		xa += bullet.xa * 0.5;
		ya += bullet.ya * 0.5;
		for (int i = 0; i < 4; i++) {
			double xd = (random.nextDouble() - random.nextDouble()) * 4 - bullet.xa * 3;
			double yd = (random.nextDouble() - random.nextDouble()) * 4 - bullet.ya * 3;
			level.add(new Gore(bullet.x, bullet.y, xa + xd, ya + yd));
		}
		if (noHurtTime != 0) return true;

		if (hatCount > 0) {
			while (hatCount > 0) {
				Hat hat = new Hat(x, y);
				hat.ya -= hatCount * 0.05;
				hat.xxa = (random.nextFloat() - random.nextFloat()) * (hatCount - 1) * 0.5;
				hat.time += hatCount * 6;
				level.add(hat);
				hat.tryMove(0, -hatCount * 2);
				hatCount--;
			}
			noHurtTime = 20;
		} else {
			Sound.oof.play();
			damage++;
			noHurtTime = 20;
			if (damage == 4) {
				Sound.death.play();
				for (int i = 0; i < 16; i++) {
					level.add(new PlayerGore(x + random.nextDouble() * w, y + random.nextDouble() * h));
				}
				remove();
			} else {
				level.add(new PlayerGore(bullet.x, bullet.y));
			}
		}

		return true;
	}

	public void readSign (Sign sign) {
		if (sign.autoRead || readSign) {
			sign.autoRead = false;
			if (sign.id == 6) {
				sign.remove();
				gunLevel = 1;
				level.getGun(1);
			}
			if (sign.id == 15) {
				sign.remove();
				gunLevel = 2;
				level.getGun(2);
			}
			level.readSign(sign);
		}
		sign.remove();
	}

	@Override
	public void outOfBounds () {
	}

	@Override
	public void explode (Explosion explosion) {
		die();
	}

	@Override
	public void collideMonster (Entity e) {
		die();
	}
}

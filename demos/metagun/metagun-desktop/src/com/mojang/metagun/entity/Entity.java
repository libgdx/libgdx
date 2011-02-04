package com.mojang.metagun.entity;

import java.util.Random;

import com.mojang.metagun.level.Camera;
import com.mojang.metagun.level.Level;
import com.mojang.metagun.screen.Screen;

public abstract class Entity {
    protected boolean onGround = false;
    protected static Random random = new Random();

    public double xa, ya;
    public double x, y;
    protected double bounce = 0.05;
    public int w = 10, h = 10;

    protected Level level;
    public boolean removed = false;
    public int xSlot, ySlot;

    public boolean interactsWithWorld = false;

    public void init(Level level) {
        this.level = level;
    }

    public void tryMove(double xa, double ya) {
        onGround = false;
        if (level.isFree(this, x + xa, y, w, h, xa, 0)) {
            x += xa;
        } else {
            hitWall(xa, 0);
            if (xa < 0) {
                double xx = x / 10;
                xa = -(xx - ((int) xx)) * 10;
            } else {
                double xx = (x + w) / 10;
                xa = 10 - (xx - ((int) xx)) * 10;
            }
            if (level.isFree(this, x + xa, y, w, h, xa, 0)) {
                x += xa;
            }
            this.xa *= -bounce;
        }
        if (level.isFree(this, x, y + ya, w, h, 0, ya)) {
            y += ya;
        } else {
            if (ya > 0) onGround = true;
            hitWall(0, ya);
            if (ya < 0) {
                double yy = y / 10;
                ya = -(yy - ((int) yy)) * 10;
            } else {
                double yy = (y + h) / 10;
                ya = 10 - (yy - ((int) yy)) * 10;
            }
            if (level.isFree(this, x, y + ya, w, h, 0, ya)) {
                y += ya;
            }
            this.ya *= -bounce;
        }
    }

    protected void hitWall(double xa, double ya) {
    }

    public void remove() {
        removed = true;
    }

    public void tick() {
    }

    public abstract void render(Screen screen, Camera camera);    

    public boolean shot(Bullet bullet) {
        return false;
    }

    public void hitSpikes() {
    }

    public void shove(Gunner enemy) {
    }

    public void outOfBounds() {
        if (y < 0) return;
        remove();
    }

    public void explode(Explosion explosion) {
    }

    public void collideMonster(Entity e) {
    }
}

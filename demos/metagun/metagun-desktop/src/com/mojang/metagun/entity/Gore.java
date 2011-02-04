package com.mojang.metagun.entity;

import java.awt.*;

import com.mojang.metagun.Art;
import com.mojang.metagun.level.*;

public class Gore extends Entity {
    private int life;

    public Gore(double x, double y, double xa, double ya) {
        this.x = x;
        this.y = y;
        this.w = 1;
        this.h = 1;
        bounce = 0.2;
        this.xa = (xa + (random.nextDouble() - random.nextDouble()) * 1)*0.2;
        this.ya = (ya + (random.nextDouble() - random.nextDouble()) * 1)*0.2;

        life = random.nextInt(20) + 10;
    }

    public void tick() {
        if (life-- <= 0) remove();
        onGround = false;
        tryMove(xa, ya);

        xa *= 0.999;
        ya *= 0.999;
        ya += Level.GRAVITY*0.15;
    }

    protected void hitWall(double xa, double ya) {
        this.xa *= 0.4;
        this.ya *= 0.4;
    }

    public void render(Graphics g, Camera camera) {
        int xp = (int) x;
        int yp = (int) y;
        g.drawImage(Art.guys[7][1], xp, yp, null);
    }
}

package com.mojang.metagun.entity;

import java.awt.*;

import com.mojang.metagun.*;
import com.mojang.metagun.level.*;

public class Gremlin extends Entity {
    private static final int MAX_TEMPERATURE = 80 * 5;
    private int temperature = 0;
    public int jumpDelay = 0;
    private int power = 0;

    public Gremlin(int power, int x, int y) {
        this.power = power;
        this.x = x;
        this.y = y;
        w = 30;
        h = 30;
        bounce = 0;
    }

    public void tick() {
        if (temperature > 0) {
            temperature--;
            for (int i = 0; i < 1; i++) {
                if (random.nextInt(MAX_TEMPERATURE) <= temperature) {
                    double xd = (random.nextDouble() - random.nextDouble()) * 0.2;
                    double yd = (random.nextDouble() - random.nextDouble()) * 0.2;
                    level.add(new Spark(x + random.nextDouble() * w, y + random.nextDouble() * h, xa * 0.2 + xd, ya * 0.2 + yd));
                }
            }
        }
        tryMove(xa, ya);
        xa *= 0.4;
        ya *= Level.FRICTION;
        ya += Level.GRAVITY;

        if (onGround) {
            if (power==1 && jumpDelay <= 19) {
                if (jumpDelay % 2 == 0) {
                    if (jumpDelay % 4 == 0) {
                        Sound.hit.play();
                    }
                    double dir = jumpDelay / 32.0f * Math.PI * 2+0.1;
                    double xa = Math.cos(dir);
                    double ya = -Math.sin(dir);
                    level.add(new Bullet(this, x + 15, y + 10, xa, ya));
                }
            }
            if (jumpDelay++ > 60) {
                ya = -2;
                jumpDelay = 0;
            }
        }

        java.util.List<Entity> entities = level.getEntities((int) x+4, (int) y+4, w-8, h-4);
        for (int i = 0; i < entities.size(); i++) {
            entities.get(i).collideMonster(this);
        }
    }

    public void render(Graphics g, Camera camera) {
        int xp = (int) x;
        int yp = (int) y;
        if (onGround) {
            g.drawImage(Art.gremlins[0][power], xp, yp, null);
        } else {
            g.drawImage(Art.gremlins[ya > 0 ? 2 : 1][power], xp, yp, null);
        }
        g.setColor(Color.BLACK);
        g.fillRect(xp + 5, yp - 8, 20, 3);
        g.setColor(Color.RED);
        g.fillRect(xp + 5, yp - 8, 20 - (20 * temperature / MAX_TEMPERATURE), 2);
    }

    public void hitSpikes() {
        die();
    }

    private void die() {
        Sound.death.play();
        for (int i = 0; i < 16; i++) {
            level.add(new PlayerGore(x + random.nextDouble() * w, y + random.nextDouble() * h));
        }
        Sound.boom.play();
        for (int i = 0; i < 32; i++) {
            double dir = i * Math.PI * 2 / 8.0;
            double xa = Math.sin(dir);
            double ya = Math.cos(dir);
            double dist = ((i / 8) + 1);
            level.add(new Explosion(0, i * 3, x + w / 2 + xa * dist, y + h / 2 + ya * dist, xa, ya));
        }
        remove();
    }

    public boolean shot(Bullet bullet) {
        Sound.pew.play();
        for (int i = 0; i < 4; i++) {
            double xd = (random.nextDouble() - random.nextDouble()) * 4 - bullet.xa * 3;
            double yd = (random.nextDouble() - random.nextDouble()) * 4 - bullet.ya * 3;
            level.add(new Gore(bullet.x, bullet.y, xa + xd, ya + yd));
        }
        Sound.oof.play();
        temperature += 80;
        if (temperature >= MAX_TEMPERATURE) {
            die();
        } else {
            level.add(new PlayerGore(bullet.x, bullet.y));
        }

        return true;
    }

    public void explode(Explosion explosion) {
        die();
    }
}

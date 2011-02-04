package com.mojang.metagun.entity;

import java.awt.*;

import com.mojang.metagun.*;
import com.mojang.metagun.level.*;

public class Boss extends BossPart {
    private static final int MAX_TEMPERATURE = 80 * 5;
    private int temperature = 0;
    public int slamTime = 0;
    private double xo, yo;
    public int time = 0;

    public Boss(int x, int y) {
        this.x = x;
        this.y = y;
        w = 14;
        h = 14;
        bounce = 0;
    }

    public void tick() {
        if (dieIn > 0) {
            if (--dieIn == 0) die();
        }
        xa = x - xo;
        ya = y - yo;
        time++;
        if (time % 60 == 0) {
            for (int i = 0; i < 16; i++) {
                double xxa = Math.sin(i * Math.PI * 2 / 16);
                double yya = Math.cos(i * Math.PI * 2 / 16);
                level.add(new Gunner(x + xxa * 4, y + yya * 4, xa * 0.2 + xxa, ya * 0.2 + yya - 1));
            }
        } else if (time%60>20 && time%60<40 && time%4==0) {
            double xd = (level.player.x + level.player.w / 2) - (x + w / 2);
            double yd = (level.player.y + level.player.h / 2) - (y + h / 2);
            double dd = Math.sqrt(xd * xd + yd * yd);
            xd /= dd;
            yd /= dd;
            Sound.hit.play();
            level.add(new Bullet(this, x + 2, y + 2, xd, yd));
        }
        xo = x;
        yo = y;

        java.util.List<Entity> entities = level.getEntities((int) x + 4, (int) y + 4, w - 8, h - 4);
        for (int i = 0; i < entities.size(); i++) {
            entities.get(i).collideMonster(this);
        }
    }

    public void render(Graphics g, Camera camera) {
        int xp = (int) x - 2;
        int yp = (int) y - 2;
        g.drawImage(Art.gremlins[3][1], xp, yp, null);
        g.setColor(Color.BLACK);
        yp += 2;
        xp -= 7;
        g.fillRect(xp + 5, yp - 8, 20, 3);
        g.setColor(Color.RED);
        g.fillRect(xp + 5, yp - 8, 20 - (20 * temperature / MAX_TEMPERATURE), 2);
    }

    public void hitSpikes() {
    }

    private void die() {
        Sound.death.play();
        for (int i = 0; i < 32; i++) {
            level.add(new PlayerGore(x + random.nextDouble() * w, y + random.nextDouble() * h));
        }
        Sound.boom.play();
        for (int i = 0; i < 32; i++) {
            double dir = i * Math.PI * 2 / 8.0;
            double xa = Math.sin(dir);
            double ya = Math.cos(dir);
            double dist = ((i / 8) + 1);
            level.add(new Explosion(1, i * 3, x + w / 2 + xa * dist, y + h / 2 + ya * dist, xa, ya));
        }
        remove();
    }

    public boolean shot(Bullet bullet) {
        return true;
    }

    public void explode(Explosion explosion) {
        if (explosion.power > 0) {
            die();
        }
    }

}

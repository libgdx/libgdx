package com.mojang.metagun.screen;

import java.awt.Graphics;

import com.mojang.metagun.*;
import com.mojang.metagun.level.*;

public class LevelTransitionScreen extends Screen {
    private static final int TRANSITION_DURATION = 20;
    private Level level1;
    private Level level2;
    private int time = 0;
    private Screen parent;
    private int xa, ya;
    private int xLevel, yLevel;

    public LevelTransitionScreen(Screen parent, int xLevel, int yLevel, Level level1, Level level2, int xa, int ya) {
        this.level1 = level1;
        this.level2 = level2;
        this.xLevel = xLevel;
        this.yLevel = yLevel;
        this.parent = parent;
        this.xa = xa;
        this.ya = ya;
    }

    public void tick(Input input) {
        time++;
        if (time == TRANSITION_DURATION) {
            setScreen(parent);
        }
    }

    public void render(Graphics g) {
        Camera c = new Camera(320, 240);
        double pow = time / (double) TRANSITION_DURATION;
        
        g.drawImage(Art.bg, -xLevel*160-(int)(xa*160*pow), -yLevel*120-(int)(ya*120*pow), null);
        
        g.translate((int) (-xa * 310 * pow), (int) (-ya * 230 * pow));
        level1.render(g, c);
        g.translate(xa * 310, ya * 230);
        level2.render(g, c);
    }
}

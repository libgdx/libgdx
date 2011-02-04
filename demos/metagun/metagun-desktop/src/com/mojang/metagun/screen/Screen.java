package com.mojang.metagun.screen;

import java.awt.Graphics;
import java.util.Random;

import com.mojang.metagun.*;

public abstract class Screen {
    protected static Random random = new Random();
    private Metagun metagun;
    
    public void removed() {
    }

    public final void init(Metagun metagun) {
        this.metagun = metagun;
    }
    
    protected void setScreen(Screen screen) {
        metagun.setScreen(screen);
    }

    String[] chars = {
                      "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789",
                      ".,!?:;\"'+-=/\\< "
    };
            
    public void drawString(String string, Graphics g, int x, int y) {
    	// FIXME
//        string = string.toUpperCase();
//        for (int i=0; i<string.length(); i++) {
//            char ch = string.charAt(i);
//            for (int ys=0; ys<chars.length; ys++) {
//                int xs = chars[ys].indexOf(ch);
//                if (xs>=0) {
//                    g.drawImage(Art.guys[xs][ys+9], x+i*6, y, null);
//                }
//            }
//        }
    }

    public abstract void render();    

    public void tick(Input input) {
    }
}
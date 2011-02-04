package com.mojang.metagun.screen;

import java.awt.*;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mojang.metagun.*;

public class TitleScreen extends Screen {
    private int time = 0;    
    
    public void render() {
        int yOffs = 480 - time * 2;
        if (yOffs < 0) yOffs = 0;
        g.drawImage(Art.bg, 0, -yOffs / 2, null);
        g.drawImage(Art.titleScreen, 0, -yOffs, null);
        if (time > 240) {
            String msg = "PRESS X TO START";
            drawString(msg, g, 160 - msg.length() * 3, 140 - 3 - (int) (Math.abs(Math.sin(time * 0.1) * 10)));

        }
        if (time >=0) {
            String msg = "COPYRIGHT MOJANG 2010";
            drawString(msg, g, 2, 240-6-2);
        }
    }

    public void tick(Input input) {
        time++;
        if (time > 240) {
            if (input.buttons[Input.SHOOT] && !input.oldButtons[Input.SHOOT]) {
                Sound.startgame.play();
                setScreen(new GameScreen());
                input.releaseAllKeys();
            }
        }
        if (time > 60*10) {
            setScreen(new ExpositionScreen());
        }
    }
}

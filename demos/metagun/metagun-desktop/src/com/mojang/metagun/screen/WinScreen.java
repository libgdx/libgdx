package com.mojang.metagun.screen;

import java.awt.*;

import com.mojang.metagun.*;

public class WinScreen extends Screen {
    private int time = 0;
    
    public void render(Graphics g) {
        int w = Art.bg.getHeight();
        g.drawImage(Art.bg, 0, -(time*2%w), null);
        g.drawImage(Art.bg, 0, -(time*2%w)+w, null);

        int offs0 = 500-time*10;
        if (offs0<0) offs0=0;
        int offs1 = 1200-time*16;
        if (offs1<0) offs1=0;
        int yOffs = 600-time*5;
        if (yOffs<-120) yOffs = -120;
        if (yOffs>0) yOffs = 0;
        g.drawImage(Art.winScreen1, offs0, yOffs+30, null);
        g.drawImage(Art.winScreen2, -offs1, yOffs*2/3+30, null);
        
        int tt = time-(60*2+30);
        int yo = 130;
        int xo = 120-8*3;
        if (tt>=0) {
            drawString("       TIME: "+Stats.instance.getTimeString(), g, xo, yo+0*6);
            drawString("     DEATHS: "+Stats.instance.deaths, g, xo, yo+1*6);
            drawString("    FEDORAS: "+Stats.instance.hats+"/"+7, g, xo, yo+2*6);
            drawString("SHOTS FIRED: "+Stats.instance.shots, g, xo, yo+3*6);
            drawString("FINAL SCORE: "+timeScale(Stats.instance.getFinalScore(), tt-30*5), g, xo, yo+5*6);

            drawString(timeHideScale(Stats.instance.getSpeedScore(), tt-30*1), g, xo+20*6, yo+0*6);
            drawString(timeHideScale(Stats.instance.getDeathScore(), tt-30*2), g, xo+20*6, yo+1*6);
            drawString(timeHideScale(Stats.instance.getHatScore(), tt-30*3), g, xo+20*6, yo+2*6);
            drawString(timeHideScale(Stats.instance.getShotScore(), tt-30*4), g, xo+20*6, yo+3*6);
        }
        
        if (time>60*7 && (time/30%2==0)) {
            String msg = "PRESS X TO RESET THE GAME"; 
            drawString(msg, g, 160-msg.length()*3, yo+10*6);
        }
    }
    
    private String timeHideScale(int val, int time) {
        if (time<10) return "";
//        if (time>60+60) return "";
        if (time<0) time = 0;
        if (time>60) time = 60;
        return "+"+val*time/60;
    }

    private String timeScale(int val, int time) {
        if (time<0) time = 0;
        if (time>60) time = 60;
        return ""+val*time/60;
    }
    
    public void tick(Input input) {
        time++;
        if (time>60*7) {
            if (input.buttons[Input.SHOOT] && !input.oldButtons[Input.SHOOT]) {
                setScreen(new TitleScreen());
            }
        }
    }
}

package com.mojang.metagun.screen;

import java.awt.Graphics;
import java.io.*;
import java.util.*;

import com.mojang.metagun.*;

public class ExpositionScreen extends Screen {
    private int time = 0;

    //    "1234567890123456789012345678901234567890"

    private List<String> lines = new ArrayList<String>();

    public ExpositionScreen() {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(ExpositionScreen.class.getResourceAsStream("exposition.txt")));

            String line = "";
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void render(Graphics g) {
        int w = Art.bg.getHeight();
        g.drawImage(Art.bg, 0, -(time / 8 % w), null);
        g.drawImage(Art.bg, 0, -(time / 8 % w) + w, null);

        int yo = time / 4;
        for (int y = 0; y <= 240 / 6; y++) {
            int yl = yo / 6 - 240 / 6+y;
            if (yl >= 0 && yl < lines.size()) {
                drawString(lines.get(yl), g, (320 - 40 * 6)/2, y * 6 - yo % 6);
            }
        }
    }

    public void tick(Input input) {
        time++;
        if (time / 4 > lines.size() * 6 + 250) {
            setScreen(new TitleScreen());
        }
        if (input.buttons[Input.SHOOT] && !input.oldButtons[Input.SHOOT]) {
            setScreen(new TitleScreen());
        }
        if (input.buttons[Input.ESCAPE] && !input.oldButtons[Input.ESCAPE]) {
            setScreen(new TitleScreen());
        }
    }
}

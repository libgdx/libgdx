package com.mojang.metagun;

import java.awt.*;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Art {
//    public static BufferedImage[][] guys = split(load("/guys.png"), 6, 6);
//    public static BufferedImage[][] player1 = split(load("/player.png"), 16, 32);
//    public static BufferedImage[][] player2 = mirrorsplit(load("/player.png"), 16, 32);
//    public static BufferedImage[][] walls = split(load("/walls.png"), 10, 10);
//    public static BufferedImage[][] gremlins = split(load("/gremlins.png"), 30, 30);
    public static TextureRegion bg = scale(load("/background.png"), 8);
//    public static TextureRegion level = load("/levels.png");
    public static TextureRegion titleScreen = load("/titlescreen.png");
//    public static BufferedImage winScreen1 = load("/winscreen1.png");;
//    public static BufferedImage winScreen2 = load("/winscreen2.png");;

    public static BufferedImage load(String name) {
        try {
            BufferedImage org = ImageIO.read(Art.class.getResource(name));
            BufferedImage res = new BufferedImage(org.getWidth(), org.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics g = res.getGraphics();
            g.drawImage(org, 0, 0, null, null);
            g.dispose();
            return res;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static BufferedImage scale(BufferedImage src, int scale) {
        int w = src.getWidth() * scale;
        int h = src.getHeight() * scale;
        BufferedImage res = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics g = res.getGraphics();
        g.drawImage(src.getScaledInstance(w, h, Image.SCALE_AREA_AVERAGING), 0, 0, null);
        g.dispose();
        return res;
    }

    private static BufferedImage[][] mirrorsplit(BufferedImage src, int xs, int ys) {
        int xSlices = src.getWidth() / xs;
        int ySlices = src.getHeight() / ys;
        BufferedImage[][] res = new BufferedImage[xSlices][ySlices];
        for (int x = 0; x < xSlices; x++) {
            for (int y = 0; y < ySlices; y++) {
                res[x][y] = new BufferedImage(xs, ys, BufferedImage.TYPE_INT_ARGB);
                Graphics g = res[x][y].getGraphics();
                g.drawImage(src, xs, 0, 0, ys, x * xs, y * ys, (x + 1) * xs, (y + 1) * ys, null);
                g.dispose();
            }
        }
        return res;
    }

    private static BufferedImage[][] split(BufferedImage src, int xs, int ys) {
        int xSlices = src.getWidth() / xs;
        int ySlices = src.getHeight() / ys;
        BufferedImage[][] res = new BufferedImage[xSlices][ySlices];
        for (int x = 0; x < xSlices; x++) {
            for (int y = 0; y < ySlices; y++) {
                res[x][y] = new BufferedImage(xs, ys, BufferedImage.TYPE_INT_ARGB);
                Graphics g = res[x][y].getGraphics();
                g.drawImage(src, -x * xs, -y * ys, null);
                g.dispose();
            }
        }
        return res;
    }
}

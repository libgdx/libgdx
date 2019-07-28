package com.badlogic.gdx.tools;

import java.awt.AlphaComposite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class AlphaTest extends JPanel {

    JFrame        frame = new JFrame();

    BufferedImage image;
    BufferedImage newImage, newImage2;

    public static void main(String[] args) {
        new AlphaTest().start();
    }
    public void start() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(this);
        setPreferredSize(new Dimension(800, 500));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        image = new BufferedImage(800, 250, BufferedImage.TYPE_INT_ARGB);

        int[] color = { 0, 0, 255, 0
        };

        WritableRaster raster = image.getRaster();

        for (int y = 0; y < 250; y++) {
            // alternate alpha values per line
            if (y % 2 == 0) {
                color[3] = 0;
            }
            else {
                color[3] = 255;
            }
            for (int x = 0; x < 800; x++) {
                raster.setPixel(x, y, color);
            }
        }
        image.setData(raster);
        newImage = new BufferedImage(image.getWidth(), image.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) newImage.getGraphics();
        g.setComposite(AlphaComposite.Src);
        g.drawImage(image, 0, 0, null);
        System.out.println(Integer.toHexString(newImage.getRGB(0,0) ));
        System.out.println(Integer.toHexString(newImage.getRGB(0,1) ));
        repaint();

    }
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (newImage != null) {
            g.drawImage(image, 0, 0, null);
            g.drawImage(newImage, 0, 251, null);
        }
    }
}
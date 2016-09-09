package com.badlogic.gdx.tools.flame;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

/** @author Inferno */
public class ImagePanel extends JPanel{
	private BufferedImage image;

	public ImagePanel() {}

	public void setImage(BufferedImage image){
		this.image = image;
	}
	
	public void setImage (String file) {
		try {
			image = ImageIO.read( new File(file) );
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(image, 0, 0, null); // see javadoc for more info on the parameters            
	}

	@Override
	public Dimension getPreferredSize () {
		Dimension dimension = super.getPreferredSize();
		if(image != null){
			dimension.width = image.getWidth();
			dimension.height = image.getHeight();
		}
		return dimension;
	}
}
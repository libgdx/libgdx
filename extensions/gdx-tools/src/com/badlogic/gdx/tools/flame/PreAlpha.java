/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.badlogic.gdx.tools.flame;

import java.awt.EventQueue;
import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

/** @author Inferno */
public class PreAlpha extends JFrame {
	BufferedImage image;
	ImagePanel imagePanel;
	String lastDir;
	
	public PreAlpha () {
		super("Premultiply alpha converter");
		addWindowListener(new WindowAdapter() {
			public void windowClosed (WindowEvent event) {
				System.exit(0);
			}
		});

		initializeComponents();
		pack();
		setLocationRelativeTo(null);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setVisible(true);
	}
        
	private void initializeComponents () {
		//Create the menu bar.
		JMenuBar menuBar = new JMenuBar();

		//Build the first menu.
		JMenu menu = new JMenu("File");
		menuBar.add(menu);

		//a group of JMenuItems
		JMenuItem menuItem = new JMenuItem("Open");
		menuItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed (ActionEvent arg0) {
				open();
			}
		});
		menu.add(menuItem);
		
		menuItem = new JMenuItem("Save");
		menuItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed (ActionEvent arg0) {
				save();
			}
		});
		menu.add(menuItem);
		setJMenuBar(menuBar);
		
		imagePanel = new ImagePanel();
		getContentPane().add(imagePanel);
	}

	protected void save () {
		FileDialog dialog = new FileDialog(this, "Save Image", FileDialog.SAVE);
		if (lastDir != null) dialog.setDirectory(lastDir);
		dialog.setVisible(true);
		final String file = dialog.getFile();
		final String dir = dialog.getDirectory();
		if (dir == null || file == null || file.trim().length() == 0) return;
		lastDir = dir;
		try {
			generatePremultiplyAlpha(new File(dir, file));
			JOptionPane.showMessageDialog(this, "Conversion complete!");
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, "Error saving image.");
			return;
		}
	}

	protected void open () {
		FileDialog dialog = new FileDialog(this, "Open Image", FileDialog.LOAD);
		if (lastDir != null) dialog.setDirectory(lastDir);
		dialog.setVisible(true);
		final String file = dialog.getFile();
		final String dir = dialog.getDirectory();
		if (dir == null || file == null || file.trim().length() == 0) return;
		lastDir = dir;
		try {
			image = ImageIO.read(new File(dir, file));
			imagePanel.setImage(image);
			imagePanel.revalidate();
			imagePanel.repaint();
			pack();
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, "Error opening image.");
			return;
		}
	}
	
	private void generatePremultiplyAlpha(File out){
		try {
			BufferedImage outImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
			float[] color = new float[4];
			WritableRaster raster = image.getRaster();
			WritableRaster outRaster = outImage.getRaster();
			for(int x =0, w = image.getWidth(); x< w; ++x)
				for(int y =0, h = image.getHeight(); y< h; ++y){
					raster.getPixel(x, y, color);
					float alpha = color[3]/255f;
					for(int i=0;i < 3; ++i) 
						color[i] *= alpha;
					outRaster.setPixel(x, y, color);
				}
			ImageIO.write(outImage, "png", out);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

	public static void main (String[] args) {
		for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
			if ("Nimbus".equals(info.getName())) {
				try {
					UIManager.setLookAndFeel(info.getClassName());
				} catch (Throwable ignored) {
				}
				break;
			}
		}
		EventQueue.invokeLater(new Runnable() {
			public void run () {
				new PreAlpha();
			}
		});
	}
}

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

package com.badlogic.gdx.tools.particleeditor;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

class PreviewImagePanel extends EditorPanel {

	ParticleEditor editor;

	DefaultListModel<String> imageListModel;
	String lastDir;

	JPanel previewContainer;

	Slider valueX;
	Slider valueY;

	Slider valueWidth;
	Slider valueHeight;

	public PreviewImagePanel (final ParticleEditor editor, String name, String description) {
		super(null, name, description);

		this.editor = editor;

		JButton addButton = new JButton("Select preview");
		JButton removeButton = new JButton("Remove preview");

		previewContainer = new JPanel(new GridLayout(1, 1));

		addButton.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent event) {
				FileDialog dialog = new FileDialog(editor, "Select Image", FileDialog.LOAD);
				if (lastDir != null) dialog.setDirectory(lastDir);
				dialog.setVisible(true);
				final String file = dialog.getFile();
				final String dir = dialog.getDirectory();
				if (dir == null || file == null || file.trim().length() == 0) return;
				lastDir = dir;

				try {
					final FileHandle absolute = Gdx.files.absolute(dir + file);
					final BufferedImage read = ImageIO.read(absolute.read());
					final Image scaledInstance = read.getScaledInstance(100, -1, Image.SCALE_SMOOTH);
					final ImageIcon image = new ImageIcon(scaledInstance);

					JLabel previewImage = new JLabel(image);
					previewImage.setOpaque(true);
					previewImage.setBackground(Color.MAGENTA);

					buildImagePanel(previewImage, absolute.file());
				} catch (IOException e) {
					e.printStackTrace();
				}

			}

			private void buildImagePanel (JLabel previewImage, File file) {
				previewContainer.removeAll();
				previewContainer.add(previewImage, new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
				previewContainer.updateUI();
				PreviewImagePanel.this.editor.renderer.setImageBackground(file);
			}
		});

		removeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed (ActionEvent e) {
				clearImagePanel();
			}

			private void clearImagePanel () {
				previewContainer.removeAll();
				previewContainer.updateUI();
				PreviewImagePanel.this.editor.renderer.setImageBackground(null);
			}
		});

		JPanel buttonPanel = new JPanel(new GridLayout());
		buttonPanel.add(addButton);
		buttonPanel.add(removeButton);

		getContentPanel().add(buttonPanel, new GridBagConstraints(0, 0, 4, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.NONE,
			new Insets(5, 5, 5, 5), 0, 0));

		initializeComponents();

		getContentPanel().add(previewContainer, new GridBagConstraints(0, 4, 2, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE,
			new Insets(10, 10, 10, 10), 0, 0));
	}

	private void initializeComponents () {
		JPanel contentPanel = getContentPanel();
		{
			JLabel label = new JLabel("X:");
			contentPanel.add(label, new GridBagConstraints(0, 1, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 6), 0, 0));
		}
		{
			valueX = new Slider(0, 0, 99999, 1, 0, 500);
			contentPanel.add(valueX, new GridBagConstraints(1, 1, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		}
		{
			JLabel label = new JLabel("Y:");
			contentPanel.add(label, new GridBagConstraints(2, 1, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(0, 12, 0, 6), 0, 0));
		}
		{
			valueY = new Slider(0, 0, 99999, 1, 0, 500);
			contentPanel.add(valueY, new GridBagConstraints(3, 1, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		}
		{
			JLabel label = new JLabel("Width:");
			contentPanel.add(label, new GridBagConstraints(0, 2, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 6), 0, 0));
		}
		{
			valueWidth = new Slider(0, 0, 99999, 1, 0, 500);
			contentPanel.add(valueWidth, new GridBagConstraints(1, 2, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		}
		{
			JLabel label = new JLabel("Height:");
			contentPanel.add(label, new GridBagConstraints(2, 2, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(0, 12, 0, 6), 0, 0));
		}
		{
			valueHeight = new Slider(0, 0, 99999, 1, 0, 500);
			contentPanel.add(valueHeight, new GridBagConstraints(3, 2, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		}

		float x = 0;
		float y = 0;
		float w = 64;
		float h = 64;

		if (editor.renderer.bgImage != null) {
			x = editor.renderer.bgImage.getX();
			y = editor.renderer.bgImage.getY();
			w = editor.renderer.bgImage.getWidth();
			h = editor.renderer.bgImage.getHeight();
		}

		valueX.setValue(x);
		valueY.setValue(y);
		valueWidth.setValue(w);
		valueHeight.setValue(h);
	}

	public void updateSpritePosition () {
		editor.renderer.updateImageBackgroundPosSize(valueX.getValue(), valueY.getValue(), valueWidth.getValue(), valueHeight.getValue());
	}

}

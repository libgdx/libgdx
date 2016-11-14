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

import java.awt.FileDialog;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.badlogic.gdx.graphics.g2d.ParticleEmitter;

class ImagePanel extends EditorPanel {
	JLabel imageLabel;
	JLabel widthLabel;
	JLabel heightLabel;
	String lastDir;

	public ImagePanel (final ParticleEditor editor, String name, String description) {
		super(null, name, description);
		JPanel contentPanel = getContentPanel();
		{
			JButton openButton = new JButton("Open");
			contentPanel.add(openButton, new GridBagConstraints(2, 1, 1, 1, 0, 0, GridBagConstraints.NORTHWEST,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 6), 0, 0));
			openButton.addActionListener(new ActionListener() {
				public void actionPerformed (ActionEvent event) {
					FileDialog dialog = new FileDialog(editor, "Open Image", FileDialog.LOAD);
					if (lastDir != null) dialog.setDirectory(lastDir);
					dialog.setVisible(true);
					final String file = dialog.getFile();
					final String dir = dialog.getDirectory();
					if (dir == null || file == null || file.trim().length() == 0) return;
					lastDir = dir;
					try {
						ImageIcon icon = new ImageIcon(new File(dir, file).toURI().toURL());
						final ParticleEmitter emitter = editor.getEmitter();
						editor.setIcon(emitter, icon);
						updateIconInfo(icon);
						emitter.setImagePath(new File(dir, file).getAbsolutePath());
						emitter.setSprite(null);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			});
		}
		{
			JButton defaultButton = new JButton("Default");
			contentPanel.add(defaultButton, new GridBagConstraints(2, 2, 1, 1, 0, 0, GridBagConstraints.NORTHWEST,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 6), 0, 0));
			defaultButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed (ActionEvent e) {
					final ParticleEmitter emitter = editor.getEmitter();
					emitter.setImagePath(ParticleEditor.DEFAULT_PARTICLE);
					emitter.setSprite(null);

					editor.setIcon(emitter, null);
					updateIconInfo(null);
				}
			});
			JButton defaultPremultButton = new JButton("Default (Premultiplied Alpha)");
			contentPanel.add(defaultPremultButton, new GridBagConstraints(3, 2, 1, 1, 0, 0, GridBagConstraints.NORTHWEST,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 6), 0, 0));
			defaultPremultButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed (ActionEvent e) {
					final ParticleEmitter emitter = editor.getEmitter();
					emitter.setImagePath(ParticleEditor.DEFAULT_PREMULT_PARTICLE);
					emitter.setSprite(null);
					editor.setIcon(emitter, null);
					updateIconInfo(null);
				}
			});
		}
		{
			widthLabel = new JLabel();
			contentPanel.add(widthLabel, new GridBagConstraints(2, 3, 1, 1, 0, 0, GridBagConstraints.NORTHWEST,
				GridBagConstraints.NONE, new Insets(0, 0, 6, 6), 0, 0));
		}
		{
			heightLabel = new JLabel();
			contentPanel.add(heightLabel, new GridBagConstraints(2, 4, 1, 1, 0, 1, GridBagConstraints.NORTHWEST,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 6), 0, 0));
		}
		{
			imageLabel = new JLabel();
			contentPanel.add(imageLabel, new GridBagConstraints(3, 1, 1, 3, 1, 0, GridBagConstraints.NORTHWEST,
				GridBagConstraints.NONE, new Insets(0, 6, 0, 0), 0, 0));
		}
		updateIconInfo(editor.getIcon(editor.getEmitter()));
	}

	public void updateIconInfo (ImageIcon icon) {
		if (icon != null) {
			imageLabel.setIcon(icon);
			widthLabel.setText("Width: " + icon.getIconWidth());
			heightLabel.setText("Height: " + icon.getIconHeight());
		} else {
			imageLabel.setIcon(null);
			widthLabel.setText("");
			heightLabel.setText("");
		}
		revalidate();
	}
}

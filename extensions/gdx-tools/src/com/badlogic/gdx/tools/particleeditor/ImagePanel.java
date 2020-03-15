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
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.ListSelectionModel;

import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter.SpriteMode;
import com.badlogic.gdx.utils.Array;

class ImagePanel extends EditorPanel {
	JPanel imagesPanel;
	JList imageList;
	DefaultListModel<String> imageListModel;
	String lastDir;

	public ImagePanel (final ParticleEditor editor, String name, String description) {
		super(null, name, description);
		JPanel contentPanel = getContentPanel();
		{
			JPanel buttonsPanel = new JPanel(new GridLayout(3, 1));
			contentPanel.add(buttonsPanel, new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.NORTHWEST,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
			JButton addButton = new JButton("Add");
			buttonsPanel.add(addButton);
			addButton.addActionListener(new ActionListener() {
				public void actionPerformed (ActionEvent event) {
					FileDialog dialog = new FileDialog(editor, "Open Image", FileDialog.LOAD);
					if (lastDir != null) dialog.setDirectory(lastDir);
					dialog.setMultipleMode(true);
					dialog.setVisible(true);
					final File[] files = dialog.getFiles();
					final String dir = dialog.getDirectory();
					if (dir == null || files == null) return;
					lastDir = dir;
					final ParticleEmitter emitter = editor.getEmitter();
					for (File file : files) {
						emitter.getImagePaths().add(file.getAbsolutePath());
					}
					emitter.getSprites().clear();
					updateImageList(emitter.getImagePaths());
				}
			});
			JButton defaultButton = new JButton("Default");
			buttonsPanel.add(defaultButton);
			defaultButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed (ActionEvent e) {
					final ParticleEmitter emitter = editor.getEmitter();
					emitter.setImagePaths(new Array<String>(new String[] { ParticleEditor.DEFAULT_PARTICLE }));
					emitter.getSprites().clear();
					updateImageList(emitter.getImagePaths());
				}
			});
			JButton defaultPremultButton = new JButton("Default (Premultiplied Alpha)");
			buttonsPanel.add(defaultPremultButton);
			defaultPremultButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed (ActionEvent e) {
					final ParticleEmitter emitter = editor.getEmitter();
					emitter.setImagePaths(new Array<String>(new String[] { ParticleEditor.DEFAULT_PREMULT_PARTICLE }));
					emitter.getSprites().clear();
					updateImageList(emitter.getImagePaths());
				}
			});
		}
		{
			JPanel modesPanel = new JPanel(new GridLayout(4, 1));
			contentPanel.add(modesPanel, new GridBagConstraints(1, 0, 1, 1, 1, 0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
			JLabel label = new JLabel("Sprite mode:");
			modesPanel.add(label);
			ButtonGroup checkboxGroup = new ButtonGroup();
			JRadioButton singleCheckbox = new JRadioButton("Single", editor.getEmitter().getSpriteMode() == SpriteMode.single);
			modesPanel.add(singleCheckbox);
			checkboxGroup.add(singleCheckbox);
			singleCheckbox.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged (ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						editor.getEmitter().setSpriteMode(SpriteMode.single);
					}
				}
			});
			JRadioButton randomCheckbox = new JRadioButton("Random", editor.getEmitter().getSpriteMode() == SpriteMode.random);
			modesPanel.add(randomCheckbox);
			checkboxGroup.add(randomCheckbox);
			randomCheckbox.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged (ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						editor.getEmitter().setSpriteMode(SpriteMode.random);
					}
				}
			});
			JRadioButton animatedCheckbox = new JRadioButton("Animated", editor.getEmitter().getSpriteMode() == SpriteMode.animated);
			modesPanel.add(animatedCheckbox);
			checkboxGroup.add(animatedCheckbox);
			animatedCheckbox.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged (ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						editor.getEmitter().setSpriteMode(SpriteMode.animated);
					}
				}
			});
		}
		{
			imagesPanel = new JPanel(new GridBagLayout());
			contentPanel.add(imagesPanel, new GridBagConstraints(2, 0, 1, 1, 1, 0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
			imageListModel = new DefaultListModel<String>();
			imageList = new JList<String>(imageListModel);
			imageList.setFixedCellWidth(250);
			imageList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			imagesPanel.add(imageList, new GridBagConstraints(0, 0, 1, 3, 0, 0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
			JButton upButton = new JButton("\u2191");
			imagesPanel.add(upButton, new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
			upButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed (ActionEvent e) {
					int index = imageList.getSelectedIndex();
					if (index <= 0) return;
					final ParticleEmitter emitter = editor.getEmitter();
					String imagePath = emitter.getImagePaths().removeIndex(index);
					emitter.getImagePaths().insert(index - 1, imagePath);
					emitter.getSprites().clear();
					updateImageList(emitter.getImagePaths());
					imageList.setSelectedIndex(index - 1);
				}
			});
			JButton downButton = new JButton("\u2193");
			imagesPanel.add(downButton, new GridBagConstraints(1, 1, 1, 1, 0, 0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
			downButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed (ActionEvent e) {
					int index = imageList.getSelectedIndex();
					if (index < 0 || index >= imageList.getModel().getSize() - 1) return;
					final ParticleEmitter emitter = editor.getEmitter();
					String imagePath = emitter.getImagePaths().removeIndex(index);
					emitter.getImagePaths().insert(index + 1, imagePath);
					emitter.getSprites().clear();
					updateImageList(emitter.getImagePaths());
					imageList.setSelectedIndex(index + 1);
				}
			});
			JButton removeButton = new JButton("X");
			imagesPanel.add(removeButton, new GridBagConstraints(1, 2, 1, 1, 0, 0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
			removeButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed (ActionEvent e) {
					int index = imageList.getSelectedIndex();
					if (index < 0) return;
					final ParticleEmitter emitter = editor.getEmitter();
					Array<String> imagePaths = emitter.getImagePaths();
					imagePaths.removeIndex(index);
					if (imagePaths.size == 0) imagePaths.add(ParticleEditor.DEFAULT_PARTICLE);
					emitter.getSprites().clear();
					updateImageList(imagePaths);
				}
			});
		}
		updateImageList(editor.getEmitter().getImagePaths());
	}

	public void updateImageList (Array<String> imagePaths) {
		if (imagePaths != null && imagePaths.size > 0) {
			imagesPanel.setVisible(true);
			imageListModel.removeAllElements();
			for (String imagePath : imagePaths) {
				imageListModel.addElement(new File(imagePath).getName());
			}
		} else {
			imagesPanel.setVisible(false);
		}
		revalidate();
	}
}

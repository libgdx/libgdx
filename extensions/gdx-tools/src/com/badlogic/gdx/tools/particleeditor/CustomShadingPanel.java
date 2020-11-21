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
import java.io.File;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;

import com.badlogic.gdx.graphics.g2d.ParticleEmitter;

public class CustomShadingPanel extends EditorPanel {
	JPanel imagesPanel;
	JList imageList;
	DefaultListModel<String> imageListModel;
	String lastDir;
	final ParticleEditor editor;
	final CustomShading shading;

	public CustomShadingPanel (final ParticleEditor editor, String name, String description) {
		super(null, name, description);
		this.editor = editor;
		this.shading = editor.renderer.customShading;

		JPanel contentPanel = getContentPanel();

		{
			JPanel shaderStagePanel = createShaderStagePanel(editor, contentPanel, true);
			contentPanel.add(shaderStagePanel, new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.NORTHWEST,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		}

		{
			JPanel shaderStagePanel = createShaderStagePanel(editor, contentPanel, false);
			contentPanel.add(shaderStagePanel, new GridBagConstraints(1, 0, 1, 1, 1, 0, GridBagConstraints.NORTHWEST,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		}

		{
			imagesPanel = new JPanel(new GridBagLayout());
			contentPanel.add(imagesPanel, new GridBagConstraints(2, 0, 1, 1, 1, 0, GridBagConstraints.NORTHWEST,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

			imagesPanel.add(new JLabel("Extra Texture Units"));

			imageListModel = new DefaultListModel<String>();
			for (int i = 0; i < shading.extraTexturePaths.size; i++) {
				String path = shading.extraTexturePaths.get(i);
				imageListModel.addElement(textureDisplayName(i, path));
			}

			imageList = new JList<String>(imageListModel);
			imageList.setFixedCellWidth(200);
			imageList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			imagesPanel.add(imageList, new GridBagConstraints(0, 1, 1, 4, 0, 0, GridBagConstraints.NORTHWEST,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

			JButton addButton = new JButton("Add");
			imagesPanel.add(addButton, new GridBagConstraints(1, 1, 1, 1, 0, 0, GridBagConstraints.NORTHWEST,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
			addButton.addActionListener(new ActionListener() {
				public void actionPerformed (ActionEvent event) {
					FileDialog dialog = new FileDialog(editor, "Open Image", FileDialog.LOAD);
					if (lastDir != null) dialog.setDirectory(lastDir);
					dialog.setVisible(true);
					final String file = dialog.getFile();
					final String dir = dialog.getDirectory();
					if (dir == null || file == null || file.trim().length() == 0) return;
					lastDir = dir;
					addTexture(new File(dir, file).getAbsolutePath());
				}
			});

			JButton upButton = new JButton("\u2191");
			imagesPanel.add(upButton, new GridBagConstraints(1, 2, 1, 1, 0, 0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
			upButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed (ActionEvent e) {
					int index = imageList.getSelectedIndex();
					if (index <= 0) return;
					swapTexture(index, index - 1);
					imageList.setSelectedIndex(index - 1);
				}
			});
			JButton downButton = new JButton("\u2193");
			imagesPanel.add(downButton, new GridBagConstraints(1, 3, 1, 1, 0, 0, GridBagConstraints.NORTHWEST,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
			downButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed (ActionEvent e) {
					int index = imageList.getSelectedIndex();
					if (index < 0 || index >= imageList.getModel().getSize() - 1) return;
					final ParticleEmitter emitter = editor.getEmitter();
					swapTexture(index, index + 1);
					imageList.setSelectedIndex(index + 1);
				}
			});
			JButton removeButton = new JButton("X");
			imagesPanel.add(removeButton, new GridBagConstraints(1, 4, 1, 1, 0, 0, GridBagConstraints.NORTHWEST,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
			removeButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed (ActionEvent e) {
					int index = imageList.getSelectedIndex();
					if (index < 0) return;
					removeTexture(index);
				}
			});
			JButton reloadButton = new JButton("Reload");
			imagesPanel.add(reloadButton, new GridBagConstraints(1, 5, 1, 1, 0, 0, GridBagConstraints.NORTHWEST,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
			reloadButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed (ActionEvent e) {
					int index = imageList.getSelectedIndex();
					if (index < 0) return;
					reloadTexture(index);
				}
			});
		}
	}

	private JPanel createShaderStagePanel (final ParticleEditor editor, JPanel contentPanel, final boolean isVertexShader) {
		JPanel buttonsPanel = new JPanel(new GridLayout(5, 1));

		JLabel label = new JLabel(isVertexShader ? "Vertex Shader" : "Frag. Shader");
		buttonsPanel.add(label);

		JButton defaultButton = new JButton("Default");
		buttonsPanel.add(defaultButton);
		defaultButton.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent event) {
				if (isVertexShader) {
					shading.setVertexShaderFile(null);
				} else {
					shading.setFragmentShaderFile(null);
				}
				displayErrors();
			}
		});

		JButton setButton = new JButton("Set");
		buttonsPanel.add(setButton);
		setButton.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent event) {
				FileDialog dialog = new FileDialog(editor, isVertexShader ? "Open Vertex Shader File" : "Open Fragment Shader File",
					FileDialog.LOAD);
				if (lastDir != null) dialog.setDirectory(lastDir);
				dialog.setVisible(true);
				final String file = dialog.getFile();
				final String dir = dialog.getDirectory();
				if (dir == null || file == null || file.trim().length() == 0) return;
				lastDir = dir;

				String path = new File(dir, file).getAbsolutePath();

				if (isVertexShader) {
					shading.setVertexShaderFile(path);
				} else {
					shading.setFragmentShaderFile(path);
				}

				displayErrors();
			}
		});

		JButton reloadButton = new JButton("Reload");
		buttonsPanel.add(reloadButton);
		reloadButton.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent event) {
				if (isVertexShader) {
					shading.reloadVertexShader();
				} else {
					shading.reloadFragmentShader();
				}
				displayErrors();
			}
		});

		JButton showButton = new JButton("Show");
		buttonsPanel.add(showButton);
		showButton.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent event) {
				JTextArea text = new JTextArea(isVertexShader ? shading.vertexShaderCode : shading.fragmentShaderCode);
				text.setEditable(false);
				JOptionPane.showMessageDialog(editor, text,
					isVertexShader ? "Current vertex shader code" : "Current fragment shader code", JOptionPane.INFORMATION_MESSAGE);
			}
		});

		return buttonsPanel;
	}

	protected void displayErrors () {
		if (shading.hasShaderErrors) {
			JOptionPane.showMessageDialog(editor, shading.shaderErrorMessage, "Shader Error", JOptionPane.ERROR_MESSAGE);
		} else if (shading.hasMissingSamplers) {
			JOptionPane.showMessageDialog(editor, shading.missingSamplerMessage, "Missing texture sampler",
				JOptionPane.WARNING_MESSAGE);
		}
	}

	private String textureDisplayName (int index, String path) {
		int unit = index + 1;
		return "u_texture" + unit + ": " + new File(path).getName();
	}

	protected void removeTexture (int index) {
		imageListModel.remove(index);
		shading.removeTexture(index);
		revalidate();
		displayErrors();
	}

	protected void swapTexture (int indexA, int indexB) {
		shading.swapTexture(indexA, indexB);
		String pathA = shading.extraTexturePaths.get(indexA);
		String pathB = shading.extraTexturePaths.get(indexB);
		imageListModel.set(indexA, textureDisplayName(indexA, pathA));
		imageListModel.set(indexB, textureDisplayName(indexB, pathB));
		revalidate();
		displayErrors();
	}

	protected void addTexture (String absolutePath) {
		imageListModel.addElement(textureDisplayName(imageListModel.getSize(), absolutePath));
		shading.addTexture(absolutePath);
		revalidate();
		displayErrors();
	}

	protected void reloadTexture (int index) {
		shading.reloadTexture(index);
		displayErrors();
	}

}

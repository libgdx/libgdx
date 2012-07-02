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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.badlogic.gdx.graphics.g2d.ParticleEmitter.SpawnEllipseSide;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter.SpawnShape;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter.SpawnShapeValue;

class SpawnPanel extends EditorPanel {
	JComboBox shapeCombo;
	JCheckBox edgesCheckbox;
	JLabel edgesLabel;
	JComboBox sideCombo;
	JLabel sideLabel;

	public SpawnPanel (final ParticleEditor editor, final SpawnShapeValue spawnShapeValue, String name, String description) {
		super(null, name, description);

		initializeComponents();

		edgesCheckbox.setSelected(spawnShapeValue.isEdges());
		sideCombo.setSelectedItem(spawnShapeValue.getShape());

		shapeCombo.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent event) {
				SpawnShape shape = (SpawnShape)shapeCombo.getSelectedItem();
				spawnShapeValue.setShape(shape);
				switch (shape) {
				case line:
				case square:
					setEdgesVisible(false);
					editor.setVisible("Spawn Width", true);
					editor.setVisible("Spawn Height", true);
					break;
				case ellipse:
					setEdgesVisible(true);
					editor.setVisible("Spawn Width", true);
					editor.setVisible("Spawn Height", true);
					break;
				case point:
					setEdgesVisible(false);
					editor.setVisible("Spawn Width", false);
					editor.setVisible("Spawn Height", false);
					break;
				}
			}
		});

		edgesCheckbox.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent event) {
				spawnShapeValue.setEdges(edgesCheckbox.isSelected());
				setEdgesVisible(true);
			}
		});

		sideCombo.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent event) {
				SpawnEllipseSide side = (SpawnEllipseSide)sideCombo.getSelectedItem();
				spawnShapeValue.setSide(side);
			}
		});

		shapeCombo.setSelectedItem(spawnShapeValue.getShape());
	}

	public void update (ParticleEditor editor) {
		shapeCombo.setSelectedItem(editor.getEmitter().getSpawnShape().getShape());
	}

	void setEdgesVisible (boolean visible) {
		edgesCheckbox.setVisible(visible);
		edgesLabel.setVisible(visible);
		visible = visible && edgesCheckbox.isSelected();
		sideCombo.setVisible(visible);
		sideLabel.setVisible(visible);
	}

	private void initializeComponents () {
		JPanel contentPanel = getContentPanel();
		{
			JLabel label = new JLabel("Shape:");
			contentPanel.add(label, new GridBagConstraints(0, 1, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 6), 0, 0));
		}
		{
			shapeCombo = new JComboBox();
			shapeCombo.setModel(new DefaultComboBoxModel(SpawnShape.values()));
			contentPanel.add(shapeCombo, new GridBagConstraints(1, 1, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		}
		{
			edgesLabel = new JLabel("Edges:");
			contentPanel.add(edgesLabel, new GridBagConstraints(2, 1, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(0, 12, 0, 6), 0, 0));
		}
		{
			edgesCheckbox = new JCheckBox();
			contentPanel.add(edgesCheckbox, new GridBagConstraints(3, 1, 1, 1, 0, 0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		}
		{
			sideLabel = new JLabel("Side:");
			contentPanel.add(sideLabel, new GridBagConstraints(4, 1, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(0, 12, 0, 6), 0, 0));
		}
		{
			sideCombo = new JComboBox();
			sideCombo.setModel(new DefaultComboBoxModel(SpawnEllipseSide.values()));
			contentPanel.add(sideCombo, new GridBagConstraints(5, 1, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		}
		{
			JPanel spacer = new JPanel();
			spacer.setPreferredSize(new Dimension());
			contentPanel.add(spacer, new GridBagConstraints(6, 0, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		}
	}
}

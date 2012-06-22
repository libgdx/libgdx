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

import java.awt.Cursor;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import com.badlogic.gdx.graphics.g2d.ParticleEmitter.ParticleValue;

class EditorPanel extends JPanel {
	private final String name;
	private final String description;
	private final ParticleValue value;
	private JPanel titlePanel;
	JToggleButton activeButton;
	private JPanel contentPanel;
	JToggleButton advancedButton;
	JPanel advancedPanel;
	private boolean hasAdvanced;
	JLabel descriptionLabel;

	public EditorPanel (ParticleValue value, String name, String description) {
		this.name = name;
		this.value = value;
		this.description = description;

		initializeComponents();

		titlePanel.addMouseListener(new MouseAdapter() {
			public void mouseClicked (MouseEvent event) {
				if (!activeButton.isVisible()) return;
				activeButton.setSelected(!activeButton.isSelected());
				updateActive();
			}
		});
		activeButton.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent event) {
				updateActive();
			}
		});
		advancedButton.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent event) {
				advancedPanel.setVisible(advancedButton.isSelected());
			}
		});

		if (value != null) {
			activeButton.setSelected(value.isActive());
			updateActive();
		}

		boolean alwaysActive = value == null ? true : value.isAlwaysActive();
		activeButton.setVisible(!alwaysActive);
		if (alwaysActive) contentPanel.setVisible(true);
		if (alwaysActive) titlePanel.setCursor(null);
	}

	void updateActive () {
		contentPanel.setVisible(activeButton.isSelected());
		advancedPanel.setVisible(activeButton.isSelected() && advancedButton.isSelected());
		advancedButton.setVisible(activeButton.isSelected() && hasAdvanced);
		descriptionLabel.setText(activeButton.isSelected() ? description : "");
		if (value != null) value.setActive(activeButton.isSelected());
	}

	public void update (ParticleEditor editor) {
	}

	public void setHasAdvanced (boolean hasAdvanced) {
		this.hasAdvanced = hasAdvanced;
		advancedButton.setVisible(hasAdvanced && (value.isActive() || value.isAlwaysActive()));
	}

	public JPanel getContentPanel () {
		return contentPanel;
	}

	public JPanel getAdvancedPanel () {
		return advancedPanel;
	}

	public String getName () {
		return name;
	}

	public void setEmbedded () {
		GridBagLayout layout = (GridBagLayout)getLayout();
		GridBagConstraints constraints = layout.getConstraints(contentPanel);
		constraints.insets = new Insets(0, 0, 0, 0);
		layout.setConstraints(contentPanel, constraints);

		titlePanel.setVisible(false);
	}

	private void initializeComponents () {
		setLayout(new GridBagLayout());
		{
			titlePanel = new JPanel(new GridBagLayout());
			add(titlePanel, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
				new Insets(3, 0, 3, 0), 0, 0));
			titlePanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			{
				JLabel label = new JLabel(name);
				titlePanel.add(label, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
					new Insets(3, 6, 3, 6), 0, 0));
				label.setFont(label.getFont().deriveFont(Font.BOLD));
			}
			{
				descriptionLabel = new JLabel(description);
				titlePanel.add(descriptionLabel, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST,
					GridBagConstraints.NONE, new Insets(3, 6, 3, 6), 0, 0));
			}
			{
				advancedButton = new JToggleButton("Advanced");
				titlePanel.add(advancedButton, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
					GridBagConstraints.NONE, new Insets(0, 0, 0, 6), 0, 0));
				advancedButton.setVisible(false);
			}
			{
				activeButton = new JToggleButton("Active");
				titlePanel.add(activeButton, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
					GridBagConstraints.NONE, new Insets(0, 0, 0, 6), 0, 0));
			}
		}
		{
			contentPanel = new JPanel(new GridBagLayout());
			add(contentPanel, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 6, 6, 6), 0, 0));
			contentPanel.setVisible(false);
		}
		{
			advancedPanel = new JPanel(new GridBagLayout());
			add(advancedPanel, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 6, 6, 6), 0, 0));
			advancedPanel.setVisible(false);
		}
	}
}

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

package com.badlogic.gdx.tools.particleeditor3d;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeListener;

import com.badlogic.gdx.graphics.g3d.particles.values.ParticleValue;
import com.badlogic.gdx.graphics.g3d.particles.values.RangedNumericValue;


public abstract class EditorPanel<T extends ParticleValue> extends JPanel {
	private final String name;
	private final String description;
	protected  T value;
	private JPanel titlePanel;
	JToggleButton activeButton;
	private JPanel contentPanel;
	JToggleButton advancedButton;
	JPanel advancedPanel;
	private boolean hasAdvanced;
	JLabel nameLabel, descriptionLabel;
	protected boolean isAlwaysActive;
	protected ParticleEditor3D editor;
	
	public EditorPanel (ParticleEditor3D editor, T value, String name, String description, boolean alwaysActive) {
		this.editor = editor;
		this.name = name;
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
		
		setValue(value, alwaysActive);
	}

	public EditorPanel (ParticleEditor3D editor, T value, String name, String description) {
		this(editor, value, name, description, true);
	}

	void updateActive () {
		contentPanel.setVisible(activeButton.isSelected());
		advancedPanel.setVisible(activeButton.isSelected() && advancedButton.isSelected());
		advancedButton.setVisible(activeButton.isSelected() && hasAdvanced);
		descriptionLabel.setText(activeButton.isSelected() ? description : "");
		if (value != null) value.setActive(activeButton.isSelected());
	}

	public void update (ParticleEditor3D editor) {
	}

	public void setHasAdvanced (boolean hasAdvanced) {
		this.hasAdvanced = hasAdvanced;
		advancedButton.setVisible(hasAdvanced && (value.isActive() || isAlwaysActive));
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
				nameLabel = new JLabel(name);
				titlePanel.add(nameLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
					new Insets(3, 6, 3, 6), 0, 0));
				nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD));
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
	
	public void setName(String name){
		nameLabel.setText(name);
	}
	
	public void setDescription(String desc){
		descriptionLabel.setText(desc);
	}
	
	
	protected void addContent(int row, int column, JComponent component){
		addContent(row, column, component, true, GridBagConstraints.CENTER, GridBagConstraints.BOTH);
	}
	
	protected void addContent(int row, int column, JComponent component, boolean addBorder){
		addContent(row, column, component, addBorder, GridBagConstraints.CENTER, GridBagConstraints.BOTH);
	}
	
	protected void addContent(int row, int column, JComponent component, int anchor, int fill){
		addContent(row, column, component, true, anchor, fill);
	}
	
	protected void addContent(int row, int column, JComponent component, boolean addBorders, int anchor, int fill){
		if(addBorders) component.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, java.awt.Color.black));
		contentPanel.add(component, new GridBagConstraints(column, row, 1, 1, 1, 1, anchor, fill,
			new Insets(0, 0, 0, 0), 0, 0));
	}

	public void setValue (T value) {
		setValue(value, isAlwaysActive);
	}
	
	public void setValue (T value, boolean alwaysActive) {
		this.value = value;
		if (value != null) {
			activeButton.setSelected(value.isActive());
			//updateActive();
		}

		isAlwaysActive = value == null ? true : alwaysActive;
		activeButton.setVisible(!isAlwaysActive);
		if (isAlwaysActive) {
			contentPanel.setVisible(true);
			titlePanel.setCursor(null);
		}
	}
	
	protected static <T> void setValue(JSpinner spinner, T object){
		ChangeListener[] listeners = spinner.getChangeListeners();
		ChangeListener listener = null;
		if(listeners != null && listeners.length >0){
			listener = listeners[0];
			spinner.removeChangeListener(listener);
		}
		spinner.setValue(object);
		if(listener != null) spinner.addChangeListener(listener);
	}
	
	protected static void setValue(JCheckBox checkBox, boolean isSelected){
		ActionListener[] listeners = checkBox.getActionListeners();
		ActionListener listener = null;
		if(listeners != null && listeners.length >0){
			listener = listeners[0];
			checkBox.removeActionListener(listener);
		}
		checkBox.setSelected(isSelected);
		if(listener != null) checkBox.addActionListener(listener);
	}
	
	protected static <T> void setValue(Slider slider, float value){
		ChangeListener[] listeners = slider.spinner.getChangeListeners();
		ChangeListener listener = null;
		if(listeners != null && listeners.length >0){
			listener = listeners[0];
			slider.spinner.removeChangeListener(listener);
		}
		slider.setValue(value);
		if(listener != null) slider.spinner.addChangeListener(listener);
	}
	
}

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

import com.badlogic.gdx.utils.JsonWriter;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;

public class SavePanel extends EditorPanel<Void> {

	JComboBox<JsonWriter.OutputType> jsonMode;
	JCheckBox prettyPrintBox;

	public SavePanel (FlameMain particleEditor3D, String name, String description) {
		super(particleEditor3D, name, description);
		setValue(null);
	}

	@Override
	protected void initializeComponents () {
		super.initializeComponents();

		jsonMode = new JComboBox<JsonWriter.OutputType>(new DefaultComboBoxModel(JsonWriter.OutputType.values()));
		jsonMode.setSelectedItem(SavePanel.this.editor.jsonOutputType);

		contentPanel.add(new JLabel("Json output mode:"), new GridBagConstraints(0, 1, 1, 1, 0, 0, GridBagConstraints.EAST,
			GridBagConstraints.NONE, new Insets(6, 0, 0, 0), 0, 0));
		contentPanel.add(jsonMode, new GridBagConstraints(1, 1, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE,
			new Insets(6, 6, 0, 0), 0, 0));

		contentPanel.add(new JLabel("Pretty print:"), new GridBagConstraints(0, 2, 1, 1, 0, 0, GridBagConstraints.EAST,
			GridBagConstraints.NONE, new Insets(6, 0, 0, 0), 0, 0));
		prettyPrintBox = new JCheckBox();
		prettyPrintBox.setSelected(SavePanel.this.editor.jsonPrettyPrint);
		contentPanel.add(prettyPrintBox, new GridBagConstraints(1, 2, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.NONE,
			new Insets(6, 6, 0, 0), 0, 0));

		jsonMode.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent event) {
				SavePanel.this.editor.jsonOutputType = (JsonWriter.OutputType)jsonMode.getSelectedItem();
			}
		});

		prettyPrintBox.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent event) {
				SavePanel.this.editor.jsonPrettyPrint = prettyPrintBox.isSelected();
			}
		});
	}
}

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

import com.badlogic.gdx.graphics.g2d.ParticleEmitter;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class OptionsPanel extends EditorPanel {
	JCheckBox attachedCheckBox;
	JCheckBox continuousCheckbox;
	JCheckBox alignedCheckbox;
	JCheckBox additiveCheckbox;
	JCheckBox behindCheckbox;
	JCheckBox premultipliedAlphaCheckbox;

	public OptionsPanel (final ParticleEditor editor, String name, String description) {
		super(null, name, description);

		initializeComponents();

		attachedCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent event) {
				editor.getEmitter().setAttached(attachedCheckBox.isSelected());
			}
		});

		continuousCheckbox.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent event) {
				editor.getEmitter().setContinuous(continuousCheckbox.isSelected());
			}
		});

		alignedCheckbox.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent event) {
				editor.getEmitter().setAligned(alignedCheckbox.isSelected());
			}
		});

		additiveCheckbox.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent event) {
				editor.getEmitter().setAdditive(additiveCheckbox.isSelected());
			}
		});

		behindCheckbox.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent event) {
				editor.getEmitter().setBehind(behindCheckbox.isSelected());
			}
		});

		premultipliedAlphaCheckbox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed (ActionEvent event) {
				editor.getEmitter().setPremultipliedAlpha(premultipliedAlphaCheckbox.isSelected());
			}
		});

		ParticleEmitter emitter = editor.getEmitter();
		attachedCheckBox.setSelected(emitter.isAttached());
		continuousCheckbox.setSelected(emitter.isContinuous());
		alignedCheckbox.setSelected(emitter.isAligned());
		additiveCheckbox.setSelected(emitter.isAdditive());
		behindCheckbox.setSelected(emitter.isBehind());
		premultipliedAlphaCheckbox.setSelected(emitter.isPremultipliedAlpha());
	}

	private void initializeComponents () {
		JPanel contentPanel = getContentPanel();
		{
			JLabel label = new JLabel("Additive:");
			contentPanel.add(label, new GridBagConstraints(0, 1, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(6, 0, 0, 0), 0, 0));
		}
		{
			additiveCheckbox = new JCheckBox();
			contentPanel.add(additiveCheckbox, new GridBagConstraints(1, 1, 1, 1, 0, 0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(6, 6, 0, 0), 0, 0));
		}
		{
			JLabel label = new JLabel("Attached:");
			contentPanel.add(label, new GridBagConstraints(0, 2, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(6, 0, 0, 0), 0, 0));
		}
		{
			attachedCheckBox = new JCheckBox();
			contentPanel.add(attachedCheckBox, new GridBagConstraints(1, 2, 1, 1, 1, 0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(6, 6, 0, 0), 0, 0));
		}
		{
			JLabel label = new JLabel("Continuous:");
			contentPanel.add(label, new GridBagConstraints(0, 3, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(6, 0, 0, 0), 0, 0));
		}
		{
			continuousCheckbox = new JCheckBox();
			contentPanel.add(continuousCheckbox, new GridBagConstraints(1, 3, 1, 1, 0, 0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(6, 6, 0, 0), 0, 0));
		}
		{
			JLabel label = new JLabel("Aligned:");
			contentPanel.add(label, new GridBagConstraints(0, 4, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(6, 0, 0, 0), 0, 0));
		}
		{
			alignedCheckbox = new JCheckBox();
			contentPanel.add(alignedCheckbox, new GridBagConstraints(1, 4, 1, 1, 0, 0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(6, 6, 0, 0), 0, 0));
		}
		{
			JLabel label = new JLabel("Behind:");
			contentPanel.add(label, new GridBagConstraints(0, 5, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(6, 0, 0, 0), 0, 0));
		}
		{
			behindCheckbox = new JCheckBox();
			contentPanel.add(behindCheckbox, new GridBagConstraints(1, 5, 1, 1, 0, 0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(6, 6, 0, 0), 0, 0));
		}
		{
			JLabel label = new JLabel("Premultiplied Alpha:");
			contentPanel.add(label, new GridBagConstraints(0, 6, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(6, 0, 0, 0), 0, 0));
		}
		{
			premultipliedAlphaCheckbox = new JCheckBox();
			contentPanel.add(premultipliedAlphaCheckbox, new GridBagConstraints(1, 6, 1, 1, 0, 0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(6, 6, 0, 0), 0, 0));
		}
	}
}

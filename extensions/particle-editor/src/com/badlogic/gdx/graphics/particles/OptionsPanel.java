
package com.badlogic.gdx.graphics.particles;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.badlogic.gdx.graphics.particles.ParticleEmitter;

class OptionsPanel extends EditorPanel {
	JCheckBox attachedCheckBox;
	JCheckBox continuousCheckbox;
	JCheckBox alignedCheckbox;
	JCheckBox additiveCheckbox;
	JCheckBox behindCheckbox;

	public OptionsPanel (final ParticleEditor editor) {
		super("Options", null);

		initializeComponents();

		attachedCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent event) {
				synchronized (editor.effect) {
					editor.getEmitter().setAttached(attachedCheckBox.isSelected());
				}
			}
		});

		continuousCheckbox.setSelected(editor.getEmitter().isContinuous());
		continuousCheckbox.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent event) {
				synchronized (editor.effect) {
					editor.getEmitter().setContinuous(continuousCheckbox.isSelected());
				}
			}
		});

		alignedCheckbox.addChangeListener(new ChangeListener() {
			public void stateChanged (ChangeEvent event) {
				synchronized (editor.effect) {
					editor.getEmitter().setAligned(alignedCheckbox.isSelected());
				}
			}
		});

		additiveCheckbox.addChangeListener(new ChangeListener() {
			public void stateChanged (ChangeEvent event) {
				synchronized (editor.effect) {
					editor.getEmitter().setAdditive(additiveCheckbox.isSelected());
				}
			}
		});

		behindCheckbox.addChangeListener(new ChangeListener() {
			public void stateChanged (ChangeEvent event) {
				synchronized (editor.effect) {
					editor.getEmitter().setBehind(behindCheckbox.isSelected());
				}
			}
		});

		ParticleEmitter particles = editor.getEmitter();
		attachedCheckBox.setSelected(particles.isAttached());
		continuousCheckbox.setSelected(particles.isContinuous());
		alignedCheckbox.setSelected(particles.isAligned());
		additiveCheckbox.setSelected(particles.isAdditive());
		behindCheckbox.setSelected(particles.isBehind());
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
	}
}

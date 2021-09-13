
package com.badlogic.gdx.tools.flame;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.badlogic.gdx.graphics.g3d.particles.batches.BillboardParticleBatch;

/** @author Inferno */
public class BillboardBatchPanel extends EditorPanel<BillboardParticleBatch> {
	JComboBox alignCombo;
	JCheckBox useGPUBox;
	JComboBox sortCombo;
	JComboBox srcBlendFunction, destBlendFunction;

	public BillboardBatchPanel (FlameMain particleEditor3D, BillboardParticleBatch renderer) {
		super(particleEditor3D, "Billboard Batch", "Renderer used to draw billboards particles.");
		initializeComponents(renderer);
		setValue(renderer);
	}

	private void initializeComponents (BillboardParticleBatch renderer) {
		// Align
		alignCombo = new JComboBox();
		alignCombo.setModel(new DefaultComboBoxModel(AlignModeWrapper.values()));
		alignCombo.setSelectedItem(AlignModeWrapper.find(renderer.getAlignMode()));
		alignCombo.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent event) {
				AlignModeWrapper align = (AlignModeWrapper)alignCombo.getSelectedItem();
				editor.getBillboardBatch().setAlignMode(align.mode);
			}
		});

		// Cpu/Gpu
		useGPUBox = new JCheckBox();
		useGPUBox.setSelected(renderer.isUseGPU());
		useGPUBox.addChangeListener(new ChangeListener() {
			public void stateChanged (ChangeEvent event) {
				editor.getBillboardBatch().setUseGpu(useGPUBox.isSelected());
			}
		});

		// Sort
		sortCombo = new JComboBox();
		sortCombo.setModel(new DefaultComboBoxModel(SortMode.values()));
		sortCombo.setSelectedItem(SortMode.find(renderer.getSorter()));
		sortCombo.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent event) {
				SortMode mode = (SortMode)sortCombo.getSelectedItem();
				editor.getBillboardBatch().setSorter(mode.sorter);
			}
		});

		// Blending source
		srcBlendFunction = new JComboBox();
		srcBlendFunction.setModel(new DefaultComboBoxModel(BlendFunction.values()));
		srcBlendFunction.setSelectedItem(BlendFunction.find(renderer.getBlendingAttribute().sourceFunction));
		srcBlendFunction.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent event) {
				BlendFunction blend = (BlendFunction)srcBlendFunction.getSelectedItem();
				editor.getBillboardBatch().getBlendingAttribute().sourceFunction = blend.blend;
			}
		});

		// Blending destination
		destBlendFunction = new JComboBox();
		destBlendFunction.setModel(new DefaultComboBoxModel(BlendFunction.values()));
		destBlendFunction.setSelectedItem(BlendFunction.find(renderer.getBlendingAttribute().destFunction));
		destBlendFunction.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent event) {
				BlendFunction blend = (BlendFunction)destBlendFunction.getSelectedItem();
				editor.getBillboardBatch().getBlendingAttribute().destFunction = blend.blend;
			}
		});

		int i = 0;
		Insets insets = new Insets(6, 0, 0, 0);
		contentPanel.add(new JLabel("Align"),
			new GridBagConstraints(0, i, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, insets, 0, 0));
		contentPanel.add(alignCombo,
			new GridBagConstraints(1, i++, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, insets, 0, 0));
		contentPanel.add(new JLabel("Use GPU"),
			new GridBagConstraints(0, i, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, insets, 0, 0));
		contentPanel.add(useGPUBox,
			new GridBagConstraints(1, i++, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, insets, 0, 0));
		contentPanel.add(new JLabel("Sort"),
			new GridBagConstraints(0, i, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, insets, 0, 0));
		contentPanel.add(sortCombo,
			new GridBagConstraints(1, i++, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, insets, 0, 0));
		contentPanel.add(new JLabel("Blending Src"),
			new GridBagConstraints(0, i, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, insets, 0, 0));
		contentPanel.add(srcBlendFunction,
			new GridBagConstraints(1, i++, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, insets, 0, 0));
		contentPanel.add(new JLabel("Blending Dest"),
			new GridBagConstraints(0, i, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, insets, 0, 0));
		contentPanel.add(destBlendFunction,
			new GridBagConstraints(1, i++, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, insets, 0, 0));
	}

}

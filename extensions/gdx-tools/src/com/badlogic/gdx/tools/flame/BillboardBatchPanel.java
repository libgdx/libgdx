
package com.badlogic.gdx.tools.flame;

import static java.awt.GridBagConstraints.NONE;
import static java.awt.GridBagConstraints.WEST;

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

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.particles.ParticleShader.AlignMode;
import com.badlogic.gdx.graphics.g3d.particles.ParticleSorter;
import com.badlogic.gdx.graphics.g3d.particles.batches.BillboardParticleBatch;

/** @author Inferno */
public class BillboardBatchPanel extends EditorPanel<BillboardParticleBatch> {

	private enum BlendFunction {
		SRC_ALPHA(GL20.GL_SRC_ALPHA), SRC_COLOR(GL20.GL_SRC_COLOR), DST_ALPHA(GL20.GL_DST_ALPHA), DST_COLOR(
			GL20.GL_DST_COLOR), ONE_MINUS_COLOR(
				GL20.GL_ONE_MINUS_SRC_COLOR), ONE_MINUS_ALPHA(GL20.GL_ONE_MINUS_SRC_ALPHA), ZERO(GL20.GL_ZERO), ONE(GL20.GL_ONE);

		public int blend;

		private BlendFunction (int blend) {
			this.blend = blend;
		}

		public static BlendFunction find (int function) {
			for (BlendFunction func : values()) {
				if (func.blend == function) return func;
			}

			return null;
		}

	}

	private enum AlignModeWrapper {
		Screen(AlignMode.Screen, "Screen"), ViewPoint(AlignMode.ViewPoint, "View Point");
		// ParticleDirection( AlignMode.ParticleDirection, "Particle Direction");

		public String desc;
		public AlignMode mode;

		AlignModeWrapper (AlignMode mode, String desc) {
			this.mode = mode;
			this.desc = desc;
		}

		@Override
		public String toString () {
			return desc;
		}
	}

	private enum SortMode {
		None("None", new ParticleSorter.None()), Distance("Distance", new ParticleSorter.Distance());

		public String desc;
		public ParticleSorter sorter;

		SortMode (String desc, ParticleSorter sorter) {
			this.sorter = sorter;
			this.desc = desc;
		}

		@Override
		public String toString () {
			return desc;
		}
	}

	JComboBox srcBlendFunction, destBlendFunction;
	JComboBox alignCombo;
	JCheckBox useGPUBox;
	JComboBox sortCombo;

	public BillboardBatchPanel (FlameMain particleEditor3D, BillboardParticleBatch renderer) {
		super(particleEditor3D, "Billboard Batch", "Renderer used to draw billboards particles.");
		initializeComponents(renderer);
		setValue(renderer);
	}

	private void initializeComponents (BillboardParticleBatch renderer) {
		// Src
		srcBlendFunction = new JComboBox();
		srcBlendFunction.setModel(new DefaultComboBoxModel(BlendFunction.values()));
		srcBlendFunction.setSelectedItem(BlendFunction.find(renderer.getBlendingAttribute().sourceFunction));
		srcBlendFunction.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent event) {
				BlendFunction blend = (BlendFunction)srcBlendFunction.getSelectedItem();
				editor.getBillboardBatch().getBlendingAttribute().sourceFunction = blend.blend;
			}
		});

		// Dest
		destBlendFunction = new JComboBox();
		destBlendFunction.setModel(new DefaultComboBoxModel(BlendFunction.values()));
		destBlendFunction.setSelectedItem(BlendFunction.find(renderer.getBlendingAttribute().destFunction));
		destBlendFunction.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent event) {
				BlendFunction blend = (BlendFunction)destBlendFunction.getSelectedItem();
				editor.getBillboardBatch().getBlendingAttribute().destFunction = blend.blend;
			}
		});

		// Align
		alignCombo = new JComboBox();
		alignCombo.setModel(new DefaultComboBoxModel(AlignModeWrapper.values()));
		alignCombo.setSelectedItem(getAlignModeWrapper(renderer.getAlignMode()));
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
		sortCombo.setSelectedItem(getSortMode(renderer.getSorter()));
		sortCombo.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent event) {
				SortMode mode = (SortMode)sortCombo.getSelectedItem();
				editor.getBillboardBatch().setSorter(mode.sorter);
			}
		});

		int i = 0;
		Insets insets = new Insets(6, 0, 0, 0);
		contentPanel.add(new JLabel("Align"), new GridBagConstraints(0, i, 1, 1, 0, 0, WEST, NONE, insets, 0, 0));
		contentPanel.add(alignCombo, new GridBagConstraints(1, i++, 1, 1, 1, 0, WEST, NONE, insets, 0, 0));
		contentPanel.add(new JLabel("Use GPU"), new GridBagConstraints(0, i, 1, 1, 0, 0, WEST, NONE, insets, 0, 0));
		contentPanel.add(useGPUBox, new GridBagConstraints(1, i++, 1, 1, 1, 0, WEST, NONE, insets, 0, 0));
		contentPanel.add(new JLabel("Sort"), new GridBagConstraints(0, i, 1, 1, 0, 0, WEST, NONE, insets, 0, 0));
		contentPanel.add(sortCombo, new GridBagConstraints(1, i++, 1, 1, 0, 0, WEST, NONE, insets, 0, 0));
		contentPanel.add(new JLabel("Src Func"), new GridBagConstraints(0, i, 1, 1, 0, 0, WEST, NONE, insets, 0, 0));
		contentPanel.add(srcBlendFunction, new GridBagConstraints(1, i++, 1, 1, 0, 0, WEST, NONE, insets, 0, 0));
		contentPanel.add(new JLabel("Dest Func"), new GridBagConstraints(0, i, 1, 1, 0, 0, WEST, NONE, insets, 0, 0));
		contentPanel.add(destBlendFunction, new GridBagConstraints(1, i++, 1, 1, 0, 0, WEST, NONE, insets, 0, 0));
	}

	private Object getSortMode (ParticleSorter sorter) {
		Class type = sorter.getClass();
		for (SortMode wrapper : SortMode.values()) {
			if (wrapper.sorter.getClass() == type) return wrapper;
		}
		return null;
	}

	private Object getAlignModeWrapper (AlignMode alignMode) {
		for (AlignModeWrapper wrapper : AlignModeWrapper.values()) {
			if (wrapper.mode == alignMode) return wrapper;
		}
		return null;
	}

}

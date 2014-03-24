package com.badlogic.gdx.tools.particleeditor3d;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.badlogic.gdx.assets.loaders.TextureLoader;
import com.badlogic.gdx.assets.loaders.resolvers.AbsoluteFileHandleResolver;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.particles.BillboardParticle;
import com.badlogic.gdx.graphics.g3d.particles.ParticleController;
import com.badlogic.gdx.graphics.g3d.particles.ParticleSorter;
import com.badlogic.gdx.graphics.g3d.particles.ParticleShader.AlignMode;
import com.badlogic.gdx.graphics.g3d.particles.ParticleSorter.BillboardDistanceParticleSorter;
import com.badlogic.gdx.graphics.g3d.particles.batches.BillboardParticleBatch;
import com.badlogic.gdx.graphics.g3d.particles.influencers.RegionInfluencer;

/** @author Inferno */
public class BillboardBatchPanel extends EditorPanel<BillboardParticleBatch> {
	private enum AlignModeWrapper{
		Screen( AlignMode.Screen, "Screen"),
		ViewPoint(AlignMode.ViewPoint, "View Point"),
		ParticleDirection( AlignMode.ParticleDirection, "Particle Direction");
		
		public String desc;
		public AlignMode mode;
		AlignModeWrapper(AlignMode mode, String desc){
			this.mode = mode;
			this.desc = desc;
		}
		
		@Override
		public String toString () {
			return desc;
		}
	}
	
	private enum SortWrapper{
		Far( new BillboardDistanceParticleSorter(), "Far First"),
		Near( new BillboardDistanceParticleSorter(ParticleSorter.DistanceParticleSorter.COMPARATOR_NEAR_DISTANCE), "Near First"),
		Younger( new ParticleSorter<BillboardParticle>(ParticleSorter.COMPARATOR_YOUNGER), "Younger First"),
		Older( new BillboardDistanceParticleSorter(), "Older First");
		
		public String desc;
		public ParticleSorter sorter;
		SortWrapper(ParticleSorter sorter, String desc){
			this.sorter = sorter;
			this.desc = desc;
		}
		
		@Override
		public String toString () {
			return desc;
		}
	}
	

	JComboBox alignCombo,sortCombo;
	JCheckBox useGPUBox;

	public BillboardBatchPanel (ParticleEditor3D particleEditor3D, BillboardParticleBatch renderer) {
		super(particleEditor3D, "Billboard Batch", "Renderer used to draw billboards particles.");
		initializeComponents(renderer);
		setValue(renderer);
	}

	private void initializeComponents (BillboardParticleBatch renderer) {
		//Align
		alignCombo = new JComboBox();
		alignCombo.setModel(new DefaultComboBoxModel(AlignModeWrapper.values()));
		alignCombo.setSelectedItem(getAlignModeWrapper(renderer.getAlignMode()));
		alignCombo.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent event) {
				AlignModeWrapper align = (AlignModeWrapper)alignCombo.getSelectedItem();
				editor.getBillboardBatch().setAlignMode(align.mode);
			}
		});
		
		//Cpu/Gpu
		useGPUBox = new JCheckBox();
		useGPUBox.setSelected(renderer.isUseGPU());
		useGPUBox.addChangeListener(new ChangeListener() {
			public void stateChanged (ChangeEvent event) {
				editor.getBillboardBatch().setUseGpu(useGPUBox.isSelected());
			}
		});
		
		//Sort
		sortCombo = new JComboBox();
		sortCombo.setModel(new DefaultComboBoxModel(SortWrapper.values()));
		sortCombo.setSelectedItem(getSortWrapper(renderer.getSorter()));
		sortCombo.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent event) {
				SortWrapper sorterWrapper = (SortWrapper)sortCombo.getSelectedItem();
				editor.getBillboardBatch().setSorter(sorterWrapper.sorter);
			}
		});
		
		int i =0;
		contentPanel.add(new JLabel("Align"), new GridBagConstraints(0, i, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE,
			new Insets(6, 0, 0, 0), 0, 0));
		contentPanel.add(alignCombo, new GridBagConstraints(1, i++, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.NONE,
			new Insets(6, 0, 0, 0), 0, 0));
		contentPanel.add(new JLabel("Use GPU"), new GridBagConstraints(0, i, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE,
			new Insets(6, 0, 0, 0), 0, 0));
		contentPanel.add(useGPUBox, new GridBagConstraints(1, i++, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.NONE,
			new Insets(6, 0, 0, 0), 0, 0));
		contentPanel.add(new JLabel("Sort"), new GridBagConstraints(0, i, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE,
			new Insets(6, 0, 0, 0), 0, 0));
		contentPanel.add(sortCombo, new GridBagConstraints(1, i++, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.NONE,
			new Insets(6, 0, 0, 0), 0, 0));
	}

	private Object getSortWrapper (ParticleSorter<BillboardParticle> sorter) {
		Class type = sorter.getClass();
		for(SortWrapper wrapper : SortWrapper.values()){
			if(wrapper.sorter.getClass().isAssignableFrom(type))
				return wrapper;
		}
		return null;
	}

	private Object getAlignModeWrapper (AlignMode alignMode) {
		for(AlignModeWrapper wrapper : AlignModeWrapper.values()){
			if(wrapper.mode == alignMode)
				return wrapper;
		}
		return null;
	}

}

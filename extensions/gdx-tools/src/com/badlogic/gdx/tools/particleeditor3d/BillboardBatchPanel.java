package com.badlogic.gdx.tools.particleeditor3d;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.badlogic.gdx.assets.loaders.TextureLoader;
import com.badlogic.gdx.assets.loaders.resolvers.AbsoluteFileHandleResolver;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.particles.BillboardParticle;
import com.badlogic.gdx.graphics.g3d.particles.ParticleController;
import com.badlogic.gdx.graphics.g3d.particles.ParticleShader.AlignMode;
import com.badlogic.gdx.graphics.g3d.particles.influencers.RegionInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.renderers.BillboardBatch;

public class BillboardBatchPanel extends EditorPanel<BillboardBatch> {
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
	
	private static class TexturePanel extends ImagePickerPanel<BillboardBatch> {

		public TexturePanel (ParticleEditor3D editor, BillboardBatch batch) {
			super(editor, "Texture", "The texture which contains all the particles");
			//set(regionInfluencer);
			setValue(batch);
			setIsAlwayShown(true);
		}

		@Override
		protected void setDefaultImage () {
			ParticleController<BillboardParticle> emitter = editor.getEmitter();
			RegionInfluencer influencer = emitter.findInfluencer(RegionInfluencer.class);
			String currentTexturePath = editor.assetManager.getAssetFileName(value.getTexture());
			if(currentTexturePath != ParticleEditor3D.DEFAULT_BILLBOARD_PARTICLE){
				setTexture(value, influencer, (Texture)editor.assetManager.get(ParticleEditor3D.DEFAULT_BILLBOARD_PARTICLE), currentTexturePath);
			}
		}
		

		@Override
		protected void onImageFileSelected (String absolutePath) {
			ParticleController<BillboardParticle> emitter = editor.getEmitter();
			RegionInfluencer influencer = emitter.findInfluencer(RegionInfluencer.class);
			
			final String currentTexturePath = editor.assetManager.getAssetFileName(value.getTexture());
			if(currentTexturePath != absolutePath){
				setTexture(value, influencer, editor.load(absolutePath, Texture.class, new TextureLoader(new AbsoluteFileHandleResolver()), null), currentTexturePath);
			}
		}
		
		protected void setTexture (BillboardBatch batch, RegionInfluencer influencer, Texture resource, String currentTexturePath) {
			if(resource != null){
				editor.setTexture(resource);
				influencer.init();
				editor.assetManager.setReferenceCount(currentTexturePath, editor.assetManager.getReferenceCount(currentTexturePath)-1);
			}
		}
	}
	
	
	JComboBox alignCombo;
	JCheckBox useGPUBox;

	public BillboardBatchPanel (ParticleEditor3D particleEditor3D, BillboardBatch renderer) {
		super(particleEditor3D, "Billboard Batch", "Renderer used to draw billboards particles.");
		initializeComponents(renderer);
		setValue(renderer);
		//set(renderer);
	}

	/*
	public void set(BillboardRenderer renderer){
		alignCombo.setSelectedItem(renderer.getAlignMode());
		useGPUBox.setSelected(renderer.isUseGPU());
		additiveBox.setSelected(renderer.isAdditive());
	}
	*/
	
	private void initializeComponents (BillboardBatch renderer) {
		JPanel contentPanel = getContentPanel();
		
		//Align
		alignCombo = new JComboBox();
		alignCombo.setModel(new DefaultComboBoxModel(AlignModeWrapper.values()));
		alignCombo.setSelectedItem(renderer.getAlignMode());
		alignCombo.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent event) {
				AlignModeWrapper align = (AlignModeWrapper)alignCombo.getSelectedItem();
				ParticleController controller = editor.getEmitter();
				editor.getBillboardBatch().setAlignMode(align.mode);
			}
		});
		
		//Cpu/Gpu
		useGPUBox = new JCheckBox();
		useGPUBox.setSelected(renderer.isUseGPU());
		useGPUBox.addChangeListener(new ChangeListener() {
			public void stateChanged (ChangeEvent event) {
				ParticleController controller = editor.getEmitter();
				editor.getBillboardBatch().setUseGpu(useGPUBox.isSelected());
			}
		});
		
		int i =0;
		OptionsPanel optionsPanel = new OptionsPanel();
		optionsPanel.addOption(i++, 0, "Align", alignCombo);
		optionsPanel.addOption(i++, 0, "Use GPU", useGPUBox);
		
		i=0;
		addContent(i++, 0, optionsPanel, false);
	}

}

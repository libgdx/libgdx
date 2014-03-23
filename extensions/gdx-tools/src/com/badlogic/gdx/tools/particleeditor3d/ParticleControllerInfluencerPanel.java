package com.badlogic.gdx.tools.particleeditor3d;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.CompoundBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import javax.swing.table.DefaultTableModel;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.particles.ParticleController;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffect;
import com.badlogic.gdx.graphics.g3d.particles.controllers.BillboardParticleController;
import com.badlogic.gdx.graphics.g3d.particles.controllers.ModelInstanceParticleController;
import com.badlogic.gdx.graphics.g3d.particles.controllers.PointSpriteParticleController;
import com.badlogic.gdx.graphics.g3d.particles.influencers.ModelInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.ParticleControllerInfluencer;
import com.badlogic.gdx.tools.particleeditor3d.TemplatePickerPanel.Listener;
import com.badlogic.gdx.utils.Array;

public class ParticleControllerInfluencerPanel extends InfluencerPanel<ParticleControllerInfluencer> implements TemplatePickerPanel.Listener<ParticleController>, 
																																									LoaderButton.Listener<ParticleEffect>, 
																																					com.badlogic.gdx.tools.particleeditor3d.EventManager.Listener{
	TemplatePickerPanel<ParticleController> controllerPicker;
	
	public ParticleControllerInfluencerPanel (ParticleEditor3D editor, ParticleControllerInfluencer influencer, boolean single, String name, String desc) {
		super(editor, influencer, name, desc, true, false);
		controllerPicker.setMultipleSelectionAllowed(!single);
		EventManager.get().attach(ParticleEditor3D.EVT_ASSET_RELOADED, this);
	}
	
	@Override
	public void setValue (ParticleControllerInfluencer value) {
		super.setValue(value);
		if(value == null) return;
		controllerPicker.setValue(value.templates);
	}
	
	protected void initializeComponents () {
		super.initializeComponents();
		controllerPicker = new TemplatePickerPanel<ParticleController>(editor, null, this, ParticleController.class){
			@Override
			protected String getTemplateName (ParticleController template, int index) {
				return template.name;
			}
		};
		reloadControllers ();
		controllerPicker.setIsAlwayShown(true);
		
		
		contentPanel.add(new LoaderButton.ParticleEffectLoaderButton(editor, this), new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,
			0, 0, 6), 0, 0));
		contentPanel.add(controllerPicker, new GridBagConstraints(0, 1, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,
			0, 0, 6), 0, 0));
	}

	@Override
	public void onTemplateChecked (ParticleController model, boolean isChecked) {
		editor.getEmitter().init();
		editor.effect.start();
	}

	@Override
	public void onResourceLoaded (ParticleEffect resource) {
		reloadControllers();
	}

	private void reloadControllers () {
		Array<ParticleEffect> effects = new Array<ParticleEffect>();
		Array<ParticleController> controllers = new Array<ParticleController>();
		editor.assetManager.get(ParticleEffect.class, effects);
		for(ParticleEffect effect : effects){
			controllers.addAll(effect.getControllers());
		}
		controllerPicker.setLoadedTemplates(controllers);
	}

	@Override
	public void handle (int aEventType, Object aEventData) {
		if(aEventType == ParticleEditor3D.EVT_ASSET_RELOADED){
			Object[] data = (Object[])aEventData;
			if(data[0] instanceof ParticleEffect){
				ParticleEffect oldEffect = (ParticleEffect) data[0];
				int currentCount = value.templates.size;
				value.templates.removeAll(oldEffect.getControllers(), true);
				if(value.templates.size != currentCount){
					int diff = currentCount - value.templates.size;
					if(diff > 0){
						ParticleEffect newEffect = (ParticleEffect) data[1];
						Array<ParticleController> newControllers = newEffect.getControllers();
						if(newControllers.size > 0){
							for(int i=0, c=Math.min(diff, newControllers.size); i<c; ++i)
								value.templates.add(newControllers.get(i));
						}
					}
					else {
						value.templates.addAll( ((ParticleEffect)editor.assetManager.get(ParticleEditor3D.DEFAULT_BILLBOARD_PARTICLE)).getControllers());
					}
					
					//controllerPicker.setExcludedTemplates(editor.getEmitter())
					controllerPicker.reloadTemplates();
					controllerPicker.setValue(value.templates);
					editor.getEmitter().init();
					editor.effect.start();
				}
			}
		}
	}
	
	
}

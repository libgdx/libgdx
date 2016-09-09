package com.badlogic.gdx.tools.flame;

import java.awt.GridBagConstraints;
import java.awt.Insets;

import com.badlogic.gdx.graphics.g3d.particles.ParticleController;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffect;
import com.badlogic.gdx.graphics.g3d.particles.influencers.ParticleControllerInfluencer;
import com.badlogic.gdx.utils.Array;

/** @author Inferno */
public class ParticleControllerInfluencerPanel extends InfluencerPanel<ParticleControllerInfluencer> implements TemplatePickerPanel.Listener<ParticleController>, 
																																									LoaderButton.Listener<ParticleEffect>, 
																																									EventManager.Listener{
	TemplatePickerPanel<ParticleController> controllerPicker;
	
	public ParticleControllerInfluencerPanel (FlameMain editor, ParticleControllerInfluencer influencer, boolean single, String name, String desc) {
		super(editor, influencer, name, desc, true, false);
		controllerPicker.setMultipleSelectionAllowed(!single);
		EventManager.get().attach(FlameMain.EVT_ASSET_RELOADED, this);
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
		editor.restart();
	}

	@Override
	public void onResourceLoaded (ParticleEffect resource) {
		reloadControllers();
	}

	private void reloadControllers () {
		Array<ParticleEffect> effects = new Array<ParticleEffect>();
		Array<ParticleController> controllers = new Array<ParticleController>();
		editor.assetManager.getAll(ParticleEffect.class, effects);
		for(ParticleEffect effect : effects){
			controllers.addAll(effect.getControllers());
		}
		controllerPicker.setLoadedTemplates(controllers);
	}

	@Override
	public void handle (int aEventType, Object aEventData) {
		if(aEventType == FlameMain.EVT_ASSET_RELOADED){
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
						value.templates.addAll( ((ParticleEffect)editor.assetManager.get(FlameMain.DEFAULT_BILLBOARD_PARTICLE)).getControllers());
					}
					
					controllerPicker.reloadTemplates();
					controllerPicker.setValue(value.templates);
					editor.restart();
				}
			}
		}
	}
	
	
}

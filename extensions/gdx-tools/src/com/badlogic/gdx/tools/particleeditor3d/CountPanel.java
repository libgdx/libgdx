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
import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.badlogic.gdx.graphics.g3d.particles.ParticleController;
import com.badlogic.gdx.graphics.g3d.particles.emitters.Emitter;

class CountPanel extends EditorPanel {
	Slider maxSlider, minSlider;

	public CountPanel (final ParticleEditor3D editor, String name, String description, int min, int max) {
		super(editor, null, name, description, true);

		initializeComponents(min, max);
		//set(editor.getEmitter().emitter.minParticleCount, editor.getEmitter().emitter.maxParticleCount);
	}
	
	/*
	public void set(int min, int max){
		minSlider.setValue(min);
		maxSlider.setValue(max);
	}
	*/

	private void initializeComponents (int min, int max) {
		//Min
		minSlider = new Slider(0, 0, 99999, 1);
		minSlider.setValue(min);
		minSlider.addChangeListener(new ChangeListener() {
			public void stateChanged (ChangeEvent event) {
				ParticleController controller = editor.getEmitter();
				controller.emitter.minParticleCount = (int)minSlider.getValue();
				controller.init();
				editor.effect.start();
			}
		});

		//Max
		maxSlider = new Slider(0, 0, 99999, 1);
		maxSlider.setValue(max);
		maxSlider.addChangeListener(new ChangeListener() {
			public void stateChanged (ChangeEvent event) {
				ParticleController controller = editor.getEmitter();
				controller.emitter.maxParticleCount = (int)maxSlider.getValue();
				controller.init();
				editor.effect.start();
			}
		});
		
		int i=0;
		OptionsPanel optionsPanel = new OptionsPanel();
		optionsPanel.addOption(i++, 0, "Min", minSlider);
		optionsPanel.addOption(i++, 0, "Max", maxSlider);
		addContent(0, 0, optionsPanel, false, GridBagConstraints.WEST, GridBagConstraints.NONE);
		
	}
}

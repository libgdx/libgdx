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
import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.badlogic.gdx.graphics.g3d.particles.ParticleController;

/** @author Inferno */
class CountPanel extends EditorPanel {
	Slider maxSlider, minSlider;

	public CountPanel (final FlameMain editor, String name, String description, int min, int max) {
		super(editor, name, description);

		initializeComponents(min, max);
		setValue(null);
	}
	
	private void initializeComponents (int min, int max) {
		//Min
		minSlider = new Slider(0, 0, 999999, 1);
		minSlider.setValue(min);
		minSlider.addChangeListener(new ChangeListener() {
			public void stateChanged (ChangeEvent event) {
				ParticleController controller = editor.getEmitter();
				controller.emitter.minParticleCount = (int)minSlider.getValue();
				editor.restart();
			}
		});

		//Max
		maxSlider = new Slider(0, 0, 999999, 1);
		maxSlider.setValue(max);
		maxSlider.addChangeListener(new ChangeListener() {
			public void stateChanged (ChangeEvent event) {
				ParticleController controller = editor.getEmitter();
				controller.emitter.maxParticleCount = (int)maxSlider.getValue();
				editor.restart();
			}
		});
		
		int i =0;
		contentPanel.add(new JLabel("Min"), new GridBagConstraints(0, i, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE,
			new Insets(6, 0, 0, 0), 0, 0));
		contentPanel.add(minSlider, new GridBagConstraints(1, i++, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.NONE,
			new Insets(6, 0, 0, 0), 0, 0));
		contentPanel.add(new JLabel("Max"), new GridBagConstraints(0, i, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE,
			new Insets(6, 0, 0, 0), 0, 0));
		contentPanel.add(maxSlider, new GridBagConstraints(1, i++, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.NONE,
			new Insets(6, 0, 0, 0), 0, 0));
	}
}

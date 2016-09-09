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
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.badlogic.gdx.graphics.g3d.particles.values.NumericValue;


/** @author Inferno */
class NumericPanel extends ParticleValuePanel<NumericValue> {
	JSpinner valueSpinner;
	
	public NumericPanel ( FlameMain editor, NumericValue value, String name, String description) {
		super(editor, name, description);
		setValue(value);
	}
	
	@Override
	public void setValue (NumericValue value) {
		super.setValue(value);
		if(value == null)return;
		setValue(valueSpinner, value.getValue());
	}

	protected void initializeComponents () {
		super.initializeComponents();
		JPanel contentPanel = getContentPanel();
		{
			JLabel label = new JLabel("Value:");
			contentPanel.add(label, new GridBagConstraints(0, 1, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 6), 0, 0));
		}
		{
			valueSpinner = new JSpinner(new SpinnerNumberModel(new Float(0), new Float(-99999), new Float(99999), new Float(0.1f)));
			contentPanel.add(valueSpinner, new GridBagConstraints(1, 1, 1, 1, 1, 0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		}
		valueSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged (ChangeEvent event) {
				NumericPanel.this.value.setValue((Float)valueSpinner.getValue());
			}
		});
	}
}

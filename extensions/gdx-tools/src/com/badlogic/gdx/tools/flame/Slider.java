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

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeListener;

/** @author Inferno */
public class Slider extends JPanel {
	public JSpinner spinner;

	public Slider (float initialValue, final float min, final float max, float stepSize) {
		spinner = new JSpinner(new SpinnerNumberModel(initialValue, min, max, stepSize));
		setLayout(new BorderLayout());
		add(spinner);
	}

	public void setValue (float value) {
		spinner.setValue((double)value);
	}

	public float getValue () {
		return ((Double)spinner.getValue()).floatValue();
	}

	public void addChangeListener (ChangeListener listener) {
		spinner.addChangeListener(listener);
	}

	public Dimension getPreferredSize () {
		Dimension size = super.getPreferredSize();
		size.width = 75;
		size.height = 26;
		return size;
	}
}

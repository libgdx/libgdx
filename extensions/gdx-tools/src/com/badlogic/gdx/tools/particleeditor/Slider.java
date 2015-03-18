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

package com.badlogic.gdx.tools.particleeditor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

// BOZO - Slider is a placeholder that uses a spinner until the slider (in NewSlider) is complete.
class Slider extends JPanel {
	private JSpinner spinner;

	public Slider (float initialValue, final float min, final float max, float stepSize, final float sliderMin,
		final float sliderMax) {
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

	public static void main (String[] args) throws Exception {
		EventQueue.invokeLater(new Runnable() {
			public void run () {
				JFrame frame = new JFrame();
				frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				frame.setSize(480, 320);
				frame.setLocationRelativeTo(null);
				JPanel panel = new JPanel();
				frame.getContentPane().add(panel);
				panel.add(new Slider(200, 100, 500, 0.1f, 150, 300));
				frame.setVisible(true);
			}
		});
	}
}

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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

class NewSlider extends JPanel {
	static private final int KNOB_WIDTH = 10;

	float value, min, max, stepSize, sliderMin, sliderMax;
	ChangeListener listener;
	int border = 2;
	Color bgColor = new Color(0.6f, 0.6f, 0.6f);
	Color knobColor = Color.lightGray;

	public NewSlider (float initialValue, final float min, final float max, float stepSize, final float sliderMin,
		final float sliderMax) {
		this.min = min;
		this.max = max;
		this.stepSize = stepSize;
		this.sliderMin = sliderMin;
		this.sliderMax = sliderMax;
		value = Math.max(min, Math.min(max, initialValue));

		setLayout(new GridBagLayout());

		addMouseListener(new MouseAdapter() {
			public void mousePressed (MouseEvent event) {
				float width = getWidth() - KNOB_WIDTH - border * 2;
				float mouseX = event.getX() - KNOB_WIDTH / 2 - border;
				setValue(sliderMin + (sliderMax - sliderMin) * Math.max(0, Math.min(width, mouseX)) / width);
			}

			public void mouseReleased (MouseEvent event) {

			}

			public void mouseClicked (MouseEvent event) {
				repaint();
			}
		});
		addMouseMotionListener(new MouseMotionListener() {
			public void mouseDragged (MouseEvent event) {
				float width = getWidth() - KNOB_WIDTH - border * 2;
				float mouseX = event.getX() - KNOB_WIDTH / 2 - border;
				setValue(sliderMin + (sliderMax - sliderMin) * Math.max(0, Math.min(width, mouseX)) / width);
			}

			public void mouseMoved (MouseEvent event) {
				int mouseX = event.getX();
				int mouseY = event.getY();

			}
		});
	}

	protected void paintComponent (Graphics graphics) {
		super.paintComponent(graphics);

		Graphics2D g = (Graphics2D)graphics;
		int width = getWidth();
		int height = getHeight();

		g.setColor(bgColor);
		g.fillRect(border, border, width - border * 2, height - border * 2);

		int maxKnobX = width - border - KNOB_WIDTH;
		int knobX = (int)((width - border * 2 - KNOB_WIDTH) * (value - sliderMin) / (sliderMax - sliderMin)) + border;
		g.setColor(knobColor);
		g.fillRect(Math.max(border, Math.min(maxKnobX, knobX)), 0, KNOB_WIDTH, height);

		float displayValue = (int)(value * 10) / 10f;
		String label = displayValue == (int)displayValue ? String.valueOf((int)displayValue) : String.valueOf(displayValue);
		FontMetrics metrics = g.getFontMetrics();
		int labelWidth = metrics.stringWidth(label);
		g.setColor(Color.white);
		g.drawString(label, width / 2 - labelWidth / 2, height / 2 + metrics.getAscent() / 2);
	}

	public void setValue (float value) {
		this.value = (int)(Math.max(min, Math.min(max, value)) / stepSize) * stepSize;
		repaint();
		if (listener != null) listener.stateChanged(new ChangeEvent(this));
	}

	public float getValue () {
		return value;
	}

	public void addChangeListener (ChangeListener listener) {
		this.listener = listener;
	}

	public Dimension getPreferredSize () {
		Dimension size = super.getPreferredSize();
		size.width = 150;
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
				panel.add(new NewSlider(200, 100, 500, 0.1f, 150, 300));
				frame.setVisible(true);
			}
		});
	}
}

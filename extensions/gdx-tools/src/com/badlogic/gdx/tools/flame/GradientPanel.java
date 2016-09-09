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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JColorChooser;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.badlogic.gdx.graphics.g3d.particles.values.GradientColorValue;

/** @author Inferno */
class GradientPanel extends ParticleValuePanel<GradientColorValue> {
	private GradientEditor gradientEditor;
	ColorSlider saturationSlider, lightnessSlider;
	JPanel colorPanel;
	private ColorSlider hueSlider;

	public GradientPanel (FlameMain editor, GradientColorValue value, String name, String description, 
																boolean hideGradientEditor) {
		super(editor, name, description);
		setValue(value);

		if (hideGradientEditor) {
			gradientEditor.setVisible(false);
		}
		gradientEditor.percentages.clear();
		for (float percent : value.getTimeline())
			gradientEditor.percentages.add(percent);

		gradientEditor.colors.clear();
		float[] colors = value.getColors();
		for (int i = 0; i < colors.length;) {
			float r = colors[i++];
			float g = colors[i++];
			float b = colors[i++];
			gradientEditor.colors.add(new Color(r, g, b));
		}
		if (gradientEditor.colors.isEmpty() || gradientEditor.percentages.isEmpty()) {
			gradientEditor.percentages.clear();
			gradientEditor.percentages.add(0f);
			gradientEditor.percentages.add(1f);
			gradientEditor.colors.clear();
			gradientEditor.colors.add(Color.white);
		}
		setColor(gradientEditor.colors.get(0));
	}

	public Dimension getPreferredSize () {
		Dimension size = super.getPreferredSize();
		size.width = 10;
		return size;
	}

	protected void initializeComponents () {
		super.initializeComponents();
		JPanel contentPanel = getContentPanel();
		{
			gradientEditor = new GradientEditor() {
				public void handleSelected (Color color) {
					GradientPanel.this.setColor(color);
				}
			};
			contentPanel.add(gradientEditor, new GridBagConstraints(0, 1, 3, 1, 1.0, 0.0, GridBagConstraints.CENTER,
				GridBagConstraints.HORIZONTAL, new Insets(0, 0, 6, 0), 0, 10));
		}
		{
			hueSlider = new ColorSlider(new Color[] {Color.red, Color.yellow, Color.green, Color.cyan, Color.blue, Color.magenta,
				Color.red}) {
				protected void colorPicked () {
					saturationSlider.setColors(new Color[] {new Color(Color.HSBtoRGB(getPercentage(), 1, 1)), Color.white});
					updateColor();
				}
			};
			contentPanel.add(hueSlider, new GridBagConstraints(1, 2, 2, 1, 1.0, 0.0, GridBagConstraints.CENTER,
				GridBagConstraints.HORIZONTAL, new Insets(0, 0, 6, 0), 0, 0));
		}
		{
			saturationSlider = new ColorSlider(new Color[] {Color.red, Color.white}) {
				protected void colorPicked () {
					updateColor();
				}
			};
			contentPanel.add(saturationSlider, new GridBagConstraints(1, 3, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER,
				GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 6), 0, 0));
		}
		{
			lightnessSlider = new ColorSlider(new Color[0]) {
				protected void colorPicked () {
					updateColor();
				}
			};
			contentPanel.add(lightnessSlider, new GridBagConstraints(2, 3, 1, 1, 1, 0.0, GridBagConstraints.CENTER,
				GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		}
		{
			colorPanel = new JPanel() {
				public Dimension getPreferredSize () {
					Dimension size = super.getPreferredSize();
					size.width = 52;
					return size;
				}
			};
			contentPanel.add(colorPanel, new GridBagConstraints(0, 2, 1, 2, 0.0, 0.0, GridBagConstraints.CENTER,
				GridBagConstraints.BOTH, new Insets(3, 0, 0, 6), 0, 0));
		}

		colorPanel.addMouseListener(new MouseAdapter() {
			public void mouseClicked (MouseEvent e) {
				Color color = JColorChooser.showDialog(colorPanel, "Set Color", colorPanel.getBackground());
				if (color != null) setColor(color);
			}
		});
		colorPanel.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.black));
	}

	public void setColor (Color color) {
		float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
		hueSlider.setPercentage(hsb[0]);
		saturationSlider.setPercentage(1 - hsb[1]);
		lightnessSlider.setPercentage(1 - hsb[2]);
		colorPanel.setBackground(color);
	}

	void updateColor () {
		Color color = new Color(Color.HSBtoRGB(hueSlider.getPercentage(), 1 - saturationSlider.getPercentage(), 1));
		lightnessSlider.setColors(new Color[] {color, Color.black});
		color = new Color(Color.HSBtoRGB(hueSlider.getPercentage(), 1 - saturationSlider.getPercentage(),
			1 - lightnessSlider.getPercentage()));
		colorPanel.setBackground(color);
		gradientEditor.setColor(color);

		float[] colors = new float[gradientEditor.colors.size() * 3];
		int i = 0;
		for (Color c : gradientEditor.colors) {
			colors[i++] = c.getRed() / 255f;
			colors[i++] = c.getGreen() / 255f;
			colors[i++] = c.getBlue() / 255f;
		}
		float[] percentages = new float[gradientEditor.percentages.size()];
		i = 0;
		for (Float percent : gradientEditor.percentages)
			percentages[i++] = percent;
		value.setColors(colors);
		value.setTimeline(percentages);
	}

	public class GradientEditor extends JPanel {
		ArrayList<Color> colors = new ArrayList();
		ArrayList<Float> percentages = new ArrayList();

		int handleWidth = 12;
		int handleHeight = 12;
		int gradientX = handleWidth / 2;
		int gradientY = 0;
		int gradientWidth;
		int gradientHeight;
		int dragIndex = -1;
		int selectedIndex;

		public GradientEditor () {
			setPreferredSize(new Dimension(100, 30));

			addMouseListener(new MouseAdapter() {
				public void mousePressed (MouseEvent event) {
					dragIndex = -1;
					int mouseX = event.getX();
					int mouseY = event.getY();
					int y = gradientY + gradientHeight;
					for (int i = 0, n = colors.size(); i < n; i++) {
						int x = gradientX + (int)(percentages.get(i) * gradientWidth) - handleWidth / 2;
						if (mouseX >= x && mouseX <= x + handleWidth && mouseY >= gradientY && mouseY <= y + handleHeight) {
							dragIndex = selectedIndex = i;
							handleSelected(colors.get(selectedIndex));
							repaint();
							break;
						}
					}
				}

				public void mouseReleased (MouseEvent event) {
					if (dragIndex != -1) {
						dragIndex = -1;
						repaint();
					}
				}

				public void mouseClicked (MouseEvent event) {
					int mouseX = event.getX();
					int mouseY = event.getY();
					if (event.getClickCount() == 2) {
						if (percentages.size() <= 1) return;
						if (selectedIndex == -1 || selectedIndex == 0) return;
						int y = gradientY + gradientHeight;
						int x = gradientX + (int)(percentages.get(selectedIndex) * gradientWidth) - handleWidth / 2;
						if (mouseX >= x && mouseX <= x + handleWidth && mouseY >= gradientY && mouseY <= y + handleHeight) {
							percentages.remove(selectedIndex);
							colors.remove(selectedIndex);
							selectedIndex--;
							dragIndex = selectedIndex;
							if (percentages.size() == 2) percentages.set(1, 1f);
							handleSelected(colors.get(selectedIndex));
							repaint();
						}
						return;
					}
					if (mouseX < gradientX || mouseX > gradientX + gradientWidth) return;
					if (mouseY < gradientY || mouseY > gradientY + gradientHeight) return;
					float percent = (event.getX() - gradientX) / (float)gradientWidth;
					if (percentages.size() == 1) percent = 1f;
					for (int i = 0, n = percentages.size(); i <= n; i++) {
						if (i == n || percent < percentages.get(i)) {
							percentages.add(i, percent);
							colors.add(i, colors.get(i - 1));
							dragIndex = selectedIndex = i;
							handleSelected(colors.get(selectedIndex));
							repaint();
							break;
						}
					}
				}
			});
			addMouseMotionListener(new MouseMotionAdapter() {
				public void mouseDragged (MouseEvent event) {
					if (dragIndex == -1 || dragIndex == 0 || dragIndex == percentages.size() - 1) return;
					float percent = (event.getX() - gradientX) / (float)gradientWidth;
					percent = Math.max(percent, percentages.get(dragIndex - 1) + 0.01f);
					percent = Math.min(percent, percentages.get(dragIndex + 1) - 0.01f);
					percentages.set(dragIndex, percent);
					repaint();
				}
			});
		}

		public void setColor (Color color) {
			if (selectedIndex == -1) return;
			colors.set(selectedIndex, color);
			repaint();
		}

		public void handleSelected (Color color) {
		}

		protected void paintComponent (Graphics graphics) {
			super.paintComponent(graphics);
			Graphics2D g = (Graphics2D)graphics;
			int width = getWidth() - 1;
			int height = getHeight();

			gradientWidth = width - handleWidth;
			gradientHeight = height - 16;

			g.translate(gradientX, gradientY);
			for (int i = 0, n = colors.size() == 1 ? 1 : colors.size() - 1; i < n; i++) {
				Color color1 = colors.get(i);
				Color color2 = colors.size() == 1 ? color1 : colors.get(i + 1);
				float percent1 = percentages.get(i);
				float percent2 = colors.size() == 1 ? 1 : percentages.get(i + 1);
				int point1 = (int)(percent1 * gradientWidth);
				int point2 = (int)Math.ceil(percent2 * gradientWidth);
				g.setPaint(new GradientPaint(point1, 0, color1, point2, 0, color2, false));
				g.fillRect(point1, 0, point2 - point1, gradientHeight);
			}
			g.setPaint(null);
			g.setColor(Color.black);
			g.drawRect(0, 0, gradientWidth, gradientHeight);

			int y = gradientHeight;
			int[] yPoints = new int[3];
			yPoints[0] = y;
			yPoints[1] = y + handleHeight;
			yPoints[2] = y + handleHeight;
			int[] xPoints = new int[3];
			for (int i = 0, n = colors.size(); i < n; i++) {
				int x = (int)(percentages.get(i) * gradientWidth);
				xPoints[0] = x;
				xPoints[1] = x - handleWidth / 2;
				xPoints[2] = x + handleWidth / 2;
				if (i == selectedIndex) {
					g.setColor(colors.get(i));
					g.fillPolygon(xPoints, yPoints, 3);
					g.fillRect(xPoints[1], yPoints[1] + 2, handleWidth + 1, 2);
					g.setColor(Color.black);
				}
				g.drawPolygon(xPoints, yPoints, 3);
			}
			g.translate(-gradientX, -gradientY);
		}
	}

	static public class ColorSlider extends JPanel {
		Color[] paletteColors;
		JSlider slider;
		private ColorPicker colorPicker;

		public ColorSlider (Color[] paletteColors) {
			this.paletteColors = paletteColors;
			setLayout(new GridBagLayout());
			{
				slider = new JSlider(0, 1000, 0);
				slider.setPaintTrack(false);
				add(slider, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
					new Insets(0, 6, 0, 6), 0, 0));
			}
			{
				colorPicker = new ColorPicker();
				add(colorPicker, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
					GridBagConstraints.HORIZONTAL, new Insets(0, 6, 0, 6), 0, 0));
			}

			slider.addChangeListener(new ChangeListener() {
				public void stateChanged (ChangeEvent event) {
					colorPicked();
				}
			});
		}

		public Dimension getPreferredSize () {
			Dimension size = super.getPreferredSize();
			size.width = 10;
			return size;
		}

		public void setPercentage (float percent) {
			slider.setValue((int)(1000 * percent));
		}

		public float getPercentage () {
			return slider.getValue() / 1000f;
		}

		protected void colorPicked () {
		}

		public void setColors (Color[] colors) {
			paletteColors = colors;
			repaint();
		}

		public class ColorPicker extends JPanel {
			public ColorPicker () {
				addMouseListener(new MouseAdapter() {
					public void mouseClicked (MouseEvent event) {
						slider.setValue((int)(event.getX() / (float)getWidth() * 1000));
					}
				});
			}

			protected void paintComponent (Graphics graphics) {
				Graphics2D g = (Graphics2D)graphics;
				int width = getWidth() - 1;
				int height = getHeight() - 1;
				for (int i = 0, n = paletteColors.length - 1; i < n; i++) {
					Color color1 = paletteColors[i];
					Color color2 = paletteColors[i + 1];
					float point1 = i / (float)n * width;
					float point2 = (i + 1) / (float)n * width;
					g.setPaint(new GradientPaint(point1, 0, color1, point2, 0, color2, false));
					g.fillRect((int)point1, 0, (int)Math.ceil(point2 - point1), height);
				}
				g.setPaint(null);
				g.setColor(Color.black);
				g.drawRect(0, 0, width, height);
			}
		}
	}
}

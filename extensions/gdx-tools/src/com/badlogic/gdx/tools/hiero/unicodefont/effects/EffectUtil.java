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

package com.badlogic.gdx.tools.hiero.unicodefont.effects;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;

import com.badlogic.gdx.tools.hiero.unicodefont.GlyphPage;
import com.badlogic.gdx.tools.hiero.unicodefont.effects.ConfigurableEffect.Value;

/** Provides utility methods for effects.
 * @author Nathan Sweet */
public class EffectUtil {
	static private BufferedImage scratchImage = new BufferedImage(GlyphPage.MAX_GLYPH_SIZE, GlyphPage.MAX_GLYPH_SIZE,
		BufferedImage.TYPE_INT_ARGB);

	/** Returns an image that can be used by effects as a temp image. */
	static public BufferedImage getScratchImage () {
		Graphics2D g = (Graphics2D)scratchImage.getGraphics();
		g.setComposite(AlphaComposite.Clear);
		g.fillRect(0, 0, GlyphPage.MAX_GLYPH_SIZE, GlyphPage.MAX_GLYPH_SIZE);
		g.setComposite(AlphaComposite.SrcOver);
		g.setColor(java.awt.Color.white);
		return scratchImage;
	}

	/** Returns a value that represents a color. */
	static public Value colorValue (String name, Color currentValue) {
		return new DefaultValue(name, EffectUtil.toString(currentValue)) {
			public void showDialog () {
				Color newColor = JColorChooser.showDialog(null, "Choose a color", EffectUtil.fromString(value));
				if (newColor != null) value = EffectUtil.toString(newColor);
			}

			public Object getObject () {
				return EffectUtil.fromString(value);
			}
		};
	}

	/** Returns a value that represents an int. */
	static public Value intValue (String name, final int currentValue, final String description) {
		return new DefaultValue(name, String.valueOf(currentValue)) {
			public void showDialog () {
				JSpinner spinner = new JSpinner(new SpinnerNumberModel(currentValue, Short.MIN_VALUE, Short.MAX_VALUE, 1));
				if (showValueDialog(spinner, description)) value = String.valueOf(spinner.getValue());
			}

			public Object getObject () {
				return Integer.valueOf(value);
			}
		};
	}

	/** Returns a value that represents a float, from 0 to 1 (inclusive). */
	static public Value floatValue (String name, final float currentValue, final float min, final float max,
		final String description) {
		return new DefaultValue(name, String.valueOf(currentValue)) {
			public void showDialog () {
				JSpinner spinner = new JSpinner(new SpinnerNumberModel(currentValue, min, max, 0.1f));
				if (showValueDialog(spinner, description)) value = String.valueOf(((Double)spinner.getValue()).floatValue());
			}

			public Object getObject () {
				return Float.valueOf(value);
			}
		};
	}

	/** Returns a value that represents a boolean. */
	static public Value booleanValue (String name, final boolean currentValue, final String description) {
		return new DefaultValue(name, String.valueOf(currentValue)) {
			public void showDialog () {
				JCheckBox checkBox = new JCheckBox();
				checkBox.setSelected(currentValue);
				if (showValueDialog(checkBox, description)) value = String.valueOf(checkBox.isSelected());
			}

			public Object getObject () {
				return Boolean.valueOf(value);
			}
		};
	}

	/** Returns a value that represents a fixed number of options. All options are strings.
	 * @param options The first array has an entry for each option. Each entry is either a String[1] that is both the display value
	 *           and actual value, or a String[2] whose first element is the display value and second element is the actual value. */
	static public Value optionValue (String name, final String currentValue, final String[][] options, final String description) {
		return new DefaultValue(name, currentValue.toString()) {
			public void showDialog () {
				int selectedIndex = -1;
				DefaultComboBoxModel model = new DefaultComboBoxModel();
				for (int i = 0; i < options.length; i++) {
					model.addElement(options[i][0]);
					if (getValue(i).equals(currentValue)) selectedIndex = i;
				}
				JComboBox comboBox = new JComboBox(model);
				comboBox.setSelectedIndex(selectedIndex);
				if (showValueDialog(comboBox, description)) value = getValue(comboBox.getSelectedIndex());
			}

			private String getValue (int i) {
				if (options[i].length == 1) return options[i][0];
				return options[i][1];
			}

			public String toString () {
				for (int i = 0; i < options.length; i++)
					if (getValue(i).equals(value)) return options[i][0].toString();
				return "";
			}

			public Object getObject () {
				return value;
			}
		};
	}

	/** Convers a color to a string. */
	static public String toString (Color color) {
		if (color == null) throw new IllegalArgumentException("color cannot be null.");
		String r = Integer.toHexString(color.getRed());
		if (r.length() == 1) r = "0" + r;
		String g = Integer.toHexString(color.getGreen());
		if (g.length() == 1) g = "0" + g;
		String b = Integer.toHexString(color.getBlue());
		if (b.length() == 1) b = "0" + b;
		return r + g + b;
	}

	/** Converts a string to a color. */
	static public Color fromString (String rgb) {
		if (rgb == null || rgb.length() != 6) return Color.white;
		return new Color(Integer.parseInt(rgb.substring(0, 2), 16), Integer.parseInt(rgb.substring(2, 4), 16), Integer.parseInt(
			rgb.substring(4, 6), 16));
	}

	/** Provides generic functionality for an effect's configurable value. */
	static private abstract class DefaultValue implements Value {
		String value;
		String name;

		public DefaultValue (String name, String value) {
			this.value = value;
			this.name = name;
		}

		public void setString (String value) {
			this.value = value;
		}

		public String getString () {
			return value;
		}

		public String getName () {
			return name;
		}

		public String toString () {
			if (value == null) return "";
			return value.toString();
		}

		public boolean showValueDialog (final JComponent component, String description) {
			ValueDialog dialog = new ValueDialog(component, name, description);
			dialog.setTitle(name);
			dialog.setLocationRelativeTo(null);
			EventQueue.invokeLater(new Runnable() {
				public void run () {
					JComponent focusComponent = component;
					if (focusComponent instanceof JSpinner)
						focusComponent = ((JSpinner.DefaultEditor)((JSpinner)component).getEditor()).getTextField();
					focusComponent.requestFocusInWindow();
				}
			});
			dialog.setVisible(true);
			return dialog.okPressed;
		}
	};

	/** Provides generic functionality for a dialog to configure a value. */
	static private class ValueDialog extends JDialog {
		public boolean okPressed = false;

		public ValueDialog (JComponent component, String name, String description) {
			setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			setLayout(new GridBagLayout());
			setModal(true);

			if (component instanceof JSpinner)
				((JSpinner.DefaultEditor)((JSpinner)component).getEditor()).getTextField().setColumns(4);

			JPanel descriptionPanel = new JPanel();
			descriptionPanel.setLayout(new GridBagLayout());
			getContentPane().add(
				descriptionPanel,
				new GridBagConstraints(0, 0, 2, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0,
					0), 0, 0));
			descriptionPanel.setBackground(Color.white);
			descriptionPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.black));
			{
				JTextArea descriptionText = new JTextArea(description);
				descriptionPanel.add(descriptionText, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER,
					GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
				descriptionText.setWrapStyleWord(true);
				descriptionText.setLineWrap(true);
				descriptionText.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
				descriptionText.setEditable(false);
			}

			JPanel panel = new JPanel();
			getContentPane().add(
				panel,
				new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 5, 0,
					5), 0, 0));
			panel.add(new JLabel(name + ":"));
			panel.add(component);

			JPanel buttonPanel = new JPanel();
			getContentPane().add(
				buttonPanel,
				new GridBagConstraints(0, 2, 2, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
					new Insets(0, 0, 0, 0), 0, 0));
			{
				JButton okButton = new JButton("OK");
				buttonPanel.add(okButton);
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed (ActionEvent evt) {
						okPressed = true;
						setVisible(false);
					}
				});
			}
			{
				JButton cancelButton = new JButton("Cancel");
				buttonPanel.add(cancelButton);
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed (ActionEvent evt) {
						setVisible(false);
					}
				});
			}

			setSize(new Dimension(320, 175));
		}
	}
}

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

package com.badlogic.gdx.setup;

import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.CENTER;
import static java.awt.GridBagConstraints.HORIZONTAL;
import static java.awt.GridBagConstraints.NONE;
import static java.awt.GridBagConstraints.NORTH;
import static java.awt.GridBagConstraints.SOUTH;
import static java.awt.GridBagConstraints.SOUTHEAST;
import static java.awt.GridBagConstraints.WEST;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import com.badlogic.gdx.setup.GdxSetupUI.SetupButton;
import com.badlogic.gdx.setup.GdxSetupUI.SetupCheckBox;

public class SettingsDialog extends JDialog {

	private JPanel contentPane;
	private SetupButton buttonOK;
	private SetupButton buttonCancel;
	private JLabel linkText;
	private JPanel content;
	private JPanel bottomPanel;
	private JPanel buttonPanel;

	private JTextField mavenTextField;
	private SetupCheckBox ideaBox;
	private SetupCheckBox eclipseBox;
	SetupCheckBox offlineBox;
	private String mavenSnapshot;
	private boolean ideaSnapshot;
	private boolean eclipseSnapshot;
	private boolean offlineSnapshot;

	public SettingsDialog () {
		contentPane = new JPanel(new GridBagLayout());
		setContentPane(contentPane);
		setModal(true);
		getRootPane().setDefaultButton(buttonOK);

		uiLayout();
		uiStyle();

		buttonOK.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent e) {
				if (offlineBox.isSelected()) {
					int value = JOptionPane.showConfirmDialog(null, "You have selected offline mode. This requires you to have your dependencies already in your maven/gradle cache.\n\nThe setup will fail if you do not have the correct dependenices already.\n\nDo you want to continue?", "Warning!", JOptionPane.YES_NO_OPTION);
					if (value == 0) {
						onOK();
					}
				} else {
					onOK();
				}
			}
		});
		buttonCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed (ActionEvent e) {
				onCancel();
			}
		});

		linkText.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		linkText.addMouseListener(new MouseAdapter() {
			public void mouseClicked (MouseEvent e) {
				if (e.getClickCount() > 0) {
					if (Desktop.isDesktopSupported()) {
						Desktop desktop = Desktop.getDesktop();
						try {
							URI uri = new URI(
								"https://github.com/libgdx/libgdx/wiki/Improving-workflow-with-Gradle#how-to-remove-gradle-ide-integration-from-your-project");
							desktop.browse(uri);
						} catch (IOException ex) {
							ex.printStackTrace();
						} catch (URISyntaxException ex) {
							ex.printStackTrace();
						}
					}
				}
			}
		});

		setTitle("Advanced Settings");
		setSize(600, 300);
		setLocationRelativeTo(null);
	}

	private void uiLayout () {
		content = new JPanel(new GridBagLayout());
		content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

		bottomPanel = new JPanel(new GridBagLayout());

		buttonPanel = new JPanel(new GridBagLayout());
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		buttonOK = new SetupButton("Save");
		buttonCancel = new SetupButton("Cancel");
		buttonPanel.add(buttonOK, new GridBagConstraints(0, 0, 1, 1, 0, 0, CENTER, HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		buttonPanel.add(buttonCancel, new GridBagConstraints(1, 0, 1, 1, 0, 0, CENTER, HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

		contentPane.add(content, new GridBagConstraints(0, 0, 1, 1, 1, 1, NORTH, BOTH, new Insets(0, 0, 0, 0), 0, 0));

		JLabel settings = new JLabel("Settings");
		JLabel description = new JLabel("Description");
		settings.setForeground(new Color(255, 255, 255));
		description.setForeground(new Color(255, 255, 255));

		settings.setHorizontalAlignment(JLabel.CENTER);
		description.setHorizontalAlignment(JLabel.CENTER);

		content.add(settings, new GridBagConstraints(0, 0, 1, 1, 1, 1, NORTH, HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		content.add(description, new GridBagConstraints(3, 0, 1, 1, 1, 1, NORTH, HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

		JLabel mavenLabel = new JLabel("Maven Mirror Url");
		JLabel mavenDesc = new JLabel("Replaces Maven Central with this repository");
		mavenTextField = new JTextField(15);
		mavenTextField.setMinimumSize(mavenTextField.getPreferredSize());
		mavenLabel.setForeground(new Color(170, 170, 170));
		mavenDesc.setForeground(new Color(170, 170, 170));
		JLabel ideaLabel = new JLabel("IDEA");
		JLabel ideaDesc = new JLabel("Generates Intellij IDEA project files");
		ideaBox = new SetupCheckBox();
		ideaLabel.setForeground(new Color(170, 170, 170));
		ideaDesc.setForeground(new Color(170, 170, 170));
		ideaBox.setBackground(new Color(36, 36, 36));
		JLabel eclipseLabel = new JLabel("Eclipse");
		JLabel eclipseDesc = new JLabel("Generates Eclipse project files");
		eclipseBox = new SetupCheckBox();
		eclipseLabel.setForeground(new Color(170, 170, 170));
		eclipseDesc.setForeground(new Color(170, 170, 170));
		eclipseBox.setBackground(new Color(36, 36, 36));
		JLabel offlineLabel = new JLabel("Offline Mode");
		JLabel offlineDesc = new JLabel("Don't force download dependencies");
		offlineBox = new SetupCheckBox();
		offlineLabel.setForeground(new Color(170, 170, 170));
		offlineDesc.setForeground(new Color(170, 170, 170));
		offlineBox.setBackground(new Color(36, 36, 36));

		JSeparator separator = new JSeparator();
		separator.setForeground(new Color(85, 85, 85));
		separator.setBackground(new Color(85, 85, 85));

		content.add(separator, new GridBagConstraints(0, 1, 4, 1, 1, 1, NORTH, HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

		content.add(mavenLabel, new GridBagConstraints(0, 2, 1, 1, 1, 1, NORTH, HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		content.add(mavenTextField, new GridBagConstraints(1, 2, 2, 1, 1, 1, NORTH, HORIZONTAL, new Insets(0, 15, 0, 0), 0, 0));
		content.add(mavenDesc, new GridBagConstraints(3, 2, 1, 1, 1, 1, NORTH, HORIZONTAL, new Insets(0, 15, 0, 0), 0, 0));

		content.add(ideaLabel, new GridBagConstraints(0, 3, 1, 1, 1, 1, NORTH, HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		content.add(ideaBox, new GridBagConstraints(1, 3, 2, 1, 1, 1, NORTH, HORIZONTAL, new Insets(0, 15, 0, 0), 0, 0));
		content.add(ideaDesc, new GridBagConstraints(3, 3, 2, 1, 1, 1, NORTH, HORIZONTAL, new Insets(0, 15, 0, 0), 0, 0));

		content.add(eclipseLabel, new GridBagConstraints(0, 4, 1, 1, 1, 1, NORTH, HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		content.add(eclipseBox, new GridBagConstraints(1, 4, 2, 1, 1, 1, NORTH, HORIZONTAL, new Insets(0, 15, 0, 0), 0, 0));
		content.add(eclipseDesc, new GridBagConstraints(3, 4, 1, 1, 1, 1, NORTH, HORIZONTAL, new Insets(0, 15, 0, 0), 0, 0));
		
		content.add(offlineLabel, new GridBagConstraints(0, 5, 1, 1, 1, 1, NORTH, HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		content.add(offlineBox, new GridBagConstraints(1, 5, 2, 1, 1, 1, NORTH, HORIZONTAL, new Insets(0, 15, 0, 0), 0, 0));
		content.add(offlineDesc, new GridBagConstraints(3, 5, 1, 1, 1, 1, NORTH, HORIZONTAL, new Insets(0, 15, 0, 0), 0, 0));


		String text = "<p style=\"font-size:10\">Click for more info on using Gradle without IDE integration</p>";
		linkText = new JLabel("<html>" + text + "</html>");

		bottomPanel.add(linkText, new GridBagConstraints(0, 0, 1, 1, 1, 1, WEST, NONE, new Insets(0, 10, 0, 0), 0, 0));
		bottomPanel.add(buttonPanel, new GridBagConstraints(3, 0, 1, 1, 1, 1, SOUTHEAST, NONE, new Insets(0, 0, 0, 0), 0, 0));

		contentPane.add(bottomPanel, new GridBagConstraints(0, 1, 4, 1, 1, 1, SOUTH, HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

	}

	private void uiStyle () {
		content.setBackground(new Color(36, 36, 36));
		content.setForeground(new Color(255, 255, 255));
		bottomPanel.setBackground(new Color(36, 36, 36));
		bottomPanel.setForeground(new Color(255, 255, 255));
		buttonPanel.setBackground(new Color(36, 36, 36));
		buttonPanel.setForeground(new Color(255, 255, 255));
		linkText.setForeground(new Color(20, 150, 20));

		contentPane.setBackground(new Color(36, 36, 36));
		Border line = BorderFactory.createLineBorder(new Color(80, 80, 80));
		Border empty = new EmptyBorder(4, 4, 4, 4);
		CompoundBorder border = new CompoundBorder(line, empty);
		mavenTextField.setBorder(border);
		mavenTextField.setCaretColor(new Color(255, 255, 255));
		mavenTextField.setBackground(new Color(46, 46, 46));
		mavenTextField.setForeground(new Color(255, 255, 255));
	}

	public void showDialog () {
		takeSnapshot();
		setVisible(true);
	}

	public List<String> getGradleArgs () {
		List<String> list = new ArrayList<String>();
		list.add("--no-daemon");
		if (offlineBox.isSelected()) {
			list.add("--offline");	
		}
		if (eclipseBox.isSelected()) {
			list.add("eclipse");
			list.add("afterEclipseImport");
		}
		if (ideaBox.isSelected()) {
			list.add("idea");
		}
		return list;
	}

	void onOK () {
		if (mavenTextField.getText().isEmpty()) {
			DependencyBank.mavenCentral = "mavenCentral()";
		} else {
			DependencyBank.mavenCentral = "maven { url \"" + mavenTextField.getText() + "\" }";
		}
		setVisible(false);
	}

	void onCancel () {
		setVisible(false);
		restore();
	}

	private void takeSnapshot () {
		mavenSnapshot = mavenTextField.getText();
		ideaSnapshot = ideaBox.isSelected();
		eclipseSnapshot = eclipseBox.isSelected();
		offlineSnapshot = offlineBox.isSelected();
	}

	private void restore () {
		mavenTextField.setText(mavenSnapshot);
		ideaBox.setSelected(ideaSnapshot);
		eclipseBox.setSelected(eclipseSnapshot);
		offlineBox.setSelected(offlineSnapshot);
	}

}

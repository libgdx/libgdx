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

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.GridBagConstraints;
import static java.awt.GridBagConstraints.*;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;

import com.badlogic.gdx.setup.DependencyBank.AndroidAppType;

public class SettingsDialog extends JDialog {
	/**
	 * 
	 */
	private static final long	serialVersionUID	= -6619360670209786255L;
	protected static final Border empty4Border = BorderFactory.createEmptyBorder(4, 4, 4, 4);
	protected static final Color
		COLOR_DARKLIMEGREEN	= new Color(20, 150, 20),
		COLOR_GRAY36		= new Color(36, 36, 36),
		COLOR_GRAY46		= new Color(46, 46, 46),
		COLOR_GRAY80		= new Color(80, 80, 80),
		COLOR_GRAY85		= new Color(85, 85, 85),
		COLOR_GRAY170		= new Color(170, 170, 170),
		COLOR_WHITE			= Color.WHITE;

	private JPanel
			contentPane,
			idesPanel,
			androidProjectTypePanel,
			buttonPanel,navPanel;
	private JLabel linkHTML;
	private JTextField mavenTextField;
	private JCheckBox ideaBox, eclipseBox, offlineBox;
	public ButtonGroup androidProjectTypeRdoGrp;
	private JButton buttonSave, buttonCancel;
	
	private String mavenSnapshot;
	private boolean ideaSnapshot;
	private boolean eclipseSnapshot;
	private boolean offlineSnapshot;
	private ButtonModel androidAppTypeSnapshot;
	
	public SettingsDialog () {
		contentPane = new JPanel(new GridBagLayout());
		setContentPane(contentPane);
		setModal(true);
		getRootPane().setDefaultButton(buttonSave);

		uiLayout();
		uiStyle();

		buttonSave.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent e) {
				if (offlineBox.isSelected()) {
					int value = JOptionPane.showConfirmDialog(null, "You have selected offline mode. This requires you to have your dependencies already in your maven/gradle cache.\n\nThe setup will fail if you do not have the correct dependenices already.\n\nDo you want to continue?", "Warning!", JOptionPane.YES_NO_OPTION);
					if (value == 0) {
						onSave();
					}
				} else {
					onSave();
				}
			}
		});
		buttonCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed (ActionEvent e) {
				onCancel();
			}
		});
		getRootPane().registerKeyboardAction
		(
			new ActionListener() {
		        @Override
		        public void actionPerformed(ActionEvent e)
		        {	onCancel();	}
		    },
		    KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
	        JComponent.WHEN_IN_FOCUSED_WINDOW
		);

		linkHTML.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		linkHTML.addMouseListener(new MouseAdapter() {
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
		// UI widgets:
		JLabel idesSettingsLbl = new JLabel("Settings:");
		idesSettingsLbl.setForeground(COLOR_WHITE);
		idesSettingsLbl.setHorizontalAlignment(JLabel.RIGHT);
		
		JLabel idesDescLbl = new JLabel("Description");
		idesDescLbl.setForeground(COLOR_WHITE);
		idesDescLbl.setHorizontalAlignment(JLabel.CENTER);

		JLabel mavenLabel = new JLabel("Maven Mirror Url");
		JLabel mavenDesc = new JLabel("Replaces Maven Central with this repository");
		mavenTextField = new JTextField(15);
		mavenTextField.setMinimumSize(mavenTextField.getPreferredSize());
		mavenLabel.setForeground(COLOR_GRAY170);
		mavenDesc.setForeground(COLOR_GRAY170);
		
		JLabel ideaLabel = new JLabel("IDEA");
		JLabel ideaDesc = new JLabel("Generates Intellij IDEA project files");
		ideaBox = new JCheckBox();
		ideaLabel.setForeground(COLOR_GRAY170);
		ideaDesc.setForeground(COLOR_GRAY170);
		ideaBox.setBackground(COLOR_GRAY36);
		
		JLabel eclipseLabel = new JLabel("Eclipse");
		JLabel eclipseDesc = new JLabel("Generates Eclipse project files");
		eclipseBox = new JCheckBox();
		eclipseLabel.setForeground(COLOR_GRAY170);
		eclipseDesc.setForeground(COLOR_GRAY170);
		eclipseBox.setBackground(COLOR_GRAY36);
		
		JLabel offlineLabel = new JLabel("Offline Mode");
		JLabel offlineDesc = new JLabel("Don't force download dependencies");
		offlineBox = new JCheckBox();
		offlineLabel.setForeground(COLOR_GRAY170);
		offlineDesc.setForeground(COLOR_GRAY170);
		offlineBox.setBackground(COLOR_GRAY36);
		
		JLabel androidAppTypeLbl = new JLabel("Android App Type:");
		androidAppTypeLbl.setForeground(COLOR_WHITE);
		androidAppTypeLbl.setHorizontalAlignment(JLabel.RIGHT);
		
		JLabel androidAppTypeDescLbl = new JLabel("Description");		
		androidAppTypeDescLbl.setForeground(COLOR_WHITE);
		androidAppTypeDescLbl.setHorizontalAlignment(JLabel.CENTER);

		JLabel
			androidAppLbl = new JLabel(DependencyBank.AndroidAppType.AndroidApplication.name()),
			androidAppDescLbl = new JLabel(DependencyBank.AndroidAppType.AndroidApplication.description);
		JRadioButton androidAppRdo = new JRadioButton();
		androidAppRdo.setActionCommand(androidAppLbl.getText());
		androidAppRdo.addItemListener(createAndroidAppTypeRdoItemLstnr());
		androidAppRdo.setBackground(COLOR_GRAY36);
		
		androidAppLbl.setForeground(COLOR_GRAY170);
		androidAppDescLbl.setForeground(COLOR_GRAY170);
		
		androidAppRdo.setSelected(true);
		
		JLabel
			fragmentActivityLbl = new JLabel(DependencyBank.AndroidAppType.FragmentActivity.name()),
			fragmentActivityDescLbl = new JLabel(DependencyBank.AndroidAppType.FragmentActivity.description);
		JRadioButton fragmentActivityRdo = new JRadioButton();
		fragmentActivityRdo.setActionCommand(fragmentActivityLbl.getText());
		fragmentActivityRdo.addItemListener(createAndroidAppTypeRdoItemLstnr());
		fragmentActivityLbl.setForeground(COLOR_GRAY170);
		fragmentActivityDescLbl.setForeground(COLOR_GRAY170);
		fragmentActivityRdo.setBackground(COLOR_GRAY36);
		
		androidProjectTypeRdoGrp = new ButtonGroup();
		androidProjectTypeRdoGrp.add(androidAppRdo);
		androidProjectTypeRdoGrp.add(fragmentActivityRdo);

		// Link to info:
		String html = "<p style=\"font-size:10\">Click for more info on using Gradle without IDE integration</p>";
		linkHTML = new JLabel("<html>" + html + "</html>");
		
		// OK, Cancel buttons:
		buttonSave = new JButton("Save");
		buttonSave.setMnemonic(KeyEvent.VK_S);
		buttonCancel = new JButton("Cancel");
		buttonCancel.setMnemonic(KeyEvent.VK_C);
		
		JSeparator
			separatorIDEsTop = createJSeparator(COLOR_GRAY85),
			separatorIDEsBot = createJSeparator(COLOR_GRAY85),
			separatorAndroidProjectTypeTop = createJSeparator(COLOR_GRAY85),
			separatorAndroidProjectTypeBot = createJSeparator(COLOR_GRAY85);
		
		
		// Layout:
		int gridY;
		final Insets
			insets0 = new Insets(0, 0, 0, 0),
			insetsLeft15 = new Insets(0, 15, 0, 0);
		
		// Main panels:
		idesPanel = new JPanel(new GridBagLayout());
		androidProjectTypePanel = new JPanel(new GridBagLayout());
		navPanel = new JPanel(new GridBagLayout());
		
		gridY = 0;
		contentPane.add(idesPanel, createGridBagConstraints(0, gridY++, 3, insets0));
		contentPane.add(androidProjectTypePanel, createGridBagConstraints(0, gridY++, 3, insets0));
		contentPane.add(navPanel, createGridBagConstraints(0, gridY++, 3, insets0));
		
		
		// IDEs:
		idesPanel.setBorder(empty4Border);
		gridY = 0;
		
		// Settings, Description labels row:
		idesPanel.add(idesSettingsLbl, createGridBagConstraints(0, gridY, 1, insets0));
		idesPanel.add(idesDescLbl, createGridBagConstraints(2, gridY, 1, insets0));
		gridY++;

		// Separator:
		idesPanel.add(separatorIDEsTop, createGridBagConstraints(0, gridY++, 3, insets0));
		
		
		// Maven:
		idesPanel.add(mavenLabel, createGridBagConstraints(0, gridY, 1, insets0));
		idesPanel.add(mavenTextField, createGridBagConstraints(1, gridY, 1, insetsLeft15));
		idesPanel.add(mavenDesc, createGridBagConstraints(2, gridY, 1, insetsLeft15));
		gridY++;
		
		// IDEA:
		idesPanel.add(ideaLabel, createGridBagConstraints(0, gridY, 1, insets0));
		idesPanel.add(ideaBox, createGridBagConstraints(1, gridY, 1, insetsLeft15));
		idesPanel.add(ideaDesc, createGridBagConstraints(2, gridY, 1, insetsLeft15));
		gridY++;
		
		// Eclipse:
		idesPanel.add(eclipseLabel, createGridBagConstraints(0, gridY, 1, insets0));
		idesPanel.add(eclipseBox, createGridBagConstraints(1, gridY, 1, insetsLeft15));
		idesPanel.add(eclipseDesc, createGridBagConstraints(2, gridY, 1, insetsLeft15));
		gridY++;
		
		// Offline:
		idesPanel.add(offlineLabel, createGridBagConstraints(0, gridY, 1, insets0));
		idesPanel.add(offlineBox, createGridBagConstraints(1, gridY, 1, insetsLeft15));
		idesPanel.add(offlineDesc, createGridBagConstraints(2, gridY, 1, insetsLeft15));
		gridY++;
		
		// Separator
		idesPanel.add(separatorIDEsBot, createGridBagConstraints(0, gridY++, 3, insets0));
		
		
		// Android App Type:
		androidProjectTypePanel.setBorder(empty4Border);
		gridY = 0;
		
		// Android App Type, Description Labels:
		androidProjectTypePanel.add(androidAppTypeLbl, createGridBagConstraints(0, gridY, 1, insets0));
		androidProjectTypePanel.add(androidAppTypeDescLbl, createGridBagConstraints(2, gridY, 1, insets0));
		gridY++;
		
		// Separator:
		androidProjectTypePanel.add(separatorAndroidProjectTypeTop, createGridBagConstraints(0, gridY++, 3, insets0));

		// AndroidApplication type:
		androidProjectTypePanel.add(androidAppLbl, createGridBagConstraints(0, gridY, 1, insets0));
		androidProjectTypePanel.add(androidAppRdo, createGridBagConstraints(1, gridY, 1, insetsLeft15));
		androidProjectTypePanel.add(androidAppDescLbl, createGridBagConstraints(2, gridY, 1, insetsLeft15));
		gridY++;
		
		// FragmentActivity type:
		androidProjectTypePanel.add(fragmentActivityLbl, createGridBagConstraints(0, gridY, 1, insets0));
		androidProjectTypePanel.add(fragmentActivityRdo, createGridBagConstraints(1, gridY, 1, insetsLeft15));
		androidProjectTypePanel.add(fragmentActivityDescLbl, createGridBagConstraints(2, gridY, 1, insetsLeft15));
		gridY++;
		
		// Separator:
		androidProjectTypePanel.add(separatorAndroidProjectTypeBot, createGridBagConstraints(0, gridY++, 3, insets0));
		
		
		// Navigation panel:
		navPanel.setBorder(empty4Border);
		gridY = 0;
		
		// Info link; OK, Cancel buttons in bottom panel:
		buttonPanel = new JPanel(new GridBagLayout());
		buttonPanel.setBorder(empty4Border);
		buttonPanel.add(buttonSave, createGridBagConstraints(0, gridY, 1, insets0));
		buttonPanel.add(buttonCancel, createGridBagConstraints(1, gridY, 1, insets0));
		
		navPanel.add(linkHTML, createGridBagConstraints(0, gridY, 1, insetsLeft15));
		navPanel.add(buttonPanel, createGridBagConstraints(2, gridY, 1, insets0));
	}
	
	private ItemListener createAndroidAppTypeRdoItemLstnr()
	{
		return
		(new
			ItemListener()
			{
				@Override
				public void itemStateChanged(ItemEvent e) {
				    if(e.getStateChange() == ItemEvent.SELECTED)
				    {	DependencyBank.androidAppType = AndroidAppType.valueOf(((AbstractButton)e.getSource()).getActionCommand());	}
				}
			}
		);
	}
	
	private GridBagConstraints createGridBagConstraints(int gridX, int gridY, int width, Insets insets)
	{	return(new GridBagConstraints(gridX, gridY, width, 1, 1, 1, NORTH, HORIZONTAL, insets, 0, 0));	}
	
	private JSeparator createJSeparator(Color color) {
		JSeparator separator = new JSeparator();
		separator.setForeground(color);
		separator.setBackground(color);
		
		
		return(separator);
	}
	
	private void uiStyle () {
		contentPane.setBackground(COLOR_GRAY36);
		contentPane.setForeground(COLOR_WHITE);
		
		idesPanel.setBackground(COLOR_GRAY36);
		idesPanel.setForeground(COLOR_WHITE);
		
		androidProjectTypePanel.setBackground(COLOR_GRAY36);
		androidProjectTypePanel.setForeground(COLOR_WHITE);
		
		navPanel.setBackground(COLOR_GRAY36);
		navPanel.setForeground(COLOR_WHITE);
		
		buttonPanel.setBackground(COLOR_GRAY36);
		buttonPanel.setForeground(COLOR_WHITE);
		linkHTML.setForeground(COLOR_DARKLIMEGREEN);

		Border lineBorder = BorderFactory.createLineBorder(COLOR_GRAY80);
		Border emptyBorder = empty4Border;
		CompoundBorder border = new CompoundBorder(lineBorder, emptyBorder);
		mavenTextField.setBorder(border);
		mavenTextField.setCaretColor(COLOR_WHITE);
		mavenTextField.setBackground(COLOR_GRAY46);
		mavenTextField.setForeground(COLOR_WHITE);
	}
	
	public void setAndroidProjectTypeEnabled(boolean enabled)
	{ // Disable Android Project Type selection
		for(Component component : androidProjectTypePanel.getComponents())
		{	component.setEnabled(enabled);	}
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

	void onSave () {
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
		androidAppTypeSnapshot = androidProjectTypeRdoGrp.getSelection();
	}

	private void restore () {
		mavenTextField.setText(mavenSnapshot);
		ideaBox.setSelected(ideaSnapshot);
		eclipseBox.setSelected(eclipseSnapshot);
		offlineBox.setSelected(offlineSnapshot);
		androidProjectTypeRdoGrp.setSelected(androidAppTypeSnapshot, true);
	}

}

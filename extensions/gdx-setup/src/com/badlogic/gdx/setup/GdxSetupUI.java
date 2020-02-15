/*******************************************************************************
 * Copyright 2014 See AUTHORS file.
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
import static java.awt.GridBagConstraints.EAST;
import static java.awt.GridBagConstraints.HORIZONTAL;
import static java.awt.GridBagConstraints.NONE;
import static java.awt.GridBagConstraints.NORTH;
import static java.awt.GridBagConstraints.NORTHEAST;
import static java.awt.GridBagConstraints.NORTHWEST;
import static java.awt.GridBagConstraints.VERTICAL;
import static java.awt.GridBagConstraints.WEST;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FileDialog;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import com.badlogic.gdx.setup.DependencyBank.ProjectDependency;
import com.badlogic.gdx.setup.DependencyBank.ProjectType;
import com.badlogic.gdx.setup.Executor.CharCallback;

@SuppressWarnings("serial")
public class GdxSetupUI extends JFrame {

	//DependencyBank dependencyBank;
	ProjectBuilder builder;
	List<ProjectType> modules = new ArrayList<ProjectType>();
	List<Dependency> dependencies = new ArrayList<Dependency>();

	UI ui = new UI();
	static Point point = new Point();
	static SetupPreferences prefs = new SetupPreferences();

	public GdxSetupUI () {
		setTitle("LibGDX Project Generator");
		setLayout(new BorderLayout());
		add(ui, BorderLayout.CENTER);
		setSize(620, 720);
		setLocationRelativeTo(null);
		setUndecorated(true);		
		pack();
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				point.x = e.getX();
				point.y = e.getY();
			}
		});
		addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseDragged(MouseEvent e) {
				Point p = getLocation();
				setLocation(p.x + e.getX() - point.x, p.y + e.getY() - point.y);
			}
		});
		setVisible(true);

		builder = new ProjectBuilder(new DependencyBank());
		modules.add(ProjectType.CORE);
		dependencies.add(builder.bank.getDependency(ProjectDependency.GDX));
		dependencies.add(builder.bank.getDependency(ProjectDependency.BOX2D));
	}

	void generate () {
		final String name = ui.form.nameText.getText().trim();
		if (name.length() == 0) {
			JOptionPane.showMessageDialog(this, "Please enter a project name.");
			return;
		}

		final String pack = ui.form.packageText.getText().trim();
		if (pack.length() == 0) {
			JOptionPane.showMessageDialog(this, "Please enter a package name.");
			return;
		}
		Pattern pattern = Pattern.compile("[a-z][a-z0-9_]*(\\.[a-z0-9_]+)+[0-9a-z_]");
		Matcher matcher = pattern.matcher(pack);
		boolean matches = matcher.matches();

		if (!matches) {
			JOptionPane.showMessageDialog(this, "Invalid package name");
			return;
		}

		final String clazz = ui.form.gameClassText.getText().trim();
		if (clazz.length() == 0) {
			JOptionPane.showMessageDialog(this, "Please enter a game class name.");
			return;
		}
		
		final Language languageEnum = ui.settings.kotlinBox.isSelected() ? Language.KOTLIN : Language.JAVA;

		final String destination = ui.form.destinationText.getText().trim();
		if (destination.length() == 0) {
			JOptionPane.showMessageDialog(this, "Please enter a destination directory.");
			return;
		}

		final String sdkLocation = ui.form.sdkLocationText.getText().trim();
		if (sdkLocation.length() == 0 && modules.contains(ProjectType.ANDROID)) {
			JOptionPane.showMessageDialog(this, "Please enter your Android SDK's path");
			return;
		}
		if (!GdxSetup.isSdkLocationValid(sdkLocation) && modules.contains(ProjectType.ANDROID)) {
			JOptionPane
					.showMessageDialog(this,
							"Your Android SDK path doesn't contain an SDK! Please install the Android SDK, including all platforms and build tools!");
			return;
		}
		
		if (modules.contains(ProjectType.HTML) && !languageEnum.gwtSupported) {
			JOptionPane.showMessageDialog(this, "HTML sub-projects are not supported by the selected programming language.");
			ui.form.gwtCheckBox.setSelected(false);
			modules.remove(ProjectType.HTML);
		}

		if (modules.contains(ProjectType.ANDROID)) {
			if (!GdxSetup.isSdkUpToDate(sdkLocation)) {
				File sdkLocationFile = new File(sdkLocation);
				try {  //give them a poke in the right direction
					if (System.getProperty("os.name").contains("Windows")) {
						String replaced = sdkLocation.replace("\\", "\\\\");
						Runtime.getRuntime().exec("\"" + replaced + "\\SDK Manager.exe\"");
					} else {
						File sdkManager = new File(sdkLocation, "tools/android");
						Runtime.getRuntime().exec(new String[] {sdkManager.getAbsolutePath(), "sdk"});
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				return;
			}
		}

		if (!GdxSetup.isEmptyDirectory(destination)) {
			int value = JOptionPane.showConfirmDialog(this, "The destination is not empty, do you want to overwrite?", "Warning!", JOptionPane.YES_NO_OPTION);
			if (value != 0) {
				return;
			}
		}

		List<String> incompatList = builder.buildProject(modules, dependencies);
		if (incompatList.size() == 0) {
			try {
				builder.build(languageEnum);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			JPanel panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
			for (String subIncompat : incompatList) {
				JLabel label = new JLabel(subIncompat);
				label.setAlignmentX(Component.CENTER_ALIGNMENT);
				panel.add(label);
			}

			JLabel infoLabel = new JLabel("<html><br><br>The project can be generated, but you wont be able to use these extensions in the respective sub modules<br>Please see the link to learn about extensions</html>");
			infoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
			panel.add(infoLabel);
			JEditorPane pane = new JEditorPane("text/html", "<a href=\"https://github.com/libgdx/libgdx/wiki/Dependency-management-with-Gradle\">Dependency Management</a>");
			pane.addHyperlinkListener(new HyperlinkListener() {
				@Override
				public void hyperlinkUpdate(HyperlinkEvent e) {
					if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED))
						try {
							Desktop.getDesktop().browse(new URI(e.getURL().toString()));
						} catch (IOException e1) {
							e1.printStackTrace();
						} catch (URISyntaxException e1) {
							e1.printStackTrace();
						}
				}
			});
			pane.setEditable(false);
			pane.setOpaque(false);
			pane.setAlignmentX(Component.CENTER_ALIGNMENT);
			panel.add(pane);

			Object[] options = {"Yes, build it!", "No, I'll change my extensions"};
			int value = JOptionPane.showOptionDialog(null, panel, "Extension Incompatibilities", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, null);
			if (value != 0) {
				return;
			} else {
				try {
					builder.build(languageEnum);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		ui.generateButton.setEnabled(false);
		new Thread() {
			public void run () {
				log("Generating app in " + destination);
				new GdxSetup().build(builder, destination, name, pack, clazz, languageEnum, sdkLocation, new CharCallback() {
					@Override
					public void character (char c) {
						log(c);
					}
				}, ui.settings.getGradleArgs());
				log("Done!");
				log("To import in Eclipse: File -> Import -> Gradle -> Gradle Project");
				log("To import to Intellij IDEA: File -> Open -> build.gradle");
				log("To import to NetBeans: File -> Open Project...");
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						ui.generateButton.setEnabled(true);
					}
				});
			}
		}.start();
	}

	void log (final char c) {
		EventQueue.invokeLater(new Runnable() {
			public void run () {
				ui.textArea.append("" + c);
				ui.textArea.setCaretPosition(ui.textArea.getDocument().getLength());
			}
		});
	}

	void log (final String text) {
		EventQueue.invokeLater(new Runnable() {
			public void run () {
				ui.textArea.append(text + "\n");
				ui.textArea.setCaretPosition(ui.textArea.getDocument().getLength());
			}
		});
	}

	class UI extends JPanel {
		Form form = new Form();
		SettingsDialog settings;
		SetupButton generateButton = new SetupButton("Generate");
		SetupButton advancedButton = new SetupButton("Advanced");
		JPanel buttonPanel = new JPanel();
		JTextArea textArea = new JTextArea(8, 40);
		JScrollPane scrollPane = new JScrollPane(textArea);
		JPanel title = new JPanel();
		JPanel topBar = new JPanel();
		JLabel windowLabel = new JLabel("    Libgdx Project Generator");
		JButton exit;
		JButton minimize;
		JLabel logo;

		{
			setBackground(new Color(36, 36, 36));
			topBar.setBackground(new Color(64, 67, 69));
			title.setBackground(new Color(94, 97, 99));
			windowLabel.setForeground(new Color(255, 255, 255));
			form.setBackground(new Color(36, 36, 36));
			for (int i = 0; i < form.getComponents().length; i++) {
				Component component = form.getComponents()[i];
				if (component instanceof JTextField) {
					component.setBackground(new Color(46, 46, 46));
					component.setForeground(new Color(255, 255, 255));
					Border line = BorderFactory.createEtchedBorder();
					Border pad = new EmptyBorder(0, 5, 0, 0);
					CompoundBorder compoundBorder = new CompoundBorder(line, pad);
					((JComponent) component).setBorder(compoundBorder);
					continue;
				}
			}
			buttonPanel.setBackground(new Color(46, 46, 46));
			textArea.setBackground(new Color(46, 46, 46));
			textArea.setForeground(new Color(255, 255, 255));

			Border line = BorderFactory.createLineBorder(Color.DARK_GRAY);
			scrollPane.setBorder(line);

			try {
				BufferedImage exitimg = ImageIO.read(GdxSetupUI.class.getResourceAsStream("/com/badlogic/gdx/setup/data/exitup.png"));
				BufferedImage minimg = ImageIO.read(GdxSetupUI.class.getResourceAsStream("/com/badlogic/gdx/setup/data/minimizeup.png"));
				BufferedImage exitimgdown = ImageIO.read(GdxSetupUI.class.getResourceAsStream("/com/badlogic/gdx/setup/data/exitdown.png"));
				BufferedImage minimgdown = ImageIO.read(GdxSetupUI.class.getResourceAsStream("/com/badlogic/gdx/setup/data/minimizedown.png"));
				BufferedImage exithover = ImageIO.read(GdxSetupUI.class.getResourceAsStream("/com/badlogic/gdx/setup/data/exithover.png"));
				BufferedImage minimghover = ImageIO.read(GdxSetupUI.class.getResourceAsStream("/com/badlogic/gdx/setup/data/minimizehover.png"));

				BufferedImage img = ImageIO.read(GdxSetupUI.class.getResourceAsStream("/com/badlogic/gdx/setup/data/logo.png"));
				ImageIcon icon = new ImageIcon(img);
				ImageIcon exitIcon = new ImageIcon(exitimg);
				ImageIcon minIcon = new ImageIcon(minimg);
				ImageIcon exitIconDown = new ImageIcon(exitimgdown);
				ImageIcon minIconDown = new ImageIcon(minimgdown);
				ImageIcon exitIconHover = new ImageIcon(exithover);
				ImageIcon minIconHover = new ImageIcon(minimghover);
				logo = new JLabel(icon);
				exit = new JButton(exitIcon);
				minimize = new JButton(minIcon);

				exit.setOpaque(false);
				exit.setContentAreaFilled(false);
				exit.setFocusPainted(false);
				exit.setBorderPainted(false);
				exit.setPressedIcon(exitIconDown);
				exit.setRolloverIcon(exitIconHover);

				minimize.setOpaque(false);
				minimize.setContentAreaFilled(false);
				minimize.setFocusPainted(false);
				minimize.setBorderPainted(false);
				minimize.setPressedIcon(minIconDown);
				minimize.setRolloverIcon(minIconHover);
			} catch (IOException e) {
				e.printStackTrace();
			}

			buttonPanel.add(advancedButton);
			buttonPanel.add(generateButton);
			textArea.setEditable(false);
			textArea.setLineWrap(true);
			uiLayout();
			uiEvents(); 
			settings = new SettingsDialog(form.gwtCheckBox);
			titleEvents(minimize, exit);
		}

		private void titleEvents(JButton minimize, JButton exit) {
			minimize.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					setState(ICONIFIED);
				}
			});
			exit.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					dispose();
					System.exit(0);
				}
			});
		}

		private void uiLayout () {
			title.setLayout(new GridLayout(1, 2));
			
			minimize.setPreferredSize(new Dimension(50, 26));
			exit.setPreferredSize(new Dimension(50, 26));

			title.add(minimize);
			title.add(exit);

			topBar.setLayout(new GridLayout(1, 1));
			topBar.add(windowLabel, new GridBagConstraints(0, 0, 0, 0, 0, 0, NORTHWEST, VERTICAL, new Insets(0, 0, 0, 0), 0, 0));

			setLayout(new GridBagLayout());
			add(topBar, new GridBagConstraints(0, 0, 0, 0, 0, 0, NORTH, HORIZONTAL, new Insets(0, 0, 0, 100), 0, 10));
			add(title, new GridBagConstraints(0, 0, 0, 0, 0, 0, NORTHEAST, NONE, new Insets(0, 0, 0, 0), 0, 0));
			add(logo, new GridBagConstraints(0, 0, 1, 1, 1, 0, CENTER, HORIZONTAL, new Insets(40, 6, 6, 6), 0, 0));
			add(form, new GridBagConstraints(0, 1, 1, 1, 1, 0, CENTER, HORIZONTAL, new Insets(6, 6, 0, 6), 0, 0));
			add(buttonPanel, new GridBagConstraints(0, 2, 1, 1, 0, 0, CENTER, NONE, new Insets(0, 0, 0, 0), 0, 0));
			add(scrollPane, new GridBagConstraints(0, 3, 1, 1, 1, 1, CENTER, BOTH, new Insets(6, 6, 6, 6), 0, 0));
		}

		private void uiEvents () {
			advancedButton.addActionListener(new ActionListener() {
				public void actionPerformed (ActionEvent e) {
					settings.showDialog(form.gwtCheckBox);
				}
			});
			generateButton.addActionListener(new ActionListener() {
				public void actionPerformed (ActionEvent e) {
					generate();
				}
			});
		}
	}

	class Form extends JPanel {
		ExternalExtensionsDialog externalExtensionsDialog = new ExternalExtensionsDialog(dependencies);
		JLabel nameLabel = new JLabel("Name:");
		JTextField nameText = new JTextField("my-gdx-game");
		JLabel packageLabel = new JLabel("Package:");
		JTextField packageText = new JTextField("com.mygdx.game");
		JLabel gameClassLabel = new JLabel("Game class:");
		JTextField gameClassText = new JTextField("MyGdxGame");
		JLabel destinationLabel = new JLabel("Destination:");
		JTextField destinationText = new JTextField(new File("test").getAbsolutePath());
		SetupButton destinationButton = new SetupButton("Browse");
		JLabel sdkLocationLabel = new JLabel("Android SDK");
		JTextField sdkLocationText = new JTextField(
				System.getProperty("os.name").contains("Windows")
				? "C:\\Path\\To\\Your\\Sdk"
				: "/path/to/your/sdk"
		);
		SetupButton sdkLocationButton = new SetupButton("Browse");

		JPanel subProjectsPanel = new JPanel(new GridLayout());
		JLabel projectsLabel = new JLabel("Sub Projects");
		JLabel extensionsLabel = new JLabel("Extensions");
		List<JPanel> extensionsPanels = new ArrayList<JPanel>();
		SetupButton showMoreExtensionsButton = new SetupButton("Show Third Party Extensions");
		SetupCheckBox gwtCheckBox;

		{
			uiLayout();
			uiEvents();
			uiStyle();
		}

		private void uiStyle() {
			nameText.setCaretColor(Color.WHITE);
			packageText.setCaretColor(Color.WHITE);
			gameClassText.setCaretColor(Color.WHITE);
			destinationText.setCaretColor(Color.WHITE);
			sdkLocationText.setCaretColor(Color.WHITE);

			nameLabel.setForeground(Color.WHITE);
			packageLabel.setForeground(Color.WHITE);
			gameClassLabel.setForeground(Color.WHITE);
			destinationLabel.setForeground(Color.WHITE);
			sdkLocationLabel.setForeground(Color.WHITE);
			sdkLocationText.setDisabledTextColor(Color.GRAY);

			projectsLabel.setForeground(new Color(255, 20, 20));
			extensionsLabel.setForeground(new Color(255, 20, 20));

			subProjectsPanel.setOpaque(true);
			subProjectsPanel.setBackground(new Color(46, 46, 46));

			for (JPanel extensionPanel : extensionsPanels) {
				extensionPanel.setOpaque(true);
				extensionPanel.setBackground(new Color(46, 46, 46));
			}
		}

		private void uiLayout () {
			setLayout(new GridBagLayout());

			add(nameLabel, new GridBagConstraints(0, 0, 1, 1, 0, 0, EAST, NONE, new Insets(0, 0, 6, 6), 0, 0));
			add(nameText, new GridBagConstraints(1, 0, 2, 1, 1, 0, CENTER, HORIZONTAL, new Insets(0, 0, 6, 0), 0, 0));

			add(packageLabel, new GridBagConstraints(0, 1, 1, 1, 0, 0, EAST, NONE, new Insets(0, 0, 6, 6), 0, 0));
			add(packageText, new GridBagConstraints(1, 1, 2, 1, 1, 0, CENTER, HORIZONTAL, new Insets(0, 0, 6, 0), 0, 0));

			add(gameClassLabel, new GridBagConstraints(0, 2, 1, 1, 0, 0, EAST, NONE, new Insets(0, 0, 6, 6), 0, 0));
			add(gameClassText, new GridBagConstraints(1, 2, 2, 1, 1, 0, CENTER, HORIZONTAL, new Insets(0, 0, 6, 0), 0, 0));

			add(destinationLabel, new GridBagConstraints(0, 3, 1, 1, 0, 0, EAST, NONE, new Insets(0, 0, 0, 6), 0, 0));
			add(destinationText, new GridBagConstraints(1, 3, 1, 1, 1, 0, CENTER, HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
			add(destinationButton, new GridBagConstraints(2, 3, 1, 1, 0, 0, CENTER, NONE, new Insets(0, 6, 0, 0), 0, 0));

			if (System.getenv("ANDROID_HOME") != null) {
				sdkLocationText.setText(System.getenv("ANDROID_HOME"));
			}
			if (prefs.getString("ANDROID_HOME", null) != null) {
				sdkLocationText.setText(prefs.getString("ANDROID_HOME"));
			}
			add(sdkLocationLabel, new GridBagConstraints(0, 4, 1, 1, 0, 0, EAST, NONE, new Insets(0, 0, 0, 6), 0, 0));
			add(sdkLocationText, new GridBagConstraints(1, 4, 1, 1, 1, 0, CENTER, HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
			add(sdkLocationButton, new GridBagConstraints(2, 4, 1, 1, 0, 0, CENTER, NONE, new Insets(0, 6, 0, 0), 0, 0));


			for (final ProjectType projectType : ProjectType.values()) {
				if (projectType.equals(ProjectType.CORE)) {
					continue;
				}
				if (projectType == ProjectType.IOSMOE) {
					//Disable
					continue;
				}

				SetupCheckBox checkBox = new SetupCheckBox(projectType.getName().substring(0, 1).toUpperCase() + projectType.getName().substring(1, projectType.getName().length()));
				if (projectType == ProjectType.HTML) {
					gwtCheckBox = checkBox;
				} 

				modules.add(projectType);
				checkBox.setSelected(true);

				subProjectsPanel.add(checkBox);
				checkBox.addItemListener(new ItemListener() {
					@Override
					public void itemStateChanged(ItemEvent e) {
						SetupCheckBox box = (SetupCheckBox) e.getSource();
						if (projectType.equals(ProjectType.ANDROID)) {
							sdkLocationText.setEnabled(box.isSelected());
						}
						if (box.isSelected()) {
							modules.add(projectType);
						} else {
							if (modules.contains(projectType)) {
								modules.remove(projectType);
							}
						}
					}
				});
			}

			add(projectsLabel, new GridBagConstraints(0, 6, 1, 1, 0, 0, WEST, WEST, new Insets(20, 0, 0, 0), 0, 0));
			add(subProjectsPanel, new GridBagConstraints(0, 7, 3, 1, 0, 0, CENTER, HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

			int depCounter = 0;

			for (int row = 0; row <= (ProjectDependency.values().length / 5); row++) {
				JPanel extensionPanel = new JPanel(new GridLayout(1, 5));
				while (depCounter < ProjectDependency.values().length) {
					if (ProjectDependency.values()[depCounter] != null) {
						final ProjectDependency projDep = ProjectDependency.values()[depCounter];
						if (projDep.equals(ProjectDependency.GDX)) {
							depCounter++;
							continue;
						}
						SetupCheckBox depCheckBox = new SetupCheckBox(projDep.name().substring(0, 1) + projDep.name().substring(1, projDep.name().length()).toLowerCase());
						depCheckBox.setToolTipText(projDep.getDescription());
						if (projDep.equals(ProjectDependency.BOX2D)) {
							depCheckBox.setSelected(true);
						}
						extensionPanel.add(depCheckBox);
						depCheckBox.addItemListener(new ItemListener() {
							@Override
							public void itemStateChanged(ItemEvent e) {
								SetupCheckBox box = (SetupCheckBox) e.getSource();
								if (box.isSelected()) {
									dependencies.add(builder.bank.getDependency(projDep));
								} else {
									if (dependencies.contains(builder.bank.getDependency(projDep))) {
										dependencies.remove(builder.bank.getDependency(projDep));
									}
								}
							}
						});
						if (depCounter % 5 == 0) {
							depCounter++;
							break;
						}
						depCounter++;
					}
				}
				
				for (int left = ((depCounter - 1) % 5); left > 1; left--) {
					extensionPanel.add(Box.createHorizontalBox());
				}

				extensionsPanels.add(extensionPanel);
			}

			add(extensionsLabel, new GridBagConstraints(0, 8, 1, 1, 0, 0, WEST, WEST, new Insets(10, 0, 0, 0), 0, 0));
			int rowCounter = 9;
			for (JPanel extensionsPanel : extensionsPanels) {
				add(extensionsPanel, new GridBagConstraints(0, rowCounter, 3, 1, 0, 0, CENTER, HORIZONTAL, new Insets(5, 0, 0, 0), 0, 0));
				rowCounter++;
			}
			add(showMoreExtensionsButton, new GridBagConstraints(0, 12, 0, 1, 0, 0, CENTER, WEST, new Insets(10, 0, 10, 0), 0, 0));
		}

		File getDirectory () {
			if (System.getProperty("os.name").contains("Mac")) {
				System.setProperty("apple.awt.fileDialogForDirectories", "true");
				FileDialog dialog = new FileDialog(GdxSetupUI.this, "Choose destination", FileDialog.LOAD);
				dialog.setVisible(true);
				String name = dialog.getFile();
				String dir = dialog.getDirectory();
				if (name == null || dir == null) return null;
				return new File(dialog.getDirectory(), dialog.getFile());
			} else {
				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				chooser.setDialogTitle("Choose destination");
				int result = chooser.showOpenDialog(null);
				if (result == JFileChooser.APPROVE_OPTION) {
					File dir = chooser.getSelectedFile();
					if (dir == null) return null;
					if (dir.getAbsolutePath().trim().length() == 0) return null;
					return dir;
				} else {
					return null;
				}
			}
		}

		private void uiEvents () {
			destinationButton.addActionListener(new ActionListener() {
				public void actionPerformed (ActionEvent e) {
					File path = getDirectory();
					if (path != null) {
						destinationText.setText(path.getAbsolutePath());
					}
				}
			});
			sdkLocationButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed (ActionEvent e) {
					File path = getDirectory();
					if (path != null) {
						sdkLocationText.setText(path.getAbsolutePath());
						prefs.putString("ANDROID_HOME", path.getAbsolutePath());
						prefs.flush();
					}
				}
			});
			showMoreExtensionsButton.addActionListener(new ActionListener() {
				 public void actionPerformed (ActionEvent e) {
					  externalExtensionsDialog.showDialog();
				 }
			});
		}
	}

	public static class SetupCheckBox extends JCheckBox {

		static Icon icon;
		static Icon iconOver;
		static Icon iconPressed;
		static Icon iconPressedSelected;
		static Icon iconSelected;
		static Icon iconOverSelected;

		static {
			try {
				BufferedImage iconImg = getCheckboxImage("checkbox");
				BufferedImage iconOverImg = getCheckboxImage("checkboxover");
				BufferedImage iconPressedImg = getCheckboxImage("checkboxpressed");
				BufferedImage iconPressedSelectedImg = getCheckboxImage("checkboxpressedselected");
				BufferedImage iconSelectedImg = getCheckboxImage("checkboxselected");
				BufferedImage iconOverSelectedImg = getCheckboxImage("checkboxoverselected");

				icon = new ImageIcon(iconImg);
				iconOver = new ImageIcon(iconOverImg);
				iconPressed = new ImageIcon(iconPressedImg);
				iconPressedSelected = new ImageIcon(iconPressedSelectedImg);
				iconSelected = new ImageIcon(iconSelectedImg);
				iconOverSelected = new ImageIcon(iconOverSelectedImg);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private static BufferedImage getCheckboxImage (String imageName) throws IOException {
			return ImageIO.read(GdxSetupUI.class.getResourceAsStream("/com/badlogic/gdx/setup/data/" + imageName + ".png"));
		}

		SetupCheckBox () {
			super();
			initUI();
		}
		
		SetupCheckBox (String selectName) {
			super(selectName);
			initUI();
		}

		private void initUI() {
			setOpaque(false);
			setBackground(new Color(0, 0, 0));
			setForeground(new Color(255, 255, 255));
			setFocusPainted(false);

			setIcon(icon);
			setRolloverIcon(iconOver);
			setPressedIcon(iconPressed);
			setSelectedIcon(iconSelected);
			setRolloverSelectedIcon(iconOverSelected);
		}
		
		public Icon getPressedIcon () {
			//checkbox is missing 'pressed selected' icon, this allows us to add it
			if (isSelected())
				return iconPressedSelected;
			else
				return super.getPressedIcon();
		}

	}

	public static class SetupButton extends JButton {
		private Color textColor = Color.WHITE;
		private Color backgroundColor = new Color(18, 18, 18);
		private Color overColor = new Color(120, 20, 20);
		private Color pressedColor = new Color(240, 40, 40);

		SetupButton (String buttonTag) {
			super(buttonTag);
			setBackground(backgroundColor);
			setForeground(textColor);
			setContentAreaFilled(false);
			setFocusPainted(false);

			Border line = BorderFactory.createLineBorder(new Color(80, 80, 80));
			Border empty = new EmptyBorder(4, 4, 4, 4);
			CompoundBorder border = new CompoundBorder(line, empty);
			setBorder(border);
		}

		@Override
		protected void paintComponent (Graphics g) {
			if (getModel().isPressed()) {
				g.setColor(pressedColor);
			} else if (getModel().isRollover()) {
				g.setColor(overColor);
			} else {
				g.setColor(getBackground());
			}
			g.fillRect(0, 0, getWidth(), getHeight());
			super.paintComponent(g);
		}
	}

	public static void main (String[] args) throws Exception {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new GdxSetupUI();				
			}
		});
	}
}

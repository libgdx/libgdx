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

package com.badlogic.gdx.tools.hiero;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JWindow;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.lwjgl.opengl.GL11;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglCanvas;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.tools.hiero.unicodefont.GlyphPage;
import com.badlogic.gdx.tools.hiero.unicodefont.HieroSettings;
import com.badlogic.gdx.tools.hiero.unicodefont.UnicodeFont;
import com.badlogic.gdx.tools.hiero.unicodefont.effects.ColorEffect;
import com.badlogic.gdx.tools.hiero.unicodefont.effects.ConfigurableEffect;
import com.badlogic.gdx.tools.hiero.unicodefont.effects.ConfigurableEffect.Value;
import com.badlogic.gdx.tools.hiero.unicodefont.effects.DistanceFieldEffect;
import com.badlogic.gdx.tools.hiero.unicodefont.effects.EffectUtil;
import com.badlogic.gdx.tools.hiero.unicodefont.effects.GradientEffect;
import com.badlogic.gdx.tools.hiero.unicodefont.effects.OutlineEffect;
import com.badlogic.gdx.tools.hiero.unicodefont.effects.OutlineWobbleEffect;
import com.badlogic.gdx.tools.hiero.unicodefont.effects.OutlineZigzagEffect;
import com.badlogic.gdx.tools.hiero.unicodefont.effects.ShadowEffect;

import static org.lwjgl.opengl.GL11.*;

/** A tool to visualize settings for {@link UnicodeFont} and to export BMFont files for use with {@link BitmapFont}.
 * @author Nathan Sweet */
public class Hiero extends JFrame {
	static final String NEHE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ\n" //
		+ "abcdefghijklmnopqrstuvwxyz\n1234567890\n" //
		+ "\"!`?'.,;:()[]{}<>|/@\\^$-%+=#_&~*\u007F";

	volatile UnicodeFont newUnicodeFont;
	UnicodeFont unicodeFont;
	Color renderingBackgroundColor = Color.BLACK;
	List<EffectPanel> effectPanels = new ArrayList<EffectPanel>();
	Preferences prefs;
	ColorEffect colorEffect;
	boolean batchMode = false;

	JScrollPane appliedEffectsScroll;
	JPanel appliedEffectsPanel;
	JButton addEffectButton;
	JTextPane sampleTextPane;
	JSpinner padAdvanceXSpinner;
	JList effectsList;
	JPanel gamePanel;
	JTextField fontFileText;
	JRadioButton fontFileRadio;
	JRadioButton systemFontRadio;
	JSpinner padBottomSpinner;
	JSpinner padLeftSpinner;
	JSpinner padRightSpinner;
	JSpinner padTopSpinner;
	JList fontList;
	JSpinner fontSizeSpinner;
	DefaultComboBoxModel fontListModel;
	JLabel backgroundColorLabel;
	JButton browseButton;
	JSpinner padAdvanceYSpinner;
	JCheckBox italicCheckBox;
	JCheckBox boldCheckBox;
	JRadioButton javaRadio;
	JRadioButton nativeRadio;
	JLabel glyphsTotalLabel;
	JLabel glyphPagesTotalLabel;
	JComboBox glyphPageHeightCombo;
	JComboBox glyphPageWidthCombo;
	JComboBox glyphPageCombo;
	JPanel glyphCachePanel;
	JRadioButton glyphCacheRadio;
	JRadioButton sampleTextRadio;
	DefaultComboBoxModel glyphPageComboModel;
	JButton resetCacheButton;
	JButton sampleAsciiButton;
	JButton sampleNeheButton;
	DefaultComboBoxModel effectsListModel;
	JMenuItem openMenuItem;
	JMenuItem saveMenuItem;
	JMenuItem exitMenuItem;
	JMenuItem saveBMFontMenuItem;
	File saveBmFontFile;
	String lastSaveFilename = "", lastSaveBMFilename = "", lastOpenFilename = "";

	public Hiero(String [] args) {
		super("Hiero v3.0 - Bitmap Font Tool");
		Splash splash = new Splash(this, "/splash.jpg", 2000);
		initialize();
		splash.close();

		gamePanel.add(new LwjglCanvas(new Renderer()).getCanvas());

		prefs = Preferences.userNodeForPackage(Hiero.class);
		java.awt.Color backgroundColor = EffectUtil.fromString(prefs.get("background", "000000"));
		backgroundColorLabel.setIcon(getColorIcon(backgroundColor));
		renderingBackgroundColor = new Color(backgroundColor.getRed() / 255f, backgroundColor.getGreen() / 255f,
			backgroundColor.getBlue() / 255f, 1);
		fontList.setSelectedValue(prefs.get("system.font", "Arial"), true);
		fontFileText.setText(prefs.get("font.file", ""));

		java.awt.Color foregroundColor = EffectUtil.fromString(prefs.get("foreground", "ffffff"));
		colorEffect = new ColorEffect();
		colorEffect.setColor(foregroundColor);
		effectsListModel.addElement(colorEffect);
		effectsListModel.addElement(new GradientEffect());
		effectsListModel.addElement(new OutlineEffect());
		effectsListModel.addElement(new OutlineWobbleEffect());
		effectsListModel.addElement(new OutlineZigzagEffect());
		effectsListModel.addElement(new ShadowEffect());
		effectsListModel.addElement(new DistanceFieldEffect());
		new EffectPanel(colorEffect);

		parseArgs(args);
		
		addWindowListener(new WindowAdapter() {
			public void windowClosed (WindowEvent event) {
				System.exit(0);
				// Gdx.app.quit();
			}
		});

		updateFontSelector();
		setVisible(true);
	}

	void initialize () {
		initializeComponents();
		initializeMenus();
		initializeEvents();

		setSize(800, 600);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		sampleNeheButton.doClick();
	}

	private void parseArgs (String[] args) {
		float scale = 1f;

		for (int i = 0; i < args.length; i++) {
			final String param = args[i];
			final boolean more = i < args.length - 1;

			if (param.equals("-b") || param.equals("--batch")) {
				batchMode = true;
			} else if (more && (param.equals("-s") || param.equals("--scale"))) {
				scale = Float.parseFloat(args[++i]);
			} else if (more && (param.equals("-i") || param.equals("--input"))) {
				File f = new File(args[++i]);
				open(f);
				fontFileRadio.setText("");
				updateFont();
			} else if (more && (param.equals("-o") || param.equals("--output"))) {
				File f = new File(args[++i]);
				saveBm(f);
			} else {
				System.err.println("Unknown parameter: " + param);
				System.exit(3);
			}
		}

		// update scale:
		fontSizeSpinner.setValue((int)(0.5f + Math.max(4, scale * ((Integer)fontSizeSpinner.getValue()))));
	}

	void updateFontSelector () {
		final boolean use2 = fontFileRadio.isSelected();
		fontList.setEnabled(!use2);
		fontFileText.setEnabled(use2);
		browseButton.setEnabled(use2);
	}

	void updateFont () {
		final boolean useFont2 = fontFileRadio.isSelected();
		UnicodeFont unicodeFont = null;

		int fontSize = ((Integer)fontSizeSpinner.getValue()).intValue();

		
		 
		if (useFont2) {
			File file = new File(fontFileText.getText());
			if (file.exists() && file.isFile()) {
				// Load from file.
				try {
					unicodeFont = new UnicodeFont(fontFileText.getText(), fontSize, boldCheckBox.isSelected(),
						italicCheckBox.isSelected());
				} catch (Throwable ex) {
					ex.printStackTrace();
					fontFileRadio.setSelected(false);
				}
			}
		}
		
		if(unicodeFont == null) {
			// Load from java.awt.Font (kerning not available!).
			unicodeFont = new UnicodeFont(Font.decode((String)fontList.getSelectedValue()), fontSize, boldCheckBox.isSelected(),
				italicCheckBox.isSelected());
		}

		unicodeFont.setPaddingTop(((Integer)padTopSpinner.getValue()).intValue());
		unicodeFont.setPaddingRight(((Integer)padRightSpinner.getValue()).intValue());
		unicodeFont.setPaddingBottom(((Integer)padBottomSpinner.getValue()).intValue());
		unicodeFont.setPaddingLeft(((Integer)padLeftSpinner.getValue()).intValue());
		unicodeFont.setPaddingAdvanceX(((Integer)padAdvanceXSpinner.getValue()).intValue());
		unicodeFont.setPaddingAdvanceY(((Integer)padAdvanceYSpinner.getValue()).intValue());
		unicodeFont.setGlyphPageWidth(((Integer)glyphPageWidthCombo.getSelectedItem()).intValue());
		unicodeFont.setGlyphPageHeight(((Integer)glyphPageHeightCombo.getSelectedItem()).intValue());
		unicodeFont.setNativeRendering(nativeRadio.isSelected());

		for (Iterator iter = effectPanels.iterator(); iter.hasNext();) {
			EffectPanel panel = (EffectPanel)iter.next();
			unicodeFont.getEffects().add(panel.getEffect());
		}

		int size = sampleTextPane.getFont().getSize();
		if (size < 14) size = 14;
		sampleTextPane.setFont(unicodeFont.getFont().deriveFont((float)size));

		this.newUnicodeFont = unicodeFont;
		updateFontSelector();
	}

	void saveBm (File file) {
		saveBmFontFile = file;
	}

	void save (File file) throws IOException {
		HieroSettings settings = new HieroSettings();
		settings.setFontName((String)fontList.getSelectedValue());
		settings.setFontSize(((Integer)fontSizeSpinner.getValue()).intValue());
		settings.setFont2File(fontFileText.getText());
	settings.setFont2Active(fontFileRadio.isSelected());
		settings.setBold(boldCheckBox.isSelected());
		settings.setItalic(italicCheckBox.isSelected());
		settings.setPaddingTop(((Integer)padTopSpinner.getValue()).intValue());
		settings.setPaddingRight(((Integer)padRightSpinner.getValue()).intValue());
		settings.setPaddingBottom(((Integer)padBottomSpinner.getValue()).intValue());
		settings.setPaddingLeft(((Integer)padLeftSpinner.getValue()).intValue());
		settings.setPaddingAdvanceX(((Integer)padAdvanceXSpinner.getValue()).intValue());
		settings.setPaddingAdvanceY(((Integer)padAdvanceYSpinner.getValue()).intValue());
		settings.setGlyphPageWidth(((Integer)glyphPageWidthCombo.getSelectedItem()).intValue());
		settings.setGlyphPageHeight(((Integer)glyphPageHeightCombo.getSelectedItem()).intValue());
		settings.setGlyphText(sampleTextPane.getText());
		for (Iterator iter = effectPanels.iterator(); iter.hasNext();) {
			EffectPanel panel = (EffectPanel)iter.next();
			settings.getEffects().add(panel.getEffect());
		}
		settings.save(file);
	}

	void open (File file) {
		EffectPanel[] panels = (EffectPanel[])effectPanels.toArray(new EffectPanel[effectPanels.size()]);
		for (int i = 0; i < panels.length; i++)
			panels[i].remove();

		HieroSettings settings = new HieroSettings(file.getAbsolutePath());
		fontList.setSelectedValue(settings.getFontName(), true);
		fontSizeSpinner.setValue(new Integer(settings.getFontSize()));
		boldCheckBox.setSelected(settings.isBold());
		italicCheckBox.setSelected(settings.isItalic());
		padTopSpinner.setValue(new Integer(settings.getPaddingTop()));
		padRightSpinner.setValue(new Integer(settings.getPaddingRight()));
		padBottomSpinner.setValue(new Integer(settings.getPaddingBottom()));
		padLeftSpinner.setValue(new Integer(settings.getPaddingLeft()));
		padAdvanceXSpinner.setValue(new Integer(settings.getPaddingAdvanceX()));
		padAdvanceYSpinner.setValue(new Integer(settings.getPaddingAdvanceY()));
		glyphPageWidthCombo.setSelectedItem(new Integer(settings.getGlyphPageWidth()));
		glyphPageHeightCombo.setSelectedItem(new Integer(settings.getGlyphPageHeight()));
		String gt = settings.getGlyphText();
		if (gt.length() > 0) {
			sampleTextPane.setText(settings.getGlyphText());
		}
		
		final String font2 = settings.getFont2File();
		if (font2.length() > 0)
			fontFileText.setText(font2);
		else
			fontFileText.setText(prefs.get("font.file", ""));

		fontFileRadio.setSelected(settings.isFont2Active());
		systemFontRadio.setSelected(!settings.isFont2Active());
 
		for (Iterator iter = settings.getEffects().iterator(); iter.hasNext();) {
			ConfigurableEffect settingsEffect = (ConfigurableEffect)iter.next();
			for (int i = 0, n = effectsListModel.getSize(); i < n; i++) {
				ConfigurableEffect effect = (ConfigurableEffect)effectsListModel.getElementAt(i);
				if (effect.getClass() == settingsEffect.getClass()) {
					effect.setValues(settingsEffect.getValues());
					new EffectPanel(effect);
					break;
				}
			}
		}

		updateFont();
	}

	private void initializeEvents () {
		fontList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged (ListSelectionEvent evt) {
				if (evt.getValueIsAdjusting()) return;
				prefs.put("system.font", (String)fontList.getSelectedValue());
				updateFont();
			}
		});

		class FontUpdateListener implements ChangeListener, ActionListener {
			public void stateChanged (ChangeEvent evt) {
				updateFont();
			}

			public void actionPerformed (ActionEvent evt) {
				updateFont();
			}

			public void addSpinners (JSpinner[] spinners) {
				for (int i = 0; i < spinners.length; i++) {
					final JSpinner spinner = spinners[i];
					spinner.addChangeListener(this);
					((JSpinner.DefaultEditor)spinner.getEditor()).getTextField().addKeyListener(new KeyAdapter() {
						String lastText;

						public void keyReleased (KeyEvent evt) {
							JFormattedTextField textField = ((JSpinner.DefaultEditor)spinner.getEditor()).getTextField();
							String text = textField.getText();
							if (text.length() == 0) return;
							if (text.equals(lastText)) return;
							lastText = text;
							int caretPosition = textField.getCaretPosition();
							try {
								spinner.setValue(Integer.valueOf(text));
								textField.setCaretPosition(caretPosition);
							} catch (Throwable ignored) {
							}
						}
					});
				}
			}
		}
		FontUpdateListener listener = new FontUpdateListener();

		listener.addSpinners(new JSpinner[] {padTopSpinner, padRightSpinner, padBottomSpinner, padLeftSpinner, padAdvanceXSpinner,
			padAdvanceYSpinner});
		fontSizeSpinner.addChangeListener(listener);

		glyphPageWidthCombo.addActionListener(listener);
		glyphPageHeightCombo.addActionListener(listener);
		boldCheckBox.addActionListener(listener);
		italicCheckBox.addActionListener(listener);
		resetCacheButton.addActionListener(listener);
		javaRadio.addActionListener(listener);
		nativeRadio.addActionListener(listener);

		sampleTextRadio.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent evt) {
				glyphCachePanel.setVisible(false);
			}
		});
		glyphCacheRadio.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent evt) {
				glyphCachePanel.setVisible(true);
			}
		});

		fontFileText.getDocument().addDocumentListener(new DocumentListener() {
			public void removeUpdate (DocumentEvent evt) {
				changed();
			}

			public void insertUpdate (DocumentEvent evt) {
				changed();
			}

			public void changedUpdate (DocumentEvent evt) {
				changed();
			}

			private void changed () {
				File file = new File(fontFileText.getText());
				if (fontList.isEnabled() && (!file.exists() || !file.isFile())) return;
				prefs.put("font.file", fontFileText.getText());
				updateFont();
			}
		});

		final ActionListener al = new ActionListener() {
			public void actionPerformed (ActionEvent evt) {
				updateFontSelector();
				updateFont();
			}
		};
		
		systemFontRadio.addActionListener(al);
		fontFileRadio.addActionListener(al);

		browseButton.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent evt) {
				FileDialog dialog = new FileDialog(Hiero.this, "Choose TrueType font file", FileDialog.LOAD);
				dialog.setLocationRelativeTo(null);
				dialog.setFile("*.ttf");
				dialog.setDirectory(prefs.get("dir.font", ""));
				dialog.setVisible(true);
				if (dialog.getDirectory() != null) {
					prefs.put("dir.font", dialog.getDirectory());
				}

				String fileName = dialog.getFile();
				if (fileName == null) return;
				fontFileText.setText(new File(dialog.getDirectory(), fileName).getAbsolutePath());
			}
		});

		backgroundColorLabel.addMouseListener(new MouseAdapter() {
			public void mouseClicked (MouseEvent evt) {
				java.awt.Color color = JColorChooser.showDialog(null, "Choose a background color",
					EffectUtil.fromString(prefs.get("background", "000000")));
				if (color == null) return;
				renderingBackgroundColor = new Color(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, 1);
				backgroundColorLabel.setIcon(getColorIcon(color));
				prefs.put("background", EffectUtil.toString(color));
			}
		});

		effectsList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged (ListSelectionEvent evt) {
				ConfigurableEffect selectedEffect = (ConfigurableEffect)effectsList.getSelectedValue();
				boolean enabled = selectedEffect != null;
				for (Iterator iter = effectPanels.iterator(); iter.hasNext();) {
					ConfigurableEffect effect = ((EffectPanel)iter.next()).getEffect();
					if (effect == selectedEffect) {
						enabled = false;
						break;
					}
				}
				addEffectButton.setEnabled(enabled);
			}
		});

		effectsList.addMouseListener(new MouseAdapter() {
			public void mouseClicked (MouseEvent evt) {
				if (evt.getClickCount() == 2 && addEffectButton.isEnabled()) addEffectButton.doClick();
			}
		});

		addEffectButton.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent evt) {
				new EffectPanel((ConfigurableEffect)effectsList.getSelectedValue());
			}
		});

		openMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent evt) {
				FileDialog dialog = new FileDialog(Hiero.this, "Open Hiero settings file", FileDialog.LOAD);
				dialog.setLocationRelativeTo(null);
				dialog.setFile("*.hiero");
				dialog.setDirectory(prefs.get("dir.open", ""));
				dialog.setVisible(true);
				if (dialog.getDirectory() != null) {
					prefs.put("dir.open", dialog.getDirectory());
				}

				String fileName = dialog.getFile();
				if (fileName == null) return;
				lastOpenFilename = fileName;
				open(new File(dialog.getDirectory(), fileName));
			}
		});

		saveMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent evt) {
				FileDialog dialog = new FileDialog(Hiero.this, "Save Hiero settings file", FileDialog.SAVE);
				dialog.setLocationRelativeTo(null);
				dialog.setFile("*.hiero");
				dialog.setDirectory(prefs.get("dir.save", ""));

				if (lastSaveFilename.length() > 0) {
					dialog.setFile(lastSaveFilename);
				} else if (lastOpenFilename.length() > 0) {
					dialog.setFile(lastOpenFilename);
				}

				dialog.setVisible(true);

				if (dialog.getDirectory() != null) {
					prefs.put("dir.save", dialog.getDirectory());
				}

				String fileName = dialog.getFile();
				if (fileName == null) return;
				if (!fileName.endsWith(".hiero")) fileName += ".hiero";
				lastSaveFilename = fileName;
				File file = new File(dialog.getDirectory(), fileName);
				try {
					save(file);
				} catch (IOException ex) {
					throw new RuntimeException("Error saving Hiero settings file: " + file.getAbsolutePath(), ex);
				}
			}
		});

		saveBMFontMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent evt) {
				FileDialog dialog = new FileDialog(Hiero.this, "Save BMFont files", FileDialog.SAVE);
				dialog.setLocationRelativeTo(null);
				dialog.setFile("*.fnt");
				dialog.setDirectory(prefs.get("dir.savebm", ""));

				if (lastSaveBMFilename.length() > 0) {
					dialog.setFile(lastSaveBMFilename);
				} else if (lastOpenFilename.length() > 0) {
					dialog.setFile(lastOpenFilename.replace(".hiero", ".fnt"));
				}

				dialog.setVisible(true);
				if (dialog.getDirectory() != null) {
					prefs.put("dir.savebm", dialog.getDirectory());
				}

				String fileName = dialog.getFile();
				if (fileName == null) return;
				lastSaveBMFilename = fileName;
				saveBm(new File(dialog.getDirectory(), fileName));
			}
		});

		exitMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent evt) {
				dispose();
			}
		});

		sampleNeheButton.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent evt) {
				sampleTextPane.setText(NEHE);
			}
		});

		sampleAsciiButton.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent evt) {
				StringBuilder buffer = new StringBuilder();
				buffer.append(NEHE);
				buffer.append('\n');
				int count = 0;
				for (int i = 33; i <= 255; i++) {
					if (buffer.indexOf(Character.toString((char)i)) != -1) continue;
					buffer.append((char)i);
					if (++count % 30 == 0) buffer.append('\n');
				}
				sampleTextPane.setText(buffer.toString());
			}
		});
	}

	private void initializeComponents () {
		getContentPane().setLayout(new GridBagLayout());
		JPanel leftSidePanel = new JPanel();
		leftSidePanel.setLayout(new GridBagLayout());
		getContentPane().add(
			leftSidePanel,
			new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0),
				0, 0));
		{
			JPanel fontPanel = new JPanel();
			leftSidePanel.add(fontPanel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
				GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
			fontPanel.setLayout(new GridBagLayout());
			fontPanel.setBorder(BorderFactory.createTitledBorder("Font"));
			{
				fontSizeSpinner = new JSpinner(new SpinnerNumberModel(32, 0, 256, 1));
				fontPanel.add(fontSizeSpinner, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
					GridBagConstraints.NONE, new Insets(0, 0, 5, 5), 0, 0));
				((JSpinner.DefaultEditor)fontSizeSpinner.getEditor()).getTextField().setColumns(2);
			}
			{
				JScrollPane fontScroll = new JScrollPane();
				fontPanel.add(fontScroll, new GridBagConstraints(1, 1, 4, 1, 1.0, 1.0, GridBagConstraints.CENTER,
					GridBagConstraints.BOTH, new Insets(0, 0, 5, 5), 0, 0));
				{
					fontListModel = new DefaultComboBoxModel(GraphicsEnvironment.getLocalGraphicsEnvironment()
						.getAvailableFontFamilyNames());
					fontList = new JList();
					fontScroll.setViewportView(fontList);
					fontList.setModel(fontListModel);
					fontList.setVisibleRowCount(6);
					fontList.setSelectedIndex(0);
					fontScroll.setMinimumSize(new Dimension(220, fontList.getPreferredScrollableViewportSize().height));
				}
			}
			{
				systemFontRadio = new JRadioButton("System:", true);
				fontPanel.add(systemFontRadio, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHEAST,
					GridBagConstraints.NONE, new Insets(0, 5, 0, 5), 0, 0));
				systemFontRadio.setMargin(new Insets(0, 0, 0, 0));
			}
			{
				fontFileRadio = new JRadioButton("File:");
				fontPanel.add(fontFileRadio, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.EAST,
					GridBagConstraints.NONE, new Insets(0, 5, 5, 5), 0, 0));
				fontFileRadio.setMargin(new Insets(0, 0, 0, 0));
			}
			{
				fontFileText = new JTextField();
				fontPanel.add(fontFileText, new GridBagConstraints(1, 2, 3, 1, 1.0, 0.0, GridBagConstraints.CENTER,
					GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 0), 0, 0));
			}
			{
				fontPanel.add(new JLabel("Size:"), new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.EAST,
					GridBagConstraints.NONE, new Insets(0, 0, 5, 5), 0, 0));
			}
			{
				boldCheckBox = new JCheckBox("Bold");
				fontPanel.add(boldCheckBox, new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
					GridBagConstraints.NONE, new Insets(0, 0, 5, 5), 0, 0));
			}
			{
				italicCheckBox = new JCheckBox("Italic");
				fontPanel.add(italicCheckBox, new GridBagConstraints(3, 3, 1, 1, 1.0, 0.0, GridBagConstraints.WEST,
					GridBagConstraints.NONE, new Insets(0, 0, 5, 5), 0, 0));
			}
			{
				browseButton = new JButton("...");
				fontPanel.add(browseButton, new GridBagConstraints(4, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
					GridBagConstraints.NONE, new Insets(0, 0, 5, 5), 0, 0));
				browseButton.setMargin(new Insets(0, 0, 0, 0));
			}
			{
				fontPanel.add(new JLabel("Rendering:"), new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.EAST,
					GridBagConstraints.NONE, new Insets(0, 0, 5, 5), 0, 0));
			}
			{
				javaRadio = new JRadioButton("Java");
				fontPanel.add(javaRadio, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
					GridBagConstraints.NONE, new Insets(0, 0, 5, 5), 0, 0));
				javaRadio.setSelected(true);
			}
			{
				nativeRadio = new JRadioButton("Native");
				fontPanel.add(nativeRadio, new GridBagConstraints(2, 4, 1, 1, 1.0, 0.0, GridBagConstraints.WEST,
					GridBagConstraints.NONE, new Insets(0, 0, 5, 5), 0, 0));
			}
			ButtonGroup buttonGroup = new ButtonGroup();
			buttonGroup.add(systemFontRadio);
			buttonGroup.add(fontFileRadio);
			buttonGroup = new ButtonGroup();
			buttonGroup.add(javaRadio);
			buttonGroup.add(nativeRadio);
		}
		{
			JPanel samplePanel = new JPanel();
			leftSidePanel.add(samplePanel, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER,
				GridBagConstraints.BOTH, new Insets(5, 0, 5, 5), 0, 0));
			samplePanel.setLayout(new GridBagLayout());
			samplePanel.setBorder(BorderFactory.createTitledBorder("Sample Text"));
			{
				JScrollPane textScroll = new JScrollPane();
				samplePanel.add(textScroll, new GridBagConstraints(0, 0, 3, 1, 1.0, 1.0, GridBagConstraints.CENTER,
					GridBagConstraints.BOTH, new Insets(0, 5, 5, 5), 0, 0));
				{
					sampleTextPane = new JTextPane();
					textScroll.setViewportView(sampleTextPane);
				}
			}
			{
				sampleNeheButton = new JButton();
				sampleNeheButton.setText("NEHE");
				samplePanel.add(sampleNeheButton, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
					GridBagConstraints.NONE, new Insets(0, 0, 5, 5), 0, 0));
			}
			{
				sampleAsciiButton = new JButton();
				sampleAsciiButton.setText("ASCII");
				samplePanel.add(sampleAsciiButton, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0, GridBagConstraints.EAST,
					GridBagConstraints.NONE, new Insets(0, 0, 5, 5), 0, 0));
			}
		}
		{
			JPanel renderingPanel = new JPanel();
			leftSidePanel.add(renderingPanel, new GridBagConstraints(0, 1, 2, 1, 1.0, 1.0, GridBagConstraints.CENTER,
				GridBagConstraints.BOTH, new Insets(0, 5, 5, 5), 0, 0));
			renderingPanel.setBorder(BorderFactory.createTitledBorder("Rendering"));
			renderingPanel.setLayout(new GridBagLayout());
			{
				JPanel wrapperPanel = new JPanel();
				renderingPanel.add(wrapperPanel, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
					GridBagConstraints.BOTH, new Insets(0, 5, 5, 5), 0, 0));
				wrapperPanel.setLayout(new BorderLayout());
				wrapperPanel.setBackground(java.awt.Color.white);
				{
					gamePanel = new JPanel();
					wrapperPanel.add(gamePanel);
					gamePanel.setLayout(new BorderLayout());
					gamePanel.setBackground(java.awt.Color.white);
				}
			}
			{
				glyphCachePanel = new JPanel() {
					private int maxWidth;

					public Dimension getPreferredSize () {
						// Keep glyphCachePanel width from ever going down so the CanvasGameContainer doesn't change sizes and flicker.
						Dimension size = super.getPreferredSize();
						maxWidth = Math.max(maxWidth, size.width);
						size.width = maxWidth;
						return size;
					}
				};
				glyphCachePanel.setVisible(false);
				renderingPanel.add(glyphCachePanel, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.NORTH,
					GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
				glyphCachePanel.setLayout(new GridBagLayout());
				{
					glyphCachePanel.add(new JLabel("Glyphs:"), new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.EAST,
						GridBagConstraints.NONE, new Insets(0, 5, 5, 5), 0, 0));
				}
				{
					glyphCachePanel.add(new JLabel("Pages:"), new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.EAST,
						GridBagConstraints.NONE, new Insets(0, 5, 5, 5), 0, 0));
				}
				{
					glyphCachePanel.add(new JLabel("Page width:"), new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
						GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 5, 5, 5), 0, 0));
				}
				{
					glyphCachePanel.add(new JLabel("Page height:"), new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
						GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 5, 5, 5), 0, 0));
				}
				{
					glyphPageWidthCombo = new JComboBox(new DefaultComboBoxModel(new Integer[] {new Integer(32), new Integer(64),
						new Integer(128), new Integer(256), new Integer(512), new Integer(1024), new Integer(2048)}));
					glyphCachePanel.add(glyphPageWidthCombo, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
						GridBagConstraints.NONE, new Insets(0, 0, 5, 5), 0, 0));
					glyphPageWidthCombo.setSelectedIndex(4);
				}
				{
					glyphPageHeightCombo = new JComboBox(new DefaultComboBoxModel(new Integer[] {new Integer(32), new Integer(64),
						new Integer(128), new Integer(256), new Integer(512), new Integer(1024), new Integer(2048)}));
					glyphCachePanel.add(glyphPageHeightCombo, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
						GridBagConstraints.NONE, new Insets(0, 0, 5, 5), 0, 0));
					glyphPageHeightCombo.setSelectedIndex(4);
				}
				{
					resetCacheButton = new JButton("Reset Cache");
					glyphCachePanel.add(resetCacheButton, new GridBagConstraints(0, 6, 2, 1, 1.0, 0.0, GridBagConstraints.CENTER,
						GridBagConstraints.NONE, new Insets(0, 5, 5, 5), 0, 0));
				}
				{
					glyphPagesTotalLabel = new JLabel("1");
					glyphCachePanel.add(glyphPagesTotalLabel, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
						GridBagConstraints.NONE, new Insets(0, 0, 5, 5), 0, 0));
				}
				{
					glyphsTotalLabel = new JLabel("0");
					glyphCachePanel.add(glyphsTotalLabel, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
						GridBagConstraints.NONE, new Insets(0, 0, 5, 5), 0, 0));
				}
				{
					glyphPageComboModel = new DefaultComboBoxModel();
					glyphPageCombo = new JComboBox();
					glyphCachePanel.add(glyphPageCombo, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
						GridBagConstraints.NONE, new Insets(0, 0, 5, 5), 0, 0));
					glyphPageCombo.setModel(glyphPageComboModel);
				}
				{
					glyphCachePanel.add(new JLabel("View:"), new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST,
						GridBagConstraints.NONE, new Insets(0, 5, 5, 5), 0, 0));
				}
			}
			{
				JPanel radioButtonsPanel = new JPanel();
				renderingPanel.add(radioButtonsPanel, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER,
					GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
				radioButtonsPanel.setLayout(new GridBagLayout());
				{
					sampleTextRadio = new JRadioButton("Sample text");
					radioButtonsPanel.add(sampleTextRadio, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
						GridBagConstraints.NONE, new Insets(0, 0, 5, 5), 0, 0));
					sampleTextRadio.setSelected(true);
				}
				{
					glyphCacheRadio = new JRadioButton("Glyph cache");
					radioButtonsPanel.add(glyphCacheRadio, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
						GridBagConstraints.NONE, new Insets(0, 0, 5, 5), 0, 0));
				}
				{
					radioButtonsPanel.add(new JLabel("Background:"), new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 5, 5, 5), 0, 0));
				}
				{
					backgroundColorLabel = new JLabel();
					radioButtonsPanel.add(backgroundColorLabel, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST,
						GridBagConstraints.NONE, new Insets(0, 0, 5, 5), 0, 0));
				}
				ButtonGroup buttonGroup = new ButtonGroup();
				buttonGroup.add(glyphCacheRadio);
				buttonGroup.add(sampleTextRadio);
			}
		}
		JPanel rightSidePanel = new JPanel();
		rightSidePanel.setLayout(new GridBagLayout());
		getContentPane().add(
			rightSidePanel,
			new GridBagConstraints(1, 0, 1, 2, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0),
				0, 0));
		{
			JPanel paddingPanel = new JPanel();
			paddingPanel.setLayout(new GridBagLayout());
			rightSidePanel.add(paddingPanel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
				GridBagConstraints.BOTH, new Insets(0, 0, 5, 5), 0, 0));
			paddingPanel.setBorder(BorderFactory.createTitledBorder("Padding"));
			{
				padTopSpinner = new JSpinner();
				paddingPanel.add(padTopSpinner, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
					GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
				((JSpinner.DefaultEditor)padTopSpinner.getEditor()).getTextField().setColumns(2);
			}
			{
				padRightSpinner = new JSpinner();
				paddingPanel.add(padRightSpinner, new GridBagConstraints(2, 2, 1, 1, 1.0, 0.0, GridBagConstraints.WEST,
					GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
				((JSpinner.DefaultEditor)padRightSpinner.getEditor()).getTextField().setColumns(2);
			}
			{
				padLeftSpinner = new JSpinner();
				paddingPanel.add(padLeftSpinner, new GridBagConstraints(0, 2, 1, 1, 1.0, 0.0, GridBagConstraints.EAST,
					GridBagConstraints.NONE, new Insets(0, 5, 0, 0), 0, 0));
				((JSpinner.DefaultEditor)padLeftSpinner.getEditor()).getTextField().setColumns(2);
			}
			{
				padBottomSpinner = new JSpinner();
				paddingPanel.add(padBottomSpinner, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
					GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
				((JSpinner.DefaultEditor)padBottomSpinner.getEditor()).getTextField().setColumns(2);
			}
			{
				JPanel advancePanel = new JPanel();
				FlowLayout advancePanelLayout = new FlowLayout();
				advancePanel.setLayout(advancePanelLayout);
				paddingPanel.add(advancePanel, new GridBagConstraints(0, 4, 3, 1, 1.0, 0.0, GridBagConstraints.CENTER,
					GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
				{
					advancePanel.add(new JLabel("X:"));
				}
				{
					padAdvanceXSpinner = new JSpinner();
					advancePanel.add(padAdvanceXSpinner);
					((JSpinner.DefaultEditor)padAdvanceXSpinner.getEditor()).getTextField().setColumns(2);
				}
				{
					advancePanel.add(new JLabel("Y:"));
				}
				{
					padAdvanceYSpinner = new JSpinner();
					advancePanel.add(padAdvanceYSpinner);
					((JSpinner.DefaultEditor)padAdvanceYSpinner.getEditor()).getTextField().setColumns(2);
				}
			}
		}
		{
			JPanel effectsPanel = new JPanel();
			effectsPanel.setLayout(new GridBagLayout());
			rightSidePanel.add(effectsPanel, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
				GridBagConstraints.BOTH, new Insets(5, 0, 5, 5), 0, 0));
			effectsPanel.setBorder(BorderFactory.createTitledBorder("Effects"));
			effectsPanel.setMinimumSize(new Dimension(210, 1));
			{
				JScrollPane effectsScroll = new JScrollPane();
				effectsPanel.add(effectsScroll, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.NORTH,
					GridBagConstraints.HORIZONTAL, new Insets(0, 5, 5, 5), 0, 0));
				{
					effectsListModel = new DefaultComboBoxModel();
					effectsList = new JList();
					effectsScroll.setViewportView(effectsList);
					effectsList.setModel(effectsListModel);
					effectsList.setVisibleRowCount(7);
					effectsScroll.setMinimumSize(effectsList.getPreferredScrollableViewportSize());
				}
			}
			{
				addEffectButton = new JButton("Add");
				effectsPanel.add(addEffectButton, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST,
					GridBagConstraints.NONE, new Insets(0, 5, 6, 5), 0, 0));
				addEffectButton.setEnabled(false);
			}
			{
				appliedEffectsScroll = new JScrollPane();
				effectsPanel.add(appliedEffectsScroll, new GridBagConstraints(1, 3, 1, 1, 1.0, 1.0, GridBagConstraints.NORTH,
					GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0, 0));
				appliedEffectsScroll.setBorder(new EmptyBorder(0, 0, 0, 0));
				appliedEffectsScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
				{
					JPanel panel = new JPanel();
					panel.setLayout(new GridBagLayout());
					appliedEffectsScroll.setViewportView(panel);
					{
						appliedEffectsPanel = new JPanel();
						appliedEffectsPanel.setLayout(new GridBagLayout());
						panel.add(appliedEffectsPanel, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.NORTH,
							GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
						appliedEffectsPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, java.awt.Color.black));
					}
				}
			}
		}
	}

	private void initializeMenus () {
		{
			JMenuBar menuBar = new JMenuBar();
			setJMenuBar(menuBar);
			{
				JMenu fileMenu = new JMenu();
				menuBar.add(fileMenu);
				fileMenu.setText("File");
				fileMenu.setMnemonic(KeyEvent.VK_F);
				{
					openMenuItem = new JMenuItem("Open Hiero settings file...");
					openMenuItem.setMnemonic(KeyEvent.VK_O);
					openMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_MASK));
					fileMenu.add(openMenuItem);
				}
				{
					saveMenuItem = new JMenuItem("Save Hiero settings file...");
					saveMenuItem.setMnemonic(KeyEvent.VK_S);
					saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_MASK));
					fileMenu.add(saveMenuItem);
				}
				fileMenu.addSeparator();
				{
					saveBMFontMenuItem = new JMenuItem("Save BMFont files (text)...");
					saveBMFontMenuItem.setMnemonic(KeyEvent.VK_B);
					saveBMFontMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, KeyEvent.CTRL_MASK));
					fileMenu.add(saveBMFontMenuItem);
				}
				fileMenu.addSeparator();
				{
					exitMenuItem = new JMenuItem("Exit");
					exitMenuItem.setMnemonic(KeyEvent.VK_X);
					fileMenu.add(exitMenuItem);
				}
			}
		}
	}

	static Icon getColorIcon (java.awt.Color color) {
		BufferedImage image = new BufferedImage(32, 16, BufferedImage.TYPE_INT_RGB);
		java.awt.Graphics g = image.getGraphics();
		g.setColor(color);
		g.fillRect(1, 1, 30, 14);
		g.setColor(java.awt.Color.black);
		g.drawRect(0, 0, 31, 15);
		return new ImageIcon(image);
	}

	private class EffectPanel extends JPanel {
		final java.awt.Color selectedColor = new java.awt.Color(0xb1d2e9);

		final ConfigurableEffect effect;
		List values;

		JButton upButton;
		JButton downButton;
		JButton deleteButton;
		private JPanel valuesPanel;
		JLabel nameLabel;
		
		GridBagConstraints constrains = new GridBagConstraints(0, -1, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER,
			GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0);

		EffectPanel (final ConfigurableEffect effect) {
			this.effect = effect;
			effectPanels.add(this);
			effectsList.getListSelectionListeners()[0].valueChanged(null);

			setLayout(new GridBagLayout());
			setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, java.awt.Color.black));
			appliedEffectsPanel.add(this, constrains);
			{
				JPanel titlePanel = new JPanel();
				titlePanel.setLayout(new LayoutManager() {
					public void removeLayoutComponent (Component comp) {
					}

					public Dimension preferredLayoutSize (Container parent) {
						return null;
					}

					public Dimension minimumLayoutSize (Container parent) {
						return null;
					}

					public void layoutContainer (Container parent) {
						
						Dimension buttonSize = upButton.getPreferredSize();
						int upButtonX = getWidth() - buttonSize.width * 3 - 6 - 5;
						upButton.setBounds(upButtonX, 0, buttonSize.width, buttonSize.height);
						downButton.setBounds(getWidth() - buttonSize.width * 2 - 3 - 5, 0, buttonSize.width, buttonSize.height);
						deleteButton.setBounds(getWidth() - buttonSize.width - 5, 0, buttonSize.width, buttonSize.height);

						Dimension labelSize = nameLabel.getPreferredSize();
						nameLabel.setBounds(5, buttonSize.height / 2 - labelSize.height / 2, getWidth() - 5,
							labelSize.height);
					}

					public void addLayoutComponent (String name, Component comp) {
					}
				});
				{
					upButton = new JButton();
					titlePanel.add(upButton);
					upButton.setText("▲");
					upButton.setMargin(new Insets(0, 0, 0, 0));
					Font font = upButton.getFont();
					upButton.setFont(new Font(font.getName(), font.getStyle(), font.getSize() - 2));
				}
				{
					downButton = new JButton();
					titlePanel.add(downButton);
					downButton.setText("▼");
					downButton.setMargin(new Insets(0, 0, 0, 0));
					Font font = downButton.getFont();
					downButton.setFont(new Font(font.getName(), font.getStyle(), font.getSize() - 2));
				}
				{
					deleteButton = new JButton();
					titlePanel.add(deleteButton);
					deleteButton.setText("X");
					deleteButton.setMargin(new Insets(0, 0, 0, 0));
					Font font = deleteButton.getFont();
					deleteButton.setFont(new Font(font.getName(), font.getStyle(), font.getSize() - 2));
				}
				{
					nameLabel = new JLabel(effect.toString());
					titlePanel.add(nameLabel);
					Font font = nameLabel.getFont();
					nameLabel.setFont(new Font(font.getName(), Font.BOLD, font.getSize()));
				}
				titlePanel.setPreferredSize(new Dimension(0, Math.max(nameLabel.getPreferredSize().height,
					deleteButton.getPreferredSize().height)));
				add(titlePanel, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(5, 0, 0, 5), 0, 0));
				titlePanel.setOpaque(false);
			}
			{
				valuesPanel = new JPanel();
				valuesPanel.setOpaque(false);
				valuesPanel.setLayout(new GridBagLayout());
				add(valuesPanel, new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER,
					GridBagConstraints.HORIZONTAL, new Insets(0, 10, 5, 0), 0, 0));
			}

			upButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed (ActionEvent evt) {
					int currentIndex = effectPanels.indexOf(EffectPanel.this);
					if (currentIndex > 0) {
						moveEffect(currentIndex - 1);
						updateFont();
						updateUpDownButtons();
					}
				}
			});
			
			downButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed (ActionEvent evt) {
					int currentIndex = effectPanels.indexOf(EffectPanel.this);
					if (currentIndex < effectPanels.size() - 1) {
						moveEffect(currentIndex + 1);
						updateFont();
						updateUpDownButtons();
					}
				}
			});
			
			deleteButton.addActionListener(new ActionListener() {
				public void actionPerformed (ActionEvent evt) {
					remove();
					updateFont();
					updateUpDownButtons();
				}
			});

			updateValues();
			updateFont();
			updateUpDownButtons();
		}

		public void remove () {
			effectPanels.remove(this);
			appliedEffectsPanel.remove(EffectPanel.this);
			getContentPane().validate();
			effectsList.getListSelectionListeners()[0].valueChanged(null);
		}

		public void updateValues () {
			prefs.put("foreground", EffectUtil.toString(colorEffect.getColor()));
			valuesPanel.removeAll();
			values = effect.getValues();
			for (Iterator iter = values.iterator(); iter.hasNext();)
				addValue((Value)iter.next());
		}
		
		public void updateUpDownButtons() {

			for(int index = 0; index < effectPanels.size(); index++){
				EffectPanel effectPanel = effectPanels.get(index);
				if (index == 0) {
					effectPanel.upButton.setEnabled(false);
				} else {
					effectPanel.upButton.setEnabled(true);
				}
				
				if (index == effectPanels.size() - 1) {
					effectPanel.downButton.setEnabled(false);
				} else {
					effectPanel.downButton.setEnabled(true);
				}
			}
		}
		
		public void moveEffect (int newIndex) {
			appliedEffectsPanel.remove(this);
			effectPanels.remove(this);
			appliedEffectsPanel.add(this, constrains, newIndex);
			effectPanels.add(newIndex, this);
		}

		public void addValue (final Value value) {
			JLabel valueNameLabel = new JLabel(value.getName() + ":");
			valuesPanel.add(valueNameLabel, new GridBagConstraints(0, -1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));

			final JLabel valueValueLabel = new JLabel();
			valuesPanel.add(valueValueLabel, new GridBagConstraints(1, -1, 1, 1, 1.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));
			valueValueLabel.setOpaque(true);
			if (value.getObject() instanceof java.awt.Color)
				valueValueLabel.setIcon(getColorIcon((java.awt.Color)value.getObject()));
			else
				valueValueLabel.setText(value.toString());

			valueValueLabel.addMouseListener(new MouseAdapter() {
				public void mouseEntered (MouseEvent evt) {
					valueValueLabel.setBackground(selectedColor);
				}

				public void mouseExited (MouseEvent evt) {
					valueValueLabel.setBackground(null);
				}

				public void mouseClicked (MouseEvent evt) {
					Object oldObject = value.getObject();
					value.showDialog();
					if (!value.getObject().equals(oldObject)) {
						effect.setValues(values);
						updateValues();
						updateFont();
					}
				}
			});
		}

		public ConfigurableEffect getEffect () {
			return effect;
		}

		public boolean equals (Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			final EffectPanel other = (EffectPanel)obj;
			if (effect == null) {
				if (other.effect != null) return false;
			} else if (!effect.equals(other.effect)) return false;
			return true;
		}
		
	}

	static private class Splash extends JWindow {
		final int minMillis;
		final long startTime;

		public Splash (Frame frame, String imageFile, int minMillis) {
			super(frame);
			this.minMillis = minMillis;
			getContentPane().add(new JLabel(new ImageIcon(Splash.class.getResource(imageFile))), BorderLayout.CENTER);
			pack();
			setLocationRelativeTo(null);
			setVisible(true);
			startTime = System.currentTimeMillis();
		}

		public void close () {
			final long endTime = System.currentTimeMillis();
			new Thread(new Runnable() {
				public void run () {
					if (endTime - startTime < minMillis) {
						addMouseListener(new MouseAdapter() {
							public void mousePressed (MouseEvent evt) {
								dispose();
							}
						});
						try {
							Thread.sleep(minMillis - (endTime - startTime));
						} catch (InterruptedException ignored) {
						}
					}
					EventQueue.invokeLater(new Runnable() {
						public void run () {
							dispose();
						}
					});
				}
			}, "Splash").start();
		}
	}

	class Renderer implements ApplicationListener {
		private String sampleText;

		@Override
		public void create () {
			glEnable(GL_SCISSOR_TEST);

			glEnable(GL_TEXTURE_2D);
			glEnableClientState(GL_TEXTURE_COORD_ARRAY);
			glEnableClientState(GL_VERTEX_ARRAY);

			glClearColor(0, 0, 0, 0);
			glClearDepth(1);

			glDisable(GL_LIGHTING);

			glEnable(GL_BLEND);
			glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		}

		@Override
		public void resize (int width, int height) {
			glViewport(0, 0, width, height);
			glScissor(0, 0, width, height);

			glMatrixMode(GL_PROJECTION);
			glLoadIdentity();
			glOrtho(0, width, height, 0, 1, -1);
			glMatrixMode(GL_MODELVIEW);
			glLoadIdentity();
		}

		public void render () {
			int viewWidth = Gdx.graphics.getWidth();
			int viewHeight = Gdx.graphics.getHeight();

			if (newUnicodeFont != null) {
				if (unicodeFont != null) unicodeFont.destroy();
				unicodeFont = newUnicodeFont;
				newUnicodeFont = null;
			}

			if (!unicodeFont.getEffects().isEmpty() && unicodeFont.loadGlyphs(25)) {
				glyphPageComboModel.removeAllElements();
				int pageCount = unicodeFont.getGlyphPages().size();
				int glyphCount = 0;
				for (int i = 0; i < pageCount; i++) {
					glyphPageComboModel.addElement("Page " + (i + 1));
					glyphCount += ((GlyphPage)unicodeFont.getGlyphPages().get(i)).getGlyphs().size();
				}
				glyphPagesTotalLabel.setText(String.valueOf(pageCount));
				glyphsTotalLabel.setText(String.valueOf(glyphCount));
			}

			if (unicodeFont == null) return;

			try {
				sampleText = sampleTextPane.getText();
			} catch (Exception ex) {
			}

			if (sampleTextRadio.isSelected()) {
				GL11.glClearColor(renderingBackgroundColor.r, renderingBackgroundColor.g, renderingBackgroundColor.b,
					renderingBackgroundColor.a);
				GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
				int offset = unicodeFont.getYOffset(sampleText);
				if (offset > 0) offset = 0;
				unicodeFont.drawString(0, -offset, sampleText, Color.WHITE, 0, sampleText.length());
			} else {
				GL11.glClearColor(1, 1, 1, 1);
				GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
				unicodeFont.addGlyphs(sampleText);
				// GL11.glColor4f(renderingBackgroundColor.r, renderingBackgroundColor.g, renderingBackgroundColor.b,
				// renderingBackgroundColor.a);
				// fillRect(0, 0, unicodeFont.getGlyphPageWidth() + 2, unicodeFont.getGlyphPageHeight() + 2);
				int index = glyphPageCombo.getSelectedIndex();
				List pages = unicodeFont.getGlyphPages();
				if (index >= 0 && index < pages.size()) {
					Texture texture = ((GlyphPage)pages.get(glyphPageCombo.getSelectedIndex())).getTexture();

					glDisable(GL_TEXTURE_2D);
					glColor4f(renderingBackgroundColor.r, renderingBackgroundColor.g, renderingBackgroundColor.b,
						renderingBackgroundColor.a);
					glBegin(GL_QUADS);
					glVertex3f(0, 0, 0);
					glVertex3f(0, texture.getHeight(), 0);
					glVertex3f(texture.getWidth(), texture.getHeight(), 0);
					glVertex3f(texture.getWidth(), 0, 0);
					glEnd();
					glEnable(GL_TEXTURE_2D);

					texture.bind();
					glColor4f(1, 1, 1, 1);
					glBegin(GL_QUADS);
					glTexCoord2f(0, 0);
					glVertex3f(0, 0, 0);

					glTexCoord2f(0, 1);
					glVertex3f(0, texture.getHeight(), 0);

					glTexCoord2f(1, 1);
					glVertex3f(texture.getWidth(), texture.getHeight(), 0);

					glTexCoord2f(1, 0);
					glVertex3f(texture.getWidth(), 0, 0);
					glEnd();
				}
			}

			if (saveBmFontFile != null) {
				try {
					BMFontUtil bmFont = new BMFontUtil(unicodeFont);
					bmFont.save(saveBmFontFile);

					if (batchMode) {
						System.exit(0);
					}
				} catch (Throwable ex) {
					System.out.println("Error saving BMFont files: " + saveBmFontFile.getAbsolutePath());
					ex.printStackTrace();
				} finally {
					saveBmFontFile = null;
				}
			}
		}

		@Override
		public void pause () {
		}

		@Override
		public void resume () {
		}

		@Override
		public void dispose () {
		}
	}

	public static void main (final String[] args) throws Exception {
// LookAndFeelInfo[] lookAndFeels = UIManager.getInstalledLookAndFeels();
// for (int i = 0, n = lookAndFeels.length; i < n; i++) {
// if ("Nimbus".equals(lookAndFeels[i].getName())) {
// try {
// UIManager.setLookAndFeel(lookAndFeels[i].getClassName());
// } catch (Throwable ignored) {
// }
// break;
// }
// }
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run () {
				new Hiero(args);
			}
		});
	}
}

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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashSet;
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
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
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
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.tools.hiero.unicodefont.UnicodeFont;
import com.badlogic.gdx.tools.hiero.unicodefont.effects.EffectUtil;

import de.matthiasmann.twlthemeeditor.fontgen.CharSet;
import de.matthiasmann.twlthemeeditor.fontgen.Effect;
import de.matthiasmann.twlthemeeditor.fontgen.FontData;
import de.matthiasmann.twlthemeeditor.fontgen.FontGenerator;
import de.matthiasmann.twlthemeeditor.fontgen.FontGenerator.ExportFormat;
import de.matthiasmann.twlthemeeditor.fontgen.FontGenerator.GeneratorMethod;
import de.matthiasmann.twlthemeeditor.fontgen.Padding;
import static org.lwjgl.opengl.GL11.*;

/** WIP, based on Matthias' TWL font renderer. Currently missing effects and saving hiero settings.<br>
 * <br>
 * A tool to visualize settings for {@link UnicodeFont} and to export BMFont files for use with {@link BitmapFont}.
 * @author Nathan Sweet */
public class Hiero4 extends JFrame {
	static final String NEHE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ\n" //
		+ "abcdefghijklmnopqrstuvwxyz\n1234567890\n" //
		+ "\"!`?'.,;:()[]{}<>|/@\\^$-%+=#_&~*\u007F";

	Color renderingBackgroundColor = Color.BLACK;
	Preferences prefs;
	Renderer renderer;
	FontData fontData;
	FontGenerator fontGenerator;
	HashSet<Character> sampleChars = new HashSet(256);
	HashSet<Character> remainingSampleChars = new HashSet(256);

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
	JRadioButton vectorRadio;
	JRadioButton drawStringRadio;
	JRadioButton freetypeRadio;
	JComboBox glyphPageHeightCombo;
	JComboBox glyphPageWidthCombo;
	JPanel glyphCachePanel;
	JRadioButton glyphCacheRadio;
	JRadioButton sampleTextRadio;
	JButton sampleAsciiButton;
	JButton sampleNeheButton;
	DefaultComboBoxModel effectsListModel;
	JMenuItem openMenuItem;
	JMenuItem saveMenuItem;
	JMenuItem exitMenuItem;
	JMenuItem saveBMFontMenuItem;

	public Hiero4 () {
		super("Hiero v4.0 - Bitmap Font Tool");
		Splash splash = new Splash(this, "/splash.jpg", 2000);
		initialize();
		splash.close();

		gamePanel.add(new LwjglCanvas(renderer = new Renderer()).getCanvas());

		prefs = Preferences.userNodeForPackage(Hiero4.class);
		java.awt.Color backgroundColor = EffectUtil.fromString(prefs.get("background", "000000"));
		backgroundColorLabel.setIcon(getColorIcon(backgroundColor));
		renderingBackgroundColor = new Color(backgroundColor.getRed() / 255f, backgroundColor.getGreen() / 255f,
			backgroundColor.getBlue() / 255f, 1);

		boolean useFontList = getFontFile(Font.decode("Arial")) != null;
		fontList.setEnabled(useFontList);
		systemFontRadio.setEnabled(useFontList);
		fontFileRadio.setSelected(!useFontList);
		fontFileText.setText(prefs.get("font.file", ""));

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

	void changeFont () {
		if (renderer.batch == null) return;

		String path;
		if (systemFontRadio.isSelected()) {
			path = getFontFile(Font.decode((String)fontList.getSelectedValue()));
		} else {
			path = fontFileText.getText();
		}
		if (path == null) {
			renderer.font = null;
			return;
		}

		int fontSize = ((Integer)fontSizeSpinner.getValue()).intValue();

		try {
			fontData = new FontData(new File(path), fontSize);
		} catch (IOException ex) {
			ex.printStackTrace();
			renderer.font = null;
			return;
		}

		sampleChars.clear();
		updateFont(true);
	}

	void updateFont (boolean force) {
		if (fontData == null || renderer.batch == null) return;

		final String text = sampleTextPane.getText();

		if (!force) {
			// Don't regenerate font unless the text has new characters or has removed characters.
			boolean newCharFound = false;
			remainingSampleChars.clear();
			remainingSampleChars.addAll(sampleChars);
			for (int i = text.length() - 1; i >= 0; i--) {
				Character ch = text.charAt(i);
				if (sampleChars.add(ch)) newCharFound = true;
				remainingSampleChars.remove(ch);
			}
			if (!newCharFound && remainingSampleChars.isEmpty()) return;
		}
		sampleChars.clear();
		for (int i = text.length() - 1; i >= 0; i--)
			sampleChars.add(text.charAt(i));

		int fontSize = ((Integer)fontSizeSpinner.getValue()).intValue();

		int style = Font.PLAIN;
		if (boldCheckBox.isSelected()) {
			style = Font.BOLD;
			if (italicCheckBox.isSelected()) style |= Font.ITALIC;
		} else if (italicCheckBox.isSelected()) //
			style = Font.ITALIC;
		fontData = fontData.deriveFont(fontSize, style);

		int sampleFontSize = sampleTextPane.getFont().getSize();
		if (sampleFontSize < 14) sampleFontSize = 14;
		sampleTextPane.setFont(fontData.getJavaFont().deriveFont((float)sampleFontSize));

		final Padding padding = new Padding((Integer)padTopSpinner.getValue(), (Integer)padLeftSpinner.getValue(),
			(Integer)padBottomSpinner.getValue(), (Integer)padRightSpinner.getValue(), (Integer)padAdvanceXSpinner.getValue());
		final int width = (Integer)glyphPageWidthCombo.getSelectedItem();
		final int height = (Integer)glyphPageHeightCombo.getSelectedItem();

		final GeneratorMethod method;
		if (vectorRadio.isSelected())
			method = GeneratorMethod.AWT_VECTOR;
		else if (drawStringRadio.isSelected())
			method = GeneratorMethod.AWT_DRAWSTRING;
		else
			method = GeneratorMethod.FREETYPE2;

		new Thread() {
			public void run () {
				fontGenerator = new FontGenerator(fontData, method);
				CharSet charset = new CharSet();
				charset.setManualCharacters(text);
				try {
					fontGenerator.generate(width, height, charset, padding, new Effect.Renderer[0], true);
					fontGenerator.write(new File("out"), ExportFormat.TEXT);
				} catch (IOException ex) {
					ex.printStackTrace();
				}
				EventQueue.invokeLater(new Runnable() {
					public void run () {
						final ByteBuffer buffer = ByteBuffer.allocateDirect(1024 * 1024 * 4);
						buffer.order(ByteOrder.LITTLE_ENDIAN);
						fontGenerator.getTextureData(buffer.asIntBuffer());
						TextureRegion glyphRegion = new TextureRegion(new Texture(new TextureData() {
							Pixmap pixmap;

							public int getWidth () {
								return width;
							}

							public int getHeight () {
								return height;
							}

							@Override
							public Pixmap consumePixmap () {
								this.pixmap = new Pixmap(width, height, Format.RGBA8888);
								pixmap.getPixels().put(buffer);
								pixmap.getPixels().rewind();
								return pixmap;
							}

							@Override
							public boolean disposePixmap () {
								return true;
							}

							@Override
							public Format getFormat () {
								return pixmap.getFormat();
							}

							@Override
							public boolean useMipMaps () {
								return false;
							}

							@Override
							public boolean isManaged () {
								return true;
							}

							@Override
							public TextureDataType getType () {
								return TextureDataType.Pixmap;
							}

							@Override
							public void consumeCustomData (int target) {
							}

							@Override
							public boolean isPrepared () {
								return true;
							}

							@Override
							public void prepare () {
							}
						}));
						renderer.font = new BitmapFont(Gdx.files.absolute("out"), glyphRegion, false);
					}
				});
			}
		}.start();
	}

	private void initializeEvents () {
		class FontChangeListener implements ChangeListener, ActionListener {
			public void stateChanged (ChangeEvent evt) {
				changeFont();
			}

			public void actionPerformed (ActionEvent evt) {
				changeFont();
			}
		}
		FontChangeListener change = new FontChangeListener();
		fontFileRadio.addActionListener(change);
		systemFontRadio.addActionListener(change);
		fontList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged (ListSelectionEvent evt) {
				if (evt.getValueIsAdjusting()) return;
				prefs.put("system.font", (String)fontList.getSelectedValue());
				changeFont();
			}
		});

		class FontUpdateListener implements ChangeListener, ActionListener, DocumentListener {
			private final boolean force;

			public FontUpdateListener (boolean force) {
				this.force = force;
			}

			public void stateChanged (ChangeEvent evt) {
				updateFont(force);
			}

			public void actionPerformed (ActionEvent evt) {
				updateFont(force);
			}

			public void removeUpdate (DocumentEvent evt) {
				updateFont(force);
			}

			public void insertUpdate (DocumentEvent evt) {
				updateFont(force);
			}

			public void changedUpdate (DocumentEvent evt) {
				updateFont(force);
			}

			public void addSpinners (JSpinner[] spinners) {
				// Swing sucks.
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
		FontUpdateListener update = new FontUpdateListener(true);
		FontUpdateListener updateText = new FontUpdateListener(false);
		update.addSpinners(new JSpinner[] {padTopSpinner, padRightSpinner, padBottomSpinner, padLeftSpinner, padAdvanceXSpinner,
			padAdvanceYSpinner});
		fontSizeSpinner.addChangeListener(update);
		glyphPageWidthCombo.addActionListener(update);
		glyphPageHeightCombo.addActionListener(update);
		boldCheckBox.addActionListener(update);
		italicCheckBox.addActionListener(update);
		vectorRadio.addActionListener(update);
		drawStringRadio.addActionListener(update);
		freetypeRadio.addActionListener(update);
		sampleTextPane.getDocument().addDocumentListener(updateText);

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
				updateFont(true);
			}
		});

		browseButton.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent evt) {
				FileDialog dialog = new FileDialog(Hiero4.this, "Choose TrueType font file", FileDialog.LOAD);
				dialog.setLocationRelativeTo(null);
				dialog.setFile("*.ttf");
				dialog.setVisible(true);
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

		saveBMFontMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent evt) {
				if (fontGenerator == null) return;
				FileDialog dialog = new FileDialog(Hiero4.this, "Save BMFont files", FileDialog.SAVE);
				dialog.setLocationRelativeTo(null);
				dialog.setFile("*.fnt");
				dialog.setVisible(true);
				String fileName = dialog.getFile();
				if (fileName == null) return;
				try {
					fontGenerator.write(new File(dialog.getDirectory(), fileName), ExportFormat.TEXT);
				} catch (IOException ex) {
					ex.printStackTrace();
				}
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
				vectorRadio = new JRadioButton("Vector");
				fontPanel.add(vectorRadio, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
					GridBagConstraints.NONE, new Insets(0, 0, 5, 5), 0, 0));
				if (!FontGenerator.GeneratorMethod.FREETYPE2.isAvailable) vectorRadio.setSelected(true);
			}
			{
				drawStringRadio = new JRadioButton("DrawString");
				fontPanel.add(drawStringRadio, new GridBagConstraints(2, 4, 1, 1, 1.0, 0.0, GridBagConstraints.WEST,
					GridBagConstraints.NONE, new Insets(0, 0, 5, 5), 0, 0));
			}
			{
				freetypeRadio = new JRadioButton("FreeType");
				fontPanel.add(freetypeRadio, new GridBagConstraints(3, 4, 1, 1, 1.0, 0.0, GridBagConstraints.WEST,
					GridBagConstraints.NONE, new Insets(0, 0, 5, 5), 0, 0));
				if (FontGenerator.GeneratorMethod.FREETYPE2.isAvailable)
					freetypeRadio.setSelected(true);
				else
					freetypeRadio.setEnabled(false);
			}
			ButtonGroup buttonGroup = new ButtonGroup();
			buttonGroup.add(systemFontRadio);
			buttonGroup.add(fontFileRadio);
			buttonGroup = new ButtonGroup();
			buttonGroup.add(vectorRadio);
			buttonGroup.add(drawStringRadio);
			buttonGroup.add(freetypeRadio);
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
					glyphCachePanel.add(new JLabel("Page width:"), new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
						GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 5, 5, 5), 0, 0));
				}
				{
					glyphCachePanel.add(new JLabel("Page height:"), new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
						GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 5, 5, 5), 0, 0));
				}
				{
					glyphPageWidthCombo = new JComboBox(new DefaultComboBoxModel(new Integer[] {new Integer(64), new Integer(128),
						new Integer(256), new Integer(512), new Integer(1024), new Integer(2048)}));
					glyphCachePanel.add(glyphPageWidthCombo, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
						GridBagConstraints.NONE, new Insets(0, 0, 5, 5), 0, 0));
					glyphPageWidthCombo.setSelectedIndex(4);
				}
				{
					glyphPageHeightCombo = new JComboBox(new DefaultComboBoxModel(new Integer[] {new Integer(64), new Integer(128),
						new Integer(256), new Integer(512), new Integer(1024), new Integer(2048)}));
					glyphCachePanel.add(glyphPageHeightCombo, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
						GridBagConstraints.NONE, new Insets(0, 0, 5, 5), 0, 0));
					glyphPageHeightCombo.setSelectedIndex(4);
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
					effectsList.setVisibleRowCount(6);
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
		SpriteBatch batch;
		BitmapFont font;

		@Override
		public void create () {
			batch = new SpriteBatch();
			changeFont();
		}

		@Override
		public void resize (int width, int height) {
			GL11.glViewport(0, 0, width, height);
			batch.getProjectionMatrix().setToOrtho(0, width, 0, height, 0, 1);
		}

		@Override
		public void render () {
			int viewWidth = Gdx.graphics.getWidth();
			int viewHeight = Gdx.graphics.getHeight();

			BitmapFont font = this.font;
			if (font == null) {
				glClearColor(0, 0, 0, 0);
				GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
				return;
			}

			if (sampleTextRadio.isSelected()) {
				glClearColor(renderingBackgroundColor.r, renderingBackgroundColor.g, renderingBackgroundColor.b,
					renderingBackgroundColor.a);
				GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

				batch.begin();
				font.drawWrapped(batch, sampleTextPane.getText(), 0, viewHeight, viewWidth);
				batch.end();
			} else {
				glClearColor(1, 1, 1, 1);
				GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

				TextureRegion region = font.getRegion();
				float y = viewHeight - region.getRegionHeight();

				glColor4f(renderingBackgroundColor.r, renderingBackgroundColor.g, renderingBackgroundColor.b, 0);
				glBegin(GL_QUADS);
				glVertex3f(0, y + region.getRegionHeight(), 0);
				glVertex3f(0, y, 0);
				glVertex3f(region.getRegionWidth(), y, 0);
				glVertex3f(region.getRegionWidth(), y + region.getRegionHeight(), 0);
				glEnd();

				batch.begin();
				batch.draw(region, 0, y);
				batch.end();
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

	static public String getFontFile (Font font) {
		try {
			font.getFamily(); // Causes font2DHandle to be set.
			Field font2DHandleField = Font.class.getDeclaredField("font2DHandle");
			font2DHandleField.setAccessible(true);
			Object font2DHandle = font2DHandleField.get(font);
			Field font2DField = font2DHandle.getClass().getDeclaredField("font2D");
			Object font2D = font2DField.get(font2DHandle);
			Field platNameField = Class.forName("sun.font.PhysicalFont").getDeclaredField("platName");
			platNameField.setAccessible(true);
			return (String)platNameField.get(font2D);
		} catch (Exception ex) {
			return null;
		}
	}

	static public void main (String[] args) throws Exception {
		LookAndFeelInfo[] lookAndFeels = UIManager.getInstalledLookAndFeels();
		for (int i = 0, n = lookAndFeels.length; i < n; i++) {
			if ("Nimbus".equals(lookAndFeels[i].getName())) {
				try {
					UIManager.setLookAndFeel(lookAndFeels[i].getClassName());
				} catch (Throwable ignored) {
				}
				break;
			}
		}
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run () {
				new Hiero4();
			}
		});
	}
}

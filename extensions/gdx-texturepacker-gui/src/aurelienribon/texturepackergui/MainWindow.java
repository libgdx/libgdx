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
package aurelienribon.texturepackergui;

import aurelienribon.ui.components.ArStyle;
import aurelienribon.ui.components.PaintedPanel;
import aurelienribon.ui.css.Style;
import aurelienribon.ui.css.swing.SwingStyle;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.tools.imagepacker.TexturePacker;
import com.badlogic.gdx.utils.GdxRuntimeException;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import res.Res;

public class MainWindow extends javax.swing.JFrame {
	private final Canvas canvas;
	private Project project;
	private File projectFile;

    public MainWindow(Project project, Canvas canvas, Component canvasCmp) {
		this.project = project;
		this.canvas = canvas;

		canvas.setCallback(new Canvas.Callback() {
			@Override public void atlasError() {
				JOptionPane.showMessageDialog(MainWindow.this, "Impossible to create the atlas in ligdx canvas, sorry.");
			}
		});

		setContentPane(new PaintedPanel());
		getContentPane().setLayout(new BorderLayout());
        initComponents();
		renderPanel.add(canvasCmp, BorderLayout.CENTER);
		loadSettings();

		browseInputBtn.addActionListener(new ActionListener() {@Override public void actionPerformed(ActionEvent e) {browseInput();}});
		browseOutputBtn.addActionListener(new ActionListener() {@Override public void actionPerformed(ActionEvent e) {browseOutput();}});
		loadProjectBtn.addActionListener(new ActionListener() {@Override public void actionPerformed(ActionEvent e) {loadProject();}});
		saveProjectBtn.addActionListener(new ActionListener() {@Override public void actionPerformed(ActionEvent e) {saveProject();}});
		launchPackBtn.addActionListener(new ActionListener() {@Override public void actionPerformed(ActionEvent e) {launchPack();}});

		SwingStyle.init();
		ArStyle.init();
		Style.registerCssClasses(getContentPane(), ".rootPanel");
		Style.registerCssClasses(projectPanel, ".titledPanel", "#projectPanel");
		Style.registerCssClasses(settingsPanel, ".titledPanel", "#settingsPanel");
		Style.registerCssClasses(renderPanel, ".titledPanel", "#renderPanel");
		Style.registerCssClasses(launchPackBtn, ".centerText");
		Style.apply(getContentPane(), new Style(Res.getUrl("css/style.css")));
    }

	private void browseInput() {
		File dir = new File(project.input);
		if (!dir.isDirectory()) dir = new File(".");

		JFileChooser chooser = new JFileChooser(dir);
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setFileFilter(new FileFilter() {
			@Override public boolean accept(File f) {return f.isDirectory();}
			@Override public String getDescription() {return "Directories";}
		});

		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			File selectedDir = chooser.getSelectedFile();
			project.input = selectedDir.getPath();
			inputField.setText(project.input);
		}
	}

	private void browseOutput() {
		File dir = new File(project.output);
		if (!dir.isDirectory()) dir = new File(".");

		JFileChooser chooser = new JFileChooser(dir);
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setFileFilter(new FileFilter() {
			@Override public boolean accept(File f) {return f.isDirectory();}
			@Override public String getDescription() {return "Directories";}
		});

		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			File selectedDir = chooser.getSelectedFile();
			project.output = selectedDir.getPath();
			outputField.setText(project.output);
			canvas.requestPackReload(project.output + "/" + project.packName);
		}
	}

	private void loadProject() {
		String dir = projectFile != null ? projectFile.getParent() : ".";
		JFileChooser chooser = new JFileChooser(dir);
		chooser.setDialogTitle("Select your project file");

		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			try {
				projectFile = chooser.getSelectedFile();
				project = Project.fromFile(projectFile);
				loadSettings();
				canvas.requestPackReload(project.output + "/" + project.packName);
			} catch (IOException ex) {
				JOptionPane.showMessageDialog(this, "Project file cannot be read.");
			}
		}
	}

	private void saveProject() {
		if (project.output.equals("") || project.input.equals(".")) {
			JOptionPane.showMessageDialog(this, "Select the input and ouput paths first");
			return;
		}

		String dir = projectFile != null ? projectFile.getParent() : "";
		JFileChooser chooser = new JFileChooser(dir);
		chooser.setDialogTitle("Select your project file");

		if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			try {
				saveSettings();
				projectFile = chooser.getSelectedFile();
				project.save(projectFile);
			} catch (IOException ex) {
				String msg = "Cannot save your project, sorry.\n"
					+ "Check if the project file can be written correctly.";
				JOptionPane.showMessageDialog(this, msg);
			}
		}
	}

	private void launchPack() {
		if (project.output.equals("") || project.input.equals("")) {
			JOptionPane.showMessageDialog(this, "Please first choose the input and output paths");
			return;
		}

		saveSettings();

		try {
			project.pack();
			canvas.requestPackReload(project.output + "/" + project.packName);
			JOptionPane.showMessageDialog(this, "Pack successful !");
		} catch (GdxRuntimeException ex) {
			String msg = "Pack unsuccessful... " + ex.getMessage();
			JOptionPane.showMessageDialog(this, msg);
		}
	}

	private void loadSettings() {
		inputField.setText(project.input);
		outputField.setText(project.output);
		packFileNameField.setText(project.packName);

		opt_alias_chk.setSelected(project.settings.alias);
		opt_alphaThreashold_nud.setValue(project.settings.alphaThreshold);
		opt_debug_chk.setSelected(project.settings.debug);
		opt_defaultMagFilter_cbox.setSelectedItem(project.settings.defaultFilterMag.toString());
		opt_defaultMinFilter_cbox.setSelectedItem(project.settings.defaultFilterMin.toString());
		opt_defaultImgFormat_cbox.setSelectedItem(project.settings.defaultFormat.toString());
		opt_jpgQuality_nud.setValue(project.settings.defaultImageQuality);
		opt_duplicatePadding_chk.setSelected(project.settings.duplicatePadding);
		opt_incremental_chk.setSelected(project.settings.incremental);
		opt_maxPageHeight_nud.setValue(project.settings.maxHeight);
		opt_maxPageWidth_nud.setValue(project.settings.maxWidth);
		opt_minPageHeight_nud.setValue(project.settings.minHeight);
		opt_minPageWidth_nud.setValue(project.settings.minWidth);
		opt_padding_nud.setValue(project.settings.padding);
		opt_outputPoT_chk.setSelected(project.settings.pot);
		opt_allowRotations_chk.setSelected(project.settings.rotate);
		opt_stripWhitespace_chk.setSelected(project.settings.stripWhitespace);
		opt_edgePadding_chk.setSelected(project.settings.edgePadding);
	}

	private void saveSettings() {
		project.input = inputField.getText();
		project.output = outputField.getText();
		project.packName = packFileNameField.getText();

		project.settings.alias = opt_alias_chk.isSelected();
		project.settings.alphaThreshold = (Integer)opt_alphaThreashold_nud.getValue();
		project.settings.debug = opt_debug_chk.isSelected();
		project.settings.defaultFileFormat = TexturePacker.FileFormat.valueOf((String)opt_defaultFileFormat_cbox.getSelectedItem());
		project.settings.defaultFilterMag = TextureFilter.valueOf((String)opt_defaultMagFilter_cbox.getSelectedItem());
		project.settings.defaultFilterMin = TextureFilter.valueOf((String)opt_defaultMinFilter_cbox.getSelectedItem());
		project.settings.defaultFormat = Format.valueOf((String)opt_defaultImgFormat_cbox.getSelectedItem());
		project.settings.defaultImageQuality = (Float)opt_jpgQuality_nud.getValue();
		project.settings.duplicatePadding = opt_duplicatePadding_chk.isSelected();
		project.settings.edgePadding = opt_edgePadding_chk.isSelected();
		project.settings.incremental = opt_incremental_chk.isSelected();
		project.settings.maxHeight = (Integer)opt_maxPageHeight_nud.getValue();
		project.settings.maxWidth = (Integer)opt_maxPageWidth_nud.getValue();
		project.settings.minHeight = (Integer)opt_minPageHeight_nud.getValue();
		project.settings.minWidth = (Integer)opt_minPageWidth_nud.getValue();
		project.settings.padding = (Integer)opt_padding_nud.getValue();
		project.settings.pot = opt_outputPoT_chk.isSelected();
		project.settings.rotate = opt_allowRotations_chk.isSelected();
		project.settings.stripWhitespace = opt_stripWhitespace_chk.isSelected();
	}

	// -------------------------------------------------------------------------
	// Generated stuff
	// -------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        configPanel = new javax.swing.JPanel();
        titlePanel = new javax.swing.JPanel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        projectPanel = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        inputField = new javax.swing.JTextField();
        browseInputBtn = new javax.swing.JButton();
        outputField = new javax.swing.JTextField();
        browseOutputBtn = new javax.swing.JButton();
        loadProjectBtn = new javax.swing.JButton();
        saveProjectBtn = new javax.swing.JButton();
        launchPackBtn = new javax.swing.JButton();
        jLabel12 = new javax.swing.JLabel();
        packFileNameField = new javax.swing.JTextField();
        settingsPanel = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        opt_defaultImgFormat_cbox = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        opt_defaultMinFilter_cbox = new javax.swing.JComboBox();
        jLabel4 = new javax.swing.JLabel();
        opt_defaultMagFilter_cbox = new javax.swing.JComboBox();
        jLabel7 = new javax.swing.JLabel();
        opt_minPageWidth_nud = new javax.swing.JSpinner();
        jLabel8 = new javax.swing.JLabel();
        opt_minPageHeight_nud = new javax.swing.JSpinner();
        jLabel9 = new javax.swing.JLabel();
        opt_maxPageWidth_nud = new javax.swing.JSpinner();
        jLabel10 = new javax.swing.JLabel();
        opt_maxPageHeight_nud = new javax.swing.JSpinner();
        jLabel6 = new javax.swing.JLabel();
        opt_padding_nud = new javax.swing.JSpinner();
        jLabel5 = new javax.swing.JLabel();
        opt_alphaThreashold_nud = new javax.swing.JSpinner();
        jLabel17 = new javax.swing.JLabel();
        opt_defaultFileFormat_cbox = new javax.swing.JComboBox();
        jLabel18 = new javax.swing.JLabel();
        opt_jpgQuality_nud = new javax.swing.JSpinner();
        opt_duplicatePadding_chk = new javax.swing.JCheckBox();
        opt_edgePadding_chk = new javax.swing.JCheckBox();
        opt_allowRotations_chk = new javax.swing.JCheckBox();
        opt_stripWhitespace_chk = new javax.swing.JCheckBox();
        opt_incremental_chk = new javax.swing.JCheckBox();
        opt_debug_chk = new javax.swing.JCheckBox();
        opt_alias_chk = new javax.swing.JCheckBox();
        opt_outputPoT_chk = new javax.swing.JCheckBox();
        opt_ignoreBlankImages_chk = new javax.swing.JCheckBox();
        centerPanel = new javax.swing.JPanel();
        renderPanel = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Texture Packer GUI");

        configPanel.setOpaque(false);

        titlePanel.setOpaque(false);

        jLabel14.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/gfx/lgdx-logo.png"))); // NOI18N

        jLabel15.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/gfx/title.png"))); // NOI18N

        jLabel13.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/gfx/texture.png"))); // NOI18N
        jLabel13.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);

        jLabel16.setForeground(Theme.MAIN_FOREGROUND);
        jLabel16.setText("v3.0.0");

        javax.swing.GroupLayout titlePanelLayout = new javax.swing.GroupLayout(titlePanel);
        titlePanel.setLayout(titlePanelLayout);
        titlePanelLayout.setHorizontalGroup(
            titlePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, titlePanelLayout.createSequentialGroup()
                .addComponent(jLabel13)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(titlePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(titlePanelLayout.createSequentialGroup()
                        .addComponent(jLabel14)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(titlePanelLayout.createSequentialGroup()
                        .addComponent(jLabel15)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel16))))
        );
        titlePanelLayout.setVerticalGroup(
            titlePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, titlePanelLayout.createSequentialGroup()
                .addComponent(jLabel14)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel15))
            .addComponent(jLabel13, javax.swing.GroupLayout.Alignment.TRAILING)
            .addComponent(jLabel16, javax.swing.GroupLayout.Alignment.TRAILING)
        );

        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel11.setText("Output directory:");

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel1.setText("Input directory:");

        browseInputBtn.setText("...");
        browseInputBtn.setMargin(new java.awt.Insets(2, 3, 2, 2));
        browseInputBtn.setOpaque(false);

        browseOutputBtn.setText("...");
        browseOutputBtn.setMargin(new java.awt.Insets(2, 3, 2, 2));
        browseOutputBtn.setOpaque(false);

        loadProjectBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/gfx/ic_open.png"))); // NOI18N
        loadProjectBtn.setText("Open project");
        loadProjectBtn.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        loadProjectBtn.setMargin(new java.awt.Insets(2, 3, 2, 3));

        saveProjectBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/gfx/ic_save.png"))); // NOI18N
        saveProjectBtn.setText("Save project");
        saveProjectBtn.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        saveProjectBtn.setMargin(new java.awt.Insets(2, 3, 2, 3));

        launchPackBtn.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        launchPackBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/gfx/ic_pack.png"))); // NOI18N
        launchPackBtn.setText("Pack !");
        launchPackBtn.setMargin(new java.awt.Insets(2, 3, 2, 3));

        jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel12.setText("Pack file name:");

        javax.swing.GroupLayout projectPanelLayout = new javax.swing.GroupLayout(projectPanel);
        projectPanel.setLayout(projectPanelLayout);
        projectPanelLayout.setHorizontalGroup(
            projectPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(projectPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(projectPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(projectPanelLayout.createSequentialGroup()
                        .addGroup(projectPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(saveProjectBtn)
                            .addComponent(loadProjectBtn, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(launchPackBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(projectPanelLayout.createSequentialGroup()
                        .addGroup(projectPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jLabel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel11, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(projectPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(projectPanelLayout.createSequentialGroup()
                                .addGroup(projectPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(outputField)
                                    .addComponent(inputField))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(projectPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(browseInputBtn)
                                    .addComponent(browseOutputBtn)))
                            .addComponent(packFileNameField))))
                .addContainerGap())
        );

        projectPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {loadProjectBtn, saveProjectBtn});

        projectPanelLayout.setVerticalGroup(
            projectPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(projectPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(projectPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(inputField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(browseInputBtn)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(projectPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(outputField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(browseOutputBtn))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(projectPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(packFileNameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(projectPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(projectPanelLayout.createSequentialGroup()
                        .addComponent(loadProjectBtn)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(saveProjectBtn))
                    .addComponent(launchPackBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel2.setText("Default image format:");

        opt_defaultImgFormat_cbox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "RGBA8888", "RGBA4444", "RGB888", "RGB565", "Alpha", "LuminanceAlpha", "Intensity" }));
        opt_defaultImgFormat_cbox.setOpaque(false);

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel3.setText("Default min. filter:");

        opt_defaultMinFilter_cbox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Nearest", "Linear", "MipMap", "MipMapNearestNearest", "MipMapNearestLinear", "MipMapLinearNearest", "MipMapLinearLinear" }));
        opt_defaultMinFilter_cbox.setOpaque(false);

        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel4.setText("Default mag. filter:");

        opt_defaultMagFilter_cbox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Nearest", "Linear", "MipMap", "MipMapNearestNearest", "MipMapNearestLinear", "MipMapLinearNearest", "MipMapLinearLinear" }));
        opt_defaultMagFilter_cbox.setOpaque(false);

        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel7.setText("Min. page width:");

        opt_minPageWidth_nud.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(16), Integer.valueOf(0), null, Integer.valueOf(1)));

        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel8.setText("Min. page height:");

        opt_minPageHeight_nud.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(16), Integer.valueOf(0), null, Integer.valueOf(1)));

        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel9.setText("Max. page width:");

        opt_maxPageWidth_nud.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(512), Integer.valueOf(0), null, Integer.valueOf(1)));

        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel10.setText("Max. page height:");

        opt_maxPageHeight_nud.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(512), Integer.valueOf(0), null, Integer.valueOf(1)));

        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel6.setText("Padding:");

        opt_padding_nud.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(2), Integer.valueOf(0), null, Integer.valueOf(1)));

        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel5.setText("Alpha threshold:");

        opt_alphaThreashold_nud.setModel(new javax.swing.SpinnerNumberModel(0, 0, 255, 1));

        jLabel17.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel17.setText("Default file format:");

        opt_defaultFileFormat_cbox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "PNG", "JPEG" }));
        opt_defaultFileFormat_cbox.setOpaque(false);

        jLabel18.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel18.setText("Jpeg quality:");

        opt_jpgQuality_nud.setModel(new javax.swing.SpinnerNumberModel(Float.valueOf(0.0f), Float.valueOf(0.0f), Float.valueOf(1.0f), Float.valueOf(0.05f)));

        opt_duplicatePadding_chk.setText("Duplicate padding");
        opt_duplicatePadding_chk.setOpaque(false);

        opt_edgePadding_chk.setText("Edge padding");
        opt_edgePadding_chk.setOpaque(false);

        opt_allowRotations_chk.setText("Allow rotations");
        opt_allowRotations_chk.setOpaque(false);

        opt_stripWhitespace_chk.setText("Strip whitespace");
        opt_stripWhitespace_chk.setOpaque(false);

        opt_incremental_chk.setText("Incremental");
        opt_incremental_chk.setOpaque(false);

        opt_debug_chk.setText("Debug output");
        opt_debug_chk.setOpaque(false);

        opt_alias_chk.setText("Use aliases");
        opt_alias_chk.setOpaque(false);

        opt_outputPoT_chk.setText("Force PoT");
        opt_outputPoT_chk.setOpaque(false);

        opt_ignoreBlankImages_chk.setText("Ignore blank imgs");
        opt_ignoreBlankImages_chk.setOpaque(false);

        javax.swing.GroupLayout settingsPanelLayout = new javax.swing.GroupLayout(settingsPanel);
        settingsPanel.setLayout(settingsPanelLayout);
        settingsPanelLayout.setHorizontalGroup(
            settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(settingsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(settingsPanelLayout.createSequentialGroup()
                        .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(opt_defaultMagFilter_cbox, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(opt_defaultMinFilter_cbox, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(opt_defaultImgFormat_cbox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(settingsPanelLayout.createSequentialGroup()
                        .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(settingsPanelLayout.createSequentialGroup()
                                .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(opt_duplicatePadding_chk)
                                    .addComponent(opt_edgePadding_chk)
                                    .addComponent(opt_incremental_chk))
                                .addGap(18, 18, 18)
                                .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(opt_ignoreBlankImages_chk)
                                    .addComponent(opt_allowRotations_chk)
                                    .addComponent(opt_stripWhitespace_chk))
                                .addGap(18, 18, 18)
                                .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(opt_outputPoT_chk)
                                    .addComponent(opt_alias_chk)
                                    .addComponent(opt_debug_chk)))
                            .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, settingsPanelLayout.createSequentialGroup()
                                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(opt_padding_nud, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(opt_alphaThreashold_nud))
                                .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(settingsPanelLayout.createSequentialGroup()
                                        .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(opt_maxPageWidth_nud, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(settingsPanelLayout.createSequentialGroup()
                                        .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, settingsPanelLayout.createSequentialGroup()
                                                .addComponent(jLabel17, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(opt_defaultFileFormat_cbox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, settingsPanelLayout.createSequentialGroup()
                                                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(opt_minPageWidth_nud, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, settingsPanelLayout.createSequentialGroup()
                                                .addComponent(jLabel18, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(opt_jpgQuality_nud, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(settingsPanelLayout.createSequentialGroup()
                                                .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(opt_maxPageHeight_nud, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(settingsPanelLayout.createSequentialGroup()
                                                .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(opt_minPageHeight_nud, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)))))))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        settingsPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {opt_jpgQuality_nud, opt_maxPageHeight_nud, opt_maxPageWidth_nud, opt_minPageHeight_nud, opt_minPageWidth_nud, opt_padding_nud});

        settingsPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {opt_allowRotations_chk, opt_duplicatePadding_chk, opt_edgePadding_chk, opt_ignoreBlankImages_chk, opt_incremental_chk, opt_stripWhitespace_chk});

        settingsPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {opt_alias_chk, opt_debug_chk, opt_outputPoT_chk});

        settingsPanelLayout.setVerticalGroup(
            settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(settingsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(opt_defaultImgFormat_cbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(opt_defaultMinFilter_cbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(opt_defaultMagFilter_cbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel17)
                    .addComponent(opt_defaultFileFormat_cbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel18)
                    .addComponent(opt_jpgQuality_nud, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(opt_minPageWidth_nud, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8)
                    .addComponent(opt_minPageHeight_nud, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(opt_maxPageWidth_nud, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10)
                    .addComponent(opt_maxPageHeight_nud, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(opt_padding_nud, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5)
                    .addComponent(opt_alphaThreashold_nud, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(settingsPanelLayout.createSequentialGroup()
                        .addComponent(opt_stripWhitespace_chk)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(opt_allowRotations_chk)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(opt_ignoreBlankImages_chk)
                            .addComponent(opt_debug_chk)))
                    .addGroup(settingsPanelLayout.createSequentialGroup()
                        .addComponent(opt_outputPoT_chk)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(opt_alias_chk))
                    .addGroup(settingsPanelLayout.createSequentialGroup()
                        .addComponent(opt_duplicatePadding_chk)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(opt_edgePadding_chk)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(opt_incremental_chk)))
                .addContainerGap(37, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout configPanelLayout = new javax.swing.GroupLayout(configPanel);
        configPanel.setLayout(configPanelLayout);
        configPanelLayout.setHorizontalGroup(
            configPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(configPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(configPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(titlePanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(projectPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(settingsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        configPanelLayout.setVerticalGroup(
            configPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(configPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(titlePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(projectPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(settingsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        getContentPane().add(configPanel, java.awt.BorderLayout.WEST);

        centerPanel.setOpaque(false);

        renderPanel.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout centerPanelLayout = new javax.swing.GroupLayout(centerPanel);
        centerPanel.setLayout(centerPanelLayout);
        centerPanelLayout.setHorizontalGroup(
            centerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, centerPanelLayout.createSequentialGroup()
                .addComponent(renderPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 494, Short.MAX_VALUE)
                .addContainerGap())
        );
        centerPanelLayout.setVerticalGroup(
            centerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(centerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(renderPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 569, Short.MAX_VALUE)
                .addContainerGap())
        );

        getContentPane().add(centerPanel, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseInputBtn;
    private javax.swing.JButton browseOutputBtn;
    private javax.swing.JPanel centerPanel;
    private javax.swing.JPanel configPanel;
    private javax.swing.JTextField inputField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JButton launchPackBtn;
    private javax.swing.JButton loadProjectBtn;
    private javax.swing.JCheckBox opt_alias_chk;
    private javax.swing.JCheckBox opt_allowRotations_chk;
    private javax.swing.JSpinner opt_alphaThreashold_nud;
    private javax.swing.JCheckBox opt_debug_chk;
    private javax.swing.JComboBox opt_defaultFileFormat_cbox;
    private javax.swing.JComboBox opt_defaultImgFormat_cbox;
    private javax.swing.JComboBox opt_defaultMagFilter_cbox;
    private javax.swing.JComboBox opt_defaultMinFilter_cbox;
    private javax.swing.JCheckBox opt_duplicatePadding_chk;
    private javax.swing.JCheckBox opt_edgePadding_chk;
    private javax.swing.JCheckBox opt_ignoreBlankImages_chk;
    private javax.swing.JCheckBox opt_incremental_chk;
    private javax.swing.JSpinner opt_jpgQuality_nud;
    private javax.swing.JSpinner opt_maxPageHeight_nud;
    private javax.swing.JSpinner opt_maxPageWidth_nud;
    private javax.swing.JSpinner opt_minPageHeight_nud;
    private javax.swing.JSpinner opt_minPageWidth_nud;
    private javax.swing.JCheckBox opt_outputPoT_chk;
    private javax.swing.JSpinner opt_padding_nud;
    private javax.swing.JCheckBox opt_stripWhitespace_chk;
    private javax.swing.JTextField outputField;
    private javax.swing.JTextField packFileNameField;
    private javax.swing.JPanel projectPanel;
    private javax.swing.JPanel renderPanel;
    private javax.swing.JButton saveProjectBtn;
    private javax.swing.JPanel settingsPanel;
    private javax.swing.JPanel titlePanel;
    // End of variables declaration//GEN-END:variables
}
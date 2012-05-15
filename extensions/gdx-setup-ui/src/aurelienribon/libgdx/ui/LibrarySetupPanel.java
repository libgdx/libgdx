package aurelienribon.libgdx.ui;

import aurelienribon.libgdx.LibraryDef;
import aurelienribon.libgdx.ProjectConfiguration;
import aurelienribon.libgdx.ui.dialogs.DownloadDialog;
import aurelienribon.libgdx.ui.dialogs.LibraryInfoDialog;
import aurelienribon.ui.css.Style;
import aurelienribon.utils.HttpUtils;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import res.Res;

/**
 * @author Aurelien Ribon | http://www.aurelienribon.com/
 */
public class LibrarySetupPanel extends javax.swing.JPanel {
	private final ProjectConfiguration cfg = AppContext.inst().getConfig();
	private final Map<String, File> selectedFiles = new HashMap<String, File>();

    public LibrarySetupPanel() {
        initComponents();

		libgdxInfoBtn.addActionListener(new ActionListener() {@Override public void actionPerformed(ActionEvent e) {showInfo("libgdx");}});
		libgdxBrowseBtn.addActionListener(new ActionListener() {@Override public void actionPerformed(ActionEvent e) {browse("libgdx");}});
		libgdxGetStableBtn.addActionListener(new ActionListener() {@Override public void actionPerformed(ActionEvent e) {getStable("libgdx");}});
		libgdxGetNightliesBtn.addActionListener(new ActionListener() {@Override public void actionPerformed(ActionEvent e) {getLatest("libgdx");}});

		Style.registerCssClasses(headerPanel, ".header");
		Style.registerCssClasses(numberLabel, ".headerNumber");
		Style.registerCssClasses(sectionLabel1, ".sectionLabel");
		Style.registerCssClasses(sectionLabel2, ".sectionLabel");
		Style.registerCssClasses(legendPanel, ".legendPanel");
    }

	public void init() {
		libgdxLabel.setToolTipText("Archive not found, please specify or download one.");

		Style.registerCssClasses(libgdxLabel, ".libraryNotFoundLabel");
		Style.registerCssClasses(legendLibFoundLabel, ".libraryFoundLabel");
		Style.registerCssClasses(legendLibNotFoundLabel, ".libraryNotFoundLabel");
		Style.apply(libgdxLabel, new Style(Res.class.getResource("style-dynamic.css")));
		Style.apply(legendLibFoundLabel, new Style(Res.class.getResource("style-dynamic.css")));
		Style.apply(legendLibNotFoundLabel, new Style(Res.class.getResource("style-dynamic.css")));

		try {
			String rawDef = IOUtils.toString(Res.getStream("libgdx.txt"));
			LibraryDef def = new LibraryDef(rawDef);
			def.isUsed = true;
			cfg.registerLibrary("libgdx", def);
		} catch (IOException ex) {
			assert false;
		}

		initLibraries();
		downloadLatestDefinition();

		libgdxLabel.setText(cfg.getLibraryDef("libgdx").name);
	}

	private void initLibraries() {
		for (File file : new File(".").listFiles()) {
			if (!file.isFile()) continue;
			for (String libraryName : cfg.getLibraryNames()) {
				LibraryDef def = cfg.getLibraryDef(libraryName);
				String stableName = FilenameUtils.getName(def.stableUrl);
				String latestName = FilenameUtils.getName(def.latestUrl);
				if (file.getName().equals(latestName)) select(libraryName, file);
				else if (file.getName().equals(stableName)) select(libraryName, file);
			}
		}
	}

	private void downloadLatestDefinition() {
		final ByteArrayOutputStream output = new ByteArrayOutputStream();

		HttpUtils.Callback callback = new HttpUtils.Callback() {
			@Override public void canceled() {}
			@Override public void updated(int length, int totalLength) {}
			@Override public void completed() {
				LibraryDef def = new LibraryDef(output.toString());
				cfg.registerLibrary("libgdx", def);
				libgdxLoadingLabel.setIcon(null);
			}
			@Override public void error(IOException ex) {
				libgdxLoadingLabel.setIcon(Res.getImage("ic_error.png"));
				libgdxLoadingLabel.setToolTipText("Error occured while downloading the latest definition.");
			}
		};

		try {
			URL input = new URL("http://libgdx.badlogicgames.com/nightlies/libgdx.txt");
			HttpUtils.downloadAsync(input, output, callback);

		} catch (MalformedURLException ex) {
			assert false;
		}
	}

	private void showInfo(String libraryName) {
		JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
		LibraryInfoDialog dialog = new LibraryInfoDialog(frame, libraryName);
		dialog.setLocationRelativeTo(frame);
		dialog.setVisible(true);
	}

	private void browse(String libraryName) {
		File file = selectedFiles.get(libraryName);
		String path = file != null ? file.getPath() : ".";
		JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);

		JFileChooser chooser = new JFileChooser(new File(path));
		chooser.setFileFilter(new FileNameExtensionFilter("Zip files (*.zip)", "zip"));
		chooser.setDialogTitle("Please select the zip archive for \"" + libraryName + "\"");

		if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
			select(libraryName, chooser.getSelectedFile());
		}
	}

	private void getStable(String libraryName) {
		String input = cfg.getLibraryDef(libraryName).stableUrl;
		String output = FilenameUtils.getName(input);
		getFile(libraryName, input, output);
	}

	private void getLatest(String libraryName) {
		String input = cfg.getLibraryDef(libraryName).latestUrl;
		String output = FilenameUtils.getName(input);
		getFile(libraryName, input, output);
	}

	private void getFile(final String libraryName, String input, String output) {
		final File zipFile = new File(output);

		DownloadDialog.Callback callback = new DownloadDialog.Callback() {
			@Override public void completed() {select(libraryName, zipFile);}
		};

		JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
		DownloadDialog dialog = new DownloadDialog(frame, callback, input, output);
		dialog.setLocationRelativeTo(frame);
		dialog.setVisible(true);
	}

	private void select(String libraryName, File zipFile) {
		selectedFiles.put(libraryName, zipFile);
		cfg.setLibraryPath(libraryName, zipFile.getPath());

		if (libraryName.equals("libgdx")) {
			libgdxLabel.setToolTipText("Archive successfully found under \"" + zipFile.getPath() + "\"");
			Style.unregister(libgdxLabel);
			Style.registerCssClasses(libgdxLabel, ".libraryFoundLabel");
			Style.apply(libgdxLabel, new Style(Res.class.getResource("style-dynamic.css")));
		}

		AppContext.inst().fireConfigChangedEvent();
	}

	// -------------------------------------------------------------------------
	// Generated stuff
	// -------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        thirdPartyLibPanel = new javax.swing.JPanel();
        libLabelChk = new javax.swing.JCheckBox();
        browseBtn = new javax.swing.JButton();
        getBtn = new javax.swing.JButton();
        headerPanel = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        numberLabel = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        sectionLabel1 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        libgdxLabel = new javax.swing.JLabel();
        libgdxLoadingLabel = new javax.swing.JLabel();
        libgdxInfoBtn = new javax.swing.JButton();
        libgdxBrowseBtn = new javax.swing.JButton();
        libgdxGetStableBtn = new javax.swing.JButton();
        libgdxGetNightliesBtn = new javax.swing.JButton();
        sectionLabel2 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        legendPanel = new aurelienribon.ui.components.PaintedPanel();
        jLabel5 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        legendLibNotFoundLabel = new javax.swing.JLabel();
        legendLibFoundLabel = new javax.swing.JLabel();

        thirdPartyLibPanel.setOpaque(false);

        libLabelChk.setText("Tween Engine");

        browseBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/gfx/ic_browse.png"))); // NOI18N

        getBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/gfx/ic_download.png"))); // NOI18N

        javax.swing.GroupLayout thirdPartyLibPanelLayout = new javax.swing.GroupLayout(thirdPartyLibPanel);
        thirdPartyLibPanel.setLayout(thirdPartyLibPanelLayout);
        thirdPartyLibPanelLayout.setHorizontalGroup(
            thirdPartyLibPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, thirdPartyLibPanelLayout.createSequentialGroup()
                .addComponent(libLabelChk, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(browseBtn)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(getBtn))
        );
        thirdPartyLibPanelLayout.setVerticalGroup(
            thirdPartyLibPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(browseBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(getBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(libLabelChk, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        setLayout(new java.awt.BorderLayout());

        jLabel4.setText("<html> Select the libraries you want to include. Direct downloads are available to stable and nightly releases.");
        jLabel4.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        numberLabel.setText("2");

        javax.swing.GroupLayout headerPanelLayout = new javax.swing.GroupLayout(headerPanel);
        headerPanel.setLayout(headerPanelLayout);
        headerPanelLayout.setHorizontalGroup(
            headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, headerPanelLayout.createSequentialGroup()
                .addComponent(numberLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, 272, Short.MAX_VALUE))
        );
        headerPanelLayout.setVerticalGroup(
            headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .addComponent(numberLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        add(headerPanel, java.awt.BorderLayout.NORTH);

        jPanel3.setOpaque(false);

        sectionLabel1.setText("Required");

        jPanel1.setOpaque(false);

        libgdxLabel.setText("xxx");

        libgdxLoadingLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/gfx/ic_loading.gif"))); // NOI18N
        libgdxLoadingLabel.setToolTipText("Downloading latest definition");

        libgdxInfoBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/gfx/ic_info.png"))); // NOI18N
        libgdxInfoBtn.setToolTipText("Information");

        libgdxBrowseBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/gfx/ic_browse.png"))); // NOI18N
        libgdxBrowseBtn.setToolTipText("Browse to select the archive");

        libgdxGetStableBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/gfx/ic_download_stable.png"))); // NOI18N
        libgdxGetStableBtn.setToolTipText("Download latest stable version");

        libgdxGetNightliesBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/gfx/ic_download_nightlies.png"))); // NOI18N
        libgdxGetNightliesBtn.setToolTipText("Download latest nightlies version");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addComponent(libgdxLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 24, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(libgdxLoadingLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(libgdxInfoBtn)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(libgdxBrowseBtn)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(libgdxGetStableBtn)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(libgdxGetNightliesBtn))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(libgdxBrowseBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(libgdxInfoBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(libgdxGetStableBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(libgdxGetNightliesBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(libgdxLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(libgdxLoadingLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        sectionLabel2.setText("Third-party");

        jLabel1.setText("Coming soon...");

        jLabel5.setText(":  zip archive not found");

        jLabel3.setText(":  zip archive found (see tooltip)");

        legendLibNotFoundLabel.setText("library");

        legendLibFoundLabel.setText("library");

        javax.swing.GroupLayout legendPanelLayout = new javax.swing.GroupLayout(legendPanel);
        legendPanel.setLayout(legendPanelLayout);
        legendPanelLayout.setHorizontalGroup(
            legendPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(legendPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(legendPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(legendPanelLayout.createSequentialGroup()
                        .addComponent(legendLibFoundLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(legendPanelLayout.createSequentialGroup()
                        .addComponent(legendLibNotFoundLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        legendPanelLayout.setVerticalGroup(
            legendPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(legendPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(legendPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(legendLibFoundLabel)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(legendPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(legendLibNotFoundLabel)
                    .addComponent(jLabel5))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(sectionLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(sectionLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
            .addComponent(legendPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(sectionLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(sectionLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 40, Short.MAX_VALUE)
                .addComponent(legendPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        add(jPanel3, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseBtn;
    private javax.swing.JButton getBtn;
    private javax.swing.JPanel headerPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JLabel legendLibFoundLabel;
    private javax.swing.JLabel legendLibNotFoundLabel;
    private aurelienribon.ui.components.PaintedPanel legendPanel;
    private javax.swing.JCheckBox libLabelChk;
    private javax.swing.JButton libgdxBrowseBtn;
    private javax.swing.JButton libgdxGetNightliesBtn;
    private javax.swing.JButton libgdxGetStableBtn;
    private javax.swing.JButton libgdxInfoBtn;
    private javax.swing.JLabel libgdxLabel;
    private javax.swing.JLabel libgdxLoadingLabel;
    private javax.swing.JLabel numberLabel;
    private javax.swing.JLabel sectionLabel1;
    private javax.swing.JLabel sectionLabel2;
    private javax.swing.JPanel thirdPartyLibPanel;
    // End of variables declaration//GEN-END:variables

}

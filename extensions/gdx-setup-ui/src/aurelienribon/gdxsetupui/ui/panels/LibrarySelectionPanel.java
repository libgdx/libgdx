package aurelienribon.gdxsetupui.ui.panels;

import aurelienribon.gdxsetupui.LibraryDef;
import aurelienribon.gdxsetupui.ui.Ctx;
import aurelienribon.gdxsetupui.ui.MainPanel;
import aurelienribon.ui.CompactCheckBox;
import aurelienribon.ui.css.Style;
import aurelienribon.utils.HttpUtils;
import aurelienribon.utils.HttpUtils.DownloadListener;
import aurelienribon.utils.HttpUtils.DownloadTask;
import aurelienribon.utils.Res;
import aurelienribon.utils.SwingUtils;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

/**
 * @author Aurelien Ribon | http://www.aurelienribon.com/
 */
public class LibrarySelectionPanel extends javax.swing.JPanel {
	private static final Style style = new Style(Res.getUrl("css/style.css"));
	private static final Color LIB_FOUND_COLOR = new Color(0x008800);
	private static final Color LIB_NOTFOUND_COLOR = new Color(0x880000);

	private final MainPanel mainPanel;
	private final Map<String, File> libsSelectedFiles = new HashMap<String, File>();
	private final Map<String, JComponent> libsNamesCmps = new HashMap<String, JComponent>();

    public LibrarySelectionPanel(MainPanel mainPanel) {
		this.mainPanel = mainPanel;
        initComponents();

		librariesScrollPane.getViewport().setOpaque(false);

		libgdxInfoBtn.addActionListener(new ActionListener() {@Override public void actionPerformed(ActionEvent e) {showInfo("libgdx");}});
		libgdxBrowseBtn.addActionListener(new ActionListener() {@Override public void actionPerformed(ActionEvent e) {browse("libgdx");}});
		libgdxGetStableBtn.addActionListener(new ActionListener() {@Override public void actionPerformed(ActionEvent e) {getStable("libgdx");}});
		libgdxGetNightliesBtn.addActionListener(new ActionListener() {@Override public void actionPerformed(ActionEvent e) {getLatest("libgdx");}});

		Style.registerCssClasses(headerPanel, ".header");
		Style.registerCssClasses(numberLabel, ".headerNumber");
		Style.registerCssClasses(sectionLabel1, ".sectionLabel");
		Style.registerCssClasses(sectionLabel2, ".sectionLabel");
		Style.registerCssClasses(legendPanel, ".optionGroupPanel");
    }

	public void initializeLibgdx() {
		libsNamesCmps.put("libgdx", libgdxLabel);
		libgdxLabel.setForeground(LIB_NOTFOUND_COLOR);
		preselectLibraryArchive("libgdx");
	}

	public synchronized void rebuildLibraries() {
		List<String> names = new ArrayList<String>(Ctx.libs.getNames());

		for (int i=names.size()-1; i>=0; i--) {
			if (Ctx.libs.getDef(names.get(i)) == null)
				names.remove(i);
		}

		Collections.sort(names, new Comparator<String>() {
			@Override public int compare(String o1, String o2) {
				String name1 = Ctx.libs.getDef(o1).name;
				String name2 = Ctx.libs.getDef(o2).name;
				return name1.compareToIgnoreCase(name2);
			}
		});

		librariesPanel.removeAll();
		librariesScrollPane.revalidate();

		for (String name : names) {
			if (!name.equals("libgdx")) buildLibraryPanel(name);
			preselectLibraryArchive(name);
		}
	}

	// -------------------------------------------------------------------------
	// Initialization of libraries
	// -------------------------------------------------------------------------

	private void buildLibraryPanel(final String libraryName) {
		ActionListener nameChkAL = new ActionListener() {@Override public void actionPerformed(ActionEvent e) {
			if (((CompactCheckBox) e.getSource()).isSelected()) {
				if (!Ctx.cfgSetup.libraries.contains(libraryName)) Ctx.cfgSetup.libraries.add(libraryName);
				if (!Ctx.cfgUpdate.libraries.contains(libraryName)) Ctx.cfgUpdate.libraries.add(libraryName);
			} else {
				Ctx.cfgSetup.libraries.remove(libraryName);
				Ctx.cfgUpdate.libraries.remove(libraryName);
			}

			Ctx.fireCfgSetupChanged();
			Ctx.fireCfgUpdateChanged();
		}};

		Action infoAction = new AbstractAction() {@Override public void actionPerformed(ActionEvent e) {showInfo(libraryName);}};
		Action browseAction = new AbstractAction() {@Override public void actionPerformed(ActionEvent e) {browse(libraryName);}};
		Action getStableAction = new AbstractAction() {@Override public void actionPerformed(ActionEvent e) {getStable(libraryName);}};
		Action getLatestAction = new AbstractAction() {@Override public void actionPerformed(ActionEvent e) {getLatest(libraryName);}};

		LibraryDef def = Ctx.libs.getDef(libraryName);

		CompactCheckBox nameChk = new CompactCheckBox(def.name + " ");
		JLabel html5Label = new JLabel(Res.getImage("gfx/ic_html5.png"));
		JButton infoBtn = new JButton(infoAction);
		JButton browseBtn = new JButton(browseAction);
		JButton getStableBtn = new JButton(getStableAction);
		JButton getLatestBtn = new JButton(getLatestAction);

		nameChk.addActionListener(nameChkAL);
		nameChk.setForeground(LIB_NOTFOUND_COLOR);
		html5Label.setToolTipText("Compatible with HTML backend");
		infoBtn.setIcon(Res.getImage("gfx/ic_info.png"));
		browseBtn.setIcon(Res.getImage("gfx/ic_browse.png"));
		getStableBtn.setIcon(Res.getImage("gfx/ic_download_stable.png"));
		getLatestBtn.setIcon(Res.getImage("gfx/ic_download_nightlies.png"));
		infoBtn.setFocusable(false);
		browseBtn.setFocusable(false);
		getStableBtn.setFocusable(false);
		getLatestBtn.setFocusable(false);

		JToolBar toolBar = new JToolBar();
		toolBar.setOpaque(false);
		toolBar.setFloatable(false);
		toolBar.add(Box.createHorizontalGlue());
		toolBar.add(infoBtn);
		toolBar.add(browseBtn);
		if (def.stableUrl != null) toolBar.add(getStableBtn); else toolBar.add(Box.createHorizontalStrut(libgdxGetStableBtn.getPreferredSize().width));
		if (def.latestUrl != null) toolBar.add(getLatestBtn); else toolBar.add(Box.createHorizontalStrut(libgdxGetNightliesBtn.getPreferredSize().width));

		JPanel leftPanel = new JPanel(new BorderLayout());
		leftPanel.setOpaque(false);
		leftPanel.add(nameChk, BorderLayout.CENTER);
		if (def.gwtModuleName != null) leftPanel.add(html5Label, BorderLayout.EAST);

		JPanel panel = new JPanel(new BorderLayout());
		panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
		panel.setOpaque(false);
		panel.add(leftPanel, BorderLayout.WEST);
		panel.add(toolBar, BorderLayout.CENTER);

		librariesPanel.add(panel);

		Style.apply(librariesPanel, style);
		libsNamesCmps.put(libraryName, nameChk);
	}

	private void preselectLibraryArchive(String libraryName) {
		LibraryDef def = Ctx.libs.getDef(libraryName);
		String stableName = FilenameUtils.getName(def.stableUrl);
		String latestName = FilenameUtils.getName(def.latestUrl);

		for (File file : new File(".").listFiles()) {
			if (file.isFile()) {
				if (file.getName().equals(latestName)) select(libraryName, file);
				else if (file.getName().equals(stableName)) select(libraryName, file);
			}
		}
	}

	// -------------------------------------------------------------------------
	// Actions
	// -------------------------------------------------------------------------

	private void showInfo(String libraryName) {
		mainPanel.showLibraryInfo(libraryName);
	}

	private void browse(String libraryName) {
		File file = libsSelectedFiles.get(libraryName);
		String path = file != null ? file.getPath() : ".";
		JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);

		JFileChooser chooser = new JFileChooser(new File(path));
		chooser.setFileFilter(new FileNameExtensionFilter("Zip files (*.zip)", "zip"));
		chooser.setDialogTitle("Please select the zip archive for \"" + libraryName + "\"");

		if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
			select(libraryName, chooser.getSelectedFile());
		}
	}

	private void getStable(final String libraryName) {
		final String input = Ctx.libs.getDef(libraryName).stableUrl;
		final String output = FilenameUtils.getName(input);
		getFile(input, output, libraryName, "Stable '" + libraryName + "'");
	}

	private void getLatest(String libraryName) {
		final String input = Ctx.libs.getDef(libraryName).latestUrl;
		final String output = FilenameUtils.getName(input);
		getFile(input, output, libraryName, "Latest '" + libraryName + "'");
	}

	private void getFile(final String input, final String output, final String libraryName, String tag) {
		OutputStream tempOutput;
		try {
			tempOutput = new BufferedOutputStream(new FileOutputStream(output + ".tmp"));
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}

		DownloadTask task = HttpUtils.downloadAsync(input, tempOutput, tag);

		task.addListener(new DownloadListener() {
			@Override public void onComplete() {
				try {
					FileUtils.deleteQuietly(new File(output));
					FileUtils.moveFile(new File(output + ".tmp"), new File(output));
				} catch (IOException ex) {
					String msg = "Could not rename \"" + output + ".tmp" + "\" into \"" + output + "\"";
					JOptionPane.showMessageDialog(SwingUtils.getJFrame(LibrarySelectionPanel.this), msg);
				}
				select(libraryName, new File(output));
			}
		});
	}

	private void select(String libraryName, File zipFile) {
		libsSelectedFiles.put(libraryName, zipFile);
		Ctx.cfgSetup.librariesZipPaths.put(libraryName, zipFile.getPath());
		Ctx.cfgUpdate.librariesZipPaths.put(libraryName, zipFile.getPath());

		libsNamesCmps.get(libraryName).setToolTipText("Using archive: \"" + zipFile.getPath() + "\"");
		libsNamesCmps.get(libraryName).setForeground(LIB_FOUND_COLOR);

		Ctx.fireCfgSetupChanged();
	}

	// -------------------------------------------------------------------------
	// Generated stuff
	// -------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        headerPanel = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        numberLabel = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        sectionLabel1 = new javax.swing.JLabel();
        libgdxPanel = new javax.swing.JPanel();
        libgdxLabel = new javax.swing.JLabel();
        libgdxToolBar = new javax.swing.JToolBar();
        libgdxInfoBtn = new javax.swing.JButton();
        libgdxBrowseBtn = new javax.swing.JButton();
        libgdxGetStableBtn = new javax.swing.JButton();
        libgdxGetNightliesBtn = new javax.swing.JButton();
        sectionLabel2 = new javax.swing.JLabel();
        legendPanel = new aurelienribon.ui.components.PaintedPanel();
        jLabel1 = new javax.swing.JLabel();
        librariesScrollPane = new javax.swing.JScrollPane();
        librariesPanel = new javax.swing.JPanel();

        setLayout(new java.awt.BorderLayout());

        jLabel4.setText("<html> Select the libraries you want to include or update. Direct downloads are available to stable and nightly releases.");
        jLabel4.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        numberLabel.setText("2");

        javax.swing.GroupLayout headerPanelLayout = new javax.swing.GroupLayout(headerPanel);
        headerPanel.setLayout(headerPanelLayout);
        headerPanelLayout.setHorizontalGroup(
            headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, headerPanelLayout.createSequentialGroup()
                .addComponent(numberLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, 219, Short.MAX_VALUE))
        );
        headerPanelLayout.setVerticalGroup(
            headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(numberLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(headerPanelLayout.createSequentialGroup()
                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        add(headerPanel, java.awt.BorderLayout.NORTH);

        jPanel3.setOpaque(false);

        sectionLabel1.setText("Required");

        libgdxPanel.setOpaque(false);

        libgdxLabel.setText("LibGDX");

        libgdxToolBar.setFloatable(false);
        libgdxToolBar.setRollover(true);

        libgdxInfoBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/gfx/ic_info.png"))); // NOI18N
        libgdxInfoBtn.setToolTipText("Information");
        libgdxInfoBtn.setFocusable(false);
        libgdxToolBar.add(libgdxInfoBtn);

        libgdxBrowseBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/gfx/ic_browse.png"))); // NOI18N
        libgdxBrowseBtn.setToolTipText("Browse to select the archive");
        libgdxBrowseBtn.setFocusable(false);
        libgdxToolBar.add(libgdxBrowseBtn);

        libgdxGetStableBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/gfx/ic_download_stable.png"))); // NOI18N
        libgdxGetStableBtn.setToolTipText("Download latest stable version");
        libgdxGetStableBtn.setFocusable(false);
        libgdxToolBar.add(libgdxGetStableBtn);

        libgdxGetNightliesBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/gfx/ic_download_nightlies.png"))); // NOI18N
        libgdxGetNightliesBtn.setToolTipText("Download latest nightlies version");
        libgdxGetNightliesBtn.setFocusable(false);
        libgdxToolBar.add(libgdxGetNightliesBtn);

        javax.swing.GroupLayout libgdxPanelLayout = new javax.swing.GroupLayout(libgdxPanel);
        libgdxPanel.setLayout(libgdxPanelLayout);
        libgdxPanelLayout.setHorizontalGroup(
            libgdxPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, libgdxPanelLayout.createSequentialGroup()
                .addComponent(libgdxLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(libgdxToolBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        libgdxPanelLayout.setVerticalGroup(
            libgdxPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(libgdxLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, libgdxPanelLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(libgdxToolBar, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        sectionLabel2.setText("Third-party");

        jLabel1.setText("<html>\n<b>Legend</b><br/>\n<font color=\"#008800\">library name:</font> zip archive found (see tooltip)<br/>\n<font color=\"#880000\">library name:</font> zip archive not found<br/>");

        javax.swing.GroupLayout legendPanelLayout = new javax.swing.GroupLayout(legendPanel);
        legendPanel.setLayout(legendPanelLayout);
        legendPanelLayout.setHorizontalGroup(
            legendPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(legendPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(12, Short.MAX_VALUE))
        );
        legendPanelLayout.setVerticalGroup(
            legendPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(legendPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        librariesScrollPane.setOpaque(false);

        librariesPanel.setOpaque(false);
        librariesPanel.setLayout(new javax.swing.BoxLayout(librariesPanel, javax.swing.BoxLayout.Y_AXIS));
        librariesScrollPane.setViewportView(librariesPanel);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(legendPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(libgdxPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(sectionLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(sectionLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(librariesScrollPane))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(sectionLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(libgdxPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20)
                .addComponent(sectionLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(librariesScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 159, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(legendPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        add(jPanel3, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel headerPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel3;
    private aurelienribon.ui.components.PaintedPanel legendPanel;
    private javax.swing.JButton libgdxBrowseBtn;
    private javax.swing.JButton libgdxGetNightliesBtn;
    private javax.swing.JButton libgdxGetStableBtn;
    private javax.swing.JButton libgdxInfoBtn;
    private javax.swing.JLabel libgdxLabel;
    private javax.swing.JPanel libgdxPanel;
    private javax.swing.JToolBar libgdxToolBar;
    private javax.swing.JPanel librariesPanel;
    private javax.swing.JScrollPane librariesScrollPane;
    private javax.swing.JLabel numberLabel;
    private javax.swing.JLabel sectionLabel1;
    private javax.swing.JLabel sectionLabel2;
    // End of variables declaration//GEN-END:variables

}

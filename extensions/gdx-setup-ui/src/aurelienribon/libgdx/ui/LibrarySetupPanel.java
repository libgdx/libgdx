package aurelienribon.libgdx.ui;

import aurelienribon.libgdx.DownloadManager;
import aurelienribon.libgdx.LibraryDef;
import aurelienribon.libgdx.ui.dialogs.DownloadDialog;
import aurelienribon.libgdx.ui.dialogs.LibraryInfoDialog;
import aurelienribon.ui.css.Style;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
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
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import res.Res;

/**
 * @author Aurelien Ribon | http://www.aurelienribon.com/
 */
public class LibrarySetupPanel extends javax.swing.JPanel {
	private static final Color LIB_FOUND_COLOR = new Color(0x008800);
	private static final Color LIB_NOTFOUND_COLOR = new Color(0x880000);

	private DownloadManager dlManager;
	private final Map<String, File> libsSelectedFiles = new HashMap<String, File>();
	private final Map<String, JComponent> libsNamesCmps = new HashMap<String, JComponent>();
	private int count = 0;

    public LibrarySetupPanel() {
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
		Style.registerCssClasses(legendPanel, ".legendPanel");
		Style.registerCssClasses(legendLabel, ".legendLabel");

		try {
			//dlManager = new DownloadManager("http://www.aurelienribon.com/libgdx-setup/config.txt");
			dlManager = new DownloadManager("http://libgdx.googlecode.com/svn/trunk/extensions/gdx-setup-ui/config/config.txt");
		} catch (MalformedURLException ex) {
			System.err.println("[warning] Malformed url for the configuration file");
		}
    }

	public void init() {
		libsNamesCmps.put("libgdx", libgdxLabel);
		libgdxLabel.setForeground(LIB_NOTFOUND_COLOR);

		try {
			String rawDef = IOUtils.toString(Res.getStream("libgdx.txt"));
			LibraryDef def = new LibraryDef(rawDef);
			Ctx.cfg.libs.add("libgdx", def);
			Ctx.cfg.libs.setUsage("libgdx", true);
			preselectLibraryArchive("libgdx");
		} catch (IOException ex) {
			assert false;
		}

		dlManager.downloadConfigFile(new DownloadManager.Callback() {
			@Override public void completed() {
				System.out.println("Successfully retrieved the configuration file.");

				if (Ctx.testLibUrl != null) {
					dlManager.addTestLibraryUrl("__test_url__", Ctx.testLibUrl);
				}

				if (Ctx.testLibDef != null) {
					dlManager.addTestLibraryDef("__test_def__", Ctx.testLibDef);
					registerLibrary("__test_def__");
				}

				for (String name : dlManager.getLibrariesNames()) downloadLibraryDef(name);
			}
			@Override public void error() {
				System.err.println("[warning] Cannot download the configuration file.");
			}
		});
	}

	// -------------------------------------------------------------------------
	// Initialization of libraries
	// -------------------------------------------------------------------------

	private void downloadLibraryDef(final String libraryName) {
		dlManager.downloadLibraryDef(libraryName, new DownloadManager.Callback() {
			@Override public void completed() {
				System.out.println("Successfully retrieved definition for library '" + libraryName + "'");
				SwingUtilities.invokeLater(new Runnable() {@Override public void run() {
					registerLibrary(libraryName);
				}});
			}
			@Override public void error() {
				System.err.println("[warning] Cannot download definition for library '" + libraryName + "'");
				SwingUtilities.invokeLater(new Runnable() {@Override public void run() {
					registerLibrary(null);
				}});
			}
		});
	}

	private void registerLibrary(String libraryName) {
		if (libraryName != null) {
			Ctx.cfg.libs.add(libraryName, dlManager.getLibraryDef(libraryName));
			Ctx.cfg.libs.setUsage(libraryName, libraryName.equals("libgdx"));
		}

		count += 1;
		int total = dlManager.getLibrariesNames().size();

		if (count < total) {
			librariesUpdateLabel.setText("Retrieving libraries: " + count + " / " + total);
		} else {
			List<String> names = new ArrayList<String>(Ctx.cfg.libs.getNames());

			Collections.sort(names, new Comparator<String>() {
				@Override public int compare(String o1, String o2) {
					String name1 = Ctx.cfg.libs.getDef(o1).name;
					String name2 = Ctx.cfg.libs.getDef(o2).name;
					return name1.compareToIgnoreCase(name2);
				}
			});

			librariesPanel.removeAll();
			librariesScrollPane.revalidate();

			for (String name : names) {
				if (!name.equals("libgdx")) buildLibraryPanel(name);
				preselectLibraryArchive(name);
			}

			Ctx.fireConfigChanged();

			if (Ctx.cfg.libs.getNames().size() < total) {
				String msg = "<html>Could not retrieve the definitions for:<br/>";
				for (String name : dlManager.getLibrariesNames()) {
					if (!Ctx.cfg.libs.getNames().contains(name)) msg += "'" + name + "', ";
				}
				sectionLabel2.setHorizontalTextPosition(SwingConstants.LEFT);
				sectionLabel2.setToolTipText(msg.substring(0, msg.length()-2));
				sectionLabel2.setIcon(Res.getImage("gfx/ic_error.png"));
			}
		}
	}

	private void buildLibraryPanel(final String libraryName) {
		ActionListener nameChkAL = new ActionListener() {@Override public void actionPerformed(ActionEvent e) {
			Ctx.cfg.libs.setUsage(libraryName, ((JCheckBox) e.getSource()).isSelected());
			Ctx.fireConfigChanged();
		}};

		Action infoAction = new AbstractAction() {@Override public void actionPerformed(ActionEvent e) {showInfo(libraryName);}};
		Action browseAction = new AbstractAction() {@Override public void actionPerformed(ActionEvent e) {browse(libraryName);}};
		Action getStableAction = new AbstractAction() {@Override public void actionPerformed(ActionEvent e) {getStable(libraryName);}};
		Action getLatestAction = new AbstractAction() {@Override public void actionPerformed(ActionEvent e) {getLatest(libraryName);}};

		LibraryDef def = Ctx.cfg.libs.getDef(libraryName);

		JCheckBox nameChk = new JCheckBox(def.name);
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

		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		toolBar.add(Box.createHorizontalGlue());
		toolBar.add(infoBtn);
		toolBar.add(browseBtn);
		if (def.stableUrl != null) toolBar.add(getStableBtn); else toolBar.add(Box.createHorizontalStrut(libgdxGetStableBtn.getWidth()));
		if (def.latestUrl != null) toolBar.add(getLatestBtn); else toolBar.add(Box.createHorizontalStrut(libgdxGetNightliesBtn.getWidth()));

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

		Style.apply(librariesPanel, new Style(Res.getUrl("css/style.css")));
		libsNamesCmps.put(libraryName, nameChk);
	}

	private void preselectLibraryArchive(String libraryName) {
		LibraryDef def = Ctx.cfg.libs.getDef(libraryName);
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
		JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
		LibraryInfoDialog dialog = new LibraryInfoDialog(frame, libraryName);
		dialog.setLocationRelativeTo(frame);
		dialog.setVisible(true);
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

	private void getStable(String libraryName) {
		String input = Ctx.cfg.libs.getDef(libraryName).stableUrl;
		String output = FilenameUtils.getName(input);
		getFile(libraryName, input, output);
	}

	private void getLatest(String libraryName) {
		String input = Ctx.cfg.libs.getDef(libraryName).latestUrl;
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
		libsSelectedFiles.put(libraryName, zipFile);
		Ctx.cfg.libs.setPath(libraryName, zipFile.getPath());

		libsNamesCmps.get(libraryName).setToolTipText("Using archive: \"" + zipFile.getPath() + "\"");
		libsNamesCmps.get(libraryName).setForeground(LIB_FOUND_COLOR);

		Ctx.fireConfigChanged();
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
        legendLabel = new javax.swing.JLabel();
        librariesScrollPane = new javax.swing.JScrollPane();
        librariesPanel = new javax.swing.JPanel();
        librariesUpdateLabel = new javax.swing.JLabel();

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
                .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, 219, Short.MAX_VALUE))
        );
        headerPanelLayout.setVerticalGroup(
            headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .addComponent(numberLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
        libgdxToolBar.add(libgdxInfoBtn);

        libgdxBrowseBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/gfx/ic_browse.png"))); // NOI18N
        libgdxBrowseBtn.setToolTipText("Browse to select the archive");
        libgdxToolBar.add(libgdxBrowseBtn);

        libgdxGetStableBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/gfx/ic_download_stable.png"))); // NOI18N
        libgdxGetStableBtn.setToolTipText("Download latest stable version");
        libgdxToolBar.add(libgdxGetStableBtn);

        libgdxGetNightliesBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/gfx/ic_download_nightlies.png"))); // NOI18N
        libgdxGetNightliesBtn.setToolTipText("Download latest nightlies version");
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

        legendLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/gfx/legend.png"))); // NOI18N

        javax.swing.GroupLayout legendPanelLayout = new javax.swing.GroupLayout(legendPanel);
        legendPanel.setLayout(legendPanelLayout);
        legendPanelLayout.setHorizontalGroup(
            legendPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(legendPanelLayout.createSequentialGroup()
                .addComponent(legendLabel)
                .addGap(0, 47, Short.MAX_VALUE))
        );
        legendPanelLayout.setVerticalGroup(
            legendPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(legendLabel)
        );

        librariesScrollPane.setOpaque(false);

        librariesPanel.setOpaque(false);
        librariesPanel.setLayout(new javax.swing.BoxLayout(librariesPanel, javax.swing.BoxLayout.Y_AXIS));

        librariesUpdateLabel.setText("Retrieving libraries: ...");
        librariesPanel.add(librariesUpdateLabel);

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
                .addComponent(librariesScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 228, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(legendPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        add(jPanel3, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel headerPanel;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JLabel legendLabel;
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
    private javax.swing.JLabel librariesUpdateLabel;
    private javax.swing.JLabel numberLabel;
    private javax.swing.JLabel sectionLabel1;
    private javax.swing.JLabel sectionLabel2;
    // End of variables declaration//GEN-END:variables

}

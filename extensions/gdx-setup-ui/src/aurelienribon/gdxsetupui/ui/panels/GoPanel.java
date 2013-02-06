package aurelienribon.gdxsetupui.ui.panels;

import aurelienribon.gdxsetupui.BaseProjectConfiguration;
import aurelienribon.gdxsetupui.Helper;
import aurelienribon.gdxsetupui.ProjectSetupConfiguration;
import aurelienribon.gdxsetupui.ProjectUpdateConfiguration;
import aurelienribon.gdxsetupui.ui.Ctx;
import aurelienribon.gdxsetupui.ui.MainPanel;
import aurelienribon.ui.css.Style;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * @author Aurelien Ribon | http://www.aurelienribon.com/
 */
public class GoPanel extends javax.swing.JPanel {
    public GoPanel(final MainPanel mainPanel) {
        initComponents();
		Style.registerCssClasses(headerPanel, ".header");
		Style.registerCssClasses(numberLabel, ".headerNumber");
		Style.registerCssClasses(errorLabel, ".statusLabel");
		Style.registerCssClasses(goBtn, ".bold");

		Ctx.listeners.add(new Ctx.Listener() {
			@Override public void modeChanged() {update();}
			@Override public void cfgSetupChanged() {update();}
			@Override public void cfgUpdateChanged() {update();}
		});

		goBtn.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				switch (Ctx.mode) {
					case SETUP: mainPanel.showProcessSetupPanel(); break;
					case UPDATE: mainPanel.showProcessUpdatePanel(); break;
				}
			}
		});
    }

	private void update() {
		errorLabel.firePropertyChange("valid", true, false);
		errorLabel.firePropertyChange("error", true, false);

		switch (Ctx.mode) {
			case SETUP:
				if (isProjectCreationValid(Ctx.cfgSetup)) {
					goBtn.setEnabled(true);
					errorLabel.setText("<html>Your configuration is valid.");
					errorLabel.firePropertyChange("valid", false, true);
				} else {
					goBtn.setEnabled(false);
					errorLabel.setText("<html>" + getCreationErrorMessage(Ctx.cfgSetup));
					errorLabel.firePropertyChange("error", false, true);
				}

				numberLabel.setText("4");
				goBtn.setText("Open the generation screen");
				break;

			case UPDATE:
				if (isProjectUpdateValid(Ctx.cfgUpdate)) {
					goBtn.setEnabled(true);
					errorLabel.setText("<html>Your configuration is valid.");
					errorLabel.firePropertyChange("valid", false, true);
				} else {
					goBtn.setEnabled(false);
					errorLabel.setText("<html>" + getUpdateErrorMessage(Ctx.cfgUpdate));
					errorLabel.firePropertyChange("error", false, true);
				}

				numberLabel.setText("3");
				goBtn.setText("Open the update screen");
				break;
		}
	}

	private boolean isProjectCreationValid(ProjectSetupConfiguration cfg) {
		if (cfg.projectName.trim().equals("")) return false;
		if (cfg.packageName.trim().equals("")) return false;
		if (cfg.packageName.endsWith(".")) return false;
		if (cfg.mainClassName.trim().equals("")) return false;

		for (String libraryName : cfg.libraries) {
			if (!isLibraryValid(cfg, libraryName)) return false;
		}

		return true;
	}

	private boolean isProjectUpdateValid(ProjectUpdateConfiguration cfg) {
		File coreDir = new File(Helper.getCorePrjPath(cfg));

		if (!coreDir.isDirectory()) return false;
		if (!new File(coreDir, ".classpath").isFile()) return false;

		for (String libraryName : cfg.libraries) {
			if (!isLibraryValid(cfg, libraryName)) return false;
		}

		return true;
	}

	private boolean isLibraryValid(BaseProjectConfiguration cfg, String libraryName) {
		String path = cfg.librariesZipPaths.get(libraryName);
		if (path == null) return false;
		if (!path.endsWith(".zip")) return false;
		if (!new File(path).isFile()) return false;
		return true;
	}

	private String getCreationErrorMessage(ProjectSetupConfiguration cfg) {
		if (cfg.projectName.trim().equals("")) return "Project name is not set.";
		if (cfg.packageName.trim().equals("")) return "Package name is not set.";
		if (cfg.packageName.endsWith(".")) return "Package name ends with a dot.";
		if (cfg.mainClassName.trim().equals("")) return "Main class name is not set.";

		for (String libraryName : cfg.libraries) {
			if (!isLibraryValid(cfg, libraryName))
				return "At least one selected library has a missing or invalid archive.";
		}

		return "No error found";
	}

	private String getUpdateErrorMessage(ProjectUpdateConfiguration cfg) {
		File coreDir = new File(Helper.getCorePrjPath(cfg));

		if (!coreDir.isDirectory()) return "No core project was selected.";
		if (!new File(coreDir, ".classpath").isFile()) return "No .classpath file was found in the selected directory.";

		for (String libraryName : cfg.libraries) {
			if (!isLibraryValid(cfg, libraryName))
				return "At least one selected library has a missing or invalid archive.";
		}

		return "No error found";
	}

	// -------------------------------------------------------------------------
	// Generated stuff
	// -------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        errorLabel = new javax.swing.JLabel();
        goBtn = new aurelienribon.ui.components.Button();
        headerPanel = new javax.swing.JPanel();
        headerLabel = new javax.swing.JLabel();
        numberLabel = new javax.swing.JLabel();

        setLayout(new java.awt.BorderLayout());

        jPanel1.setOpaque(false);

        errorLabel.setText("...");
        errorLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        goBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/gfx/ic_run.png"))); // NOI18N
        goBtn.setText("Open the generation screen");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(errorLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 241, Short.MAX_VALUE)
                    .addComponent(goBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(goBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(errorLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        add(jPanel1, java.awt.BorderLayout.CENTER);

        headerLabel.setText("<html> Ready to go?");
        headerLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        numberLabel.setText("4");

        javax.swing.GroupLayout headerPanelLayout = new javax.swing.GroupLayout(headerPanel);
        headerPanel.setLayout(headerPanelLayout);
        headerPanelLayout.setHorizontalGroup(
            headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, headerPanelLayout.createSequentialGroup()
                .addComponent(numberLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(headerLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 249, Short.MAX_VALUE))
        );
        headerPanelLayout.setVerticalGroup(
            headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(headerLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .addComponent(numberLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        add(headerPanel, java.awt.BorderLayout.NORTH);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel errorLabel;
    private aurelienribon.ui.components.Button goBtn;
    private javax.swing.JLabel headerLabel;
    private javax.swing.JPanel headerPanel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel numberLabel;
    // End of variables declaration//GEN-END:variables

}

package aurelienribon.libgdx.ui;

import aurelienribon.libgdx.ui.dialogs.AdvancedSettingsDialog;
import aurelienribon.ui.css.Style;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 * @author Aurelien Ribon | http://www.aurelienribon.com/
 */
public class ConfigPanel extends javax.swing.JPanel {
    public ConfigPanel() {
        initComponents();

		nameField.setText(Ctx.cfg.projectName);
		packageField.setText(Ctx.cfg.packageName);
		mainClassField.setText(Ctx.cfg.mainClassName);

		try {
			File destDir = new File(Ctx.cfg.destinationPath);
			destinationField.setText(destDir.getCanonicalPath());
		} catch (IOException ex) {
			assert false;
		}

		nameField.addMouseListener(selectOnFocusMouseListener);
		nameField.addKeyListener(updateOnTypeKeyListener);
		nameField.addKeyListener(projectNameKeyListener);
		packageField.addMouseListener(selectOnFocusMouseListener);
		packageField.addKeyListener(updateOnTypeKeyListener);
		packageField.addKeyListener(packageNameKeyListener);
		mainClassField.addMouseListener(selectOnFocusMouseListener);
		mainClassField.addKeyListener(updateOnTypeKeyListener);
		mainClassField.addKeyListener(mainClassNameKeyListener);

		browseBtn.addActionListener(new ActionListener() {@Override public void actionPerformed(ActionEvent e) {browse();}});
		genDesktopPrjChk.addActionListener(new ActionListener() {@Override public void actionPerformed(ActionEvent e) {update();}});
		genAndroidPrjChk.addActionListener(new ActionListener() {@Override public void actionPerformed(ActionEvent e) {update();}});
		genHtmlPrjChk.addActionListener(new ActionListener() {@Override public void actionPerformed(ActionEvent e) {update();}});

		advancedSettingsLabel.addMouseListener(new MouseAdapter() {
			@Override public void mousePressed(MouseEvent e) {
				JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(ConfigPanel.this);
				AdvancedSettingsDialog dialog = new AdvancedSettingsDialog(frame);
				dialog.setLocationRelativeTo(frame);
				dialog.setResizable(false);
				dialog.setVisible(true);
			}
		});

		update();

		Style.registerCssClasses(headerPanel, ".header");
		Style.registerCssClasses(numberLabel, ".headerNumber");
		Style.registerCssClasses(advancedSettingsLabel, ".linkLabel");

		nameField.requestFocusInWindow();
		nameField.selectAll();
    }

	private void browse() {
		String path = Ctx.cfg.destinationPath;
		JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);

		JFileChooser chooser = new JFileChooser(new File(path));
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setDialogTitle("Select the destination folder");

		if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
			destinationField.setText(chooser.getSelectedFile().getPath());
			update();
		}
	}

	private void update() {
		Ctx.cfg.projectName = nameField.getText();
		Ctx.cfg.packageName = packageField.getText();
		Ctx.cfg.mainClassName = mainClassField.getText();
		Ctx.cfg.destinationPath = destinationField.getText();
		Ctx.cfg.isDesktopIncluded = genDesktopPrjChk.isSelected();
		Ctx.cfg.isAndroidIncluded = genAndroidPrjChk.isSelected();
		Ctx.cfg.isHtmlIncluded = genHtmlPrjChk.isSelected();
		Ctx.fireConfigChanged();
	}

	private final KeyListener updateOnTypeKeyListener = new KeyAdapter() {
		@Override
		public void keyReleased(KeyEvent e) {
			update();
		}
	};

	private final MouseListener selectOnFocusMouseListener = new MouseAdapter() {
		@Override
		public void mousePressed(MouseEvent e) {
			JTextField field = (JTextField) e.getSource();
			if (!field.isFocusOwner()) field.selectAll();
		}
	};

	private final KeyListener projectNameKeyListener = new KeyAdapter() {
		private String backup;

		@Override
		public void keyPressed(KeyEvent e) {
			JTextField field = (JTextField) e.getSource();
			backup = field.getText();
		}

		@Override
		public void keyReleased(KeyEvent e) {
			JTextField field = (JTextField) e.getSource();
			if (!Pattern.compile("[a-zA-Z0-9_-]*").matcher(field.getText()).matches()) {
				String msg = "Only alphanumeric, '-' and '_' characters are allowed for project name.";
				JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(ConfigPanel.this);
				JOptionPane.showMessageDialog(frame, msg);
				field.setText(backup);
				update();
			}
		}
	};

	private final KeyListener packageNameKeyListener = new KeyAdapter() {
		private String backup;

		@Override
		public void keyPressed(KeyEvent e) {
			JTextField field = (JTextField) e.getSource();
			backup = field.getText();
		}

		@Override
		public void keyReleased(KeyEvent e) {
			JTextField field = (JTextField) e.getSource();
			if (!Pattern.compile("[a-zA-Z0-9_\\.]*").matcher(field.getText()).matches()) {
				String msg = "Only alphanumeric, '_' and '.' characters are allowed for package name.";
				JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(ConfigPanel.this);
				JOptionPane.showMessageDialog(frame, msg);
				field.setText(backup);
				update();
			}
		}
	};

	private final KeyListener mainClassNameKeyListener = new KeyAdapter() {
		private String backup;

		@Override
		public void keyPressed(KeyEvent e) {
			JTextField field = (JTextField) e.getSource();
			backup = field.getText();
		}

		@Override
		public void keyReleased(KeyEvent e) {
			JTextField field = (JTextField) e.getSource();
			if (!Pattern.compile("[a-zA-Z0-9_]*").matcher(field.getText()).matches()) {
				String msg = "Only alphanumeric and '_' characters are allowed for class name.\n";
				JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(ConfigPanel.this);

				JOptionPane.showMessageDialog(frame, msg);
				field.setText(backup);
				update();
			}
		}
	};

	// -------------------------------------------------------------------------
	// Generated stuff
	// -------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        headerPanel = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        numberLabel = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        genCommonPrjChk = new javax.swing.JCheckBox();
        destinationField = new javax.swing.JTextField();
        nameField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        browseBtn = new javax.swing.JButton();
        packageField = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        genAndroidPrjChk = new javax.swing.JCheckBox();
        genDesktopPrjChk = new javax.swing.JCheckBox();
        genHtmlPrjChk = new javax.swing.JCheckBox();
        jLabel5 = new javax.swing.JLabel();
        mainClassField = new javax.swing.JTextField();
        advancedSettingsLabel = new javax.swing.JLabel();

        setLayout(new java.awt.BorderLayout());

        jLabel4.setText("<html> Main parameters defining your project. See the overview panel to know if it suits your needs.");
        jLabel4.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        numberLabel.setText("1");

        javax.swing.GroupLayout headerPanelLayout = new javax.swing.GroupLayout(headerPanel);
        headerPanel.setLayout(headerPanelLayout);
        headerPanelLayout.setHorizontalGroup(
            headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, headerPanelLayout.createSequentialGroup()
                .addComponent(numberLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE))
        );
        headerPanelLayout.setVerticalGroup(
            headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .addComponent(numberLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        add(headerPanel, java.awt.BorderLayout.NORTH);

        jPanel1.setOpaque(false);

        genCommonPrjChk.setSelected(true);
        genCommonPrjChk.setText("generate common project (required)");
        genCommonPrjChk.setEnabled(false);

        destinationField.setEditable(false);

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel2.setText("Package");

        browseBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/gfx/ic_browse.png"))); // NOI18N

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel1.setText("Name");

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel3.setText("Destination");

        genAndroidPrjChk.setSelected(true);
        genAndroidPrjChk.setText("generate android project");

        genDesktopPrjChk.setSelected(true);
        genDesktopPrjChk.setText("generate desktop project");

        genHtmlPrjChk.setSelected(true);
        genHtmlPrjChk.setText("generate html project");

        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel5.setText("Game class");

        advancedSettingsLabel.setText("Advanced settings >");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(nameField, javax.swing.GroupLayout.DEFAULT_SIZE, 234, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(packageField))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(destinationField, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(browseBtn))
                            .addComponent(mainClassField)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(genHtmlPrjChk)
                            .addComponent(genAndroidPrjChk)
                            .addComponent(genDesktopPrjChk)
                            .addComponent(genCommonPrjChk))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(advancedSettingsLabel)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(nameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(packageField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(mainClassField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(destinationField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(browseBtn))
                .addGap(18, 18, 18)
                .addComponent(genCommonPrjChk)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(genDesktopPrjChk)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(genAndroidPrjChk)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(genHtmlPrjChk)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(advancedSettingsLabel)
                .addContainerGap())
        );

        jPanel1Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {browseBtn, destinationField, jLabel1, jLabel2, jLabel3, jLabel5, mainClassField, nameField, packageField});

        add(jPanel1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel advancedSettingsLabel;
    private javax.swing.JButton browseBtn;
    private javax.swing.JTextField destinationField;
    private javax.swing.JCheckBox genAndroidPrjChk;
    private javax.swing.JCheckBox genCommonPrjChk;
    private javax.swing.JCheckBox genDesktopPrjChk;
    private javax.swing.JCheckBox genHtmlPrjChk;
    private javax.swing.JPanel headerPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTextField mainClassField;
    private javax.swing.JTextField nameField;
    private javax.swing.JLabel numberLabel;
    private javax.swing.JTextField packageField;
    // End of variables declaration//GEN-END:variables

}

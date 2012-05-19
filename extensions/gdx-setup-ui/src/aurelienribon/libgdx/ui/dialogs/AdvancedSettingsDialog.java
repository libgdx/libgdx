package aurelienribon.libgdx.ui.dialogs;

import aurelienribon.libgdx.ui.Ctx;
import aurelienribon.ui.css.Style;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.regex.Pattern;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import res.Res;

/**
 * @author Aurelien Ribon | http://www.aurelienribon.com/
 */
public class AdvancedSettingsDialog extends javax.swing.JDialog {
    public AdvancedSettingsDialog(JFrame parent) {
        super(parent, true);
        initComponents();

		commonSuffixField.setText(Ctx.cfg.commonSuffix);
		desktopSuffixField.setText(Ctx.cfg.desktopSuffix);
		androidSuffixField.setText(Ctx.cfg.androidSuffix);
		htmlSuffixField.setText(Ctx.cfg.htmlSuffix);
		androidMinSdkField.setText(Ctx.cfg.androidMinSdkVersion);
		androidTargetSdkField.setText(Ctx.cfg.androidTargetSdkVersion);
		androidMaxSdkField.setText(Ctx.cfg.androidMaxSdkVersion);

		commonSuffixField.addMouseListener(selectOnFocusMouseListener);
		desktopSuffixField.addMouseListener(selectOnFocusMouseListener);
		androidSuffixField.addMouseListener(selectOnFocusMouseListener);
		htmlSuffixField.addMouseListener(selectOnFocusMouseListener);
		androidMinSdkField.addMouseListener(selectOnFocusMouseListener);
		androidTargetSdkField.addMouseListener(selectOnFocusMouseListener);
		androidMaxSdkField.addMouseListener(selectOnFocusMouseListener);

		commonSuffixField.addKeyListener(updateOnTypeKeyListener);
		desktopSuffixField.addKeyListener(updateOnTypeKeyListener);
		androidSuffixField.addKeyListener(updateOnTypeKeyListener);
		htmlSuffixField.addKeyListener(updateOnTypeKeyListener);
		androidMinSdkField.addKeyListener(updateOnTypeKeyListener);
		androidTargetSdkField.addKeyListener(updateOnTypeKeyListener);
		androidMaxSdkField.addKeyListener(updateOnTypeKeyListener);

		androidMinSdkField.addKeyListener(numbersOnlyKeyListener);
		androidTargetSdkField.addKeyListener(numbersOnlyKeyListener);
		androidMaxSdkField.addKeyListener(numbersOnlyKeyListener);

		Style.registerCssClasses(rootPanel, ".rootPanel");
		Style.registerCssClasses(title1, ".titleLabel");
		Style.registerCssClasses(paintedPanel1, ".optionGroupPanel");
		Style.registerCssClasses(paintedPanel2, ".optionGroupPanel");
		Style.apply(getContentPane(), new Style(Res.getUrl("css/style.css")));
    }

	private void update() {
		Ctx.cfg.commonSuffix = commonSuffixField.getText();
		Ctx.cfg.desktopSuffix = desktopSuffixField.getText();
		Ctx.cfg.androidSuffix = androidSuffixField.getText();
		Ctx.cfg.androidMinSdkVersion = androidMinSdkField.getText();
		Ctx.cfg.androidMaxSdkVersion = androidMaxSdkField.getText();
		Ctx.cfg.androidTargetSdkVersion = androidTargetSdkField.getText();
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


	private final KeyListener numbersOnlyKeyListener = new KeyAdapter() {
		private String backup;

		@Override
		public void keyPressed(KeyEvent e) {
			JTextField field = (JTextField) e.getSource();
			backup = field.getText();
		}

		@Override
		public void keyReleased(KeyEvent e) {
			JTextField field = (JTextField) e.getSource();
			if (!Pattern.compile("[0-9]*").matcher(field.getText()).matches()) {
				String msg = "Only numbers are allowed.";
				JOptionPane.showMessageDialog(AdvancedSettingsDialog.this, msg);
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

        rootPanel = new aurelienribon.ui.components.PaintedPanel();
        title1 = new javax.swing.JLabel();
        paintedPanel1 = new aurelienribon.ui.components.PaintedPanel();
        jLabel7 = new javax.swing.JLabel();
        androidMaxSdkField = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        androidTargetSdkField = new javax.swing.JTextField();
        androidMinSdkField = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        paintedPanel2 = new aurelienribon.ui.components.PaintedPanel();
        jLabel1 = new javax.swing.JLabel();
        androidSuffixField = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        commonSuffixField = new javax.swing.JTextField();
        desktopSuffixField = new javax.swing.JTextField();
        htmlSuffixField = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Advanced settings");

        title1.setText("Advanced Settings");

        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel7.setText("android:maxSdkVersion");

        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel6.setText("android:targetSdkVersion");

        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel5.setText("android:minSdkVersion");

        androidTargetSdkField.setText("15");

        androidMinSdkField.setText("5");

        jLabel8.setText("Leave a field blank to unset the value");

        javax.swing.GroupLayout paintedPanel1Layout = new javax.swing.GroupLayout(paintedPanel1);
        paintedPanel1.setLayout(paintedPanel1Layout);
        paintedPanel1Layout.setHorizontalGroup(
            paintedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(paintedPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(paintedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(paintedPanel1Layout.createSequentialGroup()
                        .addGroup(paintedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel6)
                            .addComponent(jLabel7))
                        .addGap(302, 302, 302))
                    .addGroup(paintedPanel1Layout.createSequentialGroup()
                        .addGroup(paintedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(paintedPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel5)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(paintedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(androidMaxSdkField, javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(androidTargetSdkField)
                                    .addComponent(androidMinSdkField)))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, paintedPanel1Layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(jLabel8)))
                        .addContainerGap())))
        );

        paintedPanel1Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jLabel5, jLabel6, jLabel7});

        paintedPanel1Layout.setVerticalGroup(
            paintedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(paintedPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(paintedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(androidMinSdkField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(paintedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(androidTargetSdkField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(paintedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(androidMaxSdkField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jLabel8)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel1.setText("Main project suffix");

        androidSuffixField.setText("-android");

        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel4.setText("Html project suffix");

        desktopSuffixField.setText("-desktop");

        htmlSuffixField.setText("-html");

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel3.setText("Android project suffix");

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel2.setText("Desktop project suffix");

        jLabel9.setText("Do not use twice the same suffix to avoid undefined behavior");

        javax.swing.GroupLayout paintedPanel2Layout = new javax.swing.GroupLayout(paintedPanel2);
        paintedPanel2.setLayout(paintedPanel2Layout);
        paintedPanel2Layout.setHorizontalGroup(
            paintedPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(paintedPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(paintedPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(paintedPanel2Layout.createSequentialGroup()
                        .addGroup(paintedPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, paintedPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(desktopSuffixField, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, paintedPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(commonSuffixField, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addGap(18, 18, 18)
                        .addGroup(paintedPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(paintedPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(htmlSuffixField, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(paintedPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(androidSuffixField, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, paintedPanel2Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jLabel9)))
                .addContainerGap())
        );

        paintedPanel2Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jLabel1, jLabel2, jLabel3, jLabel4});

        paintedPanel2Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {androidSuffixField, commonSuffixField, desktopSuffixField, htmlSuffixField});

        paintedPanel2Layout.setVerticalGroup(
            paintedPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(paintedPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(paintedPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(commonSuffixField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)
                    .addComponent(androidSuffixField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(paintedPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(htmlSuffixField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(desktopSuffixField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jLabel9)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        paintedPanel2Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {androidSuffixField, commonSuffixField, desktopSuffixField, htmlSuffixField, jLabel1, jLabel2, jLabel3, jLabel4});

        javax.swing.GroupLayout rootPanelLayout = new javax.swing.GroupLayout(rootPanel);
        rootPanel.setLayout(rootPanelLayout);
        rootPanelLayout.setHorizontalGroup(
            rootPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(rootPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(rootPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(title1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(paintedPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(paintedPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        rootPanelLayout.setVerticalGroup(
            rootPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(rootPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(title1)
                .addGap(18, 18, 18)
                .addComponent(paintedPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(paintedPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        getContentPane().add(rootPanel, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField androidMaxSdkField;
    private javax.swing.JTextField androidMinSdkField;
    private javax.swing.JTextField androidSuffixField;
    private javax.swing.JTextField androidTargetSdkField;
    private javax.swing.JTextField commonSuffixField;
    private javax.swing.JTextField desktopSuffixField;
    private javax.swing.JTextField htmlSuffixField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private aurelienribon.ui.components.PaintedPanel paintedPanel1;
    private aurelienribon.ui.components.PaintedPanel paintedPanel2;
    private aurelienribon.ui.components.PaintedPanel rootPanel;
    private javax.swing.JLabel title1;
    // End of variables declaration//GEN-END:variables

}

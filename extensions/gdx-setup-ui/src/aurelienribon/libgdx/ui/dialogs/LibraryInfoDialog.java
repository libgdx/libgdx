package aurelienribon.libgdx.ui.dialogs;

import aurelienribon.libgdx.LibraryDef;
import aurelienribon.libgdx.ui.AppContext;
import aurelienribon.ui.css.Style;
import java.awt.Desktop;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.swing.JFrame;
import res.Res;

/**
 * @author Aurelien Ribon | http://www.aurelienribon.com/
 */
public class LibraryInfoDialog extends javax.swing.JDialog {
	private final LibraryDef def;

    public LibraryInfoDialog(JFrame parent, String libraryName) {
        super(parent, true);
        initComponents();

		def = AppContext.inst().getConfig().getLibraryDef(libraryName);
		nameLabel.setText(def.name);
		descriptionLabel.setText("<html>" + def.description);
		homepageLabel.setText(def.homepage);
		homepageLabel.addMouseListener(urlMouseListener);

		Style.registerCssClasses(rootPanel, ".rootPanel");
		Style.registerCssClasses(nameLabel, ".titleLabel");
		Style.registerCssClasses(descriptionLabel, ".libInfoDescLabel");
		Style.registerCssClasses(homepageLabel, ".libInfoHomepageLabel");
		Style.apply(getContentPane(), new Style(Res.class.getResource("style.css")));
    }

	private final MouseListener urlMouseListener = new MouseAdapter() {
		@Override
		public void mousePressed(MouseEvent e) {
			if (!Desktop.isDesktopSupported()) return;
			try {
				Desktop.getDesktop().browse(new URI(def.homepage));
			} catch (IOException ex) {
			} catch (URISyntaxException ex) {
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
        descriptionLabel = new javax.swing.JLabel();
        homepageLabel = new javax.swing.JLabel();
        nameLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Information");

        descriptionLabel.setText("Description");
        descriptionLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        homepageLabel.setText("http://www......");

        nameLabel.setText("Library Name");

        javax.swing.GroupLayout rootPanelLayout = new javax.swing.GroupLayout(rootPanel);
        rootPanel.setLayout(rootPanelLayout);
        rootPanelLayout.setHorizontalGroup(
            rootPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(rootPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(rootPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(descriptionLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(homepageLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 526, Short.MAX_VALUE)
                    .addComponent(nameLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        rootPanelLayout.setVerticalGroup(
            rootPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(rootPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(nameLabel)
                .addGap(18, 18, 18)
                .addComponent(descriptionLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 266, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(homepageLabel)
                .addContainerGap())
        );

        getContentPane().add(rootPanel, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel descriptionLabel;
    private javax.swing.JLabel homepageLabel;
    private javax.swing.JLabel nameLabel;
    private aurelienribon.ui.components.PaintedPanel rootPanel;
    // End of variables declaration//GEN-END:variables

}

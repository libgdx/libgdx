package aurelienribon.libgdx.ui.dialogs;

import aurelienribon.libgdx.LibraryDef;
import aurelienribon.libgdx.ui.Ctx;
import aurelienribon.ui.css.Style;
import java.awt.Desktop;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import res.Res;

/**
 * @author Aurelien Ribon | http://www.aurelienribon.com/
 */
public class LibraryInfoDialog extends javax.swing.JDialog {
    public LibraryInfoDialog(JFrame parent, String libraryName) {
        super(parent, true);
        initComponents();

		LibraryDef def = Ctx.cfg.libs.getDef(libraryName);
		nameLabel.setText(def.name);
		descriptionLabel.setText("<html>" + def.description);
		versionLabel.setText(def.stableVersion);
		authorLabel.setText(def.author);
		homepageLabel.setText(def.homepage != null ? def.homepage : "<unknown>");

		if (def.authorWebsite != null) {
			authorLabel.addMouseListener(new BrowseMouseListener(def.authorWebsite));
			Style.registerCssClasses(authorLabel, ".libInfoUrlLabel");
		}

		if (def.homepage != null) {
			homepageLabel.addMouseListener(new BrowseMouseListener(def.homepage));
			Style.registerCssClasses(homepageLabel, ".libInfoUrlLabel");
		}

		if (def.logo != null) {
			logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
			logoLabel.setIcon(Res.getImage("gfx/ic48_loading.gif"));
			downloadLogo(def.logo);
		}

		Style.registerCssClasses(rootPanel, ".rootPanel");
		Style.registerCssClasses(nameLabel, ".titleLabel");
		Style.registerCssClasses(paintedPanel1, ".optionGroupPanel");
		Style.registerCssClasses(descriptionLabel, ".libInfoDescLabel");
		Style.apply(getContentPane(), new Style(Res.getUrl("css/style.css")));
    }


	private void downloadLogo(final String url) {
		new Thread(new Runnable() {@Override public void run() {
			try {
				final BufferedImage img = ImageIO.read(new URL(url));
				SwingUtilities.invokeLater(new Runnable() {@Override public void run() {
					logoLabel.setHorizontalAlignment(SwingConstants.RIGHT);
					logoLabel.setIcon(new ImageIcon(img));
				}});
			} catch (MalformedURLException ex) {
			} catch (IOException ex) {
			}
		}}).start();
	}

	private static class BrowseMouseListener extends MouseAdapter {
		private final String url;

		public BrowseMouseListener(String url) {
			this.url = url;
		}

		@Override
		public void mousePressed(MouseEvent e) {
			if (!Desktop.isDesktopSupported()) return;
			try {
				Desktop.getDesktop().browse(new URI(url));
			} catch (IOException ex) {
			} catch (URISyntaxException ex) {
			}
		}
	}

	// -------------------------------------------------------------------------
	// Generated stuff
	// -------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        rootPanel = new aurelienribon.ui.components.PaintedPanel();
        nameLabel = new javax.swing.JLabel();
        paintedPanel1 = new aurelienribon.ui.components.PaintedPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        versionLabel = new javax.swing.JLabel();
        authorLabel = new javax.swing.JLabel();
        homepageLabel = new javax.swing.JLabel();
        descriptionLabel = new javax.swing.JLabel();
        logoLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Information");

        nameLabel.setText("Library Name");

        jLabel1.setText("Author: ");

        jLabel2.setText("Project: ");

        jLabel3.setText("Version: ");

        versionLabel.setText("x.x.x");

        authorLabel.setText("Aurelien Ribon");

        homepageLabel.setText("http://www......");

        javax.swing.GroupLayout paintedPanel1Layout = new javax.swing.GroupLayout(paintedPanel1);
        paintedPanel1.setLayout(paintedPanel1Layout);
        paintedPanel1Layout.setHorizontalGroup(
            paintedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(paintedPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(paintedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(paintedPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(homepageLabel))
                    .addGroup(paintedPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(authorLabel))
                    .addGroup(paintedPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(versionLabel)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        paintedPanel1Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jLabel1, jLabel2, jLabel3});

        paintedPanel1Layout.setVerticalGroup(
            paintedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, paintedPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(paintedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(versionLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(paintedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(authorLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(paintedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(homepageLabel)
                    .addComponent(jLabel2))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        descriptionLabel.setText("Description");
        descriptionLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        logoLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        logoLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/gfx/noImage.png"))); // NOI18N
        logoLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        javax.swing.GroupLayout rootPanelLayout = new javax.swing.GroupLayout(rootPanel);
        rootPanel.setLayout(rootPanelLayout);
        rootPanelLayout.setHorizontalGroup(
            rootPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(rootPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(rootPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(nameLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 641, Short.MAX_VALUE)
                    .addComponent(paintedPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(rootPanelLayout.createSequentialGroup()
                        .addComponent(descriptionLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(logoLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        rootPanelLayout.setVerticalGroup(
            rootPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(rootPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(nameLabel)
                .addGap(18, 18, 18)
                .addGroup(rootPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(descriptionLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(rootPanelLayout.createSequentialGroup()
                        .addComponent(logoLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 26, Short.MAX_VALUE)))
                .addGap(18, 18, 18)
                .addComponent(paintedPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        getContentPane().add(rootPanel, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel authorLabel;
    private javax.swing.JLabel descriptionLabel;
    private javax.swing.JLabel homepageLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel logoLabel;
    private javax.swing.JLabel nameLabel;
    private aurelienribon.ui.components.PaintedPanel paintedPanel1;
    private aurelienribon.ui.components.PaintedPanel rootPanel;
    private javax.swing.JLabel versionLabel;
    // End of variables declaration//GEN-END:variables

}

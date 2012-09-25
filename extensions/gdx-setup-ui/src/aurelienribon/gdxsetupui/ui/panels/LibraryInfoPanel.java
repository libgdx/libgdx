package aurelienribon.gdxsetupui.ui.panels;

import aurelienribon.gdxsetupui.LibraryDef;
import aurelienribon.gdxsetupui.ui.Ctx;
import aurelienribon.gdxsetupui.ui.MainPanel;
import aurelienribon.ui.css.Style;
import aurelienribon.utils.Res;
import aurelienribon.utils.SwingUtils;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/**
 * @author Aurelien Ribon | http://www.aurelienribon.com/
 */
public class LibraryInfoPanel extends javax.swing.JPanel {
    public LibraryInfoPanel(final MainPanel mainPanel) {
        initComponents();

		closeLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		closeLabel.addMouseListener(new MouseAdapter() {
			@Override public void mousePressed(MouseEvent e) {
				mainPanel.hideLibraryInfo();
			}
		});

		Style.registerCssClasses(nameLabel, ".libInfoTitleLabel");
		Style.registerCssClasses(paintedPanel1, ".optionGroupPanel");
		Style.registerCssClasses(descriptionLabel, ".libInfoDescLabel");
		Style.registerCssClasses(closeLabel, ".linkLabel");
    }

	public void setup(String libraryName) {
		LibraryDef def = Ctx.libs.getDef(libraryName);
		nameLabel.setText(def.name);
		descriptionLabel.setText("<html>" + def.description);
		versionLabel.setText(def.stableVersion);
		authorLabel.setText(def.author);
		homepageLabel.setText(def.homepage != null ? def.homepage : "<unknown>");

		// Clean links

		for (MouseListener ml : authorLabel.getMouseListeners()) authorLabel.removeMouseListener(ml);
		for (MouseListener ml : homepageLabel.getMouseListeners()) homepageLabel.removeMouseListener(ml);

		// Setup links

		if (def.authorWebsite != null) {
			SwingUtils.addBrowseBehavior(authorLabel, def.authorWebsite);
			Style.registerCssClasses(authorLabel, ".linkLabel");
		} else if (authorLabel.getMouseListeners().length > 0) {
			Style.unregisterAllCssClasses(authorLabel);
		}

		if (def.homepage != null) {
			SwingUtils.addBrowseBehavior(homepageLabel, def.homepage);
			Style.registerCssClasses(homepageLabel, ".linkLabel");
		} else if (homepageLabel.getMouseListeners().length > 0) {
			Style.unregisterAllCssClasses(homepageLabel);
		}

		// Setup logo

		if (def.logo != null) {
			logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
			logoLabel.setIcon(Res.getImage("gfx/ic66_loading.gif"));
			downloadLogo(def.logo);
		} else {
			logoLabel.setHorizontalAlignment(SwingConstants.RIGHT);
			logoLabel.setIcon(null);
		}

		Style.apply(this, new Style(Res.getUrl("css/style.css")));
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

	// -------------------------------------------------------------------------
	// Generated stuff
	// -------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

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
        closeLabel = new javax.swing.JLabel();

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
                .addContainerGap(423, Short.MAX_VALUE))
        );
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
        logoLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        closeLabel.setText("< Close this panel");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(paintedPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(nameLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(closeLabel)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(descriptionLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(logoLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(nameLabel)
                .addGap(18, 18, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(descriptionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 295, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(logoLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(closeLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(paintedPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel authorLabel;
    private javax.swing.JLabel closeLabel;
    private javax.swing.JLabel descriptionLabel;
    private javax.swing.JLabel homepageLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel logoLabel;
    private javax.swing.JLabel nameLabel;
    private aurelienribon.ui.components.PaintedPanel paintedPanel1;
    private javax.swing.JLabel versionLabel;
    // End of variables declaration//GEN-END:variables

}

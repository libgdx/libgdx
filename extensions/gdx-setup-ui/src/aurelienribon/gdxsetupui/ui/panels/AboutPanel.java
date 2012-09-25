package aurelienribon.gdxsetupui.ui.panels;

import aurelienribon.gdxsetupui.ui.MainPanel;
import aurelienribon.ui.css.Style;
import aurelienribon.utils.SwingUtils;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Aurelien Ribon | http://www.aurelienribon.com/
 */
public class AboutPanel extends javax.swing.JPanel {
    public AboutPanel(final MainPanel mainPanel) {
        initComponents();

		Style.registerCssClasses(linkLibGDXLabel, ".linkLabel");
		Style.registerCssClasses(linkARLabel, ".linkLabel");
		Style.registerCssClasses(linkCSSEngineLabel, ".linkLabel");
		Style.registerCssClasses(linkSlidingLayoutLabel, ".linkLabel");
		Style.registerCssClasses(linkUTELabel, ".linkLabel");
		Style.registerCssClasses(linkCommonsIOLabel, ".linkLabel");

		backBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mainPanel.hideAboutPanel();
			}
		});

		SwingUtils.addBrowseBehavior(linkLibGDXLabel, "http://libgdx.badlogicgames.com");
		SwingUtils.addBrowseBehavior(linkARLabel, "http://www.aurelienribon.com");
		SwingUtils.addBrowseBehavior(linkCSSEngineLabel, "http://code.google.com/p/java-universal-css-engine/");
		SwingUtils.addBrowseBehavior(linkSlidingLayoutLabel, "https://github.com/AurelienRibon/sliding-layout");
		SwingUtils.addBrowseBehavior(linkUTELabel, "http://www.aurelienribon.com/blog/projects/universal-tween-engine/");
		SwingUtils.addBrowseBehavior(linkCommonsIOLabel, "http://commons.apache.org/io/");
    }

	// -------------------------------------------------------------------------
	// Generated stuff
	// -------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        backBtn = new aurelienribon.ui.components.Button();
        jLabel2 = new javax.swing.JLabel();
        linkLibGDXLabel = new javax.swing.JLabel();
        linkARLabel = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        linkCSSEngineLabel = new javax.swing.JLabel();
        linkSlidingLayoutLabel = new javax.swing.JLabel();
        linkUTELabel = new javax.swing.JLabel();
        linkCommonsIOLabel = new javax.swing.JLabel();

        backBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/gfx/ic_back.png"))); // NOI18N
        backBtn.setText("Back");

        jLabel2.setText("<html>Developed by <b>Aurelien Ribon</b> [1], as a contribution for the awesome framework LibGDX [2].");

        linkLibGDXLabel.setText("[2] http://libgdx.badlogicgames.com");

        linkARLabel.setText("[1] http://www.aurelienribon.com");

        jLabel1.setText("<html> Everything started as an idea to quickly setup the eclipse environment required to use LibGDX and its multiple backends. With this application, we hope you will be able to concentrate more on your ideas, and less on the IDE plumbing!");

        jLabel5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/gfx/logo2.png"))); // NOI18N

        jLabel6.setText("<html><b>Libraries used under the hood</b> ");

        linkCSSEngineLabel.setText(":: CSS Engine for Swing");

        linkSlidingLayoutLabel.setText(":: Sliding Layout for Swing");

        linkUTELabel.setText(":: Universal Tween Engine");

        linkCommonsIOLabel.setText(":: Apache Commons IO");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 476, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(jLabel5)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(backBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(linkLibGDXLabel)
                                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(linkARLabel)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(linkSlidingLayoutLabel)
                                    .addComponent(linkCSSEngineLabel))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(linkUTELabel)
                                    .addComponent(linkCommonsIOLabel))))
                        .addGap(0, 0, Short.MAX_VALUE))))
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {linkCSSEngineLabel, linkCommonsIOLabel, linkSlidingLayoutLabel, linkUTELabel});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(linkARLabel)
                .addGap(0, 0, 0)
                .addComponent(linkLibGDXLabel)
                .addGap(18, 18, 18)
                .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(linkCSSEngineLabel)
                        .addGap(0, 0, 0)
                        .addComponent(linkSlidingLayoutLabel))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(linkUTELabel)
                        .addGap(0, 0, 0)
                        .addComponent(linkCommonsIOLabel)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 29, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(backBtn, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private aurelienribon.ui.components.Button backBtn;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel linkARLabel;
    private javax.swing.JLabel linkCSSEngineLabel;
    private javax.swing.JLabel linkCommonsIOLabel;
    private javax.swing.JLabel linkLibGDXLabel;
    private javax.swing.JLabel linkSlidingLayoutLabel;
    private javax.swing.JLabel linkUTELabel;
    // End of variables declaration//GEN-END:variables

}

package aurelienribon.libgdx.ui.dialogs;

import aurelienribon.libgdx.ProjectConfiguration;
import aurelienribon.libgdx.ProjectSetup;
import aurelienribon.libgdx.ui.Ctx;
import aurelienribon.ui.css.Style;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import res.Res;

/**
 * @author Aurelien Ribon | http://www.aurelienribon.com/
 */
public class GoDialog extends javax.swing.JDialog {
	private final ProjectSetup setup;

    public GoDialog(JFrame parent) {
        super(parent, true);
        initComponents();

		Style.registerCssClasses(rootPanel, ".rootPanel");
		Style.registerCssClasses(title1, ".titleLabel");
		Style.registerCssClasses(title2, ".titleLabel");
		Style.registerCssClasses(importQuestion, ".questionLabel");
		Style.registerCssClasses(fixHtmlQuestion, ".questionLabel");
		Style.registerCssClasses(paintedPanel1, ".optionGroupPanel");
		Style.registerCssClasses(progressArea, ".progressArea");
		Style.apply(getContentPane(), new Style(Res.getUrl("css/style.css")));

		importQuestion.addMouseListener(new MouseAdapter() {
			@Override public void mousePressed(MouseEvent e) {
				ImportHelpDialog dialog = new ImportHelpDialog(null);
				dialog.setLocationRelativeTo(GoDialog.this);
				dialog.pack();
				dialog.setVisible(true);
			}
		});

		fixHtmlQuestion.addMouseListener(new MouseAdapter() {
			@Override public void mousePressed(MouseEvent e) {
				FixHtmlHelpDialog dialog = new FixHtmlHelpDialog(null);
				dialog.setLocationRelativeTo(GoDialog.this);
				dialog.pack();
				dialog.setVisible(true);
			}
		});

		setup = new ProjectSetup(Ctx.cfg);

		new Thread(new Runnable() {
			@Override public void run() {
				try {
					report("Decompressing projects...");
					setup.inflateProjects();
					report(" done\nDecompressing libraries...");
					setup.inflateLibraries();
					report(" done\nConfiguring libraries...");
					setup.configureLibraries();
					report(" done\nPost-processing files...");
					setup.postProcess();
					report(" done\nCopying projects...");
					setup.copy();
					report(" done\nCleaning...");
					setup.clean();
					report(" done\nAll done!");
				} catch (final IOException ex) {
					report("\n[error] " + ex.getMessage());
				}
			}
		}).start();
    }

	private void report(final String txt) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override public void run() {
				progressArea.append(txt);
			}
		});
	}

	// -------------------------------------------------------------------------
	// Generated stuff
	// -------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        rootPanel = new aurelienribon.ui.components.PaintedPanel();
        title1 = new javax.swing.JLabel();
        title2 = new javax.swing.JLabel();
        paintedPanel1 = new aurelienribon.ui.components.PaintedPanel();
        importQuestion = new javax.swing.JLabel();
        fixHtmlQuestion = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        progressArea = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Generation");

        title1.setText("Generation in progress");

        title2.setText("Frequently Asked Questions");

        paintedPanel1.setOpaque(false);

        importQuestion.setText("How do I import the projects into eclipse?");

        fixHtmlQuestion.setText("How do I fix the \"gwt-servlet not found\" error in my html project?");

        javax.swing.GroupLayout paintedPanel1Layout = new javax.swing.GroupLayout(paintedPanel1);
        paintedPanel1.setLayout(paintedPanel1Layout);
        paintedPanel1Layout.setHorizontalGroup(
            paintedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(paintedPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(paintedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(importQuestion)
                    .addComponent(fixHtmlQuestion))
                .addContainerGap(71, Short.MAX_VALUE))
        );
        paintedPanel1Layout.setVerticalGroup(
            paintedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(paintedPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(importQuestion)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(fixHtmlQuestion)
                .addContainerGap())
        );

        progressArea.setColumns(20);
        progressArea.setEditable(false);
        progressArea.setRows(5);
        jScrollPane1.setViewportView(progressArea);

        javax.swing.GroupLayout rootPanelLayout = new javax.swing.GroupLayout(rootPanel);
        rootPanel.setLayout(rootPanelLayout);
        rootPanelLayout.setHorizontalGroup(
            rootPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(rootPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(rootPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(title1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(title2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(paintedPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1))
                .addContainerGap())
        );
        rootPanelLayout.setVerticalGroup(
            rootPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(rootPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(title1)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 136, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(title2)
                .addGap(18, 18, 18)
                .addComponent(paintedPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        getContentPane().add(rootPanel, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel fixHtmlQuestion;
    private javax.swing.JLabel importQuestion;
    private javax.swing.JScrollPane jScrollPane1;
    private aurelienribon.ui.components.PaintedPanel paintedPanel1;
    private javax.swing.JTextArea progressArea;
    private aurelienribon.ui.components.PaintedPanel rootPanel;
    private javax.swing.JLabel title1;
    private javax.swing.JLabel title2;
    // End of variables declaration//GEN-END:variables

}

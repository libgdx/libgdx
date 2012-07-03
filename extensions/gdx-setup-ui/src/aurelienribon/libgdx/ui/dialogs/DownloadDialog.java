package aurelienribon.libgdx.ui.dialogs;

import aurelienribon.ui.css.Style;
import aurelienribon.utils.HttpUtils;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import org.apache.commons.io.FileUtils;
import res.Res;

/**
 * @author Aurelien Ribon | http://www.aurelienribon.com/
 */
public class DownloadDialog extends javax.swing.JDialog {
	private final Callback callback;
	private final String out;

	public DownloadDialog(JFrame parent, Callback callback, String in, String out) {
		super(parent, true);
		this.callback = callback;
		this.out = out;

		initComponents();
		nameLabel.setText(in);
		countLabel.setText("...waiting for response from the server...");

		Style.registerCssClasses(rootPanel, ".rootPanel");
		Style.apply(getContentPane(), new Style(Res.getUrl("css/style.css")));

		try {
			new File(out).getCanonicalFile().getParentFile().mkdirs();
		} catch (IOException ex) {
			assert false;
		}

		try {
			URL inputURL = new URL(in);
			OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(out + ".tmp"));

			final HttpUtils.DownloadTask task = HttpUtils.downloadAsync(inputURL, outputStream, fullCallback);

			addWindowListener(new WindowAdapter() {@Override public void windowClosing(WindowEvent e) {
				task.stop();
				dispose();
			}});

		} catch (MalformedURLException ex) {
			JOptionPane.showMessageDialog(getContentPane(), ex.getMessage());
		} catch (IOException ex) {
			JOptionPane.showMessageDialog(getContentPane(), ex.getMessage());
		}
	}

	public static interface Callback {
		public void completed();
	}

	private final HttpUtils.Callback fullCallback = new HttpUtils.Callback() {
		@Override
		public void completed() {
			try {
				FileUtils.deleteQuietly(new File(out));
				FileUtils.moveFile(new File(out + ".tmp"), new File(out));
				dispose();
				callback.completed();
			} catch (IOException ex) {
				String msg = "Could not rename \"" + out + ".tmp" + "\" into \"" + out + "\"";
				JOptionPane.showMessageDialog(getContentPane(), msg);
				dispose();
			}
		}

		@Override
		public void canceled() {
			FileUtils.deleteQuietly(new File(out + ".tmp"));
			dispose();
		}

		@Override
		public void error(IOException ex) {
			FileUtils.deleteQuietly(new File(out + ".tmp"));
			String msg = "Something went wrong during the download.\n"
				+ ex.getClass().getSimpleName() + ": " + ex.getMessage();
			JOptionPane.showMessageDialog(getContentPane(), msg);
			dispose();
		}

		@Override
		public void updated(int length, int totalLength) {
			if (totalLength > 0) {
				progressBar.setIndeterminate(false);
				progressBar.setValue((int) Math.round((double) length * 100 / totalLength));
				countLabel.setText((length/1024) + " / " + (totalLength/1024));
			} else {
				progressBar.setIndeterminate(true);
				countLabel.setText((length/1024) + " / unknown");
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
        jLabel3 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();
        countLabel = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        nameLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Download in progress...");
        setResizable(false);

        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/gfx/ic64_download.png"))); // NOI18N

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel1.setText("Downloading: ");

        countLabel.setText("xxx");

        jLabel2.setText("KiloBytes downloaded: ");

        nameLabel.setText("xxx");

        javax.swing.GroupLayout rootPanelLayout = new javax.swing.GroupLayout(rootPanel);
        rootPanel.setLayout(rootPanelLayout);
        rootPanelLayout.setHorizontalGroup(
            rootPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(rootPanelLayout.createSequentialGroup()
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(rootPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, 471, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(rootPanelLayout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(nameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 387, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(rootPanelLayout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(countLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 354, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        rootPanelLayout.setVerticalGroup(
            rootPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGroup(rootPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(rootPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(nameLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(progressBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(rootPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(countLabel))
                .addContainerGap())
        );

        getContentPane().add(rootPanel, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel countLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JProgressBar progressBar;
    private aurelienribon.ui.components.PaintedPanel rootPanel;
    // End of variables declaration//GEN-END:variables

}

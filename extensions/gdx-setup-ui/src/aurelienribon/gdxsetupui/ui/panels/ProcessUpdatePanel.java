/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package aurelienribon.gdxsetupui.ui.panels;

import aurelienribon.gdxsetupui.ProjectUpdate;
import aurelienribon.gdxsetupui.ui.Ctx;
import aurelienribon.gdxsetupui.ui.MainPanel;
import aurelienribon.ui.css.Style;
import javax.swing.SwingUtilities;

/**
 * @author Aurelien Ribon | http://www.aurelienribon.com/
 */
public class ProcessUpdatePanel extends javax.swing.JPanel {
    public ProcessUpdatePanel(final MainPanel mainPanel) {
        initComponents();

		Style.registerCssClasses(jScrollPane1, ".frame");
		Style.registerCssClasses(progressArea, ".progressArea");
    }

	public void launch() {
		progressArea.setText("");

		final ProjectUpdate update = new ProjectUpdate(Ctx.cfgUpdate, Ctx.libs);

		new Thread(new Runnable() {
			@Override public void run() {
				try {
					report("Decompressing libraries...");
					update.inflateLibraries();
					report(" done\nEditing classpaths...");
					update.editClasspaths();
					report(" done\nAll done!");
				} catch (final Exception ex) {
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

        jScrollPane1 = new javax.swing.JScrollPane();
        progressArea = new javax.swing.JTextArea();

        progressArea.setEditable(false);
        progressArea.setColumns(20);
        progressArea.setRows(5);
        jScrollPane1.setViewportView(progressArea);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 341, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea progressArea;
    // End of variables declaration//GEN-END:variables

}
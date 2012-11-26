package aurelienribon.gdxsetupui.ui.panels;

import aurelienribon.gdxsetupui.Helper.ClasspathEntry;
import aurelienribon.gdxsetupui.Helper.GwtModule;
import aurelienribon.gdxsetupui.ui.Ctx;
import aurelienribon.gdxsetupui.ui.MainPanel;
import aurelienribon.ui.css.Style;
import aurelienribon.utils.notifications.AutoListModel;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 * @author Aurelien Ribon | http://www.aurelienribon.com/
 */
public class ClasspathsPanel extends javax.swing.JPanel {
    public ClasspathsPanel(final MainPanel mainPanel) {
        initComponents();

		Style.registerCssClasses(jScrollPane2, ".frame");
		Style.registerCssClasses(jScrollPane6, ".frame");
		Style.registerCssClasses(jScrollPane4, ".frame");
		Style.registerCssClasses(jScrollPane5, ".frame");
		Style.registerCssClasses(jScrollPane7, ".frame");
		Style.registerCssClasses(paintedPanel1, ".optionGroupPanel");

		coreList.setModel(new AutoListModel<ClasspathEntry>(Ctx.cfgUpdate.coreClasspath));
		androidList.setModel(new AutoListModel<ClasspathEntry>(Ctx.cfgUpdate.androidClasspath));
		desktopList.setModel(new AutoListModel<ClasspathEntry>(Ctx.cfgUpdate.desktopClasspath));
		htmlList.setModel(new AutoListModel<ClasspathEntry>(Ctx.cfgUpdate.htmlClasspath));
		gwtList.setModel(new AutoListModel<GwtModule>(Ctx.cfgUpdate.gwtModules));

		coreList.setCellRenderer(classpathListCellRenderer);
		androidList.setCellRenderer(classpathListCellRenderer);
		desktopList.setCellRenderer(classpathListCellRenderer);
		htmlList.setCellRenderer(classpathListCellRenderer);
		gwtList.setCellRenderer(modulesListCellRenderer);

		backBtn.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				mainPanel.hideGenerationUpdatePanel();
			}
		});

 		deleteBtn.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				delete();
			}
		});

 		validateBtn.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				mainPanel.launchUpdateProcess();
			}
		});
  }

	private void delete() {
		for (Object o : coreList.getSelectedValues()) {
			ClasspathEntry e = (ClasspathEntry) o;
			if (!e.added && !e.overwritten) Ctx.cfgUpdate.coreClasspath.remove(e);
		}

		for (Object o : androidList.getSelectedValues()) {
			ClasspathEntry e = (ClasspathEntry) o;
			if (!e.added && !e.overwritten) Ctx.cfgUpdate.androidClasspath.remove(e);
		}

		for (Object o : desktopList.getSelectedValues()) {
			ClasspathEntry e = (ClasspathEntry) o;
			if (!e.added && !e.overwritten) Ctx.cfgUpdate.desktopClasspath.remove(e);
		}

		for (Object o : htmlList.getSelectedValues()) {
			ClasspathEntry e = (ClasspathEntry) o;
			if (!e.added && !e.overwritten) Ctx.cfgUpdate.htmlClasspath.remove(e);
		}

		for (Object o : gwtList.getSelectedValues()) {
			GwtModule m = (GwtModule) o;
			if (!m.added && !m.overwritten) Ctx.cfgUpdate.gwtModules.remove(m);
		}

		coreList.clearSelection();
		androidList.clearSelection();
		desktopList.clearSelection();
		htmlList.clearSelection();
		gwtList.clearSelection();
	}

	// -------------------------------------------------------------------------
	// List renderer
	// -------------------------------------------------------------------------

	private final ListCellRenderer classpathListCellRenderer = new DefaultListCellRenderer() {
		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			ClasspathEntry entryPath = (ClasspathEntry) value;

			label.setBorder(BorderFactory.createEmptyBorder(3, 6, 3, 6));
			label.setText(entryPath.path);

			if (entryPath.overwritten) {
				label.setForeground(new Color(0x3D5277));
			} else if (entryPath.added) {
				label.setForeground(new Color(0x008800));
			} else {
				label.setForeground(new Color(0xD1B40F));
			}

			return label;
		}
	};

	private final ListCellRenderer modulesListCellRenderer = new DefaultListCellRenderer() {
		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			GwtModule module = (GwtModule) value;

			label.setBorder(BorderFactory.createEmptyBorder(3, 6, 3, 6));
			label.setText(module.name);

			if (module.overwritten) {
				label.setForeground(new Color(0x3D5277));
			} else if (module.added) {
				label.setForeground(new Color(0x008800));
			} else {
				label.setForeground(new Color(0xD1B40F));
			}

			return label;
		}
	};

	// -------------------------------------------------------------------------
	// Generated stuff
	// -------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        paintedPanel1 = new aurelienribon.ui.components.PaintedPanel();
        jLabel2 = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        coreList = new javax.swing.JList();
        jLabel1 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane6 = new javax.swing.JScrollPane();
        htmlList = new javax.swing.JList();
        jLabel6 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        androidList = new javax.swing.JList();
        jLabel5 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        desktopList = new javax.swing.JList();
        jLabel4 = new javax.swing.JLabel();
        jPanel8 = new javax.swing.JPanel();
        jScrollPane7 = new javax.swing.JScrollPane();
        gwtList = new javax.swing.JList();
        jLabel7 = new javax.swing.JLabel();
        validateBtn = new aurelienribon.ui.components.Button();
        backBtn = new aurelienribon.ui.components.Button();
        deleteBtn = new aurelienribon.ui.components.Button();

        jLabel2.setText("<html><b>Legend</b>\n<br/>\n<font color=\"#3D5277\"><b>Blue</b></font> is an element that will be updated, <font color=\"#008800\"><b>green</b></font> is a new element (you selected a new library), and <font color=\"#D1B40F\"><b>orange</b></font> is an element that is not updated, or that is unknown.\n<br/><br/>\nPlease review your classpaths before proceeding. Specifically, you should look at the orange entries, and remove those that are not needed in your project. When updating a project, some libraries may have changed their names, leaving old entries undesirable.");
        jLabel2.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        javax.swing.GroupLayout paintedPanel1Layout = new javax.swing.GroupLayout(paintedPanel1);
        paintedPanel1.setLayout(paintedPanel1Layout);
        paintedPanel1Layout.setHorizontalGroup(
            paintedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(paintedPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addContainerGap())
        );
        paintedPanel1Layout.setVerticalGroup(
            paintedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(paintedPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addContainerGap())
        );

        jPanel7.setOpaque(false);
        jPanel7.setLayout(new java.awt.GridLayout(1, 2, 10, 0));

        jPanel6.setOpaque(false);
        jPanel6.setLayout(new java.awt.GridLayout(2, 1, 0, 10));

        jPanel1.setOpaque(false);

        coreList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane2.setViewportView(coreList);

        jLabel1.setText("Core project classpath");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addContainerGap(164, Short.MAX_VALUE))
            .addComponent(jScrollPane2)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addGap(0, 0, 0)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 91, Short.MAX_VALUE))
        );

        jPanel6.add(jPanel1);

        jPanel4.setOpaque(false);

        htmlList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane6.setViewportView(htmlList);

        jLabel6.setText("Html project classpath");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jLabel6)
                .addContainerGap())
            .addComponent(jScrollPane6)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jLabel6)
                .addGap(0, 0, 0)
                .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 91, Short.MAX_VALUE))
        );

        jPanel6.add(jPanel4);

        jPanel7.add(jPanel6);

        jPanel5.setOpaque(false);
        jPanel5.setLayout(new java.awt.GridLayout(3, 1, 0, 10));

        jPanel3.setOpaque(false);

        androidList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane5.setViewportView(androidList);

        jLabel5.setText("Android project classpath");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jLabel5)
                .addContainerGap())
            .addComponent(jScrollPane5)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jLabel5)
                .addGap(0, 0, 0)
                .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 53, Short.MAX_VALUE))
        );

        jPanel5.add(jPanel3);

        jPanel2.setOpaque(false);

        desktopList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane4.setViewportView(desktopList);

        jLabel4.setText("Desktop project classpath");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jLabel4)
                .addContainerGap())
            .addComponent(jScrollPane4)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jLabel4)
                .addGap(0, 0, 0)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 53, Short.MAX_VALUE))
        );

        jPanel5.add(jPanel2);

        jPanel8.setOpaque(false);

        gwtList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane7.setViewportView(gwtList);

        jLabel7.setText("GWT modules");

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addComponent(jLabel7)
                .addContainerGap())
            .addComponent(jScrollPane7)
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addComponent(jLabel7)
                .addGap(0, 0, 0)
                .addComponent(jScrollPane7, javax.swing.GroupLayout.DEFAULT_SIZE, 53, Short.MAX_VALUE))
        );

        jPanel5.add(jPanel8);

        jPanel7.add(jPanel5);

        validateBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/gfx/ic_ok.png"))); // NOI18N
        validateBtn.setText("Launch!");

        backBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/gfx/ic_back.png"))); // NOI18N
        backBtn.setText("Back");

        deleteBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/gfx/ic_delete.png"))); // NOI18N
        deleteBtn.setText("<html> Delete selected <font color=\"#D1B40F\"><b>unknown</b></font> element(s)");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(paintedPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(deleteBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(validateBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(backBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, 555, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(backBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(validateBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(deleteBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(paintedPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList androidList;
    private aurelienribon.ui.components.Button backBtn;
    private javax.swing.JList coreList;
    private aurelienribon.ui.components.Button deleteBtn;
    private javax.swing.JList desktopList;
    private javax.swing.JList gwtList;
    private javax.swing.JList htmlList;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private aurelienribon.ui.components.PaintedPanel paintedPanel1;
    private aurelienribon.ui.components.Button validateBtn;
    // End of variables declaration//GEN-END:variables

}

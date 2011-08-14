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
package com.badlogic.gdx.box2deditor.ui;

import com.badlogic.gdx.box2deditor.AppContext;
import com.badlogic.gdx.box2deditor.renderpanel.App;
import com.badlogic.gdx.backends.lwjgl.LwjglCanvas;
import com.badlogic.gdx.math.Vector2;
import java.awt.BorderLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author Aurelien Ribon (aurelien.ribon@gmail.com)
 */
public class MainWindow extends javax.swing.JFrame {
	private final DefaultListModel assetsListModel;

	// -------------------------------------------------------------------------

    public MainWindow(final LwjglCanvas glCanvas) {
        initComponents();
		renderPanel.add(glCanvas.getCanvas(), BorderLayout.CENTER);

		assetsListModel = new DefaultListModel();
		assets_assetList.setModel(assetsListModel);

		addComponentListener(componentListener);
    }

	// -------------------------------------------------------------------------

	private final ComponentListener componentListener = new ComponentAdapter() {
		@Override
		public void componentShown(ComponentEvent e) {
			File outputFile = AppContext.instance().outputFile;
			if (outputFile != null)
				setOutputFile(outputFile, true);
		}
	};

	// -------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        shapeModeBtnGrp = new javax.swing.ButtonGroup();
        renderPanel = new javax.swing.JPanel();
        westPanel = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        assets_assetListScrollPane = new javax.swing.JScrollPane();
        assets_assetList = new javax.swing.JList();
        assets_removeAssetBtn = new javax.swing.JButton();
        jPanel11 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        help_assetsLbl = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        init_addAssetsByFilesBtn = new javax.swing.JButton();
        init_setOutputFileBtn = new javax.swing.JButton();
        init_outputFileLbl = new javax.swing.JTextField();
        jSeparator3 = new javax.swing.JSeparator();
        jPanel9 = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        help_configurationLbl = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        export_saveBtn = new javax.swing.JButton();
        jPanel10 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        help_exportLbl = new javax.swing.JLabel();
        eastPanel = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        jPanel8 = new javax.swing.JPanel();
        shape_drawAssetChk = new javax.swing.JCheckBox();
        shape_drawAssetOpacity50Chk = new javax.swing.JCheckBox();
        shape_drawShapeChk = new javax.swing.JCheckBox();
        shape_drawPolysChk = new javax.swing.JCheckBox();
        jPanel2 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        help_optionsLbl = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jPanel16 = new javax.swing.JPanel();
        jLabel20 = new javax.swing.JLabel();
        help_toolsLbl = new javax.swing.JLabel();
        tools_insertPointsBtn = new javax.swing.JButton();
        tools_removePointsBtn = new javax.swing.JButton();
        tools_clearPointsBtn = new javax.swing.JButton();
        jPanel17 = new javax.swing.JPanel();
        jPanel18 = new javax.swing.JPanel();
        jLabel22 = new javax.swing.JLabel();
        help_hintsLbl = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        renderPanel.setLayout(new java.awt.BorderLayout());
        getContentPane().add(renderPanel, java.awt.BorderLayout.CENTER);

        westPanel.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 0, 1, new java.awt.Color(0, 0, 0)));

        assets_assetList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        assets_assetList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                assets_assetListValueChanged(evt);
            }
        });
        assets_assetListScrollPane.setViewportView(assets_assetList);

        assets_removeAssetBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/aurelienribon/box2deditor/gfx/ic_delete.png"))); // NOI18N
        assets_removeAssetBtn.setText("Remove selected asset(s)");
        assets_removeAssetBtn.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        assets_removeAssetBtn.setMargin(new java.awt.Insets(2, 3, 2, 2));
        assets_removeAssetBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                assets_removeAssetBtnActionPerformed(evt);
            }
        });

        jPanel11.setBackground(new java.awt.Color(102, 102, 102));
        jPanel11.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));

        jLabel9.setForeground(new java.awt.Color(255, 255, 255));
        jLabel9.setText("Assets");

        help_assetsLbl.setIcon(new javax.swing.ImageIcon(getClass().getResource("/aurelienribon/box2deditor/gfx/ic_help.png"))); // NOI18N
        help_assetsLbl.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                help_assetsLblMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addComponent(jLabel9)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 145, Short.MAX_VALUE)
                .addComponent(help_assetsLbl))
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel9)
            .addComponent(help_assetsLbl)
        );

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(assets_removeAssetBtn, javax.swing.GroupLayout.DEFAULT_SIZE, 183, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(assets_assetListScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 183, Short.MAX_VALUE)
                .addContainerGap())
            .addComponent(jPanel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(assets_assetListScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 365, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(assets_removeAssetBtn)
                .addContainerGap())
        );

        init_addAssetsByFilesBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/aurelienribon/box2deditor/gfx/ic_add.png"))); // NOI18N
        init_addAssetsByFilesBtn.setText("Add assets by image files or dirs");
        init_addAssetsByFilesBtn.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        init_addAssetsByFilesBtn.setMargin(new java.awt.Insets(2, 3, 2, 2));
        init_addAssetsByFilesBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                init_addAssetsByFilesBtnActionPerformed(evt);
            }
        });

        init_setOutputFileBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/aurelienribon/box2deditor/gfx/ic_gear.png"))); // NOI18N
        init_setOutputFileBtn.setText("Set / load output file");
        init_setOutputFileBtn.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        init_setOutputFileBtn.setMargin(new java.awt.Insets(2, 3, 2, 2));
        init_setOutputFileBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                init_setOutputFileBtnActionPerformed(evt);
            }
        });

        init_outputFileLbl.setEditable(false);
        init_outputFileLbl.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        init_outputFileLbl.setText("<no file specified>");

        jSeparator3.setForeground(new java.awt.Color(102, 102, 102));

        jPanel9.setBackground(new java.awt.Color(102, 102, 102));
        jPanel9.setLayout(new java.awt.BorderLayout());

        jPanel7.setBackground(new java.awt.Color(102, 102, 102));
        jPanel7.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));

        jLabel5.setForeground(new java.awt.Color(255, 255, 255));
        jLabel5.setText("Configuration");

        help_configurationLbl.setIcon(new javax.swing.ImageIcon(getClass().getResource("/aurelienribon/box2deditor/gfx/ic_help.png"))); // NOI18N
        help_configurationLbl.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                help_configurationLblMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 112, Short.MAX_VALUE)
                .addComponent(help_configurationLbl))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel5)
            .addComponent(help_configurationLbl)
        );

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSeparator3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 203, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(init_addAssetsByFilesBtn, javax.swing.GroupLayout.DEFAULT_SIZE, 183, Short.MAX_VALUE)
                    .addComponent(init_setOutputFileBtn, javax.swing.GroupLayout.DEFAULT_SIZE, 183, Short.MAX_VALUE)
                    .addComponent(init_outputFileLbl, javax.swing.GroupLayout.DEFAULT_SIZE, 183, Short.MAX_VALUE))
                .addContainerGap())
            .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, 203, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(init_setOutputFileBtn)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(init_outputFileLbl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(init_addAssetsByFilesBtn)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel5Layout.createSequentialGroup()
                    .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(150, Short.MAX_VALUE)))
        );

        export_saveBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/aurelienribon/box2deditor/gfx/ic_export.png"))); // NOI18N
        export_saveBtn.setText("Save");
        export_saveBtn.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        export_saveBtn.setMargin(new java.awt.Insets(2, 3, 2, 2));
        export_saveBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                export_saveBtnActionPerformed(evt);
            }
        });

        jPanel10.setBackground(new java.awt.Color(102, 102, 102));
        jPanel10.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));

        jLabel7.setForeground(new java.awt.Color(255, 255, 255));
        jLabel7.setText("Export");

        help_exportLbl.setIcon(new javax.swing.ImageIcon(getClass().getResource("/aurelienribon/box2deditor/gfx/ic_help.png"))); // NOI18N
        help_exportLbl.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                help_exportLblMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 145, Short.MAX_VALUE)
                .addComponent(help_exportLbl))
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel7)
            .addComponent(help_exportLbl)
        );

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(export_saveBtn, javax.swing.GroupLayout.DEFAULT_SIZE, 183, Short.MAX_VALUE)
                .addContainerGap())
            .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(export_saveBtn)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout westPanelLayout = new javax.swing.GroupLayout(westPanel);
        westPanel.setLayout(westPanelLayout);
        westPanelLayout.setHorizontalGroup(
            westPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        westPanelLayout.setVerticalGroup(
            westPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(westPanelLayout.createSequentialGroup()
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        getContentPane().add(westPanel, java.awt.BorderLayout.WEST);

        eastPanel.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 1, 0, 0, new java.awt.Color(0, 0, 0)));

        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel10.setIcon(new javax.swing.ImageIcon(getClass().getResource("/aurelienribon/box2deditor/gfx/logo.gif"))); // NOI18N

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, 187, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel10)
                .addContainerGap(13, Short.MAX_VALUE))
        );

        shape_drawAssetChk.setSelected(true);
        shape_drawAssetChk.setText("Draw asset");
        shape_drawAssetChk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                shape_drawAssetChkActionPerformed(evt);
            }
        });

        shape_drawAssetOpacity50Chk.setText("...with opacity at 50%");
        shape_drawAssetOpacity50Chk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                shape_drawAssetOpacity50ChkActionPerformed(evt);
            }
        });

        shape_drawShapeChk.setSelected(true);
        shape_drawShapeChk.setText("Draw shape");
        shape_drawShapeChk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                shape_drawShapeChkActionPerformed(evt);
            }
        });

        shape_drawPolysChk.setSelected(true);
        shape_drawPolysChk.setText("Draw convex polygons");
        shape_drawPolysChk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                shape_drawPolysChkActionPerformed(evt);
            }
        });

        jPanel2.setBackground(new java.awt.Color(102, 102, 102));
        jPanel2.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));

        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setText("Options");

        help_optionsLbl.setIcon(new javax.swing.ImageIcon(getClass().getResource("/aurelienribon/box2deditor/gfx/ic_help.png"))); // NOI18N
        help_optionsLbl.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                help_optionsLblMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 144, Short.MAX_VALUE)
                .addComponent(help_optionsLbl))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel3)
            .addComponent(help_optionsLbl)
        );

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addComponent(shape_drawAssetOpacity50Chk))
                    .addComponent(shape_drawAssetChk)
                    .addComponent(shape_drawShapeChk)
                    .addComponent(shape_drawPolysChk))
                .addContainerGap(45, Short.MAX_VALUE))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(shape_drawAssetChk)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(shape_drawAssetOpacity50Chk)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(shape_drawShapeChk)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(shape_drawPolysChk)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel16.setBackground(new java.awt.Color(102, 102, 102));
        jPanel16.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));

        jLabel20.setForeground(new java.awt.Color(255, 255, 255));
        jLabel20.setText("Edition tools");

        help_toolsLbl.setIcon(new javax.swing.ImageIcon(getClass().getResource("/aurelienribon/box2deditor/gfx/ic_help.png"))); // NOI18N
        help_toolsLbl.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                help_toolsLblMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel16Layout = new javax.swing.GroupLayout(jPanel16);
        jPanel16.setLayout(jPanel16Layout);
        jPanel16Layout.setHorizontalGroup(
            jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel16Layout.createSequentialGroup()
                .addComponent(jLabel20)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 123, Short.MAX_VALUE)
                .addComponent(help_toolsLbl))
        );
        jPanel16Layout.setVerticalGroup(
            jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel20)
            .addComponent(help_toolsLbl)
        );

        tools_insertPointsBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/aurelienribon/box2deditor/gfx/ic_add.png"))); // NOI18N
        tools_insertPointsBtn.setText("Insert point(s)");
        tools_insertPointsBtn.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        tools_insertPointsBtn.setMargin(new java.awt.Insets(2, 3, 2, 2));
        tools_insertPointsBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tools_insertPointsBtnActionPerformed(evt);
            }
        });

        tools_removePointsBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/aurelienribon/box2deditor/gfx/ic_remove.png"))); // NOI18N
        tools_removePointsBtn.setText("Remove point(s)");
        tools_removePointsBtn.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        tools_removePointsBtn.setMargin(new java.awt.Insets(2, 3, 2, 2));
        tools_removePointsBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tools_removePointsBtnActionPerformed(evt);
            }
        });

        tools_clearPointsBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/aurelienribon/box2deditor/gfx/ic_delete.png"))); // NOI18N
        tools_clearPointsBtn.setText("Clear all points");
        tools_clearPointsBtn.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        tools_clearPointsBtn.setMargin(new java.awt.Insets(2, 3, 2, 2));
        tools_clearPointsBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tools_clearPointsBtnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel16, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tools_insertPointsBtn, javax.swing.GroupLayout.DEFAULT_SIZE, 187, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tools_removePointsBtn, javax.swing.GroupLayout.DEFAULT_SIZE, 187, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tools_clearPointsBtn, javax.swing.GroupLayout.DEFAULT_SIZE, 187, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel16, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(tools_insertPointsBtn)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tools_removePointsBtn)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tools_clearPointsBtn)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel18.setBackground(new java.awt.Color(102, 102, 102));
        jPanel18.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));

        jLabel22.setForeground(new java.awt.Color(255, 255, 255));
        jLabel22.setText("Hints");

        help_hintsLbl.setIcon(new javax.swing.ImageIcon(getClass().getResource("/aurelienribon/box2deditor/gfx/ic_help.png"))); // NOI18N
        help_hintsLbl.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                help_hintsLblMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel18Layout = new javax.swing.GroupLayout(jPanel18);
        jPanel18.setLayout(jPanel18Layout);
        jPanel18Layout.setHorizontalGroup(
            jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel18Layout.createSequentialGroup()
                .addComponent(jLabel22)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 157, Short.MAX_VALUE)
                .addComponent(help_hintsLbl))
        );
        jPanel18Layout.setVerticalGroup(
            jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel22)
            .addComponent(help_hintsLbl)
        );

        jLabel1.setForeground(new java.awt.Color(102, 102, 102));
        jLabel1.setText("<html>\n\n1 &bull; Set the output file,<br/>\n2 &bull; Add some assets,<br/>\n3 &bull; Select an asset and define its collision shapes (you can define multiple shapes).<br/><br/>\n\n&bull; <font color=\"#00AA00\">Create a shape</font> with <font color=\"#0000FF\">ctrl + left clics</font><br/>\n&bull; <font color=\"#00AA00\">Edit a shape</font> by <font color=\"#0000FF\">dragging the left mouse button</font><br/>\n&bull; <font color=\"#00AA00\">Test collisions</font> with <font color=\"#0000FF\">shift + dragging the left mouse button</font><br/><br/>\n\n&bull; <font color=\"#00AA00\">Zoom</font> by<font color=\"#0000FF\"> scrolling</font><br/>\n&bull; <font color=\"#00AA00\">Pan</font> by <font color=\"#0000FF\">dragging the right mouse button</font><br/>\n\n");
        jLabel1.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        javax.swing.GroupLayout jPanel17Layout = new javax.swing.GroupLayout(jPanel17);
        jPanel17.setLayout(jPanel17Layout);
        jPanel17Layout.setHorizontalGroup(
            jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel18, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel17Layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 187, Short.MAX_VALUE)
                .addGap(10, 10, 10))
        );
        jPanel17Layout.setVerticalGroup(
            jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel17Layout.createSequentialGroup()
                .addComponent(jPanel18, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 265, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout eastPanelLayout = new javax.swing.GroupLayout(eastPanel);
        eastPanel.setLayout(eastPanelLayout);
        eastPanelLayout.setHorizontalGroup(
            eastPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel17, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        eastPanelLayout.setVerticalGroup(
            eastPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(eastPanelLayout.createSequentialGroup()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel17, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(104, Short.MAX_VALUE))
        );

        getContentPane().add(eastPanel, java.awt.BorderLayout.EAST);

        pack();
    }// </editor-fold>//GEN-END:initComponents

	private void init_addAssetsByFilesBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_init_addAssetsByFilesBtnActionPerformed
		if (AppContext.instance().outputFile == null) {
			JOptionPane.showMessageDialog(this, "Output file has not been set yet.");
			return;
		}
		
		String[] assets = promptAssetsByFiles();
		if (assets != null)
			for (String asset : assets)
				addAsset(asset, true);
	}//GEN-LAST:event_init_addAssetsByFilesBtnActionPerformed

	private void init_setOutputFileBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_init_setOutputFileBtnActionPerformed
		File file = promptOutputFile();
		if (file != null)
			setOutputFile(file, false);
	}//GEN-LAST:event_init_setOutputFileBtnActionPerformed

	private void export_saveBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_export_saveBtnActionPerformed
		File outputFile = AppContext.instance().outputFile;
		if (outputFile == null) {
			JOptionPane.showMessageDialog(this, "Output file has not been set yet.");
			return;
		}

		try {			
			AppContext.instance().exportToFile();
			JOptionPane.showMessageDialog(this, "Save successfully done !");

			int idx = assets_assetList.getSelectedIndex();
			loadAssets();
			assets_assetList.setSelectedIndex(idx);

		} catch (IOException ex) {
			JOptionPane.showMessageDialog(this, "Something went wrong while writing the file, sorry :/"
				+ "\n(nah, don't expect more details)");
		}
	}//GEN-LAST:event_export_saveBtnActionPerformed

	private void assets_assetListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_assets_assetListValueChanged
		AppContext.instance().clearTempObjects();
		AppContext.instance().setCurrentName(null);
		App.instance().clearAsset();

		if (assets_assetList.isSelectionEmpty())
			return;

		int idx = assets_assetList.getSelectedIndex();
		if (isAssetValid(idx)) {
			String name = (String) assetsListModel.get(idx);
			String path = AppContext.instance().getFullPath(name);

			Vector2 size = App.instance().setAssetByFile(path);
			AppContext.instance().setCurrentSize(size);
			AppContext.instance().setCurrentName(name);
			AppContext.instance().loadCurrentModel();
		}
	}//GEN-LAST:event_assets_assetListValueChanged

	private void assets_removeAssetBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_assets_removeAssetBtnActionPerformed
		if (!assets_assetList.isSelectionEmpty()) {
			int[] idxs = assets_assetList.getSelectedIndices();
			Arrays.sort(idxs);
			for (int i=idxs.length-1; i>=0; i--)
				removeAsset(idxs[i]);
		}
	}//GEN-LAST:event_assets_removeAssetBtnActionPerformed

	private void shape_drawShapeChkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_shape_drawShapeChkActionPerformed
		AppContext.instance().isShapeDrawn = shape_drawShapeChk.isSelected();
	}//GEN-LAST:event_shape_drawShapeChkActionPerformed

	private void shape_drawPolysChkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_shape_drawPolysChkActionPerformed
		AppContext.instance().arePolyDrawn = shape_drawPolysChk.isSelected();
	}//GEN-LAST:event_shape_drawPolysChkActionPerformed

	private void shape_drawAssetChkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_shape_drawAssetChkActionPerformed
		AppContext.instance().isAssetDrawn = shape_drawAssetChk.isSelected();
	}//GEN-LAST:event_shape_drawAssetChkActionPerformed

	private void shape_drawAssetOpacity50ChkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_shape_drawAssetOpacity50ChkActionPerformed
		AppContext.instance().isAssetDrawnWithOpacity50 = shape_drawAssetOpacity50Chk.isSelected();
	}//GEN-LAST:event_shape_drawAssetOpacity50ChkActionPerformed

	private void tools_insertPointsBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tools_insertPointsBtnActionPerformed
		AppContext.instance().insertPointBetweenSelected();
	}//GEN-LAST:event_tools_insertPointsBtnActionPerformed

	private void tools_removePointsBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tools_removePointsBtnActionPerformed
		AppContext.instance().removeSelectedPoints();
	}//GEN-LAST:event_tools_removePointsBtnActionPerformed

	private void tools_clearPointsBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tools_clearPointsBtnActionPerformed
		AppContext.instance().clearTempObjects();
	}//GEN-LAST:event_tools_clearPointsBtnActionPerformed

	private void help_configurationLblMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_help_configurationLblMouseClicked
		JOptionPane.showMessageDialog(this, 
			"<html>"
			+ "First, set the output file to either an existing file or a new file. If you choose an existing file,<br/>"
			+ "you will be prompted to load its content over the current one (if any).<br/><br/>"
			+ ""
			+ "Then, add some image assets (png, jpg...) and start drawing your shapes !<br/><br/>"
			+ ""
			+ "<i><u>Note</u>: Your assets will be referenced relatively to the location of the output file. If you move them<br/>"
			+ "away, the editor won't be able to load them correctly thereafter.</i>"
		);
	}//GEN-LAST:event_help_configurationLblMouseClicked

	private void help_exportLblMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_help_exportLblMouseClicked
		JOptionPane.showMessageDialog(this,
			"<html>"
			+ "Saving your work to the output file will erase its content and replace it with every shape you drew.<br/>"
			+ "Note that assets without any shape defined won't be saved.<br/><br/>"
			+ ""
			+ "Also, once you saved, the asset list is cleared and the output file is loaded back, to assure you that<br/>"
			+ "everything went well."
		);
	}//GEN-LAST:event_help_exportLblMouseClicked

	private void help_assetsLblMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_help_assetsLblMouseClicked
		JOptionPane.showMessageDialog(this,
			"<html>"
			+ "Each asset from the list let you define a collision mask by creating one or more contour shape.<br/><br/>"
			+ ""
			+ "<i><u>Note</u>: The names of the asset that appear in the list are the keys you will have to use in the<br/>"
			+ "FixtureAtlas.</i>"
		);
	}//GEN-LAST:event_help_assetsLblMouseClicked

	private void help_optionsLblMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_help_optionsLblMouseClicked
		JOptionPane.showMessageDialog(this,
			"<html>"
			+ "Options speak for themselves, don't they ? :)"
		);
	}//GEN-LAST:event_help_optionsLblMouseClicked

	private void help_toolsLblMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_help_toolsLblMouseClicked
		JOptionPane.showMessageDialog(this,
			"<html>"
			+ "Edition tools can only be used when you defined at least one shape for the current asset.<br/><br/>"
			+ ""
			+ "&bull; <u>Insert points</u>: select two or more adjacent points to insert new point(s) between them.<br/>"
			+ "&bull; <u>Remove points</u>: removes the selected points. Be careful because you can end up with shapes<br/>"
			+ "with less than 3 vertices.<br/>"
			+ "&bull; <u>Clear all points</u>: removes every shape from the current asset."
		);
	}//GEN-LAST:event_help_toolsLblMouseClicked

	private void help_hintsLblMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_help_hintsLblMouseClicked
		JOptionPane.showMessageDialog(this,
			"<html>"
			+ "Main help section that I want everyone to see."
		);
	}//GEN-LAST:event_help_hintsLblMouseClicked

	// -------------------------------------------------------------------------

	private static final String UNKNOWN_PREFIX = "[NOT FOUND] ";

	private void setOutputFile(File file, boolean force) {
		File oldFile = AppContext.instance().outputFile;
		AppContext.instance().outputFile = file;
		init_outputFileLbl.setText(file.getPath());

		if (oldFile != null)
			updateAssets(oldFile.getParent(), file.getParent());

		if (file.exists()) {
			if (!force) {
				int answer = JOptionPane.showConfirmDialog(this,
				"Selected file already exists. Do you want to load its content ?"
				+ "\nLoaded content will replace the current one.",
				"", JOptionPane.YES_NO_OPTION);

				if (answer == JOptionPane.YES_OPTION)
					loadAssets();

			} else {
				loadAssets();
			}
		}
	}

	private void addAsset(String name, boolean absolutePath) {
		String newName = absolutePath
			? AppContext.instance().getPathRelativeToOutputFile(name)
			: name;

		if (newName == null)
			return;

		String path = absolutePath
			? name
			: AppContext.instance().getFullPath(name);

		File file = new File(path);

		if (file.exists()) {
			if (!assetsListModel.contains(newName))
				assetsListModel.addElement(newName);
			AppContext.instance().addModel(newName);
		} else {
			if (!assetsListModel.contains(UNKNOWN_PREFIX + newName))
				assetsListModel.addElement(UNKNOWN_PREFIX + newName);
			AppContext.instance().addModel(newName);
		}
	}

	private void removeAsset(int idx) {
		String oldName = (String) assetsListModel.get(idx);
		AppContext.instance().removeModel(oldName);

		assetsListModel.remove(idx);
		if (assetsListModel.size() > idx)
			assets_assetList.setSelectedIndex(idx);
		else if (assetsListModel.size() > 0)
			assets_assetList.setSelectedIndex(idx-1);
	}

	private boolean isAssetValid(int idx) {
		String name = (String) assetsListModel.get(idx);
		if (name.startsWith(UNKNOWN_PREFIX))
			return false;

		String path = AppContext.instance().getFullPath(name);
		File file = new File(path);

		if (!file.exists()) {
			assetsListModel.set(idx, UNKNOWN_PREFIX + name);
			return false;
		}

		return true;
	}

	private void updateAssets(String oldRoot, String newRoot) {
		for (int i=assetsListModel.getSize()-1; i>=0; i--) {
			String oldName = (String) assetsListModel.get(i);

			if (oldName.startsWith(UNKNOWN_PREFIX)) {
				oldName = oldName.substring(UNKNOWN_PREFIX.length());
				File f = new File(newRoot, oldName);
				if (f.exists())
					assetsListModel.set(i, oldName);
			} else {
				String newName = new File(oldRoot, oldName).getPath();
				newName = AppContext.instance().getPathRelativeToOutputFile(newName);

				if (newName != null) {
					assetsListModel.set(i, newName);
					AppContext.instance().changeModelName(oldName, newName);
				} else {
					assetsListModel.set(i, UNKNOWN_PREFIX + oldName);
				}
			}
		}

		int idx = assets_assetList.getSelectedIndex();
		if (idx > -1) {
			assets_assetList.clearSelection();
			assets_assetList.setSelectedIndex(idx);
		}
	}

	private void loadAssets() {
		File outputFile = AppContext.instance().outputFile;
		if (outputFile == null || !outputFile.exists())
			return;

		try {
			AppContext.instance().importFromFile();
			assetsListModel.clear();
			for (String name : AppContext.instance().getModelNames())
				addAsset(name, false);

		} catch (IOException ex) {
			JOptionPane.showMessageDialog(this,
				"Something went wrong while reading the output "
				+ "file, sorry :/"
				+ "\n(nah, don't expect more details)");
		}
	}

	// -------------------------------------------------------------------------

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList assets_assetList;
    private javax.swing.JScrollPane assets_assetListScrollPane;
    private javax.swing.JButton assets_removeAssetBtn;
    private javax.swing.JPanel eastPanel;
    private javax.swing.JButton export_saveBtn;
    private javax.swing.JLabel help_assetsLbl;
    private javax.swing.JLabel help_configurationLbl;
    private javax.swing.JLabel help_exportLbl;
    private javax.swing.JLabel help_hintsLbl;
    private javax.swing.JLabel help_optionsLbl;
    private javax.swing.JLabel help_toolsLbl;
    private javax.swing.JButton init_addAssetsByFilesBtn;
    private javax.swing.JTextField init_outputFileLbl;
    private javax.swing.JButton init_setOutputFileBtn;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel17;
    private javax.swing.JPanel jPanel18;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JPanel renderPanel;
    private javax.swing.ButtonGroup shapeModeBtnGrp;
    private javax.swing.JCheckBox shape_drawAssetChk;
    private javax.swing.JCheckBox shape_drawAssetOpacity50Chk;
    private javax.swing.JCheckBox shape_drawPolysChk;
    private javax.swing.JCheckBox shape_drawShapeChk;
    private javax.swing.JButton tools_clearPointsBtn;
    private javax.swing.JButton tools_insertPointsBtn;
    private javax.swing.JButton tools_removePointsBtn;
    private javax.swing.JPanel westPanel;
    // End of variables declaration//GEN-END:variables


	private final FileFilter imageUiFilter = new FileFilter() {
		@Override public boolean accept(File f) {
			if (f.isDirectory())
				return true;
			return isSameString(getExtension(f), "png", "jpg", "jpeg");
		}

		@Override public String getDescription() {
			return "Image files or directories";
		}
	};

	private final java.io.FileFilter imageFilter = new java.io.FileFilter() {
		@Override
		public boolean accept(File f) {
			if (f.isDirectory())
				return true;
			return isSameString(getExtension(f), "png", "jpg", "jpeg");
		}
	};
	
    private String[] promptAssetsByFiles() {
		String startupPath = AppContext.instance().getRootDirectory();
		JFileChooser chooser = new JFileChooser(startupPath);
		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		chooser.setMultiSelectionEnabled(true);
		chooser.setFileFilter(imageUiFilter);

		if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			List<String> assetPaths = new ArrayList<String>();
			List<File> remainingFiles = new ArrayList<File>();

			Collections.addAll(remainingFiles, chooser.getSelectedFiles());
			int deepness = 0;
			int maxDeepness = 8;

			while (!remainingFiles.isEmpty()) {
				File currentFile = remainingFiles.remove(0);
				if (currentFile.isFile()) {
					assetPaths.add(currentFile.getPath());
				} else if (deepness < maxDeepness) {
					deepness += 1;
					Collections.addAll(remainingFiles, currentFile.listFiles(imageFilter));
				}
			}

			return assetPaths.toArray(new String[assetPaths.size()]);
		}

		return null;
	}

	private File promptOutputFile() {
		File outputFile = AppContext.instance().outputFile;
		File startupDir = outputFile != null ? outputFile.getParentFile() : new File(".");
		if (!startupDir.isDirectory())
			startupDir = new File(".");

		JFileChooser chooser = new JFileChooser(startupDir);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setMultiSelectionEnabled(false);

		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			File selectedFile = chooser.getSelectedFile();
			return selectedFile;
		}

		return null;
	}

	private boolean isSameString(String str1, String... strs) {
		for (String str : strs)
			if (str1.equalsIgnoreCase(str))
				return true;
		return false;
	}

	private String getExtension(File f) {
		int idx = f.getName().lastIndexOf(".");
		if (idx < 0 || idx == f.getName().length()-1)
			return "";
		return f.getName().substring(idx+1);
	}
}
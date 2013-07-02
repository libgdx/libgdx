package com.badlogic.gdx.tools.font;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import com.esotericsoftware.tablelayout.swing.Table;

public class FontPackTool extends JFrame {

	
	public static void main(String[] args) {
		new FontPackTool();
	}
	
	JList fontList;
	AddFontDialog addFontDialog;
	Font[] allFonts;
	String[] fontNames;
//	List<String> fontNamesList = new ArrayList<String>();
	
	public FontPackTool() {
		super("Font Packer");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		GraphicsEnvironment e = GraphicsEnvironment.getLocalGraphicsEnvironment();
		allFonts = e.getAllFonts();
		fontNames = new String[allFonts.length];
		for (int i=0; i<allFonts.length; i++) {
			fontNames[i] = allFonts[i].getFontName();
			//fontNamesList.add(allFonts[i].getFontName());
		}
		
		addFontDialog = new AddFontDialog(this);
		setupGUI();
		
		setSize(500, 400);
		setLocationRelativeTo(null);
		setVisible(true);
		addFontDialog.setVisible(true);
	}
	
	public void setupGUI() {
		Table table = new Table();
		table.top().left().pad(10);
		
		table.addCell(new JLabel("Fonts:")).left().padBottom(5);
		table.row();
		
		Table contentTable = new Table();
		
		fontList = new JList(new Object[] { "Arial (12, 14, 16)", "Arial Bold (12, 14, 16)" });
		JScrollPane scroll = new JScrollPane(fontList, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		
		contentTable.addCell(scroll).size(250, 150);
		
		JButton addFontButton = new JButton("Add");
		addFontButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed (ActionEvent arg0) {
				addFontDialog.setVisible(true);
			}
		});
		
		Table listButtonsTable = new Table();
		listButtonsTable.addCell(addFontButton).left().row();
		listButtonsTable.addCell(new JButton("Edit")).left().row();
		listButtonsTable.addCell(new JButton("Remove")).left();
		
		contentTable.addCell(listButtonsTable).top();
		
		table.addCell(contentTable);
		
		setContentPane(table);
	}
	
	enum Types {
		FreeTypeFont,
		BMFont;
	}
	
	class AddFontDialog extends JDialog {
		
		JComboBox typeSelect;
		JPanel cardPanel;
		CardLayout cardLayout;
		
		JList systemFontsList;
		
		Table freeTypeCard;
		Table bmFontCard;
				
		AddFontDialog(JFrame parent) {
			super(parent);
			setModal(true);
			
			Table table = new Table();
			table.top().left().pad(10);
			
			Table selectTable = new Table();
			selectTable.left().top();
			selectTable.addCell("Type:");
			
			typeSelect = new JComboBox(Types.values());
			typeSelect.addItemListener(new ItemListener() {
				
				@Override
				public void itemStateChanged (ItemEvent e) {
					cardLayout.show(cardPanel, e.getItem().toString());
				}
			});
			selectTable.addCell(typeSelect);
			
			freeTypeCard = new Table();
			bmFontCard = new Table();
			
			//////// FreeTypeFont Card
			
			systemFontsList = new JList(fontNames);
			systemFontsList.setPrototypeCellValue("|XXXXXXXXXXXXXXXX");
			systemFontsList.setCellRenderer(new FontCellRenderer());
			
			JScrollPane scroller = new JScrollPane(systemFontsList);
			freeTypeCard.addCell("System Fonts:").left().padBottom(5).row();
			freeTypeCard.addCell(scroller).size(250, 150);
			freeTypeCard.row();
			
			cardLayout = new CardLayout();
			cardPanel = new JPanel(cardLayout);
			cardPanel.add(freeTypeCard, Types.FreeTypeFont.name());
			cardPanel.add(bmFontCard, Types.BMFont.name());
			
			cardLayout.show(cardPanel, Types.FreeTypeFont.name());
			
			table.addCell(selectTable).left();
			table.row();
			table.addCell(cardPanel).padTop(10);
			
//			pack();
			setSize(350, 250);
			setLocationRelativeTo(parent);
			setContentPane(table);
		}
	}
	
	class FontCellRenderer extends DefaultListCellRenderer {
		public Component getListCellRendererComponent (JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
						
			if (index>=0 && index<allFonts.length) {
				Font f = allFonts[index];
//				setFont(f.deriveFont(16f));
//				setAlignmentY(Component.CENTER_ALIGNMENT);
				
			}
			
			return this;
		}
	}
}


package com.esotericsoftware.tablelayout.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.LayoutManager;

import javax.swing.JPanel;

public class Stack extends JPanel {
	public Stack () {
		super(new LayoutManager() {
			public void layoutContainer (Container parent) {
				int width = parent.getWidth();
				int height = parent.getHeight();
				for (int i = 0, n = parent.getComponentCount(); i < n; i++) {
					parent.getComponent(i).setLocation(0, 0);
					parent.getComponent(i).setSize(width, height);
				}
			}

			public Dimension preferredLayoutSize (Container parent) {
				Dimension size = new Dimension();
				for (int i = 0, n = parent.getComponentCount(); i < n; i++) {
					Dimension pref = parent.getComponent(i).getPreferredSize();
					size.width = Math.max(size.width, pref.width);
					size.height = Math.max(size.height, pref.height);
				}
				return size;
			}

			public Dimension minimumLayoutSize (Container parent) {
				Dimension size = new Dimension();
				for (int i = 0, n = parent.getComponentCount(); i < n; i++) {
					Dimension min = parent.getComponent(i).getMinimumSize();
					size.width = Math.max(size.width, min.width);
					size.height = Math.max(size.height, min.height);
				}
				return size;
			}

			public void addLayoutComponent (String name, Component comp) {
			}

			public void removeLayoutComponent (Component comp) {
			}
		});
	}

	protected void paintChildren (Graphics g) {
		super.paintChildren(g);
	}
}

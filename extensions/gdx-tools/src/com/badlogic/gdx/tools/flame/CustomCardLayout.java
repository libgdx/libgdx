package com.badlogic.gdx.tools.flame;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;

/** @author Inferno */
public class CustomCardLayout extends CardLayout {

	@Override
	public Dimension preferredLayoutSize (Container parent) {
		Component component = getCurrentCard(parent);
		return component != null ? component.getPreferredSize() : super.preferredLayoutSize(parent);
	}
	
	public <K> K getCurrentCard(Container container){
		Component c[] = container.getComponents();
		int i = 0;
		int j = c.length;
		while (i < j) {
			if (c[i].isVisible()) {
				return (K)c[i];
			}
			else
				i ++;
		}
		return null;
	}

}

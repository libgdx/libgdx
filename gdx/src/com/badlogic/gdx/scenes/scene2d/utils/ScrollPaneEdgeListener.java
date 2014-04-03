
package com.badlogic.gdx.scenes.scene2d.utils;

import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;

/** Listen to when a scroll pane scrolls into an edge
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com> */
public interface ScrollPaneEdgeListener {
	/** Called when an edge of a scroll pane has been hit
	 * @param scrollPane the scroll pane that sent the event
	 * @param edge what edge was hit */
	public void hitEdge (ScrollPane scrollPane, Edges edge);

	/** All edges the scroll pane can hit */
	public enum Edges {
		LEFT, RIGHT, TOP, BOTTOM
	}
}

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

package com.badlogic.gdx.path;


/** A Node class that is used when finding a path from one node to another.
 * @author Baseball435 */

public class Node {

	private final int x, y;
	private final Heuristic heuristic;
	
	private int g, h;
	private boolean walkable;
	private Node parent;
	
	/** Sets the node to the x and y values.
	 * @param x the x position.
	 * @param y the y position.
	 * @param heuristic the method to get the Heuristic value for nodes. */
	public Node(int x, int y, Heuristic heuristic) {
		this.x = x;
		this.y = y;
		this.heuristic = heuristic;
	}
	
	/** Calculates the H value of the node given the goal. 
	 * @param goal the ending node of the path. */
	public void calculateH(Node goal) {
		h = heuristic.getCost(this, goal);
	}
	
	/** Calculates the G value of the node */
	public void calculateG() {
		if (parent != null) {
			g += parent.getG() + 1;
		} else {
			g = 0;
		}
	}
	
	/** Calculates the H value given the goal node and the G value. 
	 * @param goal the ending node of the path. */
	public void calculateAll(Node goal) {
		calculateH(goal);
		calculateG();
	}
	
	/** Sets the parent node 
	 * @param node the node
	 */
	public void setParent(Node node) {
		parent = node;
	}
	
	/** Sets the walkable value
	 * @param walkable the walkable value
	 */
	public void setWalkable(boolean walkable) {
		this.walkable = walkable;
	}
	
	/** @return whether the node can be walked on */
	public boolean isWalkable() {
		return walkable;
	}
	
	/** @return the nodes parent. */
	public Node getParent() {
		return parent;
	}
	
	/** @return the G value */
	public int getG() {
		return g;
	}
	
	/** @return the H value */
	public int getH() {
		return h;
	}
	
	/** @return the F value */
	public int getF() {
		return g + h;
	}
	
	/** @return the X position */
	public int getX() {
		return x;
	}

	/** @return the Y position */
	public int getY() {
		return y;
	}
	
}

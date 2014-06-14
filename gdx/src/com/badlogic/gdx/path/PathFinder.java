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

import java.util.Comparator;

import com.badlogic.gdx.utils.Array;

/** A Pathfinding class intended to make pathfinding simple given the width, height, and the node positions that are able to be walked on.
 * @author Baseball435 */

public class PathFinder {

	private Array<Node> open = new Array<Node>();
	private Array<Node> closed = new Array<Node>();
	
	private final Node[][] nodeMap;
	private final int width, height;
	
	/** Sets the node map to the correct width and height and sets the array to the node's default values. 
	 * @param width the width of the map.
	 * @param height the height of the map.*/
	public PathFinder(int width, int height) {
		this.width = width;
		this.height = height;
		nodeMap = new Node[width][height];
		initNodeMap();
	}
	
	/** Sets the node map to the correct width and height and sets the map array to the according to the mutlidimensional boolean array which provides the walkable nodes. 
	 * @param walkables A multidimensional boolean array with the appropriate values set according to the individual nodes that can or can't be walked on.
	 * @param width the width of the map.
	 * @param height the height of the map. */
	public PathFinder(boolean[][] walkables, int width, int height) {
		this(width, height);
		setWalkables(walkables);
	}
	
	/** Sets the entire node map to default values. */
	private void initNodeMap() {
		for (int j = 0; j < height; j++) {
			for (int i = 0; i < width; i++) {
				nodeMap[i][j] = new Node(i, j);
			}
		}
	}
	
	/** Prints out a visual representation of the current node map array. */
	public void printMap() {
		for (int y = 0; y < nodeMap.length; y++) {
			for (int x = 0; x < nodeMap[0].length; x++) {
				System.out.print((nodeMap[x][y].isWalkable() ? 0 : 1) + " ");
			}
			System.out.println("");
		}
	}
	
	/** Sets the individual nodes walkable value according to a multidimensional boolean array.
	 * @param walkables A multidimensional boolean array with the appropriate values set according to the individual nodes that can or can't be walked on. */
	public void setWalkables(boolean[][] walkables) {
		for (int y = 0; y < walkables.length; y++) {
			for (int x = 0; x < walkables[0].length; x++) {
				nodeMap[x][y].setWalkable(walkables[x][y]);
			}
		}
	}
	
	/** @return whether the node is on the closed list */
	private boolean onClosedList(Node node) {
		return closed.contains(node, true);
	}
	
	/** @return whether the node is on the open list */
	private boolean onOpenedList(Node node) {
		return open.contains(node, true);
	}
	
	/** Gets the adjacent nodes that are walkable, aren't on the closed or open list, and are within width and height.
	 * @param node the node to find adjacent nodes from.
	 * @param goal the node that is the end of the path.
	 * @return all of the adjacent nodes. */
	private Array<Node> getAdjacent(Node node, Node goal) {
		Array<Node> ret = new Array<Node>();
		
		int x = node.getX();
		int y = node.getY();
		
		if (x >= 0 && x < width && y + 1 >= 0 && y + 1 < height && nodeMap[x][y + 1].isWalkable() && !onClosedList(nodeMap[x][y + 1]) && !onOpenedList(nodeMap[x][y + 1])) {
			ret.add(nodeMap[x][y + 1]);
		}
		if (x >= 0 && x < width && y - 1 >= 0 && y - 1 < height && nodeMap[x][y - 1].isWalkable() && !onClosedList(nodeMap[x][y - 1]) && !onOpenedList(nodeMap[x][y - 1])) {
			ret.add(nodeMap[x][y - 1]);
		}
		if (x + 1 >= 0 && x + 1 < width && y >= 0 && y < height && nodeMap[x + 1][y].isWalkable() && !onClosedList(nodeMap[x + 1][y]) && !onOpenedList(nodeMap[x + 1][y])) {
			ret.add(nodeMap[x + 1][y]);
		}
		if (x - 1 >= 0 && x - 1 < width && y >= 0 && y < height && nodeMap[x - 1][y].isWalkable() && !onClosedList(nodeMap[x - 1][y]) && !onOpenedList(nodeMap[x - 1][y])) {
			ret.add(nodeMap[x - 1][y]);
		}
		
		for (Node n : ret) {
			n.calculateAll(goal);
			n.setParent(node); 
		}
		
		return ret;
	}
	
	/** Finds a path from the starting node to the goal node 
	 * @param start the beginning of the path node.
	 * @param goal the ending of the path node. 
	 * @return the nodes that create the path */
	public Array<Node> findPath(Node start, Node goal) {
		Array<Node> ret = new Array<Node>();
	
		open.clear();
		closed.clear();
		
		Node finished = null;
		
		open.add(start);
		start.setParent(null);
		
		while (open.size > 0) {
			open.sort(sortF);
			
			Node current = open.first();
			
			if (current.getX() == goal.getX() && current.getY() == goal.getY()) {
				finished = current;
				break;
			}
			
			open.removeValue(current, false);
			closed.add(current);
			open.addAll(getAdjacent(current, goal)); 
		}
		
		if (finished != null) { 
			ret.add(finished);
			Node n = finished; 
			while (n.getParent() != null) { 
				ret.add(n.getParent());
				n = n.getParent();
			}
		}
		
		return ret;
	}
	
	/** Compares the sorted array so that the f value goes from decreasing to increasing */
	private Comparator<Node> sortF = new Comparator<Node>() {
		@Override
		public int compare(Node one, Node two) {
			if (one.getF() == two.getF()) {
				return 0;
			} else if (one.getF() > two.getF()) {
				return 1; 
			}
			return -1; 
		}
	};
	
}


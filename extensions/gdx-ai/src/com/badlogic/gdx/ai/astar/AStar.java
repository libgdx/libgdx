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

package com.badlogic.gdx.ai.astar;

import java.util.Comparator;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Pool.Poolable;

/** Generic path finding A* implementation. It works on any graph structure as long as the nodes implement the {@link AStarNode}
 * interface. Can be pooled or manually reset by calling {@link #reset()}.
 * @author Daniel Holderbaum */
public class AStar<T> implements Poolable {

	private ObjectMap<AStarNode<T>, Float> fValues = new ObjectMap<AStarNode<T>, Float>();
	private ObjectMap<AStarNode<T>, AStarNode<T>> predecessors = new ObjectMap<AStarNode<T>, AStarNode<T>>();
	private Array<AStarNode<T>> open = new Array<AStarNode<T>>();
	private Array<AStarNode<T>> closed = new Array<AStarNode<T>>();
	private Comparator<AStarNode<T>> nodeComparator = new AStarNodeFValueComparator<T>(fValues);
	private AStarNode<T> start;
	private AStarNode<T> target;

	/** Tries to find a path from the given start node to the target node.
	 * @param start The start node.
	 * @param target The target node.
	 * @return The path if one could be found, {@code null} otherwise. */
	public Array<T> findPath (AStarNode<T> start, AStarNode<T> target) {
		if (start.equals(target)) {
			return null;
		}

		this.start = start;
		this.target = target;
		// initialize the open list by adding the start node
		enqueue(start, 0);
		// iterate until either
		// - we found the optimal solution
		// - it's clear that no solution exists
		do {
			// find the node with the lowest F value
			AStarNode<T> currentNode = next();
			// in case we reached the target, we can calculate the path
			if (currentNode.equals(target)) {
				return calculatePath();
			}
			// remove the node from the open list and add it to the closed list
			// so it won't be searched anymore
			closed.add(currentNode);
			open.removeValue(currentNode, false);

			// since we didn't find the target yet, we keep searching and add
			// all neighbours to the open list
			expandNode(currentNode);
		} while (open.size != 0);

		// there is no path from start to target
		return null;
	}

	/** Adds all neighbours of the given node to the open list if they are not yet in the closed list. */
	private void expandNode (AStarNode<T> currentNode) {
		for (AStarNode<T> neighbour : currentNode.getNeighbours()) {
			// if the neighbour is already on the closed list, we skip it
			if (isClosed(neighbour)) {
				continue;
			}

			float tentativeG = g(currentNode) + cost(currentNode, neighbour);
			// if the neighbour is already on the open list, but the g value is
			// not better, we skip it
			if (open.contains(neighbour, false) && tentativeG >= g(neighbour)) {
				continue;
			}

			// remember where we came from
			predecessors.put(neighbour, currentNode);

			// update the F value
			float f = tentativeG + h(neighbour);
			if (open.contains(neighbour, false)) {
				fValues.put(neighbour, f);
			} else {
				enqueue(neighbour, f);
			}
		}
	}

	private float h (AStarNode<T> node) {
		return node.distanceTo(target);
	}

	private float g (AStarNode<T> node) {
		if (node.equals(start)) {
			return 0;
		}

		return g(predecessors.get(node)) + cost(predecessors.get(node), node);
	}

	private float cost (AStarNode<T> node, AStarNode<T> node2) {
		return node.distanceTo(node2);
	}

	private void enqueue (AStarNode<T> node, float f) {
		open.add(node);
		fValues.put(node, f);
		open.sort(nodeComparator);
	}

	private AStarNode<T> next () {
		AStarNode<T> firstNode = open.get(0);
		open.removeIndex(0);
		return firstNode;
	}

	private boolean isClosed (AStarNode<T> node) {
		return closed.contains(node, false);
	}

	/** Runs through the predecessors from target to start. Then reverses this path. */
	private Array<T> calculatePath () {
		Array<T> path = new Array<T>();
		AStarNode<T> current = target;
		while (!predecessors.get(current).equals(start)) {
			path.add(current.cast());
			current = predecessors.get(current);
		}
		path.add(start.cast());
		path.reverse();

		return path;
	}

	@Override
	public void reset () {
		fValues.clear();
		predecessors.clear();
		open.clear();
		closed.clear();
		start = null;
		target = null;
	}

	/** Compares the given nodes via their F values.
	 * @author Daniel Holderbaum */
	private static class AStarNodeFValueComparator<T> implements Comparator<AStarNode<T>> {

		ObjectMap<AStarNode<T>, Float> fValues;

		public AStarNodeFValueComparator (ObjectMap<AStarNode<T>, Float> fValues) {
			this.fValues = fValues;
		}

		@Override
		public int compare (AStarNode<T> o1, AStarNode<T> o2) {
			if (fValues.get(o1) < fValues.get(o2)) {
				return -1;
			} else if (fValues.get(o1) > fValues.get(o2)) {
				return 1;
			} else {
				return 0;
			}
		}
	}
}

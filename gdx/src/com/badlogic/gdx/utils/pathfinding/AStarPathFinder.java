
package com.badlogic.gdx.utils.pathfinding;

import com.badlogic.gdx.utils.BinaryHeap;

/** A path finder that uses the AStar heuristic based algorithm to determine a path.
 * <p>
 * Original implementation by Kevin Glass from Slick2D.
 * </p>
 * @author hneuer */
public class AStarPathFinder implements NavContext, PathFinder {
	/** The set of nodes that we do not yet consider fully searched */
	private final BinaryHeap<AStarAlgoData> openList = new BinaryHeap<AStarAlgoData>();
	/** The graph being searched */
	private final NavGraph graph;
	/** The maximum depth of search we're willing to accept before giving up */
	private final int maxSearchDistance;
	/** The heuristic we're applying to determine which nodes to search first */
	private final AStarHeuristicCalculator heuristicCalculator;

	/** The mover going through the path */
	private Object mover;
	/** The distance searched so far */
	private int distance;
	/** Unique ID for each search run. Used to mark nodes. */
	private int checkedID;
	/** The current source node in the context (part of the NavContext implementation) */
	private NavNode sourceNodeInContext;

	/** Create a path finder with a specific heuristic. */
	public AStarPathFinder (NavGraph graph, int maxSearchDistance, AStarHeuristicCalculator heuristic) {
		this.heuristicCalculator = heuristic;
		this.graph = graph;
		this.maxSearchDistance = maxSearchDistance;
	}

	@Override
	public boolean findPath (Object mover, NavNode startNode, NavNode targetNode, NavPath out) {
		this.mover = mover;
		distance = 0;

		if (isBlocked(targetNode, targetNode)) return false;

		checkedID++;
		if (checkedID < 0) checkedID = 1;

		BinaryHeap<AStarAlgoData> openList = this.openList;
		AStarHeuristicCalculator heuristicCalculator = this.heuristicCalculator;
		int maxSearchDistance = this.maxSearchDistance;

		openList.clear();
		addToOpenList(getAlgoData(startNode));

		AStarAlgoData currentData = null;
		int maxDepth = 0;
		while (maxDepth < maxSearchDistance && openList.size != 0) {
			AStarAlgoData lastData = currentData;
			currentData = openList.pop();
			currentData.open = false;
			distance = currentData.depth;
			currentData.closed = true;

			if (currentData.node == targetNode && lastData != null && !isBlocked(lastData.node, targetNode)) break;

			float currentCost = currentData.cost;
			for (NavNode neighborNode : currentData.node.neighbors) {
				AStarAlgoData neighborData = getAlgoData(neighborNode);
				if (!isBlocked(currentData.node, neighborNode)) {
					sourceNodeInContext = startNode;
					float nextStepCost = currentCost + graph.getCost(this, neighborNode);
					if (nextStepCost < neighborData.cost) {
						if (neighborData.open) {
							openList.remove(neighborData);
							neighborData.open = false;
						}
						neighborData.closed = false;
					}
					if (!neighborData.open && !neighborData.closed) {
						neighborData.cost = nextStepCost;
						neighborData.heuristic = heuristicCalculator.getCost(this, mover, neighborNode, targetNode);
						neighborData.depth = currentData.depth + 1;
						neighborNode.parent = currentData.node;
						maxDepth = Math.max(maxDepth, neighborData.depth);
						addToOpenList(neighborData);
					}
				}
			}
		}

		boolean pathFound = targetNode.parent != null;
		if (pathFound) out.fill(startNode, targetNode);
		return pathFound;
	}

	/** Get the AStar data from the given node and reset it if it has not been used in this run. If it does not exist at all (node
	 * is reached the first time in the very first run) create a new one. */
	private AStarAlgoData getAlgoData (NavNode node) {
		AStarAlgoData ad = (AStarAlgoData)node.algoData;
		if (node.algoData == null) {
			ad = new AStarAlgoData(node);
			node.algoData = ad;
		}

		if (ad.checkedID != checkedID) {
			ad.reset();
			ad.checkedID = checkedID;
		}
		return ad;
	}

	/** Ask the graph if the way from start to target node is blocked. */
	private boolean isBlocked (NavNode startNode, NavNode targetNode) {
		sourceNodeInContext = startNode;
		return graph.blocked(this, targetNode);
	}

	private void addToOpenList (AStarAlgoData node) {
		openList.add(node, node.cost + node.heuristic);
		node.open = true;
	}

	@Override
	public Object getMover () {
		return mover;
	}

	@Override
	public float getSearchDistance () {
		return distance;
	}

	@Override
	public NavNode getSourceNode () {
		return sourceNodeInContext;
	}

	/** The description of a class providing a cost for a given tile based on a target location and entity being moved. This
	 * heuristic controls what priority is placed on different tiles during the search for a path */
	public interface AStarHeuristicCalculator<N extends NavNode> {
		public float getCost (NavContext<N> map, Object mover, N startNode, N targetNode);
	}

	class AStarAlgoData extends BinaryHeap.Node {
		/** Backlink to the node. */
		final NavNode node;
		/** Heuristic from this node to the target. */
		float heuristic;
		/** Search depth to reach this node. */
		int depth;
		/** ID of the current search. */
		int checkedID;
		/** This node's cost. */
		float cost;
		/** In the open list */
		boolean open;
		/** In the closed list */
		boolean closed;

		public AStarAlgoData (NavNode node) {
			super(0);
			this.node = node;
		}

		void reset () {
			closed = false;
			open = false;
			cost = 0;
			depth = 0;
			node.parent = null;
		}
	}
}

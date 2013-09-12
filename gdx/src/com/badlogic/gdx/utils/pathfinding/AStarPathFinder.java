
package com.badlogic.gdx.utils.pathfinding;

import com.badlogic.gdx.utils.BinaryHeap;
import com.badlogic.gdx.utils.BinaryHeap.Node;

/** A path finder that uses the AStar heuristic based algorithm to determine a path.
 * <p>
 * Original implementation by Kevin Glass from Slick2D.
 * </p>
 * @author hneuer */
public class AStarPathFinder implements PathFindingContext, PathFinder {
	/** The default heuristic calculator providing the Manhattan distance. */
	public static final AStarHeuristicCalculator MANHATTAN = new AStarHeuristicCalculator() {
		@Override
		public float getCost (TileBasedMap map, Object mover, int x, int y, int tx, int ty) {
			return Math.abs(tx - x) + Math.abs(ty - y);
		}
	};

	/** A heuristic calculator providing the closest squared distance. */
	public static final AStarHeuristicCalculator CLOSEST_SQUARED = new AStarHeuristicCalculator() {
		@Override
		public float getCost (TileBasedMap map, Object mover, int x, int y, int tx, int ty) {
			int dx = tx - x;
			int dy = ty - y;
			return dx * dx + dy * dy;
		}
	};

	/** A heuristic calculator providing the closest distance. */
	public static final AStarHeuristicCalculator CLOSEST = new AStarHeuristicCalculator() {
		@Override
		public float getCost (TileBasedMap map, Object mover, int x, int y, int tx, int ty) {
			return (float)Math.sqrt(CLOSEST_SQUARED.getCost(map, mover, x, y, tx, ty));
		}
	};

	/** The set of nodes that we do not yet consider fully searched */
	private final BinaryHeap<SearchNode> openList = new BinaryHeap<SearchNode>();
	/** The map being searched */
	private final TileBasedMap map;
	/** Map width in tiles */
	private final int mapWidth;
	/** Map height in tiles */
	private final int mapHeight;
	/** The maximum depth of search we're willing to accept before giving up */
	private final int maxSearchDistance;
	/** The complete set of nodes across the map */
	private final SearchNode[][] nodes;
	/** True if we allow diagonal movement */
	private final boolean allowDiagMovement;
	/** The heuristic we're applying to determine which nodes to search first */
	private final AStarHeuristicCalculator heuristicCalculator;

	/** The node we're currently searching from */
	private SearchNode currentNode;
	/** The mover going through the path */
	private Object mover;
	/** The x coordinate of the source tile we're moving from */
	private int sourceX;
	/** The y coordinate of the source tile we're moving from */
	private int sourceY;
	/** The distance searched so far */
	private int distance;

	/** Create a path finder with the default heuristic - Manhattan distance to target. */
	public AStarPathFinder (TileBasedMap map, int maxSearchDistance, boolean allowDiagMovement) {
		this(map, maxSearchDistance, allowDiagMovement, MANHATTAN);
	}

	/** Create a path finder with a specific heuristic. */
	public AStarPathFinder (TileBasedMap map, int maxSearchDistance, boolean allowDiagMovement, AStarHeuristicCalculator heuristic) {
		this.heuristicCalculator = heuristic;
		this.map = map;
		this.mapWidth = map.getWidthInTiles();
		this.mapHeight = map.getHeightInTiles();
		this.maxSearchDistance = maxSearchDistance;
		this.allowDiagMovement = allowDiagMovement;

		nodes = new SearchNode[mapWidth][mapHeight];
		for (int x = 0, w = mapWidth; x < w; x++)
			for (int y = 0, h = mapHeight; y < h; y++)
				nodes[x][y] = new SearchNode(x, y);
	}

	@Override
	public boolean findPath (Object mover, int sx, int sy, int tx, int ty, NavPath out) {
		this.mover = mover;
		currentNode = null;
		distance = 0;

		if (isBlocked(tx, ty, tx, ty)) return false;

		for (int x = 0, w = mapWidth; x < w; x++)
			for (int y = 0, h = mapHeight; y < h; y++)
				nodes[x][y].reset();

		SearchNode targetNode = nodes[tx][ty];
		openList.clear();
		addToOpenList(nodes[sx][sy]);

		int maxDepth = 0;
		while (maxDepth < maxSearchDistance && openList.size != 0) {
			int lastX = sx;
			int lastY = sy;
			if (currentNode != null) {
				lastX = currentNode.x;
				lastY = currentNode.y;
			}

			currentNode = openList.pop();
			currentNode.open = false;
			distance = currentNode.depth;
			currentNode.closed = true;

			if (currentNode == targetNode && !isBlocked(lastX, lastY, tx, ty)) break;

			int currentX = currentNode.x;
			int currentY = currentNode.y;
			float currentCost = currentNode.cost;
			for (int dx = -1; dx < 2; dx++)
				for (int dy = -1; dy < 2; dy++) {
					if ((dx == 0 && dy == 0) || (!allowDiagMovement && dx != 0 && dy != 0)) continue;
					int neighborX = currentX + dx;
					int neighborY = currentY + dy;
					if (neighborX < 0 || neighborY < 0 || neighborX >= mapWidth || neighborY >= mapHeight) continue;
					if (!isBlocked(currentX, currentY, neighborX, neighborY)) {
						float nextStepCost = currentCost + getMovementCost(currentX, currentY, neighborX, neighborY);
						SearchNode neighbor = nodes[neighborX][neighborY];
						if (nextStepCost < neighbor.cost) {
							if (neighbor.open) {
								openList.remove(neighbor);
								neighbor.open = false;
							}
							neighbor.closed = false;
						}
						if (!neighbor.open && !neighbor.closed) {
							neighbor.cost = nextStepCost;
							neighbor.heuristic = heuristicCalculator.getCost(map, mover, neighborX, neighborY, tx, ty);
							maxDepth = Math.max(maxDepth, neighbor.setParent(currentNode));
							addToOpenList(neighbor);
						}
					}
				}
		}

		boolean pathFound = targetNode.parent != null;
		if (pathFound) {
			out.clear();
			SearchNode startNode = nodes[sx][sy];
			while (targetNode != startNode) {
				out.appendStep(targetNode.x, targetNode.y);
				targetNode = targetNode.parent;
			}
			out.appendStep(sx, sy);
			out.reverse();
		}
		return pathFound;
	}

	private float getMovementCost (int sx, int sy, int tx, int ty) {
		sourceX = sx;
		sourceY = sy;
		return map.getCost(this, tx, ty);
	}

	private void addToOpenList (SearchNode node) {
		openList.add(node, node.cost + node.heuristic);
		node.open = true;
	}

	private boolean isBlocked (int sx, int sy, int tx, int ty) {
		sourceX = sx;
		sourceY = sy;
		return map.blocked(this, tx, ty);
	}

	@Override
	public Object getMover () {
		return mover;
	}

	@Override
	public int getSearchDistance () {
		return distance;
	}

	@Override
	public int getSourceX () {
		return sourceX;
	}

	@Override
	public int getSourceY () {
		return sourceY;
	}

	/** A single node in the search graph */
	class SearchNode extends Node {
		/** The x coordinate of the node */
		final int x;
		/** The y coordinate of the node */
		final int y;
		/** The path cost for this node */
		float cost;
		/** The parent of this node, how we reached it in the search */
		SearchNode parent;
		/** The heuristic cost of this node */
		float heuristic;
		/** The search depth of this node */
		int depth;
		/** In the open list */
		boolean open;
		/** In the closed list */
		boolean closed;

		/** Create a new node for a given position.
		 * 
		 * @param x The x coordinate of the node
		 * @param y The y coordinate of the node */
		SearchNode (int x, int y) {
			super(0);
			this.x = x;
			this.y = y;
		}

		/** Set the parent of this node and calculates the node's search depth.
		 * 
		 * @param parent The parent node which lead us to this node
		 * @return The depth we have now reached in searching */
		int setParent (SearchNode parent) {
			this.parent = parent;
			depth = parent.depth + 1;
			return depth;
		}

		/** Reset the state of this node */
		void reset () {
			closed = false;
			open = false;
			cost = 0;
			depth = 0;
			parent = null;
		}
	}

	/** The description of a class providing a cost for a given tile based on a target location and entity being moved. This
	 * heuristic controls what priority is placed on different tiles during the search for a path */
	public interface AStarHeuristicCalculator {

		/** Get the additional heuristic cost of the given tile. This controls the order in which tiles are searched while attempting
		 * to find a path to the target location. The lower the cost the more likely the tile will be searched.
		 * 
		 * @param map The map on which the path is being found
		 * @param mover The entity that is moving along the path
		 * @param x The x coordinate of the tile being evaluated
		 * @param y The y coordinate of the tile being evaluated
		 * @param tx The x coordinate of the target location
		 * @param ty The y coordinate of the target location
		 * @return The cost associated with the given tile */
		public float getCost (TileBasedMap map, Object mover, int x, int y, int tx, int ty);
	}
}

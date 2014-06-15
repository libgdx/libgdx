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

package com.badlogic.gdx.ai.pathfinding.tiled;

import com.badlogic.gdx.ai.pathfinding.NavPath;
import com.badlogic.gdx.utils.IntArray;

/** Implementation of a navigation path for a tile map. It holds lists of x/y coordinates which represent the tile coordinates that
 * are part of the path.
 * @author hneuer */
public class NavPathTileBased implements NavPath<NavNodeTileBased> {
	public final IntArray x = new IntArray();
	public final IntArray y = new IntArray();

	@Override
	public void fill (NavNodeTileBased startNode, NavNodeTileBased targetNode) {
		x.clear();
		y.clear();
		NavNodeTileBased current = targetNode;
		while (current != startNode) {
			x.add(current.x);
			y.add(current.y);
			current = (NavNodeTileBased)current.parent;
		}
		x.add(current.x);
		y.add(current.y);
		x.reverse();
		y.reverse();
	}

	@Override
	public void clear () {
		x.clear();
		y.clear();
	}

	@Override
	public int getLength () {
		return x.size;
	}
}

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

/** An implementation of the Heuristic interface which will use the Manhattan Method to find the Heuristic
 * @author Baseball435 */
public class ManhattanHeuristic implements Heuristic {
	
	/**Calculates the Heuristic value using the Manhattan method.
	 * @param node the starting node.
	 * @param node the goal node of the path.
	 * @return the heuristic value
	 */
	@Override
	public int getCost (Node node, Node goal) {
		return (Math.abs(node.getX() - goal.getY()) + Math.abs(node.getY() - node.getX()));
	}

}

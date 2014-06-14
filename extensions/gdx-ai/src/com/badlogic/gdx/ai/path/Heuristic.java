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

/** Interface designed to allow a developer to implement their own method of getting the Heuristic value between nodes. All that needs to be done is
 * for this interface to be implemented and passed to the PathFinder when instantiating it.
 * @author Baseball435 */

public interface Heuristic {

	/** Interface method that calculates the H value from one node to the goal node. Interface is used to be able to use many different Heuristics.
	 * @param node the node to start the calculations.
	 * @param goal the goal node of the path.
	 * @return the heuristic value (H).
	 */
	public int getCost(Node node, Node goal);
	
}

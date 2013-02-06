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

package com.badlogic.gdx.physics.tokamak;

/*** See <a href="http://www.tokamakphysics.com/documentation/reference/neSimulatorSizeInfo.htm">http://www.tokamakphysics.com/
 * documentation/reference/neSimulatorSizeInfo.htm</a>
 * @author mzechner */
public class SimulatorSizeInfo {
	/** Number of rigid bodies in the simulation */
	public int rigidBodiesCount = 50;
	/** Number of animated bodies in the simulation */
	public int animatedBodiesCount = 50;
	/** Number of rigid particles in the simulation */
	public int rigidParticlesCount = 50;
	/** Number of controller instances in the simulation */
	public int controllersCount = 50;
	/** Number of possible overlapping pairs. This has the maximum value of (n x (n - 1)) / 2, where n = rigidBodyCount +
	 * animatedBodyCount. But in practice it rarely reach that high. You can try to specify a smaller number to save memory. */
	public int overlappedPairsCount = 1125;
	/** Number of collision geometries in the simulator */
	public int geometriesCount = 50;
	/** Number of joints in the simulation */
	public int constraintsCount = 100;
	/** Number of joint Sets in the simulation */
	public int constraintSetsCount = 100;
	/** Size of the buffer use to solve joints */
	public int constraintBufferSize = 2000;
	/** Number of sensors in the simulation */
	public int sensorsCount = 100;
	/** Number of nodes use to store terrain triangles */
	public int terrainNodesStartCount = 200;
	/** Grow by this size if run out of nodes */
	public int terrainNodesGrowByCount = -1;
}

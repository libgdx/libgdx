package com.badlogic.gdx.physics.tokamak;

/***
 * See <a href="http://www.tokamakphysics.com/documentation/reference/neSimulatorSizeInfo.htm">http://www.tokamakphysics.com/documentation/reference/neSimulatorSizeInfo.htm</a>
 * @author mzechner
 *
 */
public class SimulatorSizeInfo {
	/** Number of rigid bodies in the simulation */
	public int rigidBodiesCount = 50;
	/** Number of animated bodies in the simulation */
	public int animatedBodiesCount = 50;
	/** Number of rigid particles in the simulation */
	public int rigidParticlesCount = 50;
	/** Number of controller instances in the simulation */
	public int controllersCount = 50;
	/** Number of possible overlapping pairs.
	   This has the maximum value of (n x (n - 1)) / 2,
	   where n = rigidBodyCount + animatedBodyCount.
	   But in practice it rarely reach that high.
	   You can try to specify a smaller number to save memory.
	*/
	public int overlappedPairsCount = 1125;
	/** Number of collision geometries in the simulator*/
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

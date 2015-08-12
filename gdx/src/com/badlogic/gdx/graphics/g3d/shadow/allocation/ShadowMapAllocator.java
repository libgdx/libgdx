package com.badlogic.gdx.graphics.g3d.shadow.allocation;

import com.badlogic.gdx.graphics.Camera;

/**
 * Shadow map allocator get the good texture region for each light
 * @author realitix
 */
public interface ShadowMapAllocator {
	/**
	 * Start the allocation of the texture
	 */
	public void begin();

	/**
	 * End the allocation of the texture
	 */
	public void end();

	/**
	 * Find the next texture region for the current camera
	 * @param camera Current Camera
	 * @return AllocatorResult
	 */
	public AllocatorResult nextResult(Camera camera);

	/**
	 * Return the size of the shadow map
	 * @return int
	 */
	public int getSize();
}

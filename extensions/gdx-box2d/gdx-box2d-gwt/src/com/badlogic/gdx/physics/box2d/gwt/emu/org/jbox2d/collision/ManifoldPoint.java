/*******************************************************************************
 * Copyright (c) 2013, Daniel Murphy
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 	* Redistributions of source code must retain the above copyright notice,
 * 	  this list of conditions and the following disclaimer.
 * 	* Redistributions in binary form must reproduce the above copyright notice,
 * 	  this list of conditions and the following disclaimer in the documentation
 * 	  and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
/*
 * JBox2D - A Java Port of Erin Catto's Box2D
 * 
 * JBox2D homepage: http://jbox2d.sourceforge.net/
 * Box2D homepage: http://www.box2d.org
 * 
 * This software is provided 'as-is', without any express or implied
 * warranty.  In no event will the authors be held liable for any damages
 * arising from the use of this software.
 * 
 * Permission is granted to anyone to use this software for any purpose,
 * including commercial applications, and to alter it and redistribute it
 * freely, subject to the following restrictions:
 * 
 * 1. The origin of this software must not be misrepresented; you must not
 * claim that you wrote the original software. If you use this software
 * in a product, an acknowledgment in the product documentation would be
 * appreciated but is not required.
 * 2. Altered source versions must be plainly marked as such, and must not be
 * misrepresented as being the original software.
 * 3. This notice may not be removed or altered from any source distribution.
 */

package org.jbox2d.collision;

import org.jbox2d.common.Vec2;

// updated to rev 100
/**
 * A manifold point is a contact point belonging to a contact
 * manifold. It holds details related to the geometry and dynamics
 * of the contact points.
 * The local point usage depends on the manifold type:
 * <ul><li>e_circles: the local center of circleB</li>
 * <li>e_faceA: the local center of cirlceB or the clip point of polygonB</li>
 * <li>e_faceB: the clip point of polygonA</li></ul>
 * This structure is stored across time steps, so we keep it small.<br/>
 * Note: the impulses are used for internal caching and may not
 * provide reliable contact forces, especially for high speed collisions.
 */
public class ManifoldPoint {
	/** usage depends on manifold type */
	public final Vec2 localPoint;
	/** the non-penetration impulse */
	public float normalImpulse;
	/** the friction impulse */
	public float tangentImpulse;
	/** uniquely identifies a contact point between two shapes */
	public final ContactID id;

	/**
	 * Blank manifold point with everything zeroed out.
	 */
	public ManifoldPoint() {
		localPoint = new Vec2();
		normalImpulse = tangentImpulse = 0f;
		id = new ContactID();
	}

	/**
	 * Creates a manifold point as a copy of the given point
	 * @param cp point to copy from
	 */
	public ManifoldPoint(final ManifoldPoint cp) {
		localPoint = cp.localPoint.clone();
		normalImpulse = cp.normalImpulse;
		tangentImpulse = cp.tangentImpulse;
		id = new ContactID(cp.id);
	}

	/**
	 * Sets this manifold point form the given one
	 * @param cp the point to copy from
	 */
	public void set(final ManifoldPoint cp){
		localPoint.set(cp.localPoint);
		normalImpulse = cp.normalImpulse;
		tangentImpulse = cp.tangentImpulse;
		id.set(cp.id);
	}
}

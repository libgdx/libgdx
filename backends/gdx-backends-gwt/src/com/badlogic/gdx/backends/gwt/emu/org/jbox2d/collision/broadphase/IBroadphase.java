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

package org.jbox2d.collision.broadphase;

import org.jbox2d.callbacks.QueryCallback;
import org.jbox2d.callbacks.RayCastCallback;
import org.jbox2d.collision.AABB;
import org.jbox2d.collision.RayCastInput;

public interface IBroadphase {

	int createProxy (AABB aabb, Object userData);

	void destroyProxy (int proxyId);

	void moveProxy (int proxyIdA, int proxyIdB);

	void touchProxy (int proxyId);

	AABB getFatAABB (int proxyId);

	Object getUserData (int proxyId);

	boolean testOverlap (int proxyIdA, int proxyIdB);

	int getProxyCount ();

	void query (QueryCallback callback, AABB aabb);

	void raycast (RayCastCallback callback, RayCastInput input);

	int getTreeHeight ();

	int getTreeBalance ();

	float getTreeQuality ();

}

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

package com.badlogic.gdx.math;

/** Interface that specifies a path of type T within the window 0.0<=t<=1.0.
 * @author Xoppa */
public interface Path<T> {
	T derivativeAt (T out, float t);

	/** @return The value of the path at t where 0<=t<=1 */
	T valueAt (T out, float t);

	/** @return The approximated value (between 0 and 1) on the path which is closest to the specified value. Note that the
	 *         implementation of this method might be optimized for speed against precision, see {@link #locate(Object)} for a more
	 *         precise (but more intensive) method. */
	float approximate (T v);

	/** @return The precise location (between 0 and 1) on the path which is closest to the specified value. Note that the
	 *         implementation of this method might be CPU intensive, see {@link #approximate(Object)} for a faster (but less
	 *         precise) method. */
	float locate (T v);
	
	
	/**
	 * @param samples The amount of divisions used to approximate length. Higher values will produce more precise results,
	 * 		but will be more CPU intensive.
	 * @return An approximated length of the spline through sampling the curve and accumulating the euclidean distances between
	 *       the sample points.
	 */
	float approxLength(int samples);
	
}
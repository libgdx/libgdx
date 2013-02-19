package com.badlogic.gdx.math;

/** Interface that specifies a path of type T within the window 0.0<=t<=1.0.
 * @author Xoppa */
public interface Path<T> {
	/** @return The value of the path at t where 0<=t<=1 */
	T valueAt(T out, float t);
	/** @return The approximated value (between 0 and 1) on the path which is closest to the specified value.
	 * Note that the implementation of this method might be optimized for speed against precision. */
	float approximate(T v);
}

package com.badlogic.gdx.utils.asynch;

/**
 * Task to be submitted to an {@link AsynchExecutor}, returning a result of type T.
 * @author badlogic
 *
 */
public interface AsynchTask<T> {
	public T call() throws Exception;
}

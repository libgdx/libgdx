package com.badlogic.gdx.utils.async;

/**
 * Task to be submitted to an {@link AsyncExecutor}, returning a result of type T.
 * @author badlogic
 *
 */
public interface AsyncTask<T> {
	public T call() throws Exception;
}

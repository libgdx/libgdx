package com.badlogic.gdx.utils.asynch;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Returned by {@link AsynchExecutor#submit(AsynchTask)}, allows to poll
 * for the result of the asynch workload.
 * @author badlogic
 *
 */
public class AsynchResult<T> {
	private final T result;
	
	AsynchResult(T result) {
		this.result = result;
	}
	
	/**
	 * @return whether the {@link AsynchTask} is done
	 */
	public boolean isDone() {
		return true;
	}
	
	/**
	 * @return the result, or null if there was an error, no result, or the task is still running
	 */
	public T get() {
		return result;
	}
}

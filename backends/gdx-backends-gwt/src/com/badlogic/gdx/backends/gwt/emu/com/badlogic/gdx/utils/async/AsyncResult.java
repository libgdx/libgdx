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

package com.badlogic.gdx.utils.async;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.badlogic.gdx.utils.async.AsyncExecutor;
import com.badlogic.gdx.utils.async.AsyncTask;

/**
 * Returned by {@link AsyncExecutor#submit(AsyncTask)}, allows to poll
 * for the result of the asynch workload.
 * @author badlogic
 *
 */
public class AsyncResult<T> {
	private final T result;
	
	AsyncResult(T result) {
		this.result = result;
	}
	
	/**
	 * @return whether the {@link AsyncTask} is done
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
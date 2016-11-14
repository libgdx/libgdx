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

import com.badlogic.gdx.utils.GdxRuntimeException;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/** Returned by {@link AsyncExecutor#submit(AsyncTask)}, allows to poll for the result of the asynch workload.
 * @author badlogic */
public class AsyncResult<T> {
	private final Future<T> future;

	AsyncResult (Future<T> future) {
		this.future = future;
	}

	/** @return whether the {@link AsyncTask} is done */
	public boolean isDone () {
		return future.isDone();
	}

	/** @return waits if necessary for the computation to complete and then returns the result
	 * @throws GdxRuntimeException if there was an error */
	public T get () {
		try {
			return future.get();
		} catch (InterruptedException ex) {
			return null;
		} catch (ExecutionException ex) {
			throw new GdxRuntimeException(ex.getCause());
		}
	}
}

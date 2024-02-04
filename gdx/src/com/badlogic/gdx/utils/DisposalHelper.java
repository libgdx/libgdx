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

package com.badlogic.gdx.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Util class for disposing resources. It's useful for disposing multiple resources. Null-safe: provided disposable resources
 * could be null. There is a guarantee that all resources are disposed of, even when exception(s) occurs.
 * @author Anton-Samarkyi */
public class DisposalHelper implements Disposable {
	private final List<Disposable> toDispose = new ArrayList<>();
	private List<Exception> exceptions;

	/** Constructs an empty DisposalHelper. */
	public DisposalHelper () {
	}

	/** Constructs a DisposalHelper with disposableItems set to dispose.
	 * @param disposableItems items to dispose */
	public DisposalHelper (Disposable... disposableItems) {
		addAll(disposableItems);
	}

	/** Constructs a DisposalHelper with disposableItems set to dispose.
	 * @param disposableItems items to dispose */
	public DisposalHelper (Iterable<Disposable> disposableItems) {
		addAll(disposableItems);
	}

	/** Adds item to dispose.
	 * @param disposableItem item to dispose
	 * @return this helper */
	public DisposalHelper add (Disposable disposableItem) {
		toDispose.add(disposableItem);
		return this;
	}

	/** Adds items to dispose.
	 * @param disposableItems items to dispose
	 * @return this helper */
	public DisposalHelper addAll (Disposable... disposableItems) {
		if (disposableItems != null) Collections.addAll(toDispose, disposableItems);
		return this;
	}

	/** Adds items to dispose.
	 * @param disposableItems items to dispose
	 * @return this helper */
	public DisposalHelper addAll (Iterable<Disposable> disposableItems) {
		if (disposableItems == null) return this;
		for (Disposable item : disposableItems) {
			toDispose.add(item);
		}
		return this;
	}

	@Override
	public void dispose () {
		for (Disposable item : toDispose) {
			if (item != null && item != this) {
				try {
					item.dispose();
				} catch (Exception e) {
					addException(e);
				}
			}
		}

		if (exceptions != null) {
			if (exceptions.size() == 1) {
				Exception exception = exceptions.get(0);
				if (exception instanceof RuntimeException) {
					throw (RuntimeException)exception;
				} else {
					throw new GdxRuntimeException("Disposal exception", exception);
				}
			} else {
				throw new GdxComplexDisposalException(exceptions);
			}
		}
	}

	/** Dispose without throwing any exception. By default, prints stacktrace of occurred exception to the standard output. */
	public void disposeSilently () {
		disposeSilently(true);
	}

	/** Dispose without throwing any exception.
	 * @param printStackTrace if true, prints stacktrace of occurred exception to the standard output */
	public void disposeSilently (Boolean printStackTrace) {
		try {
			dispose();
		} catch (Exception e) {
			if (printStackTrace) {
				if (e instanceof GdxComplexDisposalException) {
					for (Exception exception : exceptions) {
						exception.printStackTrace();
					}
				} else {
					e.printStackTrace();
				}
			}
		}
	}

	private void addException (Exception e) {
		if (exceptions == null) {
			exceptions = new ArrayList<>();
		}
		exceptions.add(e);
	}

	/** Disposes multiple items. There is a guarantee that all resources are disposed of, even when exception(s) occurs.
	 * @param disposableItems items to dispose */
	public static void disposeAll (Disposable... disposableItems) {
		new DisposalHelper(disposableItems).dispose();
	}

	/** Disposes multiple items. There is a guarantee that all resources are disposed of, even when exception(s) occurs.
	 * @param disposableItems items to dispose */
	public static void disposeAll (Iterable<Disposable> disposableItems) {
		new DisposalHelper(disposableItems).dispose();
	}

	/** Disposes multiple items without throwing any exception. There is a guarantee that all resources are disposed of, even when
	 * exception(s) occurs.
	 * @param disposableItems items to dispose */
	public static void disposeAllSilently (Disposable... disposableItems) {
		new DisposalHelper(disposableItems).disposeSilently();
	}

	/** Disposes multiple items without throwing any exception. There is a guarantee that all resources are disposed of, even when
	 * exception(s) occurs.
	 * @param disposableItems items to dispose */
	public static void disposeAllSilently (Iterable<Disposable> disposableItems) {
		new DisposalHelper(disposableItems).disposeSilently();
	}
}

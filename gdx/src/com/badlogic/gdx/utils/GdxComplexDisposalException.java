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

import com.badlogic.gdx.utils.StringBuilder;

import java.util.List;

/** Runtime exception that contains list of exceptions occurred during the disposing process.
 * @author Anton-Samarkyi */
public class GdxComplexDisposalException extends GdxRuntimeException {
	private static final long serialVersionUID = 1L;
	private static final String MESSAGE = "Multiple exceptions occurred during the disposal process: ";

	public final List<Exception> exceptions;

	public GdxComplexDisposalException (List<Exception> exceptions) {
		super(composeMessage(exceptions));
		this.exceptions = exceptions;
	}

	private static String composeMessage (List<Exception> exceptions) {
		StringBuilder builder = new StringBuilder(MESSAGE);

		for (Exception exception : exceptions) {
			builder.append(exception.toString()).append(", ");
		}

		return builder.substring(0, builder.length() - 2);
	}
}

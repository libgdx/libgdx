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

import com.badlogic.gdx.scenes.scene2d.utils.ToStringProvider;

/**
 * Generic provider class used to get some value from given object.
 * @param <T> type of object that this provider will be used on
 * @param <R> type of object that this provider will return
 * @author Kotcrab
 * @see ToStringProvider
 */
public interface Provider<T, R> {
	/**
	 * @param obj object that value should be returned
	 * @return value from given object
	 */
	R get (T obj);
}

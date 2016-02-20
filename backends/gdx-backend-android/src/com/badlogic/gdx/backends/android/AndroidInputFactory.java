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

package com.badlogic.gdx.backends.android;

import java.lang.reflect.Constructor;

import android.content.Context;

import com.badlogic.gdx.Application;

/** Class that instantiates AndroidInput or AndroidInputThreePlus depending on the SDK level, via reflection.
 * @author mzechner */
public class AndroidInputFactory {
	public static AndroidInput newAndroidInput (Application activity, Context context, Object view,
		AndroidApplicationConfiguration config) {
		try {
			Class<?> clazz = null;
			AndroidInput input = null;

			int sdkVersion = android.os.Build.VERSION.SDK_INT;
			if (sdkVersion >= 12) {
				clazz = Class.forName("com.badlogic.gdx.backends.android.AndroidInputThreePlus");
			} else {
				clazz = Class.forName("com.badlogic.gdx.backends.android.AndroidInput");
			}
			Constructor<?> constructor = clazz.getConstructor(Application.class, Context.class, Object.class,
				AndroidApplicationConfiguration.class);
			input = (AndroidInput)constructor.newInstance(activity, context, view, config);
			return input;
		} catch (Exception e) {
			throw new RuntimeException("Couldn't construct AndroidInput, this should never happen", e);
		}
	}
}

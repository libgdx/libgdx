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

import android.content.Context;
import com.badlogic.gdx.Application;

/** Class that instantiates AndroidInput or AndroidInputThreePlus depending on the SDK level.
 * @author mzechner */
public class AndroidInputFactory {
	public static AndroidInput newAndroidInput (Application activity, Context context, Object view,
		AndroidApplicationConfiguration config) {
		int sdkVersion = android.os.Build.VERSION.SDK_INT;
		if (sdkVersion >= 12) {
			return new AndroidInputThreePlus(activity, context, view, config);
		} else {
			return new AndroidInput(activity, context, view, config);
		}
	}
}

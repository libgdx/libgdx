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

package com.badlogic.gdx.backends.iosrobovm;

import com.badlogic.gdx.Audio;

public interface IOSAudio extends Audio {
	/** Handles the app being activated / going to foreground.
	 *
	 * For example, this could (re-)activate and configure the audio session. */
	public void activate ();

	/** Handles the app being deactivated / going to background.
	 *
	 * For example, this could deactivate the audio session. */
	public void deactivate ();
}

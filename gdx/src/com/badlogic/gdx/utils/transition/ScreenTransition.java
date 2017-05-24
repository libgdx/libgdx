/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *	 http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.utils.transition;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;

public interface ScreenTransition {
	/** Renders two textures to the given batch
	 * @param batch the {@link Batch}
	 * @param currentScreenTexture {@link Texture} from a {@link FrameBuffer}
	 * @param nextScreenTexture {@link Texture} from a {@link FrameBuffer}
	 * @param percent the current progress 0.0 - 1.0 */
	void render (Batch batch, Texture currentScreenTexture, Texture nextScreenTexture, float percent);

}

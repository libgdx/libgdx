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

package com.badlogic.gdx.tests.gles3;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.tests.utils.GdxTest;

public abstract class AbstractES3test extends GdxTest {

	protected boolean isCreated = false;

	@Override
	public final boolean needsGL20 () {
		return true;
	}

	@Override
	public final void create () {
		if (Gdx.graphics.getGL30() == null) {
			System.out.println("This test requires OpenGL ES 3.0.");
			System.out.println("Make sure needsGL20() is returning true. (ES 2.0 is a subset of ES 3.0.)");
			System.out
				.println("Otherwise, your system does not support it, or it might not be available yet for the current backend.");
			return;
		}

		isCreated = createLocal();
	}

	@Override
	public final void render () {
		if (!isCreated) return;
		renderLocal();
	}

	@Override
	public final void resize (int width, int height) {
		if (!isCreated) return;
		resizeLocal(width, height);
	}

	@Override
	public final void dispose () {
		if (!isCreated) return;
		disposeLocal();
	}

	protected abstract boolean createLocal ();

	protected abstract void renderLocal ();

	protected void disposeLocal () {
	}

	protected void resizeLocal (int width, int height) {
	}
}

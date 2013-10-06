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

 
package com.dozingcatsoftware.bouncy;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Matrix4;

public class GLFieldRenderer implements IFieldRenderer {
	static final int CIRCLE_VERTICES = 10;
	ShapeRenderer renderer;

	public GLFieldRenderer () {
		renderer = new ShapeRenderer(500);
	}

	public void begin () {
		renderer.begin(ShapeType.Line);
	}

	@Override
	public void drawLine (float x1, float y1, float x2, float y2, int r, int g, int b) {
		float fr = r / 255f;
		float fg = g / 255f;
		float fb = b / 255f;
		renderer.setColor(fr, fg, fb, 1);
		renderer.line(x1, y1, x2, y2);
	}

	@Override
	public void fillCircle (float cx, float cy, float radius, int r, int g, int b) {
		end();
		renderer.begin(ShapeType.Filled);
		float fr = r / 255f;
		float fg = g / 255f;
		float fb = b / 255f;
		renderer.setColor(fr, fg, fb, 1);
		renderer.circle(cx, cy, radius, 20);
		end();
		begin();
	}

	@Override
	public void frameCircle (float cx, float cy, float radius, int r, int g, int b) {
		end();
		renderer.begin(ShapeType.Line);
		float fr = r / 255f;
		float fg = g / 255f;
		float fb = b / 255f;
		renderer.setColor(fr, fg, fb, 1);
		renderer.circle(cx, cy, radius, 20);
		end();
		begin();
	}

	public void end () {
		renderer.end();
	}

	public void setProjectionMatrix (Matrix4 matrix) {
		renderer.setProjectionMatrix(matrix);
	}
}

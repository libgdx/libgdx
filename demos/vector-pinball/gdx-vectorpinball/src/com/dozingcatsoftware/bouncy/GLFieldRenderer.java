
package com.dozingcatsoftware.bouncy;

import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer;

public class GLFieldRenderer implements IFieldRenderer {
	static final int CIRCLE_VERTICES = 10;
	ImmediateModeRenderer renderer;
	float[] circleVerts = new float[2 * CIRCLE_VERTICES];

	public GLFieldRenderer () {
		renderer = new ImmediateModeRenderer();

		float angle = 0;
		float angleInc = 360.0f / CIRCLE_VERTICES;
		for (int i = 0, j = 0; i < CIRCLE_VERTICES; i++, angle += angleInc) {
			float x = (float)Math.cos(Math.toRadians(angle));
			float y = (float)Math.sin(Math.toRadians(angle));
			circleVerts[j++] = x;
			circleVerts[j++] = y;
		}
	}

	public void begin () {
		renderer.begin(GL10.GL_LINES);
	}

	@Override public void drawLine (float x1, float y1, float x2, float y2, int r, int g, int b) {
		float fr = r / 255f;
		float fg = g / 255f;
		float fb = b / 255f;
		renderer.color(fr, fg, fb, 1);
		renderer.vertex(x1, y1, 0);
		renderer.color(fr, fg, fb, 1);
		renderer.vertex(x2, y2, 0);
	}

	@Override public void fillCircle (float cx, float cy, float radius, int r, int g, int b) {
		end();
		renderer.begin(GL10.GL_TRIANGLE_FAN);
		float fr = r / 255f;
		float fg = g / 255f;
		float fb = b / 255f;
		int j = 0;
		for (int i = 0; i < CIRCLE_VERTICES - 1; i++) {
			renderer.color(fr, fg, fb, 1);
			renderer.vertex(cx + circleVerts[j] * radius, cy + circleVerts[j + 1] * radius, 0);
			renderer.color(fr, fg, fb, 1);
			renderer.vertex(cx + circleVerts[j + 2] * radius, cy + circleVerts[j + 3] * radius, 0);
			j += 2;
		}
		renderer.color(fr, fg, fb, 1);
		renderer.vertex(cx + circleVerts[j] * radius, cy + circleVerts[j + 1] * radius, 0);
		renderer.color(fr, fg, fb, 1);
		renderer.vertex(cx + circleVerts[0] * radius, cy + circleVerts[1] * radius, 0);
		end();
		begin();
	}

	@Override public void frameCircle (float cx, float cy, float radius, int r, int g, int b) {
		float fr = r / 255f;
		float fg = g / 255f;
		float fb = b / 255f;
		int j = 0;
		for (int i = 0; i < CIRCLE_VERTICES - 1; i++) {
			renderer.color(fr, fg, fb, 1);
			renderer.vertex(cx + circleVerts[j] * radius, cy + circleVerts[j + 1] * radius, 0);
			renderer.color(fr, fg, fb, 1);
			renderer.vertex(cx + circleVerts[j + 2] * radius, cy + circleVerts[j + 3] * radius, 0);
			j += 2;
		}
		renderer.color(fr, fg, fb, 1);
		renderer.vertex(cx + circleVerts[j] * radius, cy + circleVerts[j + 1] * radius, 0);
		renderer.color(fr, fg, fb, 1);
		renderer.vertex(cx + circleVerts[0] * radius, cy + circleVerts[1] * radius, 0);
	}

	public void end () {
		renderer.end();
	}
}

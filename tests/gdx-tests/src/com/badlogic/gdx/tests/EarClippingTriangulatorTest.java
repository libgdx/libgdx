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

package com.badlogic.gdx.tests;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.ShortArray;

public class EarClippingTriangulatorTest {
// public class EarClippingTriangulatorTest extends GdxTest {
//
// private List<TestCase> testCases = new ArrayList<TestCase>();
// private int casesX;
// private int casesY;
//
// @Override
// public void create () {
// // An empty "polygon"
// testCases.add(new TestCase(new float[] {}, true));
//
// // A point
// testCases.add(new TestCase(new float[] {0, 0}, true));
//
// // A line segment
// testCases.add(new TestCase(new float[] {0, 0, 1, 1}, true));
//
// // A counterclockwise triangle
// testCases.add(new TestCase(new float[] {0, 0, 0, 1, 1, 0,}));
//
// // A counterclockwise square
// testCases.add(new TestCase(new float[] {0, 0, 0, 1, 1, 1, 1, 0,}));
//
// // A clockwise square
// testCases.add(new TestCase(new float[] {0, 0, 1, 0, 1, 1, 0, 1,}));
//
// // Starfleet insigna
// testCases.add(new TestCase(new float[] {0, 0, 0.6f, 0.4f, 1, 0, 0.5f, 1,}));
//
// // Starfleet insigna with repeated point
// testCases.add(new TestCase(new float[] {0, 0, 0.6f, 0.4f, 0.6f, 0.4f, 1, 0, 0.5f, 1,}));
//
// // Three collinear points
// testCases.add(new TestCase(new float[] {0, 0, 1, 0, 2, 0,}));
//
// // Four collinear points
// testCases.add(new TestCase(new float[] {0, 0, 1, 0, 2, 0, 3, 0,}));
//
// // Non-consecutive collinear points
// testCases.add(new TestCase(new float[] {0, 0, 1, 1, 2, 0, 3, 1, 4, 0,}, true));
//
// // Plus shape
// testCases.add(new TestCase(new float[] {1, 0, 2, 0, 2, 1, 3, 1, 3, 2, 2, 2, 2, 3, 1, 3, 1, 2, 0, 2, 0, 1, 1, 1,}));
//
// // Star shape
// testCases.add(new TestCase(new float[] {4, 0, 5, 3, 8, 4, 5, 5, 4, 8, 3, 5, 0, 4, 3, 3,}));
//
// // U shape
// testCases.add(new TestCase(new float[] {1, 0, 2, 0, 3, 1, 3, 3, 2, 3, 2, 1, 1, 1, 1, 3, 0, 3, 0, 1,}));
//
// // Spiral
// testCases.add(new TestCase(new float[] {1, 0, 4, 0, 5, 1, 5, 4, 4, 5, 1, 5, 0, 4, 0, 3, 1, 2, 2, 2, 3, 3, 1, 3, 1, 4, 4, 4,
// 4, 1, 0, 1,}));
//
// // Test case from http://www.flipcode.com/archives/Efficient_Polygon_Triangulation.shtml
// testCases.add(new TestCase(new float[] {0, 6, 0, 0, 3, 0, 4, 1, 6, 1, 8, 0, 12, 0, 13, 2, 8, 2, 8, 4, 11, 4, 11, 6, 6, 6,
// 4, 3, 2, 6,}));
//
// // Self-intersection
// testCases.add(new TestCase(new float[] {0, 0, 1, 1, 2, -1, 3, 1, 4, 0,}, true));
//
// // Self-touching
// testCases.add(new TestCase(new float[] {0, 0, 4, 0, 4, 4, 2, 4, 2, 3, 3, 3, 3, 1, 1, 1, 1, 3, 2, 3, 2, 4, 0, 4,}, true));
//
// // Self-overlapping
// testCases.add(new TestCase(new float[] {0, 0, 4, 0, 4, 4, 1, 4, 1, 3, 3, 3, 3, 1, 1, 1, 1, 3, 3, 3, 3, 4, 0, 4,}, true));
//
// // Test case from http://www.davdata.nl/math/polygons.html
// testCases.add(new TestCase(new float[] {190, 480, 140, 180, 310, 100, 330, 390, 290, 390, 280, 260, 220, 260, 220, 430,
// 370, 430, 350, 30, 50, 30, 160, 560, 730, 510, 710, 20, 410, 30, 470, 440, 640, 410, 630, 140, 590, 140, 580, 360, 510,
// 370, 510, 60, 650, 70, 660, 450, 190, 480,}));
//
// // Issue 815, http://code.google.com/p/libgdx/issues/detail?id=815
// testCases.add(new TestCase(new float[] {-2.0f, 0.0f, -2.0f, 0.5f, 0.0f, 1.0f, 0.5f, 2.875f, 1.0f, 0.5f, 1.5f, 1.0f, 2.0f,
// 1.0f, 2.0f, 0.0f,}));
//
// // Issue 207, comment #1, http://code.google.com/p/libgdx/issues/detail?id=207#c1
// testCases.add(new TestCase(new float[] {72.42465f, 197.07095f, 78.485535f, 189.92776f, 86.12059f, 180.92929f, 99.68253f,
// 164.94557f, 105.24325f, 165.79604f, 107.21862f, 166.09814f, 112.41958f, 162.78253f, 113.73238f, 161.94562f, 123.29477f,
// 167.93805f, 126.70667f, 170.07617f, 73.22717f, 199.51062f,}));
//
// // Issue 207, comment #11, http://code.google.com/p/libgdx/issues/detail?id=207#c11
// // Also on issue 1081, http://code.google.com/p/libgdx/issues/detail?id=1081
// testCases.add(new TestCase(new float[] {2400.0f, 480.0f, 2400.0f, 176.0f, 1920.0f, 480.0f, 1920.0459f, 484.22314f,
// 1920.1797f, 487.91016f, 1920.3955f, 491.0874f, 1920.6875f, 493.78125f, 1921.0498f, 496.01807f, 1921.4766f, 497.82422f,
// 1921.9619f, 499.22607f, 1922.5f, 500.25f, 1923.085f, 500.92236f, 1923.7109f, 501.26953f, 1924.3721f, 501.31787f,
// 1925.0625f, 501.09375f, 1925.7764f, 500.62354f, 1926.5078f, 499.9336f, 1927.251f, 499.0503f, 1928.0f, 498.0f, 1928.749f,
// 496.80908f, 1929.4922f, 495.5039f, 1930.2236f, 494.11084f, 1930.9375f, 492.65625f, 1931.6279f, 491.1665f, 1932.2891f,
// 489.66797f, 1932.915f, 488.187f, 1933.5f, 486.75f, 1934.0381f, 485.3833f, 1934.5234f, 484.11328f, 1934.9502f, 482.9663f,
// 1935.3125f, 481.96875f, 1935.6045f, 481.14697f, 1935.8203f, 480.52734f, 1935.9541f, 480.13623f, 1936.0f, 480.0f,}));
//
// // Issue 1407, http://code.google.com/p/libgdx/issues/detail?id=1407
// testCases.add(new TestCase(new float[] {3.914329f, 1.9008259f, 4.414321f, 1.903619f, 4.8973203f, 1.9063174f, 5.4979978f,
// 1.9096732f,}, true));
//
// // Issue 1407, http://code.google.com/p/libgdx/issues/detail?id=1407,
// // with an additional point to show what is happening.
// testCases.add(new TestCase(new float[] {3.914329f, 1.9008259f, 4.414321f, 1.903619f, 4.8973203f, 1.9063174f, 5.4979978f,
// 1.9096732f, 4, 4,}));
//
// casesX = (int)Math.ceil(Math.sqrt(testCases.size()));
// casesY = (int)Math.ceil((float)testCases.size() / casesX);
//
// Gdx.input.setInputProcessor(new InputAdapter() {
// @Override
// public boolean keyDown (int keycode) {
// switch (keycode) {
// case Keys.RIGHT:
// cycle(1);
// break;
// case Keys.LEFT:
// cycle(-1);
// break;
// case Keys.SPACE:
// reverse();
// break;
// default:
// return super.keyDown(keycode);
// }
// return true;
// }
// });
// }
//
// @Override
// public void render () {
// Gdx.gl.glClearColor(1, 1, 1, 1);
// Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
//
// int w = Gdx.graphics.getWidth();
// int h = Gdx.graphics.getHeight();
// Gdx.gl20.glViewport(0, 0, w, h);
//
// final float M = 0.1f;
// Gdx.gl10.glMatrixMode(GL10.GL_PROJECTION);
// Gdx.gl10.glLoadIdentity();
// Gdx.gl10.glOrthof(-M, casesX * (1 + M), -M, casesY * (1 + M), -1, 1);
// Gdx.gl10.glMatrixMode(GL10.GL_MODELVIEW);
// Gdx.gl10.glLoadIdentity();
//
// int x = 0;
// int y = 0;
// for (TestCase testCase : testCases) {
// Gdx.gl10.glPushMatrix();
// Gdx.gl10.glTranslatef(x * (1 + M), y * (1 + M), 0);
// testCase.render();
// Gdx.gl10.glPopMatrix();
//
// x++;
// if (x >= casesX) {
// x = 0;
// y++;
// }
// }
// }
//
// @Override
// public void dispose () {
// for (TestCase testCase : testCases) {
// testCase.dispose();
// }
// }
//
// void cycle (int step) {
// for (TestCase testCase : testCases) {
// testCase.cycle(step);
// }
// }
//
// void reverse () {
// for (TestCase testCase : testCases) {
// testCase.reverse();
// }
// }
//
// static final Color VALID_COLOR = new Color(0.8f, 1.0f, 0.8f, 1.0f);
// static final Color INVALID_COLOR = new Color(1.0f, 0.8f, 0.8f, 1.0f);
//
// private class TestCase implements Disposable {
// final FloatArray polygon;
// final boolean invalid;
//
// final Mesh polygonMesh;
// final Mesh interiorMesh;
// final Mesh triangleOutlineMesh;
// final Rectangle boundingRect;
//
// public TestCase (float[] p) {
// this(p, false);
// }
//
// public TestCase (float[] p, boolean invalid) {
// this.invalid = invalid;
// polygon = new FloatArray(p);
//
// int numPolygonVertices = polygon.size;
// Vector2 min = new Vector2(Float.MAX_VALUE, Float.MAX_VALUE);
// Vector2 max = new Vector2(-Float.MAX_VALUE, -Float.MAX_VALUE);
// for (int i = 0; i < numPolygonVertices; i++) {
// float x = polygon.get(i++);
// float y = polygon.get(i);
// min.x = Math.min(min.x, x);
// min.y = Math.min(min.y, y);
// max.x = Math.max(max.x, x);
// max.y = Math.max(max.y, y);
// }
// boundingRect = new Rectangle(min.x, min.y, Math.max(0.001f, max.x - min.x), Math.max(0.001f, max.y - min.y));
//
// int numTriangles = Math.max(0, polygon.size / 2 - 2);
// VertexAttributes position = new VertexAttributes(
// new VertexAttribute(Usage.Position, 2, ShaderProgram.POSITION_ATTRIBUTE));
// VertexAttributes positionAndColor = new VertexAttributes(new VertexAttribute(Usage.Position, 2,
// ShaderProgram.POSITION_ATTRIBUTE), new VertexAttribute(Usage.Color, 4, ShaderProgram.COLOR_ATTRIBUTE));
// polygonMesh = new Mesh(true, polygon.size / 2, 0, position);
// interiorMesh = new Mesh(true, 3 * numTriangles, 0, positionAndColor);
// triangleOutlineMesh = new Mesh(true, 6 * numTriangles, 0, position);
//
// triangulate();
// }
//
// private void triangulate () {
// ShortArray triangles = new EarClippingTriangulator().computeTriangles(polygon);
//
// FloatArray triangleOutlines = new FloatArray(triangles.size * 2);
// for (int i = 0; i < triangles.size; i += 3) {
// float ax = polygon.get(triangles.get(i) * 2);
// float ay = polygon.get(triangles.get(i) * 2 + 1);
// float bx = polygon.get(triangles.get(i + 1) * 2);
// float by = polygon.get(triangles.get(i + 1) * 2 + 1);
// float cx = polygon.get(triangles.get(i + 2) * 2);
// float cy = polygon.get(triangles.get(i + 2) * 2 + 1);
// triangleOutlines.add(ax);
// triangleOutlines.add(ay);
// triangleOutlines.add(bx);
// triangleOutlines.add(by);
// triangleOutlines.add(bx);
// triangleOutlines.add(by);
// triangleOutlines.add(cx);
// triangleOutlines.add(cy);
// triangleOutlines.add(cx);
// triangleOutlines.add(cy);
// triangleOutlines.add(ax);
// triangleOutlines.add(ay);
// }
//
// polygonMesh.setVertices(polygon.items, 0, polygon.size);
// interiorMesh.setVertices(listToColoredVertexArray(polygon, triangles, getColor()));
// triangleOutlineMesh.setVertices(triangleOutlines.items, 0, triangleOutlines.size);
// }
//
// public void cycle (int step) {
// if (polygon.size == 0) {
// return;
// }
// while (step > 0) {
// polygon.insert(0, polygon.pop());
// polygon.insert(0, polygon.pop());
// --step;
// }
// while (step < 0) {
// polygon.add(polygon.removeIndex(0));
// ++step;
// }
// triangulate();
// }
//
// public void reverse () {
// float[] vertices = polygon.items;
// for (int i = 0, lastIndex = polygon.size - 2, n = polygon.size / 2; i < n; i++) {
// int ii = lastIndex - i;
// float temp = vertices[i];
// vertices[i] = vertices[ii];
// vertices[ii] = temp;
// i++;
// ii++;
// temp = vertices[i];
// vertices[i] = vertices[ii];
// vertices[ii] = temp;
// }
// }
//
// private Color getColor () {
// if (invalid) {
// return INVALID_COLOR;
// } else {
// return VALID_COLOR;
// }
// }
//
// public void render () {
// Gdx.gl10.glScalef(1 / boundingRect.width, 1 / boundingRect.height, 1);
// Gdx.gl10.glTranslatef(-boundingRect.x, -boundingRect.y, 0);
//
// interiorMesh.render(GL10.GL_TRIANGLES);
//
// Gdx.gl10.glColor4f(0.4f, 0.4f, 0.4f, 1.0f);
// Gdx.gl10.glLineWidth(1.0f);
// triangleOutlineMesh.render(GL10.GL_LINES);
//
// Gdx.gl10.glColor4f(0.3f, 0.0f, 0.0f, 1.0f);
// Gdx.gl10.glLineWidth(2.0f);
// polygonMesh.render(GL10.GL_LINE_LOOP);
// }
//
// @Override
// public void dispose () {
// polygonMesh.dispose();
// interiorMesh.dispose();
// triangleOutlineMesh.dispose();
// }
// }
//
// static float[] listToColoredVertexArray (FloatArray vertices, ShortArray triangles, Color color) {
// int n = triangles.size;
// float[] array = new float[n * 6];
// int i = 0;
// int j = 0;
// for (int k = 0; k < n; k++, j++) {
// float percent = n <= 3 ? 1 : (j / 3) / (float)(n / 3 - 1);
// float brightness = 0.3f + 0.4f * percent;
// array[i++] = vertices.get(triangles.get(k) * 2);
// array[i++] = vertices.get(triangles.get(k) * 2 + 1);
// array[i++] = color.r * brightness;
// array[i++] = color.g * brightness;
// array[i++] = color.b * brightness;
// array[i++] = 1;
// }
// return array;
// }
}

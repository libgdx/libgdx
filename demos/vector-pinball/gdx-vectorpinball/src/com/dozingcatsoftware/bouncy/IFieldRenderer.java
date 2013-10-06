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

/** This interface defines methods that draw graphical elements such as lines as circles to display the field. An implementation of
 * this interface is passed to FieldElement objects so they can draw themselves without depending directly on Android UI classes. */

public interface IFieldRenderer {

	public void drawLine (float x1, float y1, float x2, float y2, int r, int g, int b);

	public void fillCircle (float cx, float cy, float radius, int r, int g, int b);

	public void frameCircle (float cx, float cy, float radius, int r, int g, int b);
}

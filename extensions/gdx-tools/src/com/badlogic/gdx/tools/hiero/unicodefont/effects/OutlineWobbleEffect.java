/*
 * Copyright 2006 Jerry Huxtable
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.badlogic.gdx.tools.hiero.unicodefont.effects;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.util.Iterator;
import java.util.List;

/** @author Jerry Huxtable
 * @author Nathan Sweet */
public class OutlineWobbleEffect extends OutlineEffect {
	float detail = 1;
	float amplitude = 1;

	public OutlineWobbleEffect () {
		setStroke(new WobbleStroke());
	}

	public OutlineWobbleEffect (int width, Color color) {
		super(width, color);
	}

	public String toString () {
		return "Outline (Wobble)";
	}

	public List getValues () {
		List values = super.getValues();
		values.remove(2); // Remove "Join".
		values.add(EffectUtil.floatValue("Detail", detail, 1, 50, "This setting controls how detailed the outline will be. "
			+ "Smaller numbers cause the outline to have more detail."));
		values.add(EffectUtil.floatValue("Amplitude", amplitude, 0.5f, 50, "This setting controls the amplitude of the outline."));
		return values;
	}

	public void setValues (List values) {
		super.setValues(values);
		for (Iterator iter = values.iterator(); iter.hasNext();) {
			Value value = (Value)iter.next();
			if (value.getName().equals("Detail")) {
				detail = ((Float)value.getObject()).floatValue();
			} else if (value.getName().equals("Amplitude")) {
				amplitude = ((Float)value.getObject()).floatValue();
			}
		}
	}

	class WobbleStroke implements Stroke {
		private static final float FLATNESS = 1;

		public Shape createStrokedShape (Shape shape) {
			GeneralPath result = new GeneralPath();
			shape = new BasicStroke(getWidth(), BasicStroke.CAP_SQUARE, getJoin()).createStrokedShape(shape);
			PathIterator it = new FlatteningPathIterator(shape.getPathIterator(null), FLATNESS);
			float points[] = new float[6];
			float moveX = 0, moveY = 0;
			float lastX = 0, lastY = 0;
			float thisX = 0, thisY = 0;
			int type = 0;
			float next = 0;
			while (!it.isDone()) {
				type = it.currentSegment(points);
				switch (type) {
				case PathIterator.SEG_MOVETO:
					moveX = lastX = randomize(points[0]);
					moveY = lastY = randomize(points[1]);
					result.moveTo(moveX, moveY);
					next = 0;
					break;

				case PathIterator.SEG_CLOSE:
					points[0] = moveX;
					points[1] = moveY;
					// Fall into....

				case PathIterator.SEG_LINETO:
					thisX = randomize(points[0]);
					thisY = randomize(points[1]);
					float dx = thisX - lastX;
					float dy = thisY - lastY;
					float distance = (float)Math.sqrt(dx * dx + dy * dy);
					if (distance >= next) {
						float r = 1.0f / distance;
						while (distance >= next) {
							float x = lastX + next * dx * r;
							float y = lastY + next * dy * r;
							result.lineTo(randomize(x), randomize(y));
							next += detail;
						}
					}
					next -= distance;
					lastX = thisX;
					lastY = thisY;
					break;
				}
				it.next();
			}

			return result;
		}

		private float randomize (float x) {
			return x + (float)Math.random() * amplitude * 2 - 1;
		}
	}
}

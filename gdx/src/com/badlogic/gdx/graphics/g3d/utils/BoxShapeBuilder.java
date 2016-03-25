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

package com.badlogic.gdx.graphics.g3d.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.collision.BoundingBox;

/** BoxShapeBuilder builds box.
 * 
 * @author realitix */
public class BoxShapeBuilder extends BaseShapeBuilder {

	/** Add bounding box with default color
	 * @param box */
	public static void build (MeshPartBuilder builder, BoundingBox box) {
		build(builder, box, tmpColor0.set(1, 0.66f, 0, 1));
	}

	/** Add bounding box with custom color
	 * @param box
	 * @param color */
	public static void build (MeshPartBuilder builder, BoundingBox box, Color color) {
		builder.setColor(color);
		builder.box(box.getCorner000(tmpV0), box.getCorner010(tmpV1), box.getCorner100(tmpV2), box.getCorner110(tmpV3),
			box.getCorner001(tmpV4), box.getCorner011(tmpV5), box.getCorner101(tmpV6), box.getCorner111(tmpV7));
	}
}

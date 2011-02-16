/*
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com), Nathan Sweet (admin@esotericsoftware.com)
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

package com.badlogic.gdx.twl;

import de.matthiasmann.twl.Alignment;
import de.matthiasmann.twl.DialogLayout;
import de.matthiasmann.twl.Event;
import de.matthiasmann.twl.Widget;

/**
 * Adds convenience methods to {@link DialogLayout}.
 * @author Nathan Sweet
 */
public class Layout extends DialogLayout {
	private boolean eatEvents;

	public Layout () {
		setTheme("");
	}

	public Layout (String theme) {
		setTheme(theme);
	}

	public Layout (boolean eatEvents) {
		this.eatEvents = eatEvents;
	}

	public Layout (String theme, boolean eatEvents) {
		setTheme(theme);
		this.eatEvents = eatEvents;
	}

	protected boolean handleEvent (Event evt) {
		return eatEvents;
	}

	public Direction horizontal () {
		return new Direction(true);
	}

	public Direction vertical () {
		return new Direction(false);
	}

	public class Direction {
		private final boolean horizontal;

		Direction (boolean horizontal) {
			this.horizontal = horizontal;
		}

		/**
		 * @param widgets Either {@link Widget} or {@link Integer} objects. Integers are used to specify the gap size.
		 */
		public Group sequence (Object... widgets) {
			DialogLayout.Group dialogGroup = createSequentialGroup();
			if (horizontal)
				setHorizontalGroup(dialogGroup);
			else
				setVerticalGroup(dialogGroup);
			return new Group(null, dialogGroup).add(widgets);
		}

		/**
		 * @param widgets Either {@link Widget} or {@link Integer} objects. Integers are used to specify the gap size.
		 */
		public Group parallel (Object... widgets) {
			DialogLayout.Group dialogGroup = createParallelGroup();
			if (horizontal)
				setHorizontalGroup(dialogGroup);
			else
				setVerticalGroup(dialogGroup);
			return new Group(null, dialogGroup).add(widgets);
		}

		public class Group {
			private final Group parent;
			private final DialogLayout.Group dialogGroup;

			Group (Group parent, DialogLayout.Group dialogGroup) {
				this.parent = parent;
				this.dialogGroup = dialogGroup;
			}

			/**
			 * @param widgets Either {@link Widget} or {@link Integer} objects. Integers are used to specify the gap size.
			 */
			public Group sequence (Object... widgets) {
				return sequence(Alignment.FILL, widgets);
			}

			/**
			 * Adds the specified widgets with the {@link Alignment#FILL fill} alignment.
			 * @param widgets Either {@link Widget} or {@link Integer} objects. Integers are used to specify the gap size.
			 */
			public Group sequence (Alignment alignment, Object... widgets) {
				DialogLayout.Group dialogGroup = createSequentialGroup();
				this.dialogGroup.addGroup(dialogGroup);
				return new Group(this, dialogGroup).add(widgets);
			}

			/**
			 * @param widgets Either {@link Widget} or {@link Integer} objects. Integers are used to specify the gap size.
			 */
			public Group parallel (Object... widgets) {
				return parallel(Alignment.FILL, widgets);
			}

			/**
			 * Adds the specified widgets with the {@link Alignment#FILL fill} alignment.
			 * @param widgets Either {@link Widget} or {@link Integer} objects. Integers are used to specify the gap size.
			 */
			public Group parallel (Alignment alignment, Object... widgets) {
				DialogLayout.Group dialogGroup = createParallelGroup();
				this.dialogGroup.addGroup(dialogGroup);
				return new Group(this, dialogGroup).add(widgets);
			}

			/**
			 * Adds the specified widgets with the {@link Alignment#FILL fill} alignment.
			 * @param widgets Either {@link Widget} or {@link Integer} objects. Integers are used to specify the gap size.
			 */
			public Group add (Object... widgets) {
				return add(Alignment.FILL, widgets);
			}

			/**
			 * @param widgets Either {@link Widget} or {@link Integer} objects. Integers are used to specify the gap size.
			 */
			public Group add (Alignment alignment, Object... widgets) {
				for (int i = 0, n = widgets.length; i < n; i++) {
					Object object = widgets[i];
					if (object instanceof Integer) {
						int size = (Integer)object;
						if (size == 0)
							dialogGroup.addGap();
						else
							dialogGroup.addGap(size);
					} else
						dialogGroup.addWidget((Widget)object, alignment);
				}
				return this;
			}

			public Group add (String gapName, Widget... widgets) {
				dialogGroup.addWidgetsWithGap(gapName, widgets);
				return this;
			}

			public Group gap () {
				dialogGroup.addGap();
				return this;
			}

			public Group gap (int size) {
				if (size == 0)
					dialogGroup.addGap();
				else
					dialogGroup.addGap(size);
				return this;
			}

			public Group gap (int min, int pref, int max) {
				dialogGroup.addGap(min, pref, max);
				return this;
			}

			public Group gap (String name) {
				dialogGroup.addGap(name);
				return this;
			}

			public Group defaulGap () {
				dialogGroup.addDefaultGap();
				return this;
			}

			public Group minGap (int size) {
				dialogGroup.addMinGap(size);
				return this;
			}

			public Group end () {
				return parent;
			}
		}
	}
}
